import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

/**
 * Central error handling so feature code doesn't need a try/catch on every
 * call:
 *  - 401 (token missing/expired/invalid) -> clear session, back to login.
 *  - 403 (wrong role for this endpoint)  -> toast, stay on page.
 *  - everything else                      -> toast with the backend's
 *    message (GlobalExceptionHandler in every service returns a
 *    consistent { status, error, message } body), then re-throw so the
 *    calling component can still react if it needs to (e.g. stop a
 *    loading spinner).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        console.error(`[HTTP Error Response] Status: ${err.status} (${err.statusText}) on ${req.method} ${req.url}`, {
          url: req.url,
          status: err.status,
          statusText: err.statusText,
          errorBody: err.error,
          message: err.message,
        });

        // Public authentication / registration requests
        if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
          const message = err.error?.message ?? (err.status === 401 ? 'Invalid credentials or access denied.' : 'Request failed.');
          notifications.error(message);
          return throwError(() => err);
        }

        if (err.status === 401) {
          console.error('[401 Session Expired] Clearing session on:', req.method, req.url, err.error);
          notifications.error('Your session has expired. Please log in again.');
          auth.clearSession();
        } else if (err.status === 403) {
          notifications.error("You don't have permission to do that.");
        } else if (err.status === 0) {
          notifications.error('Cannot reach the server. Is the API gateway running?');
        } else {
          const message = err.error?.message ?? err.message ?? 'Something went wrong.';
          notifications.error(message);
        }
      }
      return throwError(() => err);
    }),
  );
};
