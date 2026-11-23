import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { daysAgoIso, monthStartIso, todayIso } from '../../core/dates';
import { Report, ReportsApi } from '../../core/reports';
import { I18n } from '../../i18n/i18n';
import { CountUp } from '../../shared/count-up';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

type Preset = 'today' | '7' | '30' | 'month';

/** Round "nice" top of the chart's value axis: 1, 2, 2.5 or 5 times a power of ten. */
function niceMax(value: number): number {
  if (value <= 0) {
    return 1;
  }
  const power = Math.pow(10, Math.floor(Math.log10(value)));
  for (const step of [1, 2, 2.5, 5, 10]) {
    if (value <= step * power) {
      return step * power;
    }
  }
  return 10 * power;
}

/** Admin: sales figures for a period, read from the registered orders. */
@Component({
  selector: 'app-reports',
  imports: [FormsModule, Icon, PageIntro, Skeleton, CountUp],
  templateUrl: './reports.html',
  styleUrl: './reports.scss',
})
export class Reports {
  private readonly api = inject(ReportsApi);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  /** The quick range in use, or null when the dates were typed by hand. */
  protected readonly preset = signal<Preset | null>('7');
  protected readonly from = signal(daysAgoIso(6));
  protected readonly to = signal(todayIso());

  protected readonly report = signal<Report | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);
  protected readonly invalidRange = computed(() => this.from() > this.to());

  /** Chart or the same numbers as a table. */
  protected readonly dayView = signal<'chart' | 'table'>('chart');

  protected readonly discountShare = computed(() => {
    const report = this.report();
    return report && report.grossValue > 0 ? (report.discountValue / report.grossValue) * 100 : 0;
  });

  /** Everything the daily chart needs: bar heights in % of a round axis top, and which labels to print. */
  protected readonly chart = computed(() => {
    const days = this.report()?.days ?? [];
    const top = niceMax(Math.max(0, ...days.map((day) => day.value)));
    const highest = days.reduce((best, day, index) => (day.value > days[best]?.value ? index : best), 0);
    // At most about 6 date labels under the axis, however long the period is, so they fit on a phone.
    const labelEvery = Math.max(1, Math.ceil(days.length / 6));
    return {
      ticks: [top, top / 2, 0],
      bars: days.map((day, index) => ({
        ...day,
        height: (day.value / top) * 100,
        isHighest: index === highest && day.value > 0,
        showLabel: index % labelEvery === 0,
        // Tooltips near the edges open inwards so they are never cut off.
        edge: index < 2 ? 'start' : index >= days.length - 2 ? 'end' : '',
      })),
    };
  });

  constructor() {
    this.load();
  }

  protected usePreset(preset: Preset): void {
    this.preset.set(preset);
    this.to.set(todayIso());
    this.from.set(
      preset === 'today' ? todayIso() : preset === '7' ? daysAgoIso(6) : preset === '30' ? daysAgoIso(29) : monthStartIso(),
    );
    this.load();
  }

  protected setFrom(value: string): void {
    if (value) {
      this.preset.set(null);
      this.from.set(value);
      this.load();
    }
  }

  protected setTo(value: string): void {
    if (value) {
      this.preset.set(null);
      this.to.set(value);
      this.load();
    }
  }

  protected load(): void {
    if (this.invalidRange()) {
      return;
    }
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.report(this.from(), this.to()).subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  protected percent(part: number, whole: number): number {
    return whole > 0 ? Math.round((part / whole) * 100) : 0;
  }
}
