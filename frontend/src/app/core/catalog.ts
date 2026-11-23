import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Product {
  id: number;
  code: string;
  name: string;
  price: number;
  stock: number;
  /** Today's best offer that applies to all customers, or null. */
  offerPercent: number | null;
  offerName: string | null;
  syncedAt: string;
}

export interface Customer {
  id: number;
  code: string;
  name: string;
  address: string | null;
  syncedAt: string;
}

export interface SyncResult {
  products: number;
  productsNew: number;
  productsChanged: number;
  productsRemoved: number;
  customers: number;
  customersNew: number;
  customersChanged: number;
  customersRemoved: number;
}

/** Admin API for products, customers and the "Refresh from SQL" action. */
@Injectable({ providedIn: 'root' })
export class CatalogApi {
  private readonly http = inject(HttpClient);

  products(): Observable<Product[]> {
    return this.http.get<Product[]>('/api/admin/products');
  }

  customers(): Observable<Customer[]> {
    return this.http.get<Customer[]>('/api/admin/customers');
  }

  /** Copies products and customers from the source database. */
  sync(): Observable<SyncResult> {
    return this.http.post<SyncResult>('/api/admin/sync', null);
  }
}
