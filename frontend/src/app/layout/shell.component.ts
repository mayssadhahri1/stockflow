import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <a class="brand" routerLink="/">📦 Stock<span>Flow</span></a>
      <nav>
        <a routerLink="/tableau-de-bord" routerLinkActive="active">Tableau de bord</a>
        <a routerLink="/produits" routerLinkActive="active">Produits</a>
        <a routerLink="/mouvements" routerLinkActive="active">Mouvements</a>
      </nav>
      <div class="user">
        <span class="avatar">{{ initiale() }}</span>
        <span class="name">{{ auth.user()?.nom }}</span>
        <span class="role" [class.admin]="auth.isAdmin()">{{ auth.isAdmin() ? 'Admin' : 'Employé' }}</span>
        <button class="btn-ghost" (click)="auth.logout()">Déconnexion</button>
      </div>
    </header>
    <main class="page">
      <router-outlet />
    </main>
  `,
  styles: `
    .topbar { position: sticky; top: 0; z-index: 10; display: flex; align-items: center; gap: 32px; padding: 0 32px; height: 64px;
      background: rgba(255,255,255,.85); backdrop-filter: blur(8px); border-bottom: 1px solid var(--line); }
    .brand { font-weight: 800; font-size: 19px; color: var(--text); text-decoration: none; }
    .brand span { color: var(--primary); }
    nav { display: flex; gap: 6px; flex: 1; }
    nav a { padding: 8px 14px; border-radius: 8px; color: var(--muted); text-decoration: none; font-weight: 600; font-size: 14px; }
    nav a:hover { background: var(--bg); color: var(--text); }
    nav a.active { background: var(--primary-soft); color: var(--primary); }
    .user { display: flex; align-items: center; gap: 10px; font-size: 14px; }
    .avatar { width: 32px; height: 32px; border-radius: 50%; display: grid; place-items: center; background: var(--primary); color: #fff; font-weight: 700; }
    .role { font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 99px; background: var(--bg); color: var(--muted); }
    .role.admin { background: var(--primary-soft); color: var(--primary-dark); }
    .page { max-width: 1280px; margin: 0 auto; padding: 32px; }
    @media (max-width: 720px) { .topbar { padding: 0 16px; gap: 12px; } .name { display: none; } .page { padding: 20px 16px; } }
  `,
})
export class ShellComponent {
  auth = inject(AuthService);
  initiale = () => (this.auth.user()?.nom ?? '?').charAt(0).toUpperCase();
}
