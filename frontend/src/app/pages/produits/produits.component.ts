import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { apiError } from '../../core/errors';
import { Produit, ProduitForm, TypeMouvement } from '../../core/models';
import { StockService } from '../../core/stock.service';
import { MouvementFormComponent, MouvementSaisi } from '../../shared/mouvement-form.component';
import { ProduitFormComponent } from '../../shared/produit-form.component';

@Component({
  selector: 'app-produits',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ProduitFormComponent, MouvementFormComponent],
  templateUrl: './produits.component.html',
  styleUrl: './produits.component.css',
})
export class ProduitsComponent implements OnInit {
  private api = inject(StockService);
  auth = inject(AuthService);

  produits = signal<Produit[]>([]);
  categories = signal<string[]>([]);
  loading = signal(true);
  error = signal('');

  // Filtres
  recherche = '';
  categorie = '';
  alerteSeulement = false;
  private recherche$ = new Subject<void>();

  // Fenêtres modales
  produitModal = signal<{ produit: Produit | null } | null>(null);
  mouvementModal = signal<{ produitId: number; type: TypeMouvement } | null>(null);
  saving = signal(false);
  modalError = signal('');

  ngOnInit(): void {
    // On attend 300 ms après la dernière frappe avant d'interroger l'API
    this.recherche$.pipe(debounceTime(300)).subscribe(() => this.charger());
    this.charger();
    this.api.categories().subscribe((c) => this.categories.set(c));
  }

  onRecherche(): void {
    this.recherche$.next();
  }

  charger(): void {
    this.api.produits({ q: this.recherche, categorie: this.categorie, alerte: this.alerteSeulement }).subscribe({
      next: (list) => {
        this.produits.set(list);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }

  ouvrirProduit(produit: Produit | null): void {
    this.modalError.set('');
    this.produitModal.set({ produit });
  }

  ouvrirMouvement(p: Produit, type: TypeMouvement): void {
    this.modalError.set('');
    this.mouvementModal.set({ produitId: p.id, type });
  }

  enregistrerProduit(body: ProduitForm): void {
    const p = this.produitModal()?.produit;
    this.saving.set(true);
    (p ? this.api.modifierProduit(p.id, body) : this.api.creerProduit(body)).subscribe({
      next: () => this.apresSucces(() => this.produitModal.set(null)),
      error: (e) => this.echec(e),
    });
  }

  enregistrerMouvement(m: MouvementSaisi): void {
    this.saving.set(true);
    this.api.enregistrerMouvement(m.produitId, m.type, m.quantite, m.motif).subscribe({
      next: () => this.apresSucces(() => this.mouvementModal.set(null)),
      error: (e) => this.echec(e),
    });
  }

  supprimer(p: Produit): void {
    if (!confirm(`Supprimer « ${p.nom} » ? Son historique de mouvements sera aussi supprimé.`)) return;
    this.api.supprimerProduit(p.id).subscribe({
      next: () => this.charger(),
      error: (e) => this.error.set(apiError(e)),
    });
  }

  exporter(): void {
    this.api.exporterCsv().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `inventaire-${new Date().toISOString().slice(0, 10)}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (e) => this.error.set(apiError(e)),
    });
  }

  private apresSucces(fermer: () => void): void {
    this.saving.set(false);
    fermer();
    this.charger();
    this.api.categories().subscribe((c) => this.categories.set(c));
  }

  private echec(e: unknown): void {
    this.saving.set(false);
    this.modalError.set(apiError(e));
  }
}
