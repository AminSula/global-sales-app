import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface AgentVisit {
  customerId: number;
  code: string;
  name: string;
  address: string | null;
  visitOrder: number;
  ordersToday: number;
}

export interface AgentDay {
  /** The server's date, yyyy-MM-dd. */
  date: string;
  visits: AgentVisit[];
}

/** A product as the agent sees it for one customer. */
export interface OrderProduct {
  id: number;
  code: string;
  name: string;
  price: number;
  stock: number;
  offerPercent: number | null;
  offerName: string | null;
}

export type OrderStatus = 'PENDING' | 'APPROVED';

export interface OrderLine {
  lineNo: number;
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discountPercent: number;
  offerName: string | null;
  lineTotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  customerCode: string;
  customerName: string;
  agentName: string;
  createdAt: string;
  total: number;
  status: OrderStatus;
  approvedAt: string | null;
  approvedByName: string | null;
  lines: OrderLine[];
}

export interface CreateOrderRequest {
  clientRequestId: string;
  customerId: number;
  lines: { productId: number; quantity: number }[];
  /** The total shown to the agent. The server refuses (409) if its own total differs. */
  expectedTotal: number;
}

/** The phone's API. The server knows which agent is calling from the login token. */
@Injectable({ providedIn: 'root' })
export class AgentApi {
  private readonly http = inject(HttpClient);

  today(): Observable<AgentDay> {
    return this.http.get<AgentDay>('/api/agent/visits');
  }

  catalog(customerId: number): Observable<OrderProduct[]> {
    return this.http.get<OrderProduct[]>(`/api/agent/customers/${customerId}/catalog`);
  }

  orders(): Observable<Order[]> {
    return this.http.get<Order[]>('/api/agent/orders');
  }

  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>('/api/agent/orders', request);
  }
}
