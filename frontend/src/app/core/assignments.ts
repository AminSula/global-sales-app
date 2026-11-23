import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Agent {
  id: number;
  fullName: string;
}

export interface AssignedCustomer {
  customerId: number;
  code: string;
  name: string;
  address: string | null;
}

export interface AssignmentDay {
  /** This agent's customers for the day, in visit order. */
  assigned: AssignedCustomer[];
  /** Customers another agent already has on that day. */
  taken: { customerId: number; agentName: string }[];
}

/** Admin API for the daily visit lists. Dates are yyyy-MM-dd. */
@Injectable({ providedIn: 'root' })
export class AssignmentsApi {
  private readonly http = inject(HttpClient);

  agents(): Observable<Agent[]> {
    return this.http.get<Agent[]>('/api/admin/agents');
  }

  day(agentId: number, date: string): Observable<AssignmentDay> {
    return this.http.get<AssignmentDay>('/api/admin/assignments', { params: { agentId, date } });
  }

  /** Replaces the agent's whole list for that day; customerIds are in visit order. */
  save(agentId: number, date: string, customerIds: number[]): Observable<AssignmentDay> {
    return this.http.put<AssignmentDay>(
      '/api/admin/assignments',
      { customerIds },
      { params: { agentId, date } },
    );
  }
}
