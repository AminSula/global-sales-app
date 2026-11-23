import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Auth } from '../../core/auth';
import { User } from '../../core/models';
import { I18n } from '../../i18n/i18n';
import { PageIntro } from '../../shared/page-intro';

type ServerState = 'checking' | 'ok' | 'failed';

/** Landing page for both roles. In step 1 it proves that login and the API work. */
@Component({
  selector: 'app-home',
  imports: [PageIntro],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly http = inject(HttpClient);
  protected readonly auth = inject(Auth);
  protected readonly t = inject(I18n).t;

  protected readonly server = signal<ServerState>('checking');
  protected readonly isAdmin = computed(() => this.auth.user()?.role === 'ADMIN');

  constructor() {
    this.checkServer();
  }

  protected checkServer(): void {
    this.server.set('checking');
    this.http.get<User>('/api/auth/me').subscribe({
      next: () => this.server.set('ok'),
      error: () => this.server.set('failed'),
    });
  }
}
