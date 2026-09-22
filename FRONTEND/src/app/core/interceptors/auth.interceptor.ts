import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Attaches Authorization Bearer token, X-User-Id, X-Role, and Content-Type
 * headers to every outgoing API request.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  console.log(`[HTTP Request Outgoing] ${req.method} ${req.url}`);

  // Do not attach stale tokens or user headers to public auth requests
  if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
    console.log(`[authInterceptor] Public route detected: ${req.url}. Skipping Authorization token.`);
    if (!req.headers.has('Content-Type') && !(req.body instanceof FormData)) {
      return next(req.clone({ setHeaders: { 'Content-Type': 'application/json' } }));
    }
    return next(req);
  }

  const token = auth.getToken();
  const user = auth.currentUser();

  let headers: Record<string, string> = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (user?.userId != null) {
    headers['X-User-Id'] = String(user.userId);
  }

  if (user?.roleName) {
    headers['X-Role'] = user.roleName;
  }

  if (user?.username) {
    headers['X-Username'] = user.username;
  }

  // Ensure Content-Type is application/json unless sending FormData
  if (!req.headers.has('Content-Type') && !(req.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (Object.keys(headers).length > 0) {
    return next(req.clone({ setHeaders: headers }));
  }

  return next(req);
};

