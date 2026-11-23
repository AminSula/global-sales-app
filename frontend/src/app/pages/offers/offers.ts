import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CatalogApi, Customer, Product } from '../../core/catalog';
import { Offer, OfferRequest, OfferStatus, OffersApi } from '../../core/offers';
import { I18n } from '../../i18n/i18n';
import { TranslationKey } from '../../i18n/sq';
import { ConfirmDialog } from '../../shared/confirm-dialog';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

/** The offer being created or edited in the form. */
interface Draft {
  id: number | null;
  name: string;
  percent: number | null;
  startDate: string;
  endDate: string;
  enabled: boolean;
  allCustomers: boolean;
  productIds: Set<number>;
  customerIds: Set<number>;
}

const STATUS_BADGE: Record<OfferStatus, string> = {
  ACTIVE: 'success',
  SCHEDULED: 'info',
  EXPIRED: '',
  PAUSED: 'warning',
};

const STATUS_KEY: Record<OfferStatus, TranslationKey> = {
  ACTIVE: 'offers.status.active',
  SCHEDULED: 'offers.status.scheduled',
  EXPIRED: 'offers.status.expired',
  PAUSED: 'offers.status.paused',
};

/** Admin: create offers and choose which products and customers they apply to. */
@Component({
  selector: 'app-offers',
  imports: [FormsModule, Icon, PageIntro, ConfirmDialog, Skeleton],
  templateUrl: './offers.html',
  styleUrl: './offers.scss',
})
export class Offers {
  private readonly api = inject(OffersApi);
  private readonly catalog = inject(CatalogApi);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly offers = signal<Offer[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly customers = signal<Customer[]>([]);

  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);
  protected readonly notice = signal<'saved' | 'deleted' | 'deleteFailed' | null>(null);

  /** Not null while the form is open. */
  protected readonly draft = signal<Draft | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<TranslationKey | null>(null);
  protected readonly productQuery = signal('');
  protected readonly customerQuery = signal('');

  protected readonly toDelete = signal<Offer | null>(null);
  private readonly confirmDelete = viewChild.required(ConfirmDialog);

  protected readonly statusBadge = STATUS_BADGE;
  protected readonly statusKey = STATUS_KEY;

  protected readonly visibleProducts = computed(() => {
    const query = this.productQuery().trim().toLowerCase();
    return this.products().filter(
      (product) => !query || product.name.toLowerCase().includes(query) || product.code.toLowerCase().includes(query),
    );
  });

  protected readonly visibleCustomers = computed(() => {
    const query = this.customerQuery().trim().toLowerCase();
    return this.customers().filter(
      (customer) =>
        !query || customer.name.toLowerCase().includes(query) || customer.code.toLowerCase().includes(query),
    );
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    forkJoin({
      offers: this.api.list(),
      products: this.catalog.products(),
      customers: this.catalog.customers(),
    }).subscribe({
      next: ({ offers, products, customers }) => {
        this.offers.set(offers);
        this.products.set(products);
        this.customers.set(customers);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  // ---------- List texts ----------

  protected productsLabel(offer: Offer): string {
    if (offer.productIds.length === 1) {
      const product = this.products().find((p) => p.id === offer.productIds[0]);
      if (product) {
        return product.name;
      }
    }
    return this.t('offers.nProducts', { count: offer.productIds.length });
  }

  protected customersLabel(offer: Offer): string {
    if (offer.allCustomers) {
      return this.t('offers.allCustomers');
    }
    if (offer.customerIds.length === 1) {
      const customer = this.customers().find((c) => c.id === offer.customerIds[0]);
      if (customer) {
        return customer.name;
      }
    }
    return this.t('offers.nCustomers', { count: offer.customerIds.length });
  }

  protected periodLabel(offer: Offer): string {
    const from = offer.startDate ? this.i18n.date(offer.startDate) : null;
    const to = offer.endDate ? this.i18n.date(offer.endDate) : null;
    if (from && to) {
      return `${from} – ${to}`;
    }
    if (from) {
      return this.t('offers.from', { date: from });
    }
    if (to) {
      return this.t('offers.until', { date: to });
    }
    return this.t('offers.noLimit');
  }

  // ---------- Form ----------

  protected startNew(): void {
    this.open({
      id: null,
      name: '',
      percent: null,
      startDate: '',
      endDate: '',
      enabled: true,
      allCustomers: true,
      productIds: new Set(),
      customerIds: new Set(),
    });
  }

  protected edit(offer: Offer): void {
    this.open({
      id: offer.id,
      name: offer.name,
      percent: offer.discountPercent,
      startDate: offer.startDate ?? '',
      endDate: offer.endDate ?? '',
      enabled: offer.enabled,
      allCustomers: offer.allCustomers,
      productIds: new Set(offer.productIds),
      customerIds: new Set(offer.customerIds),
    });
  }

  private open(draft: Draft): void {
    this.notice.set(null);
    this.formError.set(null);
    this.productQuery.set('');
    this.customerQuery.set('');
    this.draft.set(draft);
  }

  protected closeForm(): void {
    this.draft.set(null);
  }

  protected toggle(ids: Set<number>, id: number, checked: boolean): void {
    if (checked) {
      ids.add(id);
    } else {
      ids.delete(id);
    }
  }

  /** The first problem with the form, or null when it can be saved. Same rules as the server. */
  private validate(draft: Draft): TranslationKey | null {
    if (!draft.name.trim()) {
      return 'offers.error.name';
    }
    if (draft.percent === null || !(draft.percent > 0) || draft.percent > 100) {
      return 'offers.error.percent';
    }
    if (draft.startDate && draft.endDate && draft.endDate < draft.startDate) {
      return 'offers.error.dates';
    }
    if (!draft.productIds.size) {
      return 'offers.error.products';
    }
    if (!draft.allCustomers && !draft.customerIds.size) {
      return 'offers.error.customers';
    }
    return null;
  }

  protected save(): void {
    const draft = this.draft();
    if (!draft || this.saving()) {
      return;
    }
    const problem = this.validate(draft);
    this.formError.set(problem);
    if (problem) {
      return;
    }

    const request: OfferRequest = {
      name: draft.name.trim(),
      discountPercent: draft.percent!,
      allCustomers: draft.allCustomers,
      startDate: draft.startDate || null,
      endDate: draft.endDate || null,
      enabled: draft.enabled,
      productIds: [...draft.productIds],
      customerIds: draft.allCustomers ? [] : [...draft.customerIds],
    };

    this.saving.set(true);
    const call = draft.id === null ? this.api.create(request) : this.api.update(draft.id, request);
    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.draft.set(null);
        this.notice.set('saved');
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.formError.set('common.saveFailed');
      },
    });
  }

  // ---------- Delete ----------

  protected askDelete(offer: Offer): void {
    this.toDelete.set(offer);
    this.confirmDelete().show();
  }

  protected deleteConfirmed(): void {
    const offer = this.toDelete();
    if (!offer) {
      return;
    }
    this.notice.set(null);
    this.api.delete(offer.id).subscribe({
      next: () => {
        this.notice.set('deleted');
        this.load();
      },
      error: () => this.notice.set('deleteFailed'),
    });
  }
}
