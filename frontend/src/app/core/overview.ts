import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Order } from './agent';
import { ReportAgent } from './reports';

/** The admin's Overview page: today at a glance. */
export interface Overview {
  ordersToday: number;
  valueToday: number;
  averageToday: number;
  /** Everything waiting for approval, whatever day it was sent. */
  pendingOrders: number;
  pendingValue: number;
  /** Today's assigned visits, and how many of them already have an order. */
  visitsAssigned: number;
  visitsOrdered: number;
  activeOffers: number;
  products: number;
  customers: number;
  lastSyncedAt: string | null;
  /** The newest orders waiting for approval (at most five). */
  pending: Order[];
  /** Today's figures per agent. */
  agents: ReportAgent[];
}

@Injectable({ providedIn: 'root' })
export class OverviewApi {
  private readonly http = inject(HttpClient);

  overview(): Observable<Overview> {
    return this.http.get<Overview>('/api/admin/overview');
  }
}
