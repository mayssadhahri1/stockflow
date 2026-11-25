import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Dashboard, Mouvement, Produit, ProduitForm, TypeMouvement } from './models';

@Injectable({ providedIn: 'root' })
export class StockService {
  private http = inject(HttpClient);

  dashboard() {
    return this.http.get<Dashboard>('/api/dashboard');
  }

  produits(filtres: { q?: string; categorie?: string; alerte?: boolean } = {}) {
    let params = new HttpParams();
    if (filtres.q) params = params.set('q', filtres.q);
    if (filtres.categorie) params = params.set('categorie', filtres.categorie);
    if (filtres.alerte) params = params.set('alerte', 'true');
    return this.http.get<Produit[]>('/api/produits', { params });
  }

  categories() {
    return this.http.get<string[]>('/api/produits/categories');
  }

  creerProduit(body: ProduitForm) {
    return this.http.post<Produit>('/api/produits', body);
  }

  modifierProduit(id: number, body: ProduitForm) {
    return this.http.put<Produit>(`/api/produits/${id}`, body);
  }

  supprimerProduit(id: number) {
    return this.http.delete<void>(`/api/produits/${id}`);
  }

  // Le fichier est téléchargé via HttpClient pour que le jeton JWT soit envoyé
  exporterCsv() {
    return this.http.get('/api/produits/export', { responseType: 'blob' });
  }

  mouvements(produitId?: number, limite = 100) {
    let params = new HttpParams().set('limite', limite);
    if (produitId) params = params.set('produitId', produitId);
    return this.http.get<Mouvement[]>('/api/mouvements', { params });
  }

  enregistrerMouvement(produitId: number, type: TypeMouvement, quantite: number, motif: string) {
    return this.http.post<Mouvement>('/api/mouvements', { produitId, type, quantite, motif });
  }
}
