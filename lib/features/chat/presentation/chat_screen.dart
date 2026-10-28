import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_config.dart';
import '../../../core/widgets/common.dart';
import '../../backup/data/cloud_backup_service.dart';
import '../application/chat_controller.dart';

class ChatScreen extends ConsumerStatefulWidget {
  const ChatScreen({super.key});
  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final input = TextEditingController();
  final scroll = ScrollController();
  bool signingIn = false;
  @override
  void dispose() {
    input.dispose();
    scroll.dispose();
    super.dispose();
  }

  Future<void> send([String? text]) async {
    final value = text ?? input.text;
    final success = await ref.read(chatProvider.notifier).send(value);
    if (!mounted) return;
    if (success && text == null) input.clear();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (scroll.hasClients) {
        scroll.animateTo(
          scroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(chatProvider);
    final cloud = ref.watch(cloudBackupProvider);
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Row(
            children: [
              const CircleAvatar(child: Icon(Icons.auto_awesome)),
              const SizedBox(width: 12),
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Crence',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 20,
                      ),
                    ),
                    Text('Your financial companion'),
                  ],
                ),
              ),
              IconButton(
                tooltip: 'Clear conversation',
                onPressed: state.loading
                    ? null
                    : () => ref.read(chatProvider.notifier).clear(),
                icon: const Icon(Icons.delete_outline),
              ),
            ],
          ),
        ),
        if (AppConfig.chatEndpoint.isEmpty)
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 20, vertical: 8),
            child: Text(
              'AI chat needs a configured backend. Your financial records stay on this device; chat sends only the messages you type.',
            ),
          ),
        if (cloud.configured && cloud.email == null)
          TextButton.icon(
            onPressed: signingIn
                ? null
                : () async {
                    setState(() => signingIn = true);
                    try {
                      await cloud.signIn();
                    } catch (e) {
                      if (context.mounted) showError(context, e);
                    } finally {
                      if (mounted) setState(() => signingIn = false);
                    }
                  },
            icon: const Icon(Icons.account_circle_outlined),
            label: const Text('Sign in with Google'),
          ),
        Expanded(
          child: ListView.builder(
            controller: scroll,
            padding: const EdgeInsets.all(20),
            itemCount: state.messages.length + (state.loading ? 1 : 0),
            itemBuilder: (context, index) {
              if (index == state.messages.length) {
                return const Align(
                  alignment: Alignment.centerLeft,
                  child: Padding(
                    padding: EdgeInsets.all(16),
                    child: SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                  ),
                );
              }
              final message = state.messages[index];
              return Align(
                alignment: message.isUser
                    ? Alignment.centerRight
                    : Alignment.centerLeft,
                child: Container(
                  constraints: const BoxConstraints(maxWidth: 620),
                  margin: const EdgeInsets.only(bottom: 14),
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: message.isUser
                        ? Theme.of(context).colorScheme.primary
                        : Theme.of(context).colorScheme.surface,
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: SelectableText(
                    message.text,
                    style: TextStyle(
                      color: message.isUser
                          ? Theme.of(context).colorScheme.onPrimary
                          : Theme.of(context).colorScheme.onSurface,
                      height: 1.5,
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        if (state.messages.length == 1)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Wrap(
              spacing: 8,
              children: ['Give me budgeting tips', 'How can I save more?']
                  .map(
                    (q) => ActionChip(
                      label: Text(q),
                      onPressed: state.loading ? null : () => send(q),
                    ),
                  )
                  .toList(),
            ),
          ),
        if (state.error != null)
          Padding(
            padding: const EdgeInsets.all(12),
            child: Text(
              state.error!,
              style: TextStyle(color: Theme.of(context).colorScheme.error),
            ),
          ),
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: TextField(
                    controller: input,
                    maxLength: 4000,
                    minLines: 1,
                    maxLines: 4,
                    textInputAction: TextInputAction.send,
                    onSubmitted: state.loading ? null : (_) => send(),
                    decoration: const InputDecoration(
                      hintText: 'Ask Crence…',
                      counterText: '',
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                IconButton.filled(
                  tooltip: 'Send message',
                  onPressed: state.loading || AppConfig.chatEndpoint.isEmpty
                      ? null
                      : () => send(),
                  icon: const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
