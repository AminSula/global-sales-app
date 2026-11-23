import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CatalogApi, Customer, SyncResult } from '../../core/catalog';
import { I18n } from '../../i18n/i18n';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

@Component({
  selector: 'app-customers',
  imports: [FormsModule, Icon, PageIntro, Skeleton],
  templateUrl: './customers.html',
})
export class Customers {
  private readonly api = inject(CatalogApi);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly customers = signal<Customer[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);

  protected readonly syncing = signal(false);
  protected readonly syncFailed = signal(false);
  protected readonly syncResult = signal<SyncResult | null>(null);

  protected readonly query = signal('');

  protected readonly filtered = computed(() => {
    const query = this.query().trim().toLowerCase();
    if (!query) {
      return this.customers();
    }
    return this.customers().filter(
      (customer) =>
        customer.code.toLowerCase().includes(query) ||
        customer.name.toLowerCase().includes(query) ||
        (customer.address ?? '').toLowerCase().includes(query),
    );
  });

  /** All rows are stamped by the same refresh, so the newest stamp is the last refresh time. */
  protected readonly lastSynced = computed(() => {
    const stamps = this.customers().map((customer) => customer.syncedAt);
    return stamps.length ? stamps.reduce((a, b) => (a > b ? a : b)) : null;
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.customers().subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  protected refresh(): void {
    if (this.syncing()) {
      return;
    }
    this.syncing.set(true);
    this.syncFailed.set(false);
    this.syncResult.set(null);

    this.api.sync().subscribe({
      next: (result) => {
        this.syncResult.set(result);
        this.syncing.set(false);
        this.load();
      },
      error: () => {
        this.syncFailed.set(true);
        this.syncing.set(false);
      },
    });
  }
}
