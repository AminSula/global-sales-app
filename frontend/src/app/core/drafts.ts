import { Injectable } from '@angular/core';

/** An order that is being filled in, or that could not be sent yet. Kept on the phone. */
export interface Draft {
  /** Created once per order. Re-sending with the same id can never create a second order. */
  clientRequestId: string;
  /** productId -> quantity */
  quantities: Record<number, number>;
}

/**
 * Keeps unfinished orders in the phone's storage, one per agent and customer, so nothing is
 * lost when the app is closed, the phone restarts or the connection drops.
 */
@Injectable({ providedIn: 'root' })
export class Drafts {
  get(userId: number, customerId: number): Draft | null {
    try {
      const raw = localStorage.getItem(this.key(userId, customerId));
      return raw ? (JSON.parse(raw) as Draft) : null;
    } catch {
      return null;
    }
  }

  has(userId: number, customerId: number): boolean {
    return this.get(userId, customerId) !== null;
  }

  save(userId: number, customerId: number, draft: Draft): void {
    try {
      localStorage.setItem(this.key(userId, customerId), JSON.stringify(draft));
    } catch {
      // Storage unavailable: the order still works, it just is not kept after closing the app.
    }
  }

  clear(userId: number, customerId: number): void {
    try {
      localStorage.removeItem(this.key(userId, customerId));
    } catch {
      // Nothing to clear.
    }
  }

  newRequestId(): string {
    // crypto.randomUUID only exists on https/localhost; this also works over plain http on a local network.
    if (typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    const bytes = crypto.getRandomValues(new Uint8Array(16));
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;
    const hex = [...bytes].map((byte) => byte.toString(16).padStart(2, '0')).join('');
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
  }

  private key(userId: number, customerId: number): string {
    return `pilot.draft.${userId}.${customerId}`;
  }
}
