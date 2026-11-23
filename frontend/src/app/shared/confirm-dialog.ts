import { Component, ElementRef, input, output, viewChild } from '@angular/core';

/**
 * In-app confirmation window (instead of the browser's confirm()).
 *
 *   <app-confirm-dialog #dialog [title]="..." [message]="..." [confirmLabel]="..." [cancelLabel]="..."
 *                       (confirmed)="delete()" />
 *   ...then call dialog.show()
 *
 * Built on the native <dialog>, so Esc closes it and focus stays inside while it is open.
 */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <dialog #dialog (close)="onClose()">
      <form method="dialog">
        <h2>{{ title() }}</h2>
        <p class="secondary-text">{{ message() }}</p>
        <div class="actions">
          <button type="submit" value="cancel">{{ cancelLabel() }}</button>
          <button type="submit" value="confirm" [class]="danger() ? 'danger-button' : 'primary'">
            {{ confirmLabel() }}
          </button>
        </div>
      </form>
    </dialog>
  `,
  styles: `
    dialog {
      width: min(calc(100% - 32px), 420px);
      padding: var(--ui-space-5);
      border: 1px solid var(--ui-hairline);
      border-radius: var(--ui-radius-lg);
      background: var(--ui-surface);
      color: var(--ui-text);
      box-shadow: var(--ui-shadow-lg);
    }

    dialog[open] {
      animation: dialog-in 200ms ease-out;
    }

    dialog::backdrop {
      background: var(--ui-backdrop);
      backdrop-filter: blur(4px);
    }

    form {
      display: grid;
      gap: var(--ui-space-3);
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      gap: var(--ui-space-2);
      margin-top: var(--ui-space-3);
    }

    @keyframes dialog-in {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
    }
  `,
})
export class ConfirmDialog {
  readonly title = input.required<string>();
  readonly message = input('');
  readonly confirmLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();
  /** Red confirm button, for deleting. */
  readonly danger = input(false);

  readonly confirmed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  show(): void {
    const element = this.dialog().nativeElement;
    element.returnValue = '';
    element.showModal();
  }

  protected onClose(): void {
    if (this.dialog().nativeElement.returnValue === 'confirm') {
      this.confirmed.emit();
    }
  }
}
