import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';
import { Role } from './models';

/** Only logged-in users may enter. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  return auth.isLoggedIn() ? true : inject(Router).parseUrl('/login');
};

/** The login page is only for users who are not logged in. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  return auth.isLoggedIn() ? inject(Router).parseUrl(auth.homeUrl()) : true;
};

/** Keeps each role inside its own area. This is for navigation only; the server enforces access. */
export const roleGuard = (role: Role): CanActivateFn => {
  return () => {
    const auth = inject(Auth);
    return auth.user()?.role === role ? true : inject(Router).parseUrl(auth.homeUrl());
  };
};
