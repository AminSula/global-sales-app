import { ApplicationRef, Injectable, inject } from '@angular/core';

/**
 * Runs a change that repaints the whole screen (theme, language) as a soft cross-fade
 * instead of an instant jump.
 *
 * It uses the browser's View Transitions: the browser takes a picture of the page, we make the
 * change, and it fades from the old picture to the new page. The length of the fade is set in
 * styles/_base.scss (::view-transition-old / ::view-transition-new).
 *
 * Browsers without it, and users who ask their device for reduced motion, get the change at once.
 */
@Injectable({ providedIn: 'root' })
export class Smooth {
  private readonly app = inject(ApplicationRef);

  change(update: () => void): void {
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduced || typeof document.startViewTransition !== 'function') {
      update();
      return;
    }
    document.startViewTransition(() => {
      update();
      // Render now, so the "new" picture already shows the change.
      this.app.tick();
    });
  }
}
