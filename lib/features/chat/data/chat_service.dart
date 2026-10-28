import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:http/http.dart' as http;

import '../../../core/config/app_config.dart';
import '../../../core/errors/app_exception.dart';

class ChatMessage {
  const ChatMessage({required this.text, required this.isUser});
  final String text;
  final bool isUser;
}

abstract interface class ChatService {
  Future<String> send(List<ChatMessage> conversation);
}

/// The Gemini key lives on the server; only Firebase ID tokens leave the app.
class HttpChatService implements ChatService {
  HttpChatService(this.client);
  final http.Client client;
  @override
  Future<String> send(List<ChatMessage> conversation) async {
    final uri = Uri.tryParse(AppConfig.chatEndpoint);
    if (uri == null || uri.scheme != 'https' || uri.host.isEmpty) {
      throw const AppException(
        'Crence needs a configured secure AI endpoint. See the project setup guide.',
      );
    }
    final user = Firebase.apps.isEmpty
        ? null
        : FirebaseAuth.instance.currentUser;
    if (user == null) {
      throw const AppException('Sign in with Google to use Crence.');
    }
    final token = await user.getIdToken();
    try {
      final response = await client
          .post(
            uri,
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer $token',
            },
            body: jsonEncode({
              'messages': conversation
                  .takeLast(20)
                  .map(
                    (m) => {
                      'role': m.isUser ? 'user' : 'model',
                      'text': m.text,
                    },
                  )
                  .toList(),
            }),
          )
          .timeout(const Duration(seconds: 45));
      if (response.statusCode == 429) {
        throw const AppException('Crence is busy. Please try again shortly.');
      }
      if (response.statusCode != 200) {
        throw const AppException('Crence could not respond. Please try again.');
      }
      final decoded = jsonDecode(response.body) as Map<String, dynamic>;
      final text = decoded['text'];
      if (text is! String || text.trim().isEmpty) {
        throw const AppException('Crence returned an empty response.');
      }
      return text;
    } on AppException {
      rethrow;
    } catch (_) {
      throw const AppException(
        'Could not connect to Crence. Check your connection and try again.',
      );
    }
  }
}

extension<T> on List<T> {
  Iterable<T> takeLast(int count) => skip(length > count ? length - count : 0);
}
