import { NgTemplateOutlet } from '@angular/common';
import {
  Component,
  ElementRef,
  afterRenderEffect,
  computed,
  inject,
  signal,
  viewChild,
  viewChildren,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { Auth } from '../core/auth';
import { Network } from '../core/network';
import { Theme } from '../core/theme';
import { I18n } from '../i18n/i18n';
import { TranslationKey } from '../i18n/sq';
import { Icon, IconName } from '../shared/icon';

interface NavItem {
  labelKey: TranslationKey;
  path: string;
  icon: IconName;
  /** Other pages that belong to this menu item (it stays highlighted on them). */
  also?: string;
}

const ADMIN_NAV: NavItem[] = [
  { labelKey: 'nav.overview', path: '/admin', icon: 'home' },
  { labelKey: 'nav.products', path: '/admin/products', icon: 'box' },
  { labelKey: 'nav.customers', path: '/admin/customers', icon: 'users' },
  { labelKey: 'nav.assignments', path: '/admin/assignments', icon: 'calendar' },
  { labelKey: 'nav.offers', path: '/admin/offers', icon: 'tag' },
  { labelKey: 'nav.orders', path: '/admin/orders', icon: 'clipboard' },
  { labelKey: 'nav.reports', path: '/admin/reports', icon: 'chart' },
];

const AGENT_NAV: NavItem[] = [
  { labelKey: 'nav.today', path: '/agent', icon: 'home', also: '/agent/order/' },
  { labelKey: 'nav.orders', path: '/agent/orders', icon: 'clipboard' },
];

/**
 * The frame around every page.
 *   Desktop: floating sidebar + a row of buttons (language, theme, sign out).
 *   Phones:  one slim bar at the top with the pages as tabs. Agents also get the three buttons
 *            in that bar; the administrator has more pages, so the tabs scroll sideways and the
 *            buttons live in a small account menu behind the initials.
 */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, NgTemplateOutlet, Icon],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
  host: {
    '(document:click)': 'closeMenuFromOutside($event)',
    '(document:keydown.escape)': 'menuOpen.set(false)',
    '(window:resize)': 'remeasure()',
  },
})
export class Shell {
  private readonly router = inject(Router);
  protected readonly auth = inject(Auth);
  protected readonly theme = inject(Theme);
  protected readonly network = inject(Network);
  protected readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly collapsed = signal(false);
  protected readonly menuOpen = signal(false);

  protected readonly isAdmin = computed(() => this.auth.user()?.role === 'ADMIN');
  protected readonly items = computed<NavItem[]>(() => (this.isAdmin() ? ADMIN_NAV : AGENT_NAV));

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  /** Index of the current page in the menu, or -1 when the page is not in the menu. */
  protected readonly activeIndex = computed(() => {
    const path = this.url().split(/[?#]/)[0];
    const items = this.items();
    // The first item is the role's home, so it only matches exactly.
    for (let i = items.length - 1; i > 0; i--) {
      if (path === items[i].path || path.startsWith(items[i].path + '/')) {
        return i;
      }
    }
    const home = items[0];
    return path === home.path || (home.also && path.startsWith(home.also)) ? 0 : -1;
  });

  protected readonly initials = computed(() => {
    const name = this.auth.user()?.fullName ?? '';
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join('');
  });

  protected readonly roleLabel = computed(() => this.t(this.isAdmin() ? 'role.admin' : 'role.agent'));

  // ---------- Phone tabs: the highlight that slides to the active tab ----------

  private readonly tabStrip = viewChild<ElementRef<HTMLElement>>('tabStrip');
  private readonly tabs = viewChildren<ElementRef<HTMLElement>>('tab');
  /** Bumped when tab sizes may have changed (window resized, font finished loading). */
  private readonly measureTick = signal(0);
  /** Where the highlight sits, in pixels inside the tab strip. */
  protected readonly highlight = signal({ left: 0, width: 0 });
  private lastScrolledTo = -2;

  constructor() {
    // Tab widths depend on the label texts, so they are measured after each render.
    afterRenderEffect(() => {
      const index = this.activeIndex();
      const tab = this.tabs()[index]?.nativeElement;
      this.i18n.lang();
      this.measureTick();
      if (!tab) {
        return;
      }

      const next = { left: tab.offsetLeft, width: tab.offsetWidth };
      const current = this.highlight();
      if (current.left !== next.left || current.width !== next.width) {
        this.highlight.set(next);
      }

      // Bring the active tab to the middle of the strip when the page changes.
      const strip = this.tabStrip()?.nativeElement;
      if (strip && index !== this.lastScrolledTo) {
        strip.scrollTo({
          left: next.left - (strip.clientWidth - next.width) / 2,
          behavior: this.lastScrolledTo === -2 ? 'auto' : 'smooth',
        });
        this.lastScrolledTo = index;
      }
    });

    document.fonts?.ready.then(() => this.remeasure());
  }

  protected remeasure(): void {
    this.measureTick.update((tick) => tick + 1);
  }

  // ---------- Actions ----------

  protected toggleSidebar(): void {
    this.collapsed.update((value) => !value);
  }

  protected toggleMenu(event: Event): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }

  protected closeMenuFromOutside(event: Event): void {
    if (this.menuOpen() && !(event.target as HTMLElement).closest('.account')) {
      this.menuOpen.set(false);
    }
  }

  protected signOut(): void {
    this.menuOpen.set(false);
    this.auth.logout();
  }
}
