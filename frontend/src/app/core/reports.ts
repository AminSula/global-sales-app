import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ReportDay {
  /** yyyy-MM-dd */
  date: string;
  orders: number;
  value: number;
}

export interface ReportAgent {
  agentName: string;
  orders: number;
  value: number;
  /** Visits assigned in the period, and how many of them produced an order. */
  customersAssigned: number;
  customersOrdered: number;
}

export interface ReportProduct {
  code: string;
  name: string;
  quantity: number;
  value: number;
}

export interface ReportCustomer {
  code: string;
  name: string;
  orders: number;
  value: number;
}

export interface ReportOffer {
  offerName: string;
  lines: number;
  discountValue: number;
  value: number;
}

/** value = after discounts; grossValue = before discounts. */
export interface Report {
  from: string;
  to: string;
  orders: number;
  value: number;
  pendingOrders: number;
  pendingValue: number;
  approvedOrders: number;
  approvedValue: number;
  grossValue: number;
  discountValue: number;
  averageOrder: number;
  days: ReportDay[];
  agents: ReportAgent[];
  products: ReportProduct[];
  customers: ReportCustomer[];
  offers: ReportOffer[];
}

/** Admin API for the reports page. Dates are yyyy-MM-dd, both included. */
@Injectable({ providedIn: 'root' })
export class ReportsApi {
  private readonly http = inject(HttpClient);

  report(from: string, to: string): Observable<Report> {
    return this.http.get<Report>('/api/admin/reports', { params: { from, to } });
  }
}
