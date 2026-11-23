import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Order } from './agent';

/** Admin API for orders. */
@Injectable({ providedIn: 'root' })
export class AdminOrdersApi {
  private readonly http = inject(HttpClient);

  /** All orders, newest first. */
  list(): Observable<Order[]> {
    return this.http.get<Order[]>('/api/admin/orders');
  }

  /** Marks the order "E miratuar". Approval is final. */
  approve(id: number): Observable<Order> {
    return this.http.post<Order>(`/api/admin/orders/${id}/approve`, null);
  }
}
