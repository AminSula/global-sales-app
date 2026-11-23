import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Auth } from '../../core/auth';
import { CatalogApi } from '../../core/catalog';
import { todayIso } from '../../core/dates';
import { Overview as OverviewData, OverviewApi } from '../../core/overview';
import { I18n } from '../../i18n/i18n';
import { CountUp } from '../../shared/count-up';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

/** Admin home: today at a glance. (Reports is the page for analysing a longer period.) */
@Component({
  selector: 'app-overview',
  imports: [RouterLink, CountUp, Icon, PageIntro, Skeleton],
  templateUrl: './overview.html',
  styleUrl: './overview.scss',
})
export class Overview {
  private readonly api = inject(OverviewApi);
  private readonly catalog = inject(CatalogApi);
  protected readonly auth = inject(Auth);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly today = todayIso();
  protected readonly data = signal<OverviewData | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);

  protected readonly syncing = signal(false);
  protected readonly syncNotice = signal<'done' | 'failed' | null>(null);

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.overview().subscribe({
      next: (data) => {
        this.data.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  /** Refreshes products and customers from the source database, then reloads the page's numbers. */
  protected refreshData(): void {
    if (this.syncing()) {
      return;
    }
    this.syncing.set(true);
    this.syncNotice.set(null);
    this.catalog.sync().subscribe({
      next: () => {
        this.syncing.set(false);
        this.syncNotice.set('done');
        this.load();
      },
      error: () => {
        this.syncing.set(false);
        this.syncNotice.set('failed');
      },
    });
  }

  protected percent(part: number, whole: number): number {
    return whole > 0 ? Math.round((part / whole) * 100) : 0;
  }
}
