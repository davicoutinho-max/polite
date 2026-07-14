/** Every HTTP call goes through the API Gateway — no per-service ports appear anywhere in
 * frontend code (see docs/architecture/system-architecture.html). */
export const environment = {
  apiBaseUrl: 'http://localhost:8080',
};
