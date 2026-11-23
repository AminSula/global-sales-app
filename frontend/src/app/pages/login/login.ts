import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../core/auth';
import { I18n } from '../../i18n/i18n';
import { TranslationKey } from '../../i18n/sq';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected username = '';
  protected password = '';
  protected readonly sending = signal(false);
  // Stored as a key, so the message changes language together with the page.
  protected readonly errorKey = signal<TranslationKey | null>(null);

  protected submit(): void {
    if (this.sending() || !this.username.trim() || !this.password) {
      return;
    }
    this.sending.set(true);
    this.errorKey.set(null);

    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => this.router.navigateByUrl(this.auth.homeUrl()),
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        this.errorKey.set(error.status === 401 ? 'login.wrong' : 'login.noServer');
      },
    });
  }
}
