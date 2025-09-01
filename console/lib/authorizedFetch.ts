// console/lib/authorizedFetch.ts
import { getAccessToken } from '../auth/getAccessToken';

export async function authorizedFetch(
  url: string,
  options: RequestInit = {},
  scope: string
): Promise<Response> {
  const token = await getAccessToken(scope);
  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${token}`);
  // ensure content-type isn’t lost if caller set it
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }
  return fetch(url, { ...options, headers });
}
