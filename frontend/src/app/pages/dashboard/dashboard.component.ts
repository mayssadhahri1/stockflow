import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { apiError } from '../../core/errors';
import { Dashboard } from '../../core/models';
import { StockService } from '../../core/stock.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, CurrencyPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private api = inject(StockService);
  auth = inject(AuthService);

  data = signal<Dashboard | null>(null);
  error = signal('');

  // Valeur du stock par catégorie, triée et convertie en pourcentage pour les barres
  categories = computed(() => {
    const d = this.data();
    if (!d) return [];
    const entries = Object.entries(d.valeurParCategorie).sort((a, b) => b[1] - a[1]);
    const max = Math.max(1, ...entries.map(([, v]) => v));
    return entries.map(([nom, valeur]) => ({ nom, valeur, pct: (100 * valeur) / max }));
  });

  ngOnInit(): void {
    this.api.dashboard().subscribe({
      next: (d) => this.data.set(d),
      error: (e) => this.error.set(apiError(e)),
    });
  }
}
