import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Theme } from './core/theme';
import { I18n } from './i18n/i18n';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  template: '<router-outlet />',
})
export class App {
  // Created here so the saved theme and language are applied before the first screen shows.
  private readonly theme = inject(Theme);
  private readonly i18n = inject(I18n);
}
