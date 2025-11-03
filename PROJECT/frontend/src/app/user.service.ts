import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface User {
  id: string;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  city?: string;
  country?: string;
  department?: string;
  jobTitle?: string;
  active?: boolean;
  role?: string; // Role from auth-service (ADMIN/USER)
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiBase = '/api';

  constructor(private http: HttpClient) {}

  async createAuthUser(email: string, password: string, role: string = 'USER'): Promise<{ id: string; email: string; role: string }> {
    return await firstValueFrom(
      this.http.post<{ id: string; email: string; role: string }>(`${this.apiBase}/auth/users`, {
        email,
        password,
        role,
        active: true
      })
    );
  }

  async createUser(firstName: string, lastName: string, email: string, phoneNumber?: string, address?: string, city?: string, country?: string, department?: string, jobTitle?: string): Promise<User> {
    return await firstValueFrom(
      this.http.post<User>(`${this.apiBase}/users`, {
        firstName,
        lastName,
        email,
        phoneNumber,
        address,
        city,
        country,
        department,
        jobTitle,
        active: true
      })
    );
  }

  async getAllUsers(): Promise<User[]> {
    // Try user-service first, then fallback to auth-service
    try {
      return await firstValueFrom(
        this.http.get<User[]>(`${this.apiBase}/users`)
      );
    } catch (e: any) {
      console.warn('Failed to load users from user-service, trying auth-service...', e?.status || e?.message);
      // Fallback to auth-service
      try {
        const authUsers = await firstValueFrom(
          this.http.get<Array<{ id: string; email: string; firstName?: string; lastName?: string; role?: string }>>(
            `${this.apiBase}/auth/users`
          )
        );
        // Convert auth users to User format
        return authUsers.map(au => ({
          id: au.id,
          email: au.email,
          firstName: au.firstName,
          lastName: au.lastName,
          fullName: au.firstName && au.lastName ? `${au.firstName} ${au.lastName}`.trim() : undefined
        }));
      } catch (e2: any) {
        console.error('Failed to load users from auth-service:', e2?.status || e2?.message);
        throw new Error('Failed to load users from both services');
      }
    }
  }

  async getUserByEmail(email: string): Promise<User | null> {
    // Try user-service first
    try {
      const users = await firstValueFrom(
        this.http.get<User[]>(`${this.apiBase}/users`)
      );
      const user = users.find(u => u.email.toLowerCase() === email.toLowerCase());
      if (user) return user;
    } catch (e: any) {
      console.warn('Failed to load users from user-service for email lookup:', e?.status || e?.message);
    }
    
    // Fallback to auth-service
    try {
      const authUsers = await firstValueFrom(
        this.http.get<Array<{ id: string; email: string; firstName?: string; lastName?: string }>>(
          `${this.apiBase}/auth/users`
        )
      );
      const authUser = authUsers.find(au => au.email.toLowerCase() === email.toLowerCase());
      if (authUser) {
        return {
          id: authUser.id,
          email: authUser.email,
          firstName: authUser.firstName,
          lastName: authUser.lastName,
          fullName: authUser.firstName && authUser.lastName ? `${authUser.firstName} ${authUser.lastName}`.trim() : undefined
        };
      }
    } catch (e2: any) {
      console.error('Failed to load users from auth-service for email lookup:', e2?.status || e2?.message);
    }
    
    return null;
  }

  async deleteAuthUser(userId: string): Promise<void> {
    return await firstValueFrom(
      this.http.delete<void>(`${this.apiBase}/auth/users/${userId}`)
    );
  }

  async deleteUser(userId: string): Promise<void> {
    return await firstValueFrom(
      this.http.delete<void>(`${this.apiBase}/users/${userId}`)
    );
  }

  async updateAuthUser(userId: string, email: string, password: string, role: string, active: boolean = true): Promise<{ id: string; email: string; role: string }> {
    return await firstValueFrom(
      this.http.put<{ id: string; email: string; role: string }>(`${this.apiBase}/auth/users/${userId}`, {
        email,
        password,
        role,
        active
      })
    );
  }

  async updateUser(
    userId: string,
    firstName: string,
    lastName: string,
    email: string,
    phoneNumber?: string,
    address?: string,
    city?: string,
    country?: string,
    department?: string,
    jobTitle?: string,
    active: boolean = true
  ): Promise<User> {
    return await firstValueFrom(
      this.http.put<User>(`${this.apiBase}/users/${userId}`, {
        firstName,
        lastName,
        email,
        phoneNumber,
        address,
        city,
        country,
        department,
        jobTitle,
        active
      })
    );
  }

  async getUserById(userId: string): Promise<User> {
    // Try UserLocal first (device-service), then fallback to User (user-service)
    try {
      const userLocal = await firstValueFrom(
        this.http.get<{ id: string; fullName: string; email: string; active: boolean }>(
          `${this.apiBase}/users-local/${userId}`
        )
      );
      // Convert UserLocal to User format
      return {
        id: userLocal.id,
        fullName: userLocal.fullName,
        email: userLocal.email,
        active: userLocal.active
      };
    } catch (e: any) {
      // If device-service returns 404/500/any error, try user-service
      const status = e?.status || e?.error?.status || 0;
      console.warn(`UserLocal ${userId} not found in device-service (status: ${status}), trying user-service...`);
      
      // Fallback to user-service
      try {
        const user = await firstValueFrom(
          this.http.get<User>(`${this.apiBase}/users/${userId}`)
        );
        console.log(`User ${userId} found in user-service: ${user.email}`);
        return user;
      } catch (e2: any) {
        const status2 = e2?.status || e2?.error?.status || 0;
        console.warn(`User ${userId} not found in user-service either (status: ${status2})`);
        // If both fail, throw a more descriptive error
        throw new Error(`User ${userId} not found in both services`);
      }
    }
  }
}
