import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { apiError } from '../../core/errors';
import { Mouvement, Produit } from '../../core/models';
import { StockService } from '../../core/stock.service';
import { MouvementFormComponent, MouvementSaisi } from '../../shared/mouvement-form.component';

@Component({
  selector: 'app-mouvements',
  standalone: true,
  imports: [DatePipe, FormsModule, MouvementFormComponent],
  templateUrl: './mouvements.component.html',
})
export class MouvementsComponent implements OnInit {
  private api = inject(StockService);

  mouvements = signal<Mouvement[]>([]);
  produits = signal<Produit[]>([]);
  produitFiltre: number | null = null;
  error = signal('');
  modalOpen = signal(false);
  saving = signal(false);
  modalError = signal('');

  ngOnInit(): void {
    this.api.produits().subscribe((p) => this.produits.set(p));
    this.charger();
  }

  charger(): void {
    this.api.mouvements(this.produitFiltre ?? undefined).subscribe({
      next: (m) => this.mouvements.set(m),
      error: (e) => this.error.set(apiError(e)),
    });
  }

  enregistrer(m: MouvementSaisi): void {
    this.saving.set(true);
    this.api.enregistrerMouvement(m.produitId, m.type, m.quantite, m.motif).subscribe({
      next: () => {
        this.saving.set(false);
        this.modalOpen.set(false);
        this.charger();
        this.api.produits().subscribe((p) => this.produits.set(p)); // stocks à jour dans la liste
      },
      error: (e) => {
        this.saving.set(false);
        this.modalError.set(apiError(e));
      },
    });
  }
}
