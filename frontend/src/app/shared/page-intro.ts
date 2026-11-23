import { Component, input } from '@angular/core';

/** The heading block at the top of every page: eyebrow, title and one-line description. */
@Component({
  selector: 'app-page-intro',
  template: `
    <p class="eyebrow">{{ eyebrow() }}</p>
    <h1>{{ title() }}</h1>
    @if (description()) {
      <p class="secondary-text">{{ description() }}</p>
    }
  `,
  styles: `
    :host {
      display: grid;
      gap: var(--ui-space-1);
      margin-bottom: var(--ui-space-5);
    }
  `,
})
export class PageIntro {
  readonly eyebrow = input.required<string>();
  readonly title = input.required<string>();
  readonly description = input('');
}
