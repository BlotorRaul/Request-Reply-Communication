import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export type AuthMode = 'default' | 'basic' | 'jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = '/api';

  constructor(private http: HttpClient) {}

  async fetchAuthMode(): Promise<AuthMode> {
    const resp = await firstValueFrom(
      this.http.get<{ mode: string }>(`${this.apiBase}/auth/users/mode`)
    );
    const mode = (resp?.mode || 'default').toLowerCase() as AuthMode;
    localStorage.setItem('auth_mode', mode);
    return mode;
  }

  async loginJwt(email: string, password: string): Promise<void> {
    const resp = await firstValueFrom(
      this.http.post<any>(`${this.apiBase}/auth/users/login`, { email, password })
    );
    const token: string = resp?.message || resp?.token || '';
    if (!token) throw new Error('Token not returned by server');
    localStorage.setItem('jwt_token', token);
    // Save userId and email for device fetching
    if (resp?.id) localStorage.setItem('user_id', resp.id);
    if (resp?.email) localStorage.setItem('user_email', resp.email);
    if (resp?.role) localStorage.setItem('user_role', resp.role);
  }

  async loginBasic(username: string, password: string): Promise<void> {
    const b64 = btoa(`${username}:${password}`);
    const headers = new HttpHeaders({ Authorization: `Basic ${b64}` });
    // hit validate endpoint to confirm credentials, then store
    const resp = await firstValueFrom(
      this.http.post<{ valid: boolean; username: string; roles?: any[] }>(
        `${this.apiBase}/auth/validate-basic`,
        {},
        { headers }
      )
    );
    localStorage.setItem('basic_b64', b64);
    // For Basic auth, we'll need to fetch user details separately to get userId
    // For now, store username
    if (resp?.username) localStorage.setItem('user_email', resp.username);
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('basic_b64');
    localStorage.removeItem('user_id');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_role');
  }

  getUserId(): string | null {
    return localStorage.getItem('user_id');
  }

  getUserEmail(): string | null {
    return localStorage.getItem('user_email');
  }

  getUserRole(): string | null {
    return localStorage.getItem('user_role');
  }

  getMode(): AuthMode {
    return (localStorage.getItem('auth_mode') || 'default') as AuthMode;
  }

  getJwt(): string | null {
    return localStorage.getItem('jwt_token');
  }

  getBasicB64(): string | null {
    return localStorage.getItem('basic_b64');
  }
}

