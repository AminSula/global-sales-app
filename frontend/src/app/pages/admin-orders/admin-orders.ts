import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AdminOrdersApi } from '../../core/admin-orders';
import { Order, OrderStatus } from '../../core/agent';
import { I18n } from '../../i18n/i18n';
import { ConfirmDialog } from '../../shared/confirm-dialog';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

type StatusFilter = 'ALL' | OrderStatus;

/** Admin: every order sent from the phones, with details and the "E miratuar" action. */
@Component({
  selector: 'app-admin-orders',
  imports: [FormsModule, Icon, PageIntro, ConfirmDialog, Skeleton],
  templateUrl: './admin-orders.html',
  styleUrl: './admin-orders.scss',
})
export class AdminOrders {
  private readonly api = inject(AdminOrdersApi);
  /** ?open=<order id> in the address opens that order straight away (used by the Overview page). */
  private openOnLoad = Number(inject(ActivatedRoute).snapshot.queryParamMap.get('open')) || null;
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly orders = signal<Order[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);

  protected readonly status = signal<StatusFilter>('ALL');
  /** Agent name, or '' for all agents. */
  protected readonly agent = signal('');
  protected readonly query = signal('');

  /** The order shown in the details window. */
  protected readonly selected = signal<Order | null>(null);
  protected readonly approving = signal(false);
  protected readonly notice = signal<'approved' | 'approveFailed' | null>(null);

  private readonly details = viewChild.required<ElementRef<HTMLDialogElement>>('details');
  private readonly confirmApprove = viewChild.required(ConfirmDialog);

  protected readonly agents = computed(() => [...new Set(this.orders().map((order) => order.agentName))].sort());

  protected readonly pendingCount = computed(
    () => this.orders().filter((order) => order.status === 'PENDING').length,
  );

  protected readonly filtered = computed(() => {
    const status = this.status();
    const agent = this.agent();
    const query = this.query().trim().toLowerCase();
    return this.orders().filter(
      (order) =>
        (status === 'ALL' || order.status === status) &&
        (!agent || order.agentName === agent) &&
        (!query ||
          order.orderNumber.toLowerCase().includes(query) ||
          order.customerName.toLowerCase().includes(query) ||
          order.customerCode.toLowerCase().includes(query)),
    );
  });

  protected readonly filteredTotal = computed(
    () => Math.round(this.filtered().reduce((sum, order) => sum + order.total * 100, 0)) / 100,
  );

  /** quantity x price before discount, for the details window. */
  protected gross(quantity: number, unitPrice: number): number {
    return Math.round(quantity * unitPrice * 100) / 100;
  }

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.list().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
        const wanted = orders.find((order) => order.id === this.openOnLoad);
        this.openOnLoad = null;
        if (wanted) {
          this.open(wanted);
        }
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  protected open(order: Order): void {
    this.notice.set(null);
    this.selected.set(order);
    this.details().nativeElement.showModal();
  }

  protected close(): void {
    this.details().nativeElement.close();
  }

  protected askApprove(): void {
    this.confirmApprove().show();
  }

  protected approve(): void {
    const order = this.selected();
    if (!order || this.approving()) {
      return;
    }
    this.approving.set(true);
    this.notice.set(null);

    this.api.approve(order.id).subscribe({
      next: (updated) => {
        // Put the updated order in the list and in the open window.
        this.orders.update((orders) => orders.map((o) => (o.id === updated.id ? updated : o)));
        this.selected.set(updated);
        this.approving.set(false);
        this.notice.set('approved');
      },
      error: () => {
        this.approving.set(false);
        this.notice.set('approveFailed');
      },
    });
  }
}
