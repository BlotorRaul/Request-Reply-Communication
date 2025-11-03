import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface Device {
  id: string;
  name: string;
  type: string;
  status: string;
  userId: string | null;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class DeviceService {
  private readonly apiBase = '/api';

  constructor(private http: HttpClient) {}

  async getDevicesByUser(userId: string): Promise<Device[]> {
    return await firstValueFrom(
      this.http.get<Device[]>(`${this.apiBase}/devices/user/${userId}`)
    );
  }

  async getAllDevices(): Promise<Device[]> {
    return await firstValueFrom(
      this.http.get<Device[]>(`${this.apiBase}/devices`)
    );
  }

  async assignUserToDevice(deviceId: string, userId: string): Promise<Device> {
    return await firstValueFrom(
      this.http.put<Device>(`${this.apiBase}/devices/${deviceId}/assign-user/${userId}`, {})
    );
  }

  async unassignUserFromDevice(deviceId: string): Promise<Device> {
    return await firstValueFrom(
      this.http.put<Device>(`${this.apiBase}/devices/${deviceId}/unassign-user`, {})
    );
  }

  async createDevice(name: string, type: string): Promise<Device> {
    return await firstValueFrom(
      this.http.post<Device>(`${this.apiBase}/devices`, {
        name,
        type,
        status: 'INACTIVE', // Device nou este INACTIVE (fără user)
        userId: null
      })
    );
  }

  async deleteDevice(deviceId: string): Promise<void> {
    return await firstValueFrom(
      this.http.delete<void>(`${this.apiBase}/devices/${deviceId}`)
    );
  }

  async updateDevice(deviceId: string, name: string, type: string, status?: string, userId?: string | null): Promise<Device> {
    const body: any = { name, type };
    if (status !== undefined) body.status = status;
    if (userId !== undefined) body.userId = userId;
    
    return await firstValueFrom(
      this.http.put<Device>(`${this.apiBase}/devices/${deviceId}`, body)
    );
  }
}
