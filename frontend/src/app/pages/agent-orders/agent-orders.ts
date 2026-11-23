import { Component, inject, signal } from '@angular/core';
import { AgentApi, Order } from '../../core/agent';
import { I18n } from '../../i18n/i18n';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

/** The agent's own orders with their current status. Refresh to see approvals. */
@Component({
  selector: 'app-agent-orders',
  imports: [Icon, PageIntro, Skeleton],
  templateUrl: './agent-orders.html',
  styleUrl: './agent-orders.scss',
})
export class AgentOrders {
  private readonly api = inject(AgentApi);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly orders = signal<Order[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.orders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }
}
