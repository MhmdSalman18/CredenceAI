import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;

import '../../../core/config/app_config.dart';
import '../../../core/errors/app_exception.dart';
import 'backup_cipher.dart';

final cloudBackupProvider = Provider((ref) {
  final service = CloudBackupService();
  ref.onDispose(service.dispose);
  return service;
});

enum CloudDestination { firebase, drive }

class CloudBackupService {
  final http.Client _client = http.Client();
  bool _initialized = false;
  GoogleSignInAccount? _account;
  static const _filename = 'credence_flutter_backup.enc';
  static const _scopes = ['https://www.googleapis.com/auth/drive.appdata'];
  bool get configured =>
      Platform.isAndroid &&
      AppConfig.cloudConfigured &&
      AppConfig.googleServerClientId.isNotEmpty;
  String? get email => _account?.email;

  Future<void> signIn() async {
    if (!configured) {
      throw const AppException(
        'Google backup needs configuration for this Flutter app. See the project setup guide.',
      );
    }
    try {
      if (!_initialized) {
        if (Firebase.apps.isEmpty) {
          await Firebase.initializeApp(
            options: const FirebaseOptions(
              apiKey: AppConfig.firebaseApiKey,
              appId: AppConfig.firebaseAppId,
              messagingSenderId: AppConfig.firebaseSenderId,
              projectId: AppConfig.firebaseProjectId,
              storageBucket: AppConfig.firebaseStorageBucket,
            ),
          );
        }
        await GoogleSignIn.instance.initialize(
          serverClientId: AppConfig.googleServerClientId,
        );
        _initialized = true;
      }
      final account = await GoogleSignIn.instance.authenticate();
      await FirebaseAuth.instance.signInWithCredential(
        GoogleAuthProvider.credential(idToken: account.authentication.idToken),
      );
      _account = account;
    } catch (e) {
      if (e is AppException) rethrow;
      throw const AppException(
        'Google sign-in failed. Check your connection and the Android OAuth/SHA configuration.',
      );
    }
  }

  Future<void> signOut() async {
    if (_initialized) {
      await FirebaseAuth.instance.signOut();
      await GoogleSignIn.instance.signOut();
    }
    _account = null;
  }

  Future<Map<String, String>> _driveHeaders() async {
    if (_account == null) {
      throw const AppException('Sign in with Google first.');
    }
    final authorization = await _account!.authorizationClient.authorizeScopes(
      _scopes,
    );
    return {'Authorization': 'Bearer ${authorization.accessToken}'};
  }

  Future<String?> _driveFile(Map<String, String> headers) async {
    final uri = Uri.https('www.googleapis.com', '/drive/v3/files', {
      'spaces': 'appDataFolder',
      'q': "name='$_filename' and trashed=false",
      'fields': 'files(id)',
      'pageSize': '1',
    });
    final response = await _client
        .get(uri, headers: headers)
        .timeout(const Duration(seconds: 30));
    if (response.statusCode != 200) {
      throw const AppException(
        'Could not access your Drive backup. Try signing in again.',
      );
    }
    final files =
        (jsonDecode(response.body) as Map<String, dynamic>)['files'] as List;
    return files.isEmpty ? null : (files.first as Map)['id'] as String;
  }

  Future<void> upload(Uint8List bytes, CloudDestination destination) async {
    final user = _initialized ? FirebaseAuth.instance.currentUser : null;
    if (user == null || _account == null) {
      throw const AppException('Sign in with Google first.');
    }
    if (destination == CloudDestination.firebase) {
      await FirebaseStorage.instance
          .ref('users/${user.uid}/$_filename')
          .putData(
            bytes,
            SettableMetadata(contentType: 'application/octet-stream'),
          )
          .timeout(const Duration(seconds: 90));
      return;
    }
    final headers = await _driveHeaders();
    final id = await _driveFile(headers);
    if (id != null) {
      final response = await _client
          .patch(
            Uri.https('www.googleapis.com', '/upload/drive/v3/files/$id', {
              'uploadType': 'media',
            }),
            headers: {...headers, 'Content-Type': 'application/octet-stream'},
            body: bytes,
          )
          .timeout(const Duration(seconds: 90));
      if (response.statusCode != 200) {
        throw const AppException('Drive upload failed. Please try again.');
      }
    } else {
      final boundary = 'credence_${DateTime.now().microsecondsSinceEpoch}';
      final body = BytesBuilder()
        ..add(
          utf8.encode(
            '--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n${jsonEncode({
              'name': _filename,
              'parents': ['appDataFolder'],
            })}\r\n--$boundary\r\nContent-Type: application/octet-stream\r\n\r\n',
          ),
        )
        ..add(bytes)
        ..add(utf8.encode('\r\n--$boundary--\r\n'));
      final response = await _client
          .post(
            Uri.https('www.googleapis.com', '/upload/drive/v3/files', {
              'uploadType': 'multipart',
            }),
            headers: {
              ...headers,
              'Content-Type': 'multipart/related; boundary=$boundary',
            },
            body: body.takeBytes(),
          )
          .timeout(const Duration(seconds: 90));
      if (response.statusCode != 200) {
        throw const AppException('Drive upload failed. Please try again.');
      }
    }
  }

  Future<Uint8List> download(CloudDestination destination) async {
    final user = _initialized ? FirebaseAuth.instance.currentUser : null;
    if (user == null || _account == null) {
      throw const AppException('Sign in with Google first.');
    }
    if (destination == CloudDestination.firebase) {
      final data = await FirebaseStorage.instance
          .ref('users/${user.uid}/$_filename')
          .getData(BackupCipher.maxBytes)
          .timeout(const Duration(seconds: 90));
      if (data == null) throw const AppException('No backup found.');
      return data;
    }
    final headers = await _driveHeaders();
    final id = await _driveFile(headers);
    if (id == null) throw const AppException('No Drive backup found.');
    final response = await _client
        .send(
          http.Request(
            'GET',
            Uri.https('www.googleapis.com', '/drive/v3/files/$id', {
              'alt': 'media',
            }),
          )..headers.addAll(headers),
        )
        .timeout(const Duration(seconds: 60));
    if (response.statusCode != 200) {
      throw const AppException('Drive download failed.');
    }
    final bytes = BytesBuilder();
    await for (final chunk in response.stream.timeout(
      const Duration(seconds: 30),
    )) {
      bytes.add(chunk);
      if (bytes.length > BackupCipher.maxBytes) {
        throw const AppException('The backup is too large.');
      }
    }
    return bytes.takeBytes();
  }

  void dispose() => _client.close();
}
