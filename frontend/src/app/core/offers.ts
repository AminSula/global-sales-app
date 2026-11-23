import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type OfferStatus = 'ACTIVE' | 'SCHEDULED' | 'EXPIRED' | 'PAUSED';

export interface Offer {
  id: number;
  name: string;
  discountPercent: number;
  allCustomers: boolean;
  /** yyyy-MM-dd, or null for "no limit". */
  startDate: string | null;
  endDate: string | null;
  enabled: boolean;
  status: OfferStatus;
  productIds: number[];
  customerIds: number[];
}

export type OfferRequest = Omit<Offer, 'id' | 'status'>;

/** Admin API for offers. */
@Injectable({ providedIn: 'root' })
export class OffersApi {
  private readonly http = inject(HttpClient);

  list(): Observable<Offer[]> {
    return this.http.get<Offer[]>('/api/admin/offers');
  }

  create(request: OfferRequest): Observable<Offer> {
    return this.http.post<Offer>('/api/admin/offers', request);
  }

  update(id: number, request: OfferRequest): Observable<Offer> {
    return this.http.put<Offer>(`/api/admin/offers/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/offers/${id}`);
  }
}
