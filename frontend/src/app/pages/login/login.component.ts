import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { apiError } from '../../core/errors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  mode = signal<'login' | 'register'>('login');
  loading = signal(false);
  error = signal('');

  form = this.fb.nonNullable.group({
    nom: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  toggle(): void {
    this.mode.update((m) => (m === 'login' ? 'register' : 'login'));
    this.error.set('');
  }

  fillDemo(role: 'admin' | 'employe'): void {
    this.mode.set('login');
    this.form.patchValue({ email: `${role}@stockflow.tn`, password: `${role}1234` });
  }

  submit(): void {
    const { nom, email, password } = this.form.getRawValue();
    if (this.form.invalid || (this.mode() === 'register' && !nom.trim())) {
      this.error.set('Vérifiez les champs : email valide et mot de passe de 8 caractères minimum.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    const call = this.mode() === 'login' ? this.auth.login(email, password) : this.auth.register(nom, email, password);
    call.subscribe({
      next: () => this.router.navigate(['/tableau-de-bord']),
      error: (e) => {
        this.error.set(apiError(e));
        this.loading.set(false);
      },
    });
  }
}
