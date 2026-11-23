import { Component, computed, inject, input } from '@angular/core';
import { I18n } from '../i18n/i18n';

const WIDTHS = [92, 78, 86, 70, 82, 74];

/**
 * Shimmering placeholder shown while data loads.
 *
 *   <app-skeleton />                 rows, for inside a card or table area
 *   <app-skeleton [rows]="3" cards /> separate card-shaped blocks, for card lists
 */
@Component({
  selector: 'app-skeleton',
  template: `
    @for (width of widths(); track $index) {
      <span class="block" [style.--w.%]="width"></span>
    }
  `,
  styles: `
    :host {
      display: grid;
      gap: var(--ui-space-4);
      padding: var(--ui-space-5);
    }

    .block {
      width: var(--w);
      height: 14px;
      border-radius: var(--ui-radius-sm);
      background: linear-gradient(90deg, var(--ui-track) 25%, var(--ui-hairline) 50%, var(--ui-track) 75%) 0 0 / 200% 100%;
      animation: shimmer 1.4s linear infinite;
    }

    :host(.cards) {
      gap: var(--ui-space-3);
      padding: 0;
    }

    :host(.cards) .block {
      width: 100%;
      height: 72px;
      border-radius: var(--ui-radius-lg);
    }
  `,
  host: {
    role: 'status',
    '[attr.aria-label]': 'label()',
    '[class.cards]': 'cards()',
  },
})
export class Skeleton {
  readonly rows = input(5);
  readonly cards = input(false, { transform: (value: boolean | string) => value !== false });

  protected readonly widths = computed(() =>
    Array.from({ length: this.rows() }, (_, index) => WIDTHS[index % WIDTHS.length]),
  );

  private readonly i18n = inject(I18n);
  protected readonly label = computed(() => this.i18n.t('common.loading'));
}
