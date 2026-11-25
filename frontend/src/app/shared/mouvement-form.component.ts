import { Component, EventEmitter, HostListener, Input, OnInit, Output, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Produit, TypeMouvement } from '../core/models';

export interface MouvementSaisi {
  produitId: number;
  type: TypeMouvement;
  quantite: number;
  motif: string;
}

/** Fenêtre de saisie d'une entrée ou d'une sortie de stock. */
@Component({
  selector: 'app-mouvement-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="backdrop" (click)="cancel.emit()">
      <form class="modal" [formGroup]="form" (ngSubmit)="submit()" (click)="$event.stopPropagation()">
        <header>
          <h2>Mouvement de stock</h2>
          <button type="button" class="close" (click)="cancel.emit()" aria-label="Fermer">✕</button>
        </header>

        <div class="types">
          <button type="button" [class.active]="type() === 'ENTREE'" class="in" (click)="form.controls.type.setValue('ENTREE')">⬇ Entrée</button>
          <button type="button" [class.active]="type() === 'SORTIE'" class="out" (click)="form.controls.type.setValue('SORTIE')">⬆ Sortie</button>
        </div>

        <div class="form-grid">
          <label class="full">Produit *
            <select formControlName="produitId">
              <option [ngValue]="0" disabled>Choisir un produit…</option>
              @for (p of produits; track p.id) {
                <option [ngValue]="p.id">{{ p.nom }} ({{ p.reference }}) — stock : {{ p.quantite }}</option>
              }
            </select>
          </label>
          <label>Quantité *<input type="number" min="1" formControlName="quantite"></label>
          <label>Motif<input formControlName="motif" placeholder="Commande fournisseur, service…"></label>
        </div>

        @if (apercu(); as a) {
          <p class="preview" [class.bad]="a.apres < 0">
            Stock : <b>{{ a.avant }}</b> → <b>{{ a.apres }}</b>
            @if (a.apres < 0) { — stock insuffisant }
          </p>
        }
        @if (error) { <p class="error">{{ error }}</p> }

        <footer>
          <button type="button" class="btn-ghost" (click)="cancel.emit()">Annuler</button>
          <button type="submit" class="btn-primary" [disabled]="saving || form.invalid || (apercu()?.apres ?? 0) < 0">
            {{ saving ? 'Enregistrement…' : 'Valider' }}
          </button>
        </footer>
      </form>
    </div>
  `,
  styles: `
    .types { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 18px; }
    .types button { font: inherit; font-weight: 700; padding: 12px; border-radius: 12px; border: 2px solid var(--line); background: #fff; cursor: pointer; color: var(--muted); }
    .types .in.active { border-color: #3b82f6; background: #eff6ff; color: #1d4ed8; }
    .types .out.active { border-color: #f97316; background: #fff7ed; color: #c2410c; }
    .preview { margin: 16px 0 0; padding: 10px 14px; background: var(--bg); border-radius: 10px; font-size: 14px; }
    .preview.bad { background: #fef2f2; color: var(--danger); }
  `,
})
export class MouvementFormComponent implements OnInit {
  @Input() produits: Produit[] = [];
  @Input() produitId: number | null = null;
  @Input() typeInitial: TypeMouvement = 'SORTIE';
  @Input() saving = false;
  @Input() error = '';
  @Output() save = new EventEmitter<MouvementSaisi>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    produitId: [0, Validators.min(1)],
    type: ['SORTIE' as TypeMouvement],
    quantite: [1, [Validators.required, Validators.min(1)]],
    motif: [''],
  });

  private valeurs = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  type = computed(() => this.valeurs().type);

  // Aperçu du stock après le mouvement, avant même de valider
  apercu = computed(() => {
    const v = this.valeurs();
    const p = this.produits.find((x) => x.id === v.produitId);
    if (!p) return null;
    const q = Number(v.quantite) || 0;
    return { avant: p.quantite, apres: v.type === 'ENTREE' ? p.quantite + q : p.quantite - q };
  });

  ngOnInit(): void {
    this.form.patchValue({ produitId: this.produitId ?? 0, type: this.typeInitial });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cancel.emit();
  }

  submit(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    this.save.emit({ produitId: v.produitId, type: v.type, quantite: Number(v.quantite), motif: v.motif.trim() });
  }
}
