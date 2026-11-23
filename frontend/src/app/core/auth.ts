import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { Session, User } from './models';

const STORAGE_KEY = 'pilot.session';

/** Holds the logged-in user and token. The session survives closing the app. */
@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restore());

  readonly user = computed<User | null>(() => this.session()?.user ?? null);
  readonly token = computed<string | null>(() => this.session()?.token ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);

  login(username: string, password: string): Observable<Session> {
    return this.http
      .post<Session>('/api/auth/login', { username, password })
      .pipe(tap((session) => this.store(session)));
  }

  logout(): void {
    this.store(null);
    this.router.navigateByUrl('/login');
  }

  /** Where this user lands after login. */
  homeUrl(): string {
    const user = this.user();
    if (!user) {
      return '/login';
    }
    return user.role === 'ADMIN' ? '/admin' : '/agent';
  }

  private store(session: Session | null): void {
    this.session.set(session);
    try {
      if (session) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // Storage can be unavailable (private mode); the session then lasts until reload.
    }
  }

  private restore(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch {
      return null;
    }
  }
}
