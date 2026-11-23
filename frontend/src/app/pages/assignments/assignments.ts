import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Agent, AssignedCustomer, AssignmentDay, AssignmentsApi } from '../../core/assignments';
import { CatalogApi, Customer } from '../../core/catalog';
import { todayIso } from '../../core/dates';
import { I18n } from '../../i18n/i18n';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

type Notice = 'saved' | 'conflict' | 'failed' | null;

/** Admin: build each agent's visit list for a day and put it in order. */
@Component({
  selector: 'app-assignments',
  imports: [FormsModule, Icon, PageIntro, Skeleton],
  templateUrl: './assignments.html',
  styleUrl: './assignments.scss',
})
export class Assignments {
  private readonly api = inject(AssignmentsApi);
  private readonly catalog = inject(CatalogApi);
  protected readonly t = inject(I18n).t;

  protected readonly agents = signal<Agent[]>([]);
  protected readonly agentId = signal<number | null>(null);
  protected readonly date = signal(todayIso());

  private readonly allCustomers = signal<Customer[]>([]);
  /** The list being edited on screen. It is only stored when the user presses Save. */
  protected readonly assigned = signal<AssignedCustomer[]>([]);
  /** customerId -> name of the other agent who has that customer on this day. */
  private readonly taken = signal(new Map<number, string>());
  /** The ids as last loaded/saved, to know whether there are unsaved changes. */
  private readonly savedIds = signal('');

  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);
  protected readonly saving = signal(false);
  protected readonly notice = signal<Notice>(null);
  protected readonly query = signal('');

  protected readonly dirty = computed(() => this.ids(this.assigned()) !== this.savedIds());

  /** Customers not in this agent's list, with the other agent's name when already taken. */
  protected readonly others = computed(() => {
    const inList = new Set(this.assigned().map((customer) => customer.customerId));
    const query = this.query().trim().toLowerCase();
    return this.allCustomers()
      .filter((customer) => !inList.has(customer.id))
      .filter(
        (customer) =>
          !query ||
          customer.name.toLowerCase().includes(query) ||
          customer.code.toLowerCase().includes(query) ||
          (customer.address ?? '').toLowerCase().includes(query),
      )
      .map((customer) => ({ customer, takenBy: this.taken().get(customer.id) ?? null }));
  });

  constructor() {
    this.loadAll();
  }

  protected loadAll(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    forkJoin({ agents: this.api.agents(), customers: this.catalog.customers() }).subscribe({
      next: ({ agents, customers }) => {
        this.agents.set(agents);
        this.allCustomers.set(customers);
        if (this.agentId() === null && agents.length) {
          this.agentId.set(agents[0].id);
        }
        this.loadDay();
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  /** Loads the stored list for the chosen agent and date (this also discards unsaved changes). */
  protected loadDay(): void {
    const agentId = this.agentId();
    if (agentId === null) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.day(agentId, this.date()).subscribe({
      next: (day) => {
        this.apply(day);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }

  protected selectAgent(id: number): void {
    if (this.dirty() || id === this.agentId()) {
      return;
    }
    this.agentId.set(id);
    this.notice.set(null);
    this.loadDay();
  }

  protected setDate(value: string): void {
    if (this.dirty() || !value) {
      return;
    }
    this.date.set(value);
    this.notice.set(null);
    this.loadDay();
  }

  protected add(customer: Customer): void {
    this.notice.set(null);
    this.assigned.update((list) => [
      ...list,
      { customerId: customer.id, code: customer.code, name: customer.name, address: customer.address },
    ]);
  }

  protected remove(index: number): void {
    this.notice.set(null);
    this.assigned.update((list) => list.filter((_, i) => i !== index));
  }

  /** Moves a customer one place up (-1) or down (+1) in the visit order. */
  protected move(index: number, step: -1 | 1): void {
    const target = index + step;
    if (target < 0 || target >= this.assigned().length) {
      return;
    }
    this.notice.set(null);
    this.assigned.update((list) => {
      const copy = [...list];
      [copy[index], copy[target]] = [copy[target], copy[index]];
      return copy;
    });
  }

  protected discard(): void {
    this.notice.set(null);
    this.loadDay();
  }

  protected save(): void {
    const agentId = this.agentId();
    if (agentId === null || this.saving() || !this.dirty()) {
      return;
    }
    this.saving.set(true);
    this.notice.set(null);

    const customerIds = this.assigned().map((customer) => customer.customerId);
    this.api.save(agentId, this.date(), customerIds).subscribe({
      next: (day) => {
        this.apply(day);
        this.saving.set(false);
        this.notice.set('saved');
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        if (error.status === 409) {
          // Someone gave one of these customers to the other agent meanwhile: show the real state.
          this.notice.set('conflict');
          this.loadDay();
        } else {
          this.notice.set('failed');
        }
      },
    });
  }

  private apply(day: AssignmentDay): void {
    this.assigned.set(day.assigned);
    this.savedIds.set(this.ids(day.assigned));
    this.taken.set(new Map(day.taken.map((entry) => [entry.customerId, entry.agentName])));
  }

  private ids(list: AssignedCustomer[]): string {
    return list.map((customer) => customer.customerId).join(',');
  }
}
