import { Injectable, inject, signal } from '@angular/core';
import { Smooth } from './smooth';

export type ThemeName = 'light' | 'dark';

const STORAGE_KEY = 'pilot.theme';

/** Light/dark theme, remembered on the device. */
@Injectable({ providedIn: 'root' })
export class Theme {
  private readonly smooth = inject(Smooth);

  readonly current = signal<ThemeName>(this.restore());

  constructor() {
    this.apply(this.current());
  }

  toggle(): void {
    this.set(this.current() === 'light' ? 'dark' : 'light');
  }

  set(next: ThemeName): void {
    if (next === this.current()) {
      return;
    }
    // Cross-fade between the two themes instead of flashing.
    this.smooth.change(() => {
      this.current.set(next);
      this.apply(next);
    });
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Not saved; the choice lasts until reload.
    }
  }

  private apply(theme: ThemeName): void {
    document.documentElement.dataset['theme'] = theme;
  }

  private restore(): ThemeName {
    try {
      return localStorage.getItem(STORAGE_KEY) === 'dark' ? 'dark' : 'light';
    } catch {
      return 'light';
    }
  }
}
