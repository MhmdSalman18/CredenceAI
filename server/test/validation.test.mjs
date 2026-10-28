import test from 'node:test';
import assert from 'node:assert/strict';
import {validateMessages} from '../src/validation.mjs';

test('accepts a conversation and drops an orphaned initial model message', () => {
  assert.deepEqual(validateMessages({messages: [{role: 'model', text: 'Earlier reply'}, {role: 'user', text: ' Budget tips ' }]}), [{role: 'user', parts: [{text: 'Budget tips'}]}]);
});
test('rejects oversized input, unsupported roles and invalid conversation order', () => {
  for (const messages of [[], [{role: 'system', text: 'x'}], [{role: 'user', text: 'x'.repeat(4001)}], [{role: 'model', text: 'x'}], [{role: 'user', text: 'x'}, {role: 'user', text: 'y'}]]) {
    assert.throws(() => validateMessages({messages}));
  }
});
