import { DestroyRef, Directive, ElementRef, effect, inject, input, untracked } from '@angular/core';
import { I18n } from '../i18n/i18n';

/**
 * Shows a number that counts from its previous value (0 the first time) up to the new one.
 *
 *   <p [appCountUp]="report.orders"></p>
 *   <p [appCountUp]="report.value" format="money"></p>
 *
 * The element's text is written by this directive, so leave the element empty.
 * Users who ask their device for reduced motion get the final number at once.
 */
@Directive({ selector: '[appCountUp]' })
export class CountUp {
  readonly value = input.required<number>({ alias: 'appCountUp' });
  /** money = always two decimals; number = decimals only when there are any. */
  readonly format = input<'money' | 'number'>('number');
  /** Length of the animation in milliseconds. */
  readonly duration = input(700);

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly i18n = inject(I18n);
  private shown = 0;
  private frame = 0;

  constructor() {
    effect(() => {
      const target = this.value();
      // Re-run when the language changes too, so the number is formatted again.
      this.i18n.lang();
      untracked(() => this.animateTo(target));
    });
    inject(DestroyRef).onDestroy(() => cancelAnimationFrame(this.frame));
  }

  private animateTo(target: number): void {
    cancelAnimationFrame(this.frame);
    const from = this.shown;
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduced || from === target) {
      this.render(target, true);
      return;
    }

    const start = performance.now();
    const step = (now: number) => {
      const progress = Math.min(1, (now - start) / this.duration());
      const eased = 1 - Math.pow(1 - progress, 3);
      if (progress < 1) {
        this.render(from + (target - from) * eased, false);
        this.frame = requestAnimationFrame(step);
      } else {
        this.render(target, true);
      }
    };
    this.frame = requestAnimationFrame(step);
  }

  private render(value: number, final: boolean): void {
    this.shown = value;
    this.element.textContent =
      this.format() === 'money'
        ? this.i18n.money(value)
        : this.i18n.quantity(final ? value : Math.round(value));
  }
}
