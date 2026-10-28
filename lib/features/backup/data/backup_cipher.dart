import 'dart:convert';
import 'dart:isolate';
import 'dart:math';
import 'dart:typed_data';

import 'package:cryptography/cryptography.dart';

import '../../../core/errors/app_exception.dart';

/// Versioned, authenticated backups. Derived keys and passphrases never persist.
class BackupCipher {
  static const iterations = 210000;
  static const maxBytes = 20 * 1024 * 1024;

  Future<Uint8List> encrypt(String plaintext, String password) async {
    if (password.length < 10) {
      throw const AppException('Use a passphrase of at least 10 characters.');
    }
    return Isolate.run(() async {
      final random = Random.secure();
      final salt = List.generate(16, (_) => random.nextInt(256));
      final key = await Pbkdf2(
        macAlgorithm: Hmac.sha256(),
        iterations: iterations,
        bits: 256,
      ).deriveKey(secretKey: SecretKey(utf8.encode(password)), nonce: salt);
      final box = await AesGcm.with256bits().encrypt(
        utf8.encode(plaintext),
        secretKey: key,
        aad: utf8.encode('credence-backup-v1'),
      );
      return Uint8List.fromList(
        utf8.encode(
          jsonEncode({
            'format': 'credence-flutter',
            'version': 1,
            'kdf': 'PBKDF2-SHA256',
            'iterations': iterations,
            'salt': base64Encode(salt),
            'nonce': base64Encode(box.nonce),
            'ciphertext': base64Encode(box.cipherText),
            'tag': base64Encode(box.mac.bytes),
          }),
        ),
      );
    });
  }

  Future<String> decrypt(Uint8List bytes, String password) async {
    if (bytes.length > maxBytes) {
      throw const AppException('This backup is too large.');
    }
    try {
      return await Isolate.run(() async {
        final map = jsonDecode(utf8.decode(bytes)) as Map<String, dynamic>;
        if (map['format'] != 'credence-flutter' ||
            map['version'] != 1 ||
            map['iterations'] != iterations ||
            map['kdf'] != 'PBKDF2-SHA256') {
          throw const FormatException('Unsupported backup');
        }
        final salt = base64Decode(map['salt'] as String);
        final nonce = base64Decode(map['nonce'] as String);
        final tag = base64Decode(map['tag'] as String);
        if (salt.length != 16 || nonce.length != 12 || tag.length != 16) {
          throw const FormatException('Invalid header');
        }
        final key = await Pbkdf2(
          macAlgorithm: Hmac.sha256(),
          iterations: iterations,
          bits: 256,
        ).deriveKey(secretKey: SecretKey(utf8.encode(password)), nonce: salt);
        final plaintext = await AesGcm.with256bits().decrypt(
          SecretBox(
            base64Decode(map['ciphertext'] as String),
            nonce: nonce,
            mac: Mac(tag),
          ),
          secretKey: key,
          aad: utf8.encode('credence-backup-v1'),
        );
        return utf8.decode(plaintext);
      });
    } catch (_) {
      throw const AppException(
        'Cannot open this backup. Check the passphrase and file.',
      );
    }
  }
}
