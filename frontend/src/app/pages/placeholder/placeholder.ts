import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { I18n } from '../../i18n/i18n';
import { TranslationKey } from '../../i18n/sq';
import { PageIntro } from '../../shared/page-intro';

/** Temporary page for menu items that are built in a later step. */
@Component({
  selector: 'app-placeholder',
  imports: [PageIntro],
  templateUrl: './placeholder.html',
})
export class Placeholder {
  protected readonly t = inject(I18n).t;

  private readonly data = toSignal(inject(ActivatedRoute).data, { initialValue: {} as Record<string, unknown> });

  protected readonly titleKey = computed(() => this.data()['titleKey'] as TranslationKey);
  protected readonly step = computed(() => this.data()['step'] as number);
}
