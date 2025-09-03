
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '1m30s', target: 10 },
    { duration: '20s', target: 0 },
  ],
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // GET request
  let getRes = http.get(`${BASE_URL}/v1/games`);
  check(getRes, { 'status was 200': (r) => r.status == 200 });
  sleep(1);

  // POST request
  const payload = JSON.stringify({
    dungeon: [
      [1, 1, 1],
      [1, 1, 1],
      [1, 1, 1],
    ],
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  let postRes = http.post(`${BASE_URL}/v1/games`, payload, params);
  check(postRes, {
    'status was 201': (r) => r.status == 201,
    'id exists': (r) => r.json('id') !== null,
  });
  sleep(1);
}
