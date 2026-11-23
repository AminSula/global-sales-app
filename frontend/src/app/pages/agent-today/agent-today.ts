import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AgentApi, AgentDay } from '../../core/agent';
import { Auth } from '../../core/auth';
import { Drafts } from '../../core/drafts';
import { I18n } from '../../i18n/i18n';
import { Icon } from '../../shared/icon';
import { PageIntro } from '../../shared/page-intro';
import { Skeleton } from '../../shared/skeleton';

/** Agent home: the customers assigned for today, in visit order. */
@Component({
  selector: 'app-agent-today',
  imports: [RouterLink, Icon, PageIntro, Skeleton],
  templateUrl: './agent-today.html',
  styleUrl: './agent-today.scss',
})
export class AgentToday {
  private readonly api = inject(AgentApi);
  private readonly auth = inject(Auth);
  private readonly drafts = inject(Drafts);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly day = signal<AgentDay | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadFailed = signal(false);

  /** Each visit plus whether an unfinished order for that customer is kept on this phone. */
  protected readonly visits = computed(() => {
    const userId = this.auth.user()?.id ?? 0;
    return (this.day()?.visits ?? []).map((visit) => ({
      ...visit,
      hasDraft: this.drafts.has(userId, visit.customerId),
    }));
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.today().subscribe({
      next: (day) => {
        this.day.set(day);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      },
    });
  }
}
