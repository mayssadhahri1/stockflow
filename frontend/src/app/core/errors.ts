import { HttpErrorResponse } from '@angular/common/http';

/** Extrait un message lisible d'une erreur renvoyée par l'API. */
export function apiError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return "Le serveur ne répond pas. L'API Spring Boot est-elle lancée ?";
    const fields = err.error?.fields as Record<string, string> | undefined;
    if (fields) return Object.values(fields).join(' · ');
    return err.error?.message ?? 'Une erreur est survenue';
  }
  return 'Une erreur est survenue';
}
