import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, AuthMode } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <div class="card">
        <h2 class="title">Sign in</h2>
        <p class="subtitle">Mode: <b>{{ mode() }}</b></p>

        <form (ngSubmit)="onSubmit()" *ngIf="mode() !== 'default'" class="form">
          <ng-container *ngIf="mode() === 'jwt'; else basicTpl">
            <label>Email</label>
            <input [(ngModel)]="email" name="email" type="email" required />

            <label>Password</label>
            <input [(ngModel)]="password" name="password" type="password" autocomplete="off" required />
          </ng-container>

          <ng-template #basicTpl>
            <label>Username</label>
            <input [(ngModel)]="username" name="username" autocomplete="off" required />

            <label>Password</label>
            <input [(ngModel)]="password" name="password" type="password" autocomplete="off" required />
          </ng-template>

          <button class="btn" type="submit">Login</button>
        </form>

        <p *ngIf="error()" class="error">{{ error() }}</p>
      </div>
    </div>
  `,
  styles: [
    `
    .page { min-height: 100vh; display: grid; place-items: center; background:#0b1520; }
    .card { width: 380px; background: #0f1b2a; color:#e5eef7; padding: 24px 28px; border-radius: 12px; box-shadow: 0 10px 24px rgba(0,0,0,.35); }
    .title { margin:0 0 4px; font-weight:600; }
    .subtitle { margin:0 0 16px; color:#94a3b8; }
    .form { display:grid; gap:10px; }
    input { padding:10px 12px; border-radius:8px; border:1px solid #1f2a3a; background:#0b1624; color:#e5eef7; }
    input:focus { outline:none; border-color:#2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,.25); }
    .btn { margin-top:6px; padding:10px 12px; border-radius:8px; border:0; background:#2563eb; color:white; cursor:pointer; }
    .btn:hover { background:#1e4fc7; }
    .error { margin-top:10px; color:#fca5a5; }
    `,
  ],
})
export class LoginComponent {
  mode = signal<AuthMode>('default');
  error = signal<string>('');

  email = '';
  username = '';
  password = '';

  constructor(private auth: AuthService, private router: Router) {
    this.initialize();
  }

  async initialize(): Promise<void> {
    try {
      const mode = await this.auth.fetchAuthMode();
      this.mode.set(mode);
    } catch (e: any) {
      this.error.set('Cannot determine auth mode');
    }
  }

  async onSubmit(): Promise<void> {
    try {
      this.error.set('');
      const mode = this.mode();
      if (mode === 'jwt') {
        await this.auth.loginJwt(this.email, this.password);
      } else if (mode === 'basic') {
        const userOrEmail = this.username || this.email;
        await this.auth.loginBasic(userOrEmail, this.password);
      } else {
        this.error.set('Auth mode is default - no login required');
        return;
      }
      this.router.navigateByUrl('/home');
    } catch (e: any) {
      this.error.set(e?.message || 'Login failed');
    }
  }
}


