import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;

import '../../../core/errors/app_exception.dart';
import '../data/chat_service.dart';

final chatServiceProvider = Provider<ChatService>((ref) {
  final client = http.Client();
  ref.onDispose(client.close);
  return HttpChatService(client);
});
final chatProvider = NotifierProvider<ChatController, ChatState>(
  ChatController.new,
);

class ChatState {
  const ChatState({
    this.messages = const [
      ChatMessage(
        text:
            'Hi! I’m Crence, your AI financial assistant. How can I help you?',
        isUser: false,
      ),
    ],
    this.loading = false,
    this.error,
  });
  final List<ChatMessage> messages;
  final bool loading;
  final String? error;
}

class ChatController extends Notifier<ChatState> {
  @override
  ChatState build() => const ChatState();
  Future<bool> send(String input) async {
    if (state.loading || input.trim().isEmpty || input.length > 4000) {
      return false;
    }
    final previous = state.messages;
    final messages = [
      ...previous,
      ChatMessage(text: input.trim(), isUser: true),
    ];
    state = ChatState(messages: messages, loading: true);
    try {
      final answer = await ref
          .read(chatServiceProvider)
          .send(messages.skip(1).toList());
      if (!ref.mounted) return false;
      state = ChatState(
        messages: [
          ...messages,
          ChatMessage(text: answer, isUser: false),
        ],
      );
      return true;
    } catch (e) {
      if (ref.mounted) {
        state = ChatState(messages: previous, error: friendlyError(e));
      }
      return false;
    }
  }

  void clear() {
    if (!state.loading) state = const ChatState();
  }
}
