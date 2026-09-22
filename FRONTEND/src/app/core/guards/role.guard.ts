import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { RoleName } from '../../shared/models';

/**
 * Restricts a route to the roles listed in its `data.roles` array, e.g.:
 *   { path: 'users', canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } }
 *
 * Runs after authGuard, so a logged-out user is redirected to /login first;
 * this only fires for an authenticated user with the wrong role.
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const notifications = inject(NotificationService);

  const allowedRoles = (route.data['roles'] as RoleName[] | undefined) ?? [];

  if (allowedRoles.length === 0 || auth.hasAnyRole(...allowedRoles)) {
    return true;
  }

  notifications.error("You don't have permission to view that page.");
  return router.createUrlTree(['/dashboard']);
};
