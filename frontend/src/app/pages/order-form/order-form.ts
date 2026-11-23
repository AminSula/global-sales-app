import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AgentApi, AgentVisit, Order, OrderProduct } from '../../core/agent';
import { Auth } from '../../core/auth';
import { Drafts } from '../../core/drafts';
import { fromCents, lineTotalCents, toCents } from '../../core/money';
import { Network } from '../../core/network';
import { I18n } from '../../i18n/i18n';
import { TranslationKey } from '../../i18n/sq';
import { CountUp } from '../../shared/count-up';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

const MAX_QUANTITY = 9999;

/** One chosen product with every number of its calculation, in cents. */
interface Line {
  product: OrderProduct;
  quantity: number;
  /** quantity x price, before any discount */
  grossCents: number;
  discountPercent: number;
  discountCents: number;
  totalCents: number;
}

/**
 * The agent's order for one customer, in three views:
 *   pick   -> choose products and quantities
 *   review -> every line's calculation and the total, then Send
 *   sent   -> confirmation with the order number
 */
@Component({
  selector: 'app-order-form',
  imports: [FormsModule, RouterLink, Icon, PageIntro, Skeleton, CountUp],
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss',
})
export class OrderForm {
  private readonly api = inject(AgentApi);
  private readonly drafts = inject(Drafts);
  private readonly userId = inject(Auth).user()?.id ?? 0;
  private readonly customerId = Number(inject(ActivatedRoute).snapshot.paramMap.get('customerId'));
  protected readonly network = inject(Network);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly view = signal<'pick' | 'review' | 'sent'>('pick');
  protected readonly customer = signal<AgentVisit | null>(null);
  protected readonly products = signal<OrderProduct[]>([]);
  /** productId -> quantity (only products with a quantity above 0) */
  protected readonly quantities = signal<Record<number, number>>({});
  private clientRequestId = '';

  protected readonly loading = signal(true);
  protected readonly loadProblem = signal<TranslationKey | null>(null);
  protected readonly query = signal('');

  protected readonly sending = signal(false);
  protected readonly sendProblem = signal<TranslationKey | null>(null);
  protected readonly saved = signal<Order | null>(null);

  protected readonly visibleProducts = computed(() => {
    const query = this.query().trim().toLowerCase();
    return this.products().filter(
      (product) => !query || product.name.toLowerCase().includes(query) || product.code.toLowerCase().includes(query),
    );
  });

  protected readonly lines = computed<Line[]>(() => {
    const quantities = this.quantities();
    return this.products()
      .filter((product) => quantities[product.id] > 0)
      .map((product) => {
        const quantity = quantities[product.id];
        const discountPercent = product.offerPercent ?? 0;
        const grossCents = quantity * toCents(product.price);
        const totalCents = lineTotalCents(quantity, product.price, discountPercent);
        return { product, quantity, grossCents, discountPercent, discountCents: grossCents - totalCents, totalCents };
      });
  });

  protected readonly totals = computed(() => {
    const lines = this.lines();
    return {
      items: lines.reduce((sum, line) => sum + line.quantity, 0),
      gross: fromCents(lines.reduce((sum, line) => sum + line.grossCents, 0)),
      discount: fromCents(lines.reduce((sum, line) => sum + line.discountCents, 0)),
      total: fromCents(lines.reduce((sum, line) => sum + line.totalCents, 0)),
    };
  });

  protected readonly fromCents = fromCents;

  /** The price of ONE unit after the customer's offer, in cents. */
  protected lineTotalCentsFor(product: OrderProduct): number {
    return lineTotalCents(1, product.price, product.offerPercent ?? 0);
  }

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadProblem.set(null);
    forkJoin({ day: this.api.today(), products: this.api.catalog(this.customerId) }).subscribe({
      next: ({ day, products }) => {
        this.customer.set(day.visits.find((visit) => visit.customerId === this.customerId) ?? null);
        this.products.set(products);
        this.restoreDraft(products);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadProblem.set(error.status === 403 ? 'order.notAssigned' : 'common.loadFailed');
        this.loading.set(false);
      },
    });
  }

  /** Brings back the unfinished order kept on this phone, if there is one. */
  private restoreDraft(products: OrderProduct[]): void {
    const draft = this.drafts.get(this.userId, this.customerId);
    this.clientRequestId = draft?.clientRequestId ?? this.drafts.newRequestId();

    const known = new Set(products.map((product) => product.id));
    const quantities: Record<number, number> = {};
    for (const [id, quantity] of Object.entries(draft?.quantities ?? {})) {
      if (known.has(Number(id)) && quantity > 0) {
        quantities[Number(id)] = quantity;
      }
    }
    this.quantities.set(quantities);
  }

  // ---------- Quantities ----------

  protected quantityOf(product: OrderProduct): number {
    return this.quantities()[product.id] ?? 0;
  }

  protected step(product: OrderProduct, change: number): void {
    this.setQuantity(product, this.quantityOf(product) + change);
  }

  protected setQuantity(product: OrderProduct, value: number | string | null): void {
    const parsed = Math.floor(Number(value));
    const quantity = Number.isFinite(parsed) ? Math.min(Math.max(parsed, 0), MAX_QUANTITY) : 0;

    const next = { ...this.quantities() };
    if (quantity > 0) {
      next[product.id] = quantity;
    } else {
      delete next[product.id];
    }
    this.quantities.set(next);
    this.sendProblem.set(null);

    // Kept on the phone after every change, so closing the app loses nothing.
    if (Object.keys(next).length) {
      this.drafts.save(this.userId, this.customerId, { clientRequestId: this.clientRequestId, quantities: next });
    } else {
      this.drafts.clear(this.userId, this.customerId);
    }
  }

  // ---------- Views ----------

  protected review(): void {
    if (this.lines().length) {
      this.sendProblem.set(null);
      this.view.set('review');
      window.scrollTo({ top: 0 });
    }
  }

  protected backToPick(): void {
    this.view.set('pick');
  }

  // ---------- Sending ----------

  protected send(): void {
    if (this.sending() || !this.lines().length) {
      return;
    }
    if (!this.network.online()) {
      this.sendProblem.set('order.offline');
      return;
    }

    this.sending.set(true);
    this.sendProblem.set(null);

    this.api
      .createOrder({
        clientRequestId: this.clientRequestId,
        customerId: this.customerId,
        lines: this.lines().map((line) => ({ productId: line.product.id, quantity: line.quantity })),
        expectedTotal: this.totals().total,
      })
      .subscribe({
        next: (order) => {
          this.drafts.clear(this.userId, this.customerId);
          this.saved.set(order);
          this.sending.set(false);
          this.view.set('sent');
          window.scrollTo({ top: 0 });
        },
        error: (error: HttpErrorResponse) => {
          this.sending.set(false);
          if (error.status === 409) {
            // Prices, offers or products changed since they were loaded. Nothing was saved.
            this.sendProblem.set('order.changed');
            this.reloadPrices();
          } else if (error.status === 403) {
            this.sendProblem.set('order.notAssigned');
          } else if (error.status === 400) {
            this.sendProblem.set('order.invalid');
          } else {
            // No answer from the server. The order stays on the phone and can be sent again:
            // the same request id means it can never be registered twice.
            this.sendProblem.set(this.network.online() ? 'order.sendFailed' : 'order.offline');
          }
        },
      });
  }

  private reloadPrices(): void {
    this.api.catalog(this.customerId).subscribe({
      next: (products) => {
        this.products.set(products);
        const known = new Set(products.map((product) => product.id));
        const kept = Object.fromEntries(
          Object.entries(this.quantities()).filter(([id]) => known.has(Number(id))),
        ) as Record<number, number>;
        this.quantities.set(kept);
        if (Object.keys(kept).length) {
          this.drafts.save(this.userId, this.customerId, { clientRequestId: this.clientRequestId, quantities: kept });
        } else {
          this.drafts.clear(this.userId, this.customerId);
          this.view.set('pick');
        }
      },
      error: () => this.sendProblem.set('order.sendFailed'),
    });
  }
}
