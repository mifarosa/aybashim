const TOKEN_KEY = 'aybashim.token';
const USER_KEY = 'aybashim.user';

export function loadSession() {
  const token = localStorage.getItem(TOKEN_KEY);
  const user = parseStoredUser();
  return { token, user };
}

export function saveSession(auth) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem(USER_KEY, JSON.stringify({
    id: auth.userId,
    name: auth.name,
    email: auth.email
  }));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export async function apiRequest(path, options = {}, token) {
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(path, {
    ...options,
    headers
  });

  const contentType = response.headers.get('content-type') || '';
  const body = contentType.includes('application/json')
    ? await readJson(response)
    : await response.text();

  if (!response.ok) {
    const message = typeof body === 'object' && body?.message
      ? body.message
      : response.status === 500 && path.startsWith('/api')
        ? 'Backend cevap vermiyor. Spring Boot uygulamasi 8080 portunda calisiyor mu?'
        : `HTTP ${response.status}`;
    throw new Error(message);
  }

  return body;
}

function parseStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
    return null;
  }
}

async function readJson(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}
