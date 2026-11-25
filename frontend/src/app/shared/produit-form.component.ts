import { Component, EventEmitter, HostListener, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Produit, ProduitForm } from '../core/models';

/** Fenêtre d'ajout ou de modification d'un produit (réservée aux administrateurs). */
@Component({
  selector: 'app-produit-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="backdrop" (click)="cancel.emit()">
      <form class="modal" [formGroup]="form" (ngSubmit)="submit()" (click)="$event.stopPropagation()">
        <header>
          <h2>{{ produit ? 'Modifier le produit' : 'Nouveau produit' }}</h2>
          <button type="button" class="close" (click)="cancel.emit()" aria-label="Fermer">✕</button>
        </header>
        <div class="form-grid">
          <label>Référence *<input formControlName="reference" placeholder="PC-LEN-T14"></label>
          <label>Catégorie *
            <input formControlName="categorie" list="categories" placeholder="Informatique">
            <datalist id="categories">
              @for (c of categories; track c) { <option [value]="c"></option> }
            </datalist>
          </label>
          <label class="full">Nom *<input formControlName="nom" placeholder="Ordinateur portable Lenovo T14"></label>
          <label>Prix unitaire (DT) *<input type="number" step="0.001" min="0" formControlName="prixUnitaire"></label>
          <label>Seuil d'alerte<input type="number" min="0" formControlName="seuilAlerte"></label>
          @if (!produit) {
            <label>Stock initial<input type="number" min="0" formControlName="stockInitial"></label>
          }
        </div>
        @if (produit) {
          <p class="muted hint">La quantité en stock se modifie avec des entrées et des sorties, pour garder un historique complet.</p>
        }
        @if (touched() && form.invalid) { <p class="error">Référence, nom, catégorie et prix sont obligatoires.</p> }
        @if (error) { <p class="error">{{ error }}</p> }
        <footer>
          <button type="button" class="btn-ghost" (click)="cancel.emit()">Annuler</button>
          <button type="submit" class="btn-primary" [disabled]="saving">{{ saving ? 'Enregistrement…' : 'Enregistrer' }}</button>
        </footer>
      </form>
    </div>
  `,
  styles: `.hint { font-size: 13px; margin: 14px 0 0; }`,
})
export class ProduitFormComponent implements OnInit {
  @Input() produit: Produit | null = null;
  @Input() categories: string[] = [];
  @Input() saving = false;
  @Input() error = '';
  @Output() save = new EventEmitter<ProduitForm>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  touched = signal(false);

  form = this.fb.nonNullable.group({
    reference: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{2,40}$/)]],
    nom: ['', Validators.required],
    categorie: ['', Validators.required],
    prixUnitaire: [0, [Validators.required, Validators.min(0)]],
    seuilAlerte: [5, Validators.min(0)],
    stockInitial: [0, Validators.min(0)],
  });

  ngOnInit(): void {
    if (this.produit) {
      const p = this.produit;
      this.form.patchValue({ reference: p.reference, nom: p.nom, categorie: p.categorie, prixUnitaire: p.prixUnitaire, seuilAlerte: p.seuilAlerte });
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cancel.emit();
  }

  submit(): void {
    this.touched.set(true);
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    this.save.emit({ ...v, reference: v.reference.trim().toUpperCase(), nom: v.nom.trim(), categorie: v.categorie.trim() });
  }
}
