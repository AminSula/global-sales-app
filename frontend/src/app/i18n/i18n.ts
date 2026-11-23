import { Injectable, computed, inject, signal } from '@angular/core';
import { Smooth } from '../core/smooth';
import { en } from './en';
import { TranslationKey, sq } from './sq';

export type Lang = 'sq' | 'en';

const STORAGE_KEY = 'pilot.lang';
const TEXTS: Record<Lang, Record<TranslationKey, string>> = { sq, en };

/**
 * Albanian + English texts. In a component:
 *
 *   protected readonly t = inject(I18n).t;      // then in the template: {{ t('login.title') }}
 *
 * The language is a signal, so every text on screen updates as soon as it changes.
 * To add a text: add the key to sq.ts, then to en.ts (the build fails until both have it).
 */
@Injectable({ providedIn: 'root' })
export class I18n {
  private readonly smooth = inject(Smooth);

  readonly lang = signal<Lang>(this.restore());

  private readonly locale = computed(() => (this.lang() === 'sq' ? 'sq-AL' : 'en-GB'));

  constructor() {
    document.documentElement.lang = this.lang();
  }

  /** Text for a key; {placeholders} are replaced from params. */
  readonly t = (key: TranslationKey, params?: Record<string, string | number>): string => {
    let text: string = TEXTS[this.lang()][key];
    if (params) {
      for (const [name, value] of Object.entries(params)) {
        text = text.replaceAll(`{${name}}`, String(value));
      }
    }
    return text;
  };

  /** Money: always two decimals, e.g. 1,650.00 */
  readonly money = (value: number): string =>
    new Intl.NumberFormat(this.locale(), { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);

  /** Quantity: decimals only when there are any, e.g. 240 or 12.5 */
  readonly quantity = (value: number): string =>
    new Intl.NumberFormat(this.locale(), { maximumFractionDigits: 2 }).format(value);

  readonly dateTime = (iso: string): string =>
    new Intl.DateTimeFormat(this.locale(), { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso));

  /** A plain date (yyyy-MM-dd), e.g. 15 Nov 2026. */
  readonly date = (isoDate: string): string => {
    const [year, month, day] = isoDate.split('-').map(Number);
    return new Intl.DateTimeFormat(this.locale(), { dateStyle: 'medium' }).format(new Date(year, month - 1, day));
  };

  /** Day and month only, for chart axes, e.g. 15 Nov. */
  readonly dayMonth = (isoDate: string): string => {
    const [year, month, day] = isoDate.split('-').map(Number);
    return new Intl.DateTimeFormat(this.locale(), { day: 'numeric', month: 'short' }).format(
      new Date(year, month - 1, day),
    );
  };

  /** Short numbers for chart axes: 0, 5,000, 20K. */
  readonly compact = (value: number): string =>
    value >= 10000
      ? new Intl.NumberFormat(this.locale(), { notation: 'compact', maximumFractionDigits: 1 }).format(value)
      : new Intl.NumberFormat(this.locale(), { maximumFractionDigits: 2 }).format(value);

  toggle(): void {
    this.set(this.lang() === 'sq' ? 'en' : 'sq');
  }

  set(next: Lang): void {
    if (next === this.lang()) {
      return;
    }
    // Cross-fade to the other language instead of every text jumping at once.
    this.smooth.change(() => {
      this.lang.set(next);
      document.documentElement.lang = next;
    });
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Not saved; the choice lasts until reload.
    }
  }

  private restore(): Lang {
    try {
      return localStorage.getItem(STORAGE_KEY) === 'en' ? 'en' : 'sq';
    } catch {
      return 'sq';
    }
  }
}
