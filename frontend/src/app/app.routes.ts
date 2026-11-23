import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { Auth } from './core/auth';
import { authGuard, guestGuard, roleGuard } from './core/auth.guard';
import { Shell } from './layout/shell';
import { AdminOrders } from './pages/admin-orders/admin-orders';
import { AgentOrders } from './pages/agent-orders/agent-orders';
import { AgentToday } from './pages/agent-today/agent-today';
import { Assignments } from './pages/assignments/assignments';
import { Customers } from './pages/customers/customers';
import { Offers } from './pages/offers/offers';
import { OrderForm } from './pages/order-form/order-form';
import { Overview } from './pages/overview/overview';
import { Products } from './pages/products/products';
import { Reports } from './pages/reports/reports';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: () => inject(Auth).homeUrl() },
      {
        path: 'admin',
        canActivate: [roleGuard('ADMIN')],
        children: [
          { path: '', component: Overview },
          { path: 'products', component: Products },
          { path: 'customers', component: Customers },
          { path: 'assignments', component: Assignments },
          { path: 'offers', component: Offers },
          { path: 'orders', component: AdminOrders },
          { path: 'reports', component: Reports },
        ],
      },
      {
        path: 'agent',
        canActivate: [roleGuard('AGENT')],
        children: [
          { path: '', component: AgentToday },
          { path: 'order/:customerId', component: OrderForm },
          { path: 'orders', component: AgentOrders },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
