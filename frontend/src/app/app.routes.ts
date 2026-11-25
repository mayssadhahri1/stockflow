import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent) },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau-de-bord' },
      {
        path: 'tableau-de-bord',
        title: 'Tableau de bord — StockFlow',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'produits',
        title: 'Produits — StockFlow',
        loadComponent: () => import('./pages/produits/produits.component').then((m) => m.ProduitsComponent),
      },
      {
        path: 'mouvements',
        title: 'Mouvements — StockFlow',
        loadComponent: () => import('./pages/mouvements/mouvements.component').then((m) => m.MouvementsComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
