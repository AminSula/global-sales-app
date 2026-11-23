import { Injectable, signal } from '@angular/core';

/** Whether the device currently has a network connection. */
@Injectable({ providedIn: 'root' })
export class Network {
  readonly online = signal(navigator.onLine);

  constructor() {
    window.addEventListener('online', () => this.online.set(true));
    window.addEventListener('offline', () => this.online.set(false));
  }
}
