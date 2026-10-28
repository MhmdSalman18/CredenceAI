import {createServer} from 'node:http';
import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {validateMessages} from './validation.mjs';

const {GEMINI_API_KEY, GEMINI_MODEL, FIREBASE_PROJECT_ID} = process.env;
if (!GEMINI_API_KEY || !GEMINI_MODEL || !/^[a-zA-Z0-9._-]+$/.test(GEMINI_MODEL) || !FIREBASE_PROJECT_ID) {
  throw new Error('Configure GEMINI_API_KEY, GEMINI_MODEL and FIREBASE_PROJECT_ID.');
}
initializeApp({credential: applicationDefault(), projectId: FIREBASE_PROJECT_ID});
const limits = new Map();
const janitor = setInterval(() => { for (const [uid, bucket] of limits) { if (bucket.until < Date.now()) limits.delete(uid); } }, 60000);
janitor.unref();

function json(response, status, body) {
  response.writeHead(status, {'Content-Type': 'application/json', 'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff'});
  response.end(JSON.stringify(body));
}

const server = createServer(async (request, response) => {
  if (request.url !== '/chat' || request.method !== 'POST') return json(response, 404, {error: 'Not found'});
  const authorization = request.headers.authorization ?? '';
  if (!authorization.startsWith('Bearer ')) return json(response, 401, {error: 'Sign in required'});
  let uid;
  try { uid = (await getAuth().verifyIdToken(authorization.slice(7), true)).uid; }
  catch { return json(response, 401, {error: 'Invalid or expired identity'}); }

  const now = Date.now();
  const bucket = limits.get(uid) ?? {count: 0, until: now + 60000};
  if (bucket.until < now) { bucket.count = 0; bucket.until = now + 60000; }
  bucket.count++;
  limits.set(uid, bucket);
  if (bucket.count > 10) return json(response, 429, {error: 'Please try again shortly'});
  if (!(request.headers['content-type'] ?? '').includes('application/json')) return json(response, 415, {error: 'JSON required'});
  let contents;
  try {
    const chunks = [];
    let length = 0;
    for await (const chunk of request) {
      length += chunk.length;
      if (length > 65536) { json(response, 413, {error: 'Request too large'}); request.destroy(); return; }
      chunks.push(chunk);
    }
    contents = validateMessages(JSON.parse(Buffer.concat(chunks).toString('utf8')));
  } catch { return json(response, 400, {error: 'Invalid conversation'}); }
  try {
    const result = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`, {
      method: 'POST', signal: AbortSignal.timeout(35000),
      headers: {'Content-Type': 'application/json', 'x-goog-api-key': GEMINI_API_KEY},
      body: JSON.stringify({contents, systemInstruction: {parts: [{text: 'You are Crence, a concise, friendly financial education assistant for Credence AI. Explain concepts clearly. You cannot access the user’s financial records. Never claim to have viewed their accounts or executed transactions. Do not promise investment returns.'}]}, generationConfig: {maxOutputTokens: 1000}}),
    });
    if (result.status === 429) return json(response, 429, {error: 'Please try again shortly'});
    if (!result.ok) return json(response, 502, {error: 'AI service unavailable'});
    const data = await result.json();
    const text = data.candidates?.[0]?.content?.parts?.map((part) => part.text ?? '').join('').trim();
    if (!text) return json(response, 502, {error: 'No response available'});
    return json(response, 200, {text});
  } catch { return json(response, 502, {error: 'AI service unavailable'}); }
});
server.requestTimeout = 45000;
server.headersTimeout = 10000;
server.listen(Number(process.env.PORT ?? 8080), '127.0.0.1');
for (const signal of ['SIGTERM', 'SIGINT']) process.on(signal, () => server.close(() => process.exit(0)));
