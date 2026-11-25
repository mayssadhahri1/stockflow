// Types partagés avec l'API Spring Boot

export type TypeMouvement = 'ENTREE' | 'SORTIE';

export interface Produit {
  id: number;
  reference: string;
  nom: string;
  categorie: string;
  prixUnitaire: number;
  quantite: number;
  seuilAlerte: number;
  enAlerte: boolean;
  valeurStock: number;
}

export interface ProduitForm {
  reference: string;
  nom: string;
  categorie: string;
  prixUnitaire: number;
  seuilAlerte: number;
  stockInitial: number;
}

export interface Mouvement {
  id: number;
  produitId: number;
  produitReference: string;
  produitNom: string;
  type: TypeMouvement;
  quantite: number;
  stockApres: number;
  motif: string | null;
  auteur: string;
  date: string;
}

export interface Dashboard {
  nbProduits: number;
  unitesEnStock: number;
  valeurStock: number;
  nbAlertes: number;
  entrees30j: number;
  sorties30j: number;
  valeurParCategorie: Record<string, number>;
  alertes: Produit[];
  derniersMouvements: Mouvement[];
}

export interface AuthResponse {
  token: string;
  nom: string;
  email: string;
  role: 'ADMIN' | 'EMPLOYE';
}
