export function validateMessages(body) {
  if (!body || !Array.isArray(body.messages) || body.messages.length < 1 || body.messages.length > 20) {
    throw new Error('Provide between 1 and 20 messages.');
  }
  const messages = body.messages.map((message) => {
    if (!message || !['user', 'model'].includes(message.role) || typeof message.text !== 'string' || !message.text.trim() || message.text.length > 4000) {
      throw new Error('Invalid message.');
    }
    return {role: message.role, parts: [{text: message.text.trim()}]};
  });
  if (messages.at(-1).role !== 'user') throw new Error('The last message must be from the user.');
  // A truncated conversation can begin with the assistant; omit that orphan.
  if (messages[0].role === 'model') messages.shift();
  for (let i = 1; i < messages.length; i++) {
    if (messages[i].role === messages[i - 1].role) throw new Error('Message roles must alternate.');
  }
  return messages;
}
