import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { DeviceService, Device } from './device.service';
import { UserService, User } from './user.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <div class="container">
        <div class="header">
          <div>
            <h2>{{ isAdmin() ? 'All Devices' : 'My Devices' }}</h2>
            <p class="subtitle">{{ isAdmin() ? 'All devices in the system' : 'Connected devices for ' + userEmail() }}</p>
          </div>
          <div class="header-actions">
            <button *ngIf="isAdmin()" class="create-btn" (click)="onCreateDevice()" [disabled]="creating()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              {{ creating() ? 'Creating...' : 'Create Device' }}
            </button>
            <button *ngIf="isAdmin()" class="create-btn" (click)="onCreateUser()" [disabled]="creatingUser()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              {{ creatingUser() ? 'Creating...' : 'Create User' }}
            </button>
            <button *ngIf="isAdmin()" class="manage-btn" (click)="onManageDevices()" [disabled]="loadingDevices()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2" />
                <line x1="8" y1="21" x2="16" y2="21" />
                <line x1="12" y1="17" x2="12" y2="21" />
              </svg>
              Manage Devices
            </button>
            <button *ngIf="isAdmin()" class="manage-btn" (click)="onManageUsers()" [disabled]="loadingUsers()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              Manage Users
            </button>
            <button class="logout-btn" (click)="onLogout()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              Logout
            </button>
          </div>
        </div>

        <div *ngIf="loading()" class="loading">Loading devices...</div>
        <div *ngIf="error()" class="error">{{ error() }}</div>

        <div *ngIf="!loading() && !error()" class="devices-grid">
          <div *ngFor="let device of devices()" 
               class="device-card" 
               [class.clickable]="isAdmin()"
               (click)="isAdmin() && onDeviceClick(device)">
            <div class="device-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="5" y="2" width="14" height="20" rx="2" />
                <path d="M12 18h.01" />
              </svg>
            </div>
            <div class="device-info">
              <h3>{{ device.name }}</h3>
              <p class="device-type">{{ device.type }}</p>
              <div class="device-status">
                <span class="status-dot" [class.status-on]="device.userId !== null"></span>
                <span>{{ device.userId ? 'ACTIVE' : 'INACTIVE' }}</span>
              </div>
              <p class="device-user" [class.no-user]="!device.userId">
                <span *ngIf="device.userId">
                  User: {{ getUserName(device.userId) || 'Loading...' }}
                </span>
                <span *ngIf="!device.userId" class="unassigned">No user assigned</span>
              </p>
              <div *ngIf="isAdmin()" class="device-actions">
                <button class="delete-btn" (click)="onDeleteDevice($event, device)" [disabled]="deleting() === device.id">
                  {{ deleting() === device.id ? 'Deleting...' : 'Delete' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal for creating device -->
        <div *ngIf="showCreateModal()" class="modal-overlay" (click)="closeCreateModal()">
          <div class="modal-content" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Create New Device</h3>
              <button class="close-btn" (click)="closeCreateModal()">×</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="deviceName">Device Name</label>
                <input 
                  id="deviceName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newDeviceName"
                  placeholder="Enter device name"
                  maxlength="100"
                />
              </div>
              <div class="form-group">
                <label for="deviceType">Device Type</label>
                <input 
                  id="deviceType"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newDeviceType"
                  placeholder="e.g., Laptop, Phone, Sensor"
                  maxlength="50"
                />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeCreateModal()">Cancel</button>
              <button class="btn-save" (click)="saveNewDevice()" [disabled]="creating() || !canCreateDevice()">
                {{ creating() ? 'Creating...' : 'Create' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Modal for creating user -->
        <div *ngIf="showCreateUserModal()" class="modal-overlay" (click)="closeCreateUserModal()">
          <div class="modal-content modal-content-large" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Create New User</h3>
              <button class="close-btn" (click)="closeCreateUserModal()">×</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="userFirstName">First Name <span class="required">*</span></label>
                <input 
                  id="userFirstName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newUserFirstName"
                  placeholder="Enter first name"
                  maxlength="50"
                  required
                />
              </div>
              <div class="form-group">
                <label for="userLastName">Last Name <span class="required">*</span></label>
                <input 
                  id="userLastName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newUserLastName"
                  placeholder="Enter last name"
                  maxlength="50"
                  required
                />
              </div>
              <div class="form-group">
                <label for="userEmail">Email <span class="required">*</span></label>
                <input 
                  id="userEmail"
                  type="email" 
                  class="form-input"
                  [(ngModel)]="newUserEmail"
                  placeholder="Enter email address"
                  maxlength="100"
                  required
                />
              </div>
              <div class="form-group">
                <label for="userPassword">Password <span class="required">*</span></label>
                <input 
                  id="userPassword"
                  type="password" 
                  class="form-input"
                  [(ngModel)]="newUserPassword"
                  placeholder="Enter password"
                  maxlength="100"
                  required
                />
              </div>
              <div class="form-group">
                <label for="userRole">Role <span class="required">*</span></label>
                <select 
                  id="userRole"
                  class="form-input"
                  [(ngModel)]="newUserRole"
                  required
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div class="form-group">
                <label for="userPhone">Phone Number</label>
                <input 
                  id="userPhone"
                  type="tel" 
                  class="form-input"
                  [(ngModel)]="newUserPhone"
                  placeholder="Enter phone number (optional)"
                  maxlength="20"
                />
              </div>
              <div class="form-group">
                <label for="userAddress">Address</label>
                <input 
                  id="userAddress"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newUserAddress"
                  placeholder="Enter address (optional)"
                  maxlength="200"
                />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label for="userCity">City</label>
                  <input 
                    id="userCity"
                    type="text" 
                    class="form-input"
                    [(ngModel)]="newUserCity"
                    placeholder="Enter city (optional)"
                    maxlength="50"
                  />
                </div>
                <div class="form-group">
                  <label for="userCountry">Country</label>
                  <input 
                    id="userCountry"
                    type="text" 
                    class="form-input"
                    [(ngModel)]="newUserCountry"
                    placeholder="Enter country (optional)"
                    maxlength="50"
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="userDepartment">Department</label>
                <input 
                  id="userDepartment"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newUserDepartment"
                  placeholder="Enter department (optional)"
                  maxlength="100"
                />
              </div>
              <div class="form-group">
                <label for="userJobTitle">Job Title</label>
                <input 
                  id="userJobTitle"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="newUserJobTitle"
                  placeholder="Enter job title (optional)"
                  maxlength="100"
                />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeCreateUserModal()">Cancel</button>
              <button class="btn-save" (click)="saveNewUser()" [disabled]="creatingUser() || !canCreateUser()">
                {{ creatingUser() ? 'Creating...' : 'Create' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Modal for editing user -->
        <div *ngIf="showEditUserModal()" class="modal-overlay" (click)="closeEditUserModal()">
          <div class="modal-content modal-content-large" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Edit User</h3>
              <button class="close-btn" (click)="closeEditUserModal()">×</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="editFirstName">First Name <span class="required">*</span></label>
                <input 
                  id="editFirstName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editUserFirstName"
                  placeholder="Enter first name"
                  maxlength="50"
                  required
                />
              </div>
              <div class="form-group">
                <label for="editLastName">Last Name <span class="required">*</span></label>
                <input 
                  id="editLastName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editUserLastName"
                  placeholder="Enter last name"
                  maxlength="50"
                  required
                />
              </div>
              <div class="form-group">
                <label for="editEmail">Email <span class="required">*</span></label>
                <input 
                  id="editEmail"
                  type="email" 
                  class="form-input"
                  [(ngModel)]="editUserEmail"
                  placeholder="Enter email address"
                  maxlength="100"
                  required
                />
              </div>
              <div class="form-group">
                <label for="editPassword">New Password <span class="optional">(optional)</span></label>
                <input 
                  id="editPassword"
                  type="password" 
                  class="form-input"
                  [(ngModel)]="editUserPassword"
                  placeholder="Leave empty to keep current password"
                  maxlength="100"
                />
                <small class="form-hint">Only fill this if you want to change the password</small>
              </div>
              <div class="form-group">
                <label for="editRole">Role <span class="required">*</span></label>
                <select 
                  id="editRole"
                  class="form-input"
                  [(ngModel)]="editUserRole"
                  required
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div class="form-group">
                <label for="editPhone">Phone Number</label>
                <input 
                  id="editPhone"
                  type="tel" 
                  class="form-input"
                  [(ngModel)]="editUserPhone"
                  placeholder="Enter phone number (optional)"
                  maxlength="20"
                />
              </div>
              <div class="form-group">
                <label for="editAddress">Address</label>
                <input 
                  id="editAddress"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editUserAddress"
                  placeholder="Enter address (optional)"
                  maxlength="200"
                />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label for="editCity">City</label>
                  <input 
                    id="editCity"
                    type="text" 
                    class="form-input"
                    [(ngModel)]="editUserCity"
                    placeholder="Enter city (optional)"
                    maxlength="50"
                  />
                </div>
                <div class="form-group">
                  <label for="editCountry">Country</label>
                  <input 
                    id="editCountry"
                    type="text" 
                    class="form-input"
                    [(ngModel)]="editUserCountry"
                    placeholder="Enter country (optional)"
                    maxlength="50"
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="editDepartment">Department</label>
                <input 
                  id="editDepartment"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editUserDepartment"
                  placeholder="Enter department (optional)"
                  maxlength="100"
                />
              </div>
              <div class="form-group">
                <label for="editJobTitle">Job Title</label>
                <input 
                  id="editJobTitle"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editUserJobTitle"
                  placeholder="Enter job title (optional)"
                  maxlength="100"
                />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeEditUserModal()">Cancel</button>
              <button class="btn-save" (click)="saveUserEdit()" [disabled]="savingUser() || !canSaveUserEdit()">
                {{ savingUser() ? 'Saving...' : 'Save' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Modal for managing devices -->
        <div *ngIf="showManageDevicesModal()" class="modal-overlay" (click)="closeManageDevicesModal()">
          <div class="modal-content modal-content-large" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Manage Devices</h3>
              <button class="close-btn" (click)="closeManageDevicesModal()">×</button>
            </div>
            <div class="modal-body">
              <div *ngIf="loadingDevices()" class="loading">Loading devices...</div>
              <div *ngIf="devicesError()" class="error">{{ devicesError() }}</div>
              <div *ngIf="!loadingDevices() && !devicesError()" class="users-management-list">
                <div *ngFor="let device of manageDevicesList()" class="user-management-item">
                  <div class="user-info">
                    <div class="user-name-email">
                      <strong>{{ device.name }}</strong>
                      <span class="user-email-small">{{ device.type }} • {{ device.userId ? 'ACTIVE' : 'INACTIVE' }}</span>
                    </div>
                    <span class="user-role-badge" [class.status-on]="device.userId !== null" [class.status-off]="device.userId === null">
                      {{ device.userId ? 'ACTIVE' : 'INACTIVE' }}
                    </span>
                  </div>
                  <div class="user-actions">
                    <button 
                      class="edit-user-btn" 
                      (click)="onEditDevice(device)" 
                      title="Edit device">
                      Edit
                    </button>
                    <button 
                      class="delete-user-btn" 
                      (click)="onDeleteDeviceFromList(device)" 
                      [disabled]="deletingDeviceId() === device.id"
                      title="Delete device">
                      {{ deletingDeviceId() === device.id ? 'Deleting...' : 'Delete' }}
                    </button>
                  </div>
                </div>
                <div *ngIf="manageDevicesList().length === 0" class="empty-users">
                  <p>No devices found.</p>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeManageDevicesModal()">Close</button>
            </div>
          </div>
        </div>

        <!-- Modal for editing device -->
        <div *ngIf="showEditDeviceModal()" class="modal-overlay" (click)="closeEditDeviceModal()">
          <div class="modal-content" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Edit Device</h3>
              <button class="close-btn" (click)="closeEditDeviceModal()">×</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="editDeviceName">Device Name*</label>
                <input 
                  id="editDeviceName"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editDeviceName"
                  placeholder="Enter device name"
                  maxlength="100"
                  required
                />
              </div>
              <div class="form-group">
                <label for="editDeviceType">Device Type*</label>
                <input 
                  id="editDeviceType"
                  type="text" 
                  class="form-input"
                  [(ngModel)]="editDeviceType"
                  placeholder="Enter device type (e.g., Laptop, Phone, Tablet)"
                  maxlength="50"
                  required
                />
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeEditDeviceModal()">Cancel</button>
              <button class="btn-save" (click)="saveDeviceEdit()" [disabled]="savingDevice() || !canSaveDeviceEdit()">
                {{ savingDevice() ? 'Saving...' : 'Save' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Modal for managing users -->
        <div *ngIf="showManageUsersModal()" class="modal-overlay" (click)="closeManageUsersModal()">
          <div class="modal-content modal-content-large" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Manage Users</h3>
              <button class="close-btn" (click)="closeManageUsersModal()">×</button>
            </div>
            <div class="modal-body">
              <div *ngIf="loadingUsers()" class="loading">Loading users...</div>
              <div *ngIf="usersError()" class="error">{{ usersError() }}</div>
              <div *ngIf="!loadingUsers() && !usersError()" class="users-management-list">
                <div *ngFor="let user of manageUsersList()" class="user-management-item">
                  <div class="user-info">
                    <div class="user-name-email">
                      <strong>{{ getUserDisplayName(user) }}</strong>
                      <span class="user-email-small">{{ user.email }}</span>
                    </div>
                    <span class="user-role-badge" [class.role-admin]="getUserRole(user) === 'ADMIN'" [class.role-user]="getUserRole(user) === 'USER'">
                      {{ getUserRole(user) || 'USER' }}
                    </span>
                  </div>
                  <div class="user-actions">
                    <button 
                    class="edit-user-btn" 
                    (click)="onEditUser(user)" 
                    title="Edit user">
                    Edit
                  </button>
                  <button 
                    class="delete-user-btn" 
                    (click)="onDeleteUser(user)" 
                    [disabled]="deletingUserId() === user.id"
                    title="Delete user">
                      {{ deletingUserId() === user.id ? 'Deleting...' : 'Delete' }}
                    </button>
                  </div>
                </div>
                <div *ngIf="manageUsersList().length === 0" class="empty-users">
                  <p>No users found.</p>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeManageUsersModal()">Close</button>
            </div>
          </div>
        </div>

        <!-- Modal for assigning user -->
        <div *ngIf="showModal()" class="modal-overlay" (click)="closeModal()">
          <div class="modal-content" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Assign Device to User</h3>
              <button class="close-btn" (click)="closeModal()">×</button>
            </div>
            <div class="modal-body">
              <p class="device-name">{{ selectedDevice()?.name }}</p>
              <div *ngIf="loadingUsers()" class="loading">Loading users...</div>
              <div *ngIf="usersError()" class="error">{{ usersError() }}</div>
              <div *ngIf="!loadingUsers() && !usersError()" class="users-list">
                <label class="user-option">
                  <input type="radio" name="userSelect" value="" [checked]="!selectedDevice()?.userId" (change)="selectedUserId.set('')" />
                  <span class="user-label">
                    <strong>Unassign (No user)</strong>
                  </span>
                </label>
                <label *ngFor="let user of availableUsers()" class="user-option">
                  <input type="radio" 
                         name="userSelect" 
                         [value]="user.id" 
                         [checked]="selectedDevice()?.userId === user.id"
                         (change)="selectedUserId.set(user.id)" />
                  <span class="user-label">
                    <strong>{{ getUserDisplayName(user) }}</strong>
                    <span class="user-email">{{ user.email }}</span>
                  </span>
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" (click)="closeModal()">Cancel</button>
              <button class="btn-save" (click)="saveAssignment()" [disabled]="saving()">
                {{ saving() ? 'Saving...' : 'Save' }}
              </button>
            </div>
          </div>
        </div>

        <div *ngIf="!loading() && !error() && devices().length === 0" class="empty">
          <p>No devices connected.</p>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
    .page { min-height: 100vh; background: #0b1520; padding: 24px; }
    .container { max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    h2 { color: #e5eef7; margin: 0 0 8px; }
    .subtitle { color: #94a3b8; margin: 0 0 24px; }
    .header-actions {
      display: flex; align-items: center; gap: 12px;
    }
    .create-btn, .logout-btn {
      display: flex; align-items: center; gap: 8px;
      padding: 10px 16px; border-radius: 8px;
      border: 1px solid #374151; background: #1f2937;
      color: #e5eef7; cursor: pointer; font-size: 14px;
      transition: all 0.2s;
    }
    .create-btn:hover:not(:disabled) {
      background: #2563eb; border-color: #2563eb;
    }
    .create-btn:disabled {
      opacity: 0.5; cursor: not-allowed;
    }
    .logout-btn:hover {
      background: #374151; border-color: #4b5563;
    }
    .logout-btn svg { color: #94a3b8; }
    .create-btn svg { color: #e5eef7; }
    .manage-btn {
      display: flex; align-items: center; gap: 8px;
      padding: 10px 16px; border-radius: 8px;
      border: 1px solid #374151; background: #1f2937;
      color: #e5eef7; cursor: pointer; font-size: 14px;
      transition: all 0.2s;
    }
    .manage-btn:hover:not(:disabled) {
      background: #374151; border-color: #4b5563;
    }
    .manage-btn:disabled {
      opacity: 0.5; cursor: not-allowed;
    }
    .manage-btn svg { color: #e5eef7; }
    .loading, .error, .empty { color: #94a3b8; padding: 16px; text-align: center; }
    .error { color: #fca5a5; }
    .devices-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    .device-card {
      background: #0f1b2a; border-radius: 12px; padding: 20px;
      border: 1px solid #1f2a3a; box-shadow: 0 4px 12px rgba(0,0,0,.3);
      display: flex; gap: 16px; align-items: flex-start;
    }
    .device-icon { color: #2563eb; flex-shrink: 0; }
    .device-info { flex: 1; }
    .device-info h3 { color: #e5eef7; margin: 0 0 6px; font-size: 18px; }
    .device-type { color: #94a3b8; margin: 0 0 8px; font-size: 14px; }
    .device-status {
      display: flex; align-items: center; gap: 8px;
      color: #94a3b8; margin: 0 0 8px;
    }
    .status-dot {
      width: 10px; height: 10px; border-radius: 50%;
      background: #64748b; display: inline-block;
    }
    .status-dot.status-on { background: #22c55e; }
    .device-status span:last-child {
      color: #94a3b8;
    }
    .device-user { color: #64748b; margin: 0; font-size: 12px; }
    .device-user.no-user .unassigned {
      color: #f59e0b; font-style: italic;
    }
    .loading-name {
      color: #64748b; font-style: italic; font-size: 11px;
    }
    .device-actions {
      margin-top: 12px; display: flex; gap: 8px;
    }
    .delete-btn {
      padding: 6px 12px; border-radius: 6px;
      border: 1px solid #374151; background: #1f2937;
      color: #94a3b8; cursor: pointer; font-size: 12px;
      transition: all 0.2s; flex: 1;
    }
    .delete-btn:hover:not(:disabled) {
      background: #374151; border-color: #4b5563;
      color: #e5eef7;
    }
    .delete-btn:disabled {
      opacity: 0.5; cursor: not-allowed;
    }
    .device-card.clickable {
      cursor: pointer; transition: all 0.2s;
    }
    .device-card.clickable:hover {
      background: #15202e; border-color: #2563eb;
      transform: translateY(-2px);
    }
    
    /* Modal styles */
    .modal-overlay {
      position: fixed; inset: 0; background: rgba(0,0,0,0.7);
      display: flex; align-items: center; justify-content: center;
      z-index: 1000;
    }
    .modal-content {
      background: #0f1b2a; border-radius: 12px;
      border: 1px solid #1f2a3a; width: 90%; max-width: 500px;
      max-height: 80vh; display: flex; flex-direction: column;
      box-shadow: 0 8px 32px rgba(0,0,0,0.5);
    }
    .modal-header {
      display: flex; justify-content: space-between;
      align-items: center; padding: 20px;
      border-bottom: 1px solid #1f2a3a;
    }
    .modal-header h3 {
      color: #e5eef7; margin: 0; font-size: 20px;
    }
    .close-btn {
      background: none; border: none; color: #94a3b8;
      font-size: 28px; cursor: pointer; padding: 0;
      width: 32px; height: 32px; display: flex;
      align-items: center; justify-content: center;
      border-radius: 4px; transition: all 0.2s;
    }
    .close-btn:hover {
      background: #1f2a3a; color: #e5eef7;
    }
    .modal-body {
      padding: 20px; flex: 1; overflow-y: auto;
    }
    .device-name {
      color: #94a3b8; margin: 0 0 16px;
      font-size: 14px;
    }
    .users-list {
      display: flex; flex-direction: column; gap: 8px;
    }
    .user-option {
      display: flex; align-items: center; gap: 12px;
      padding: 12px; border-radius: 8px;
      border: 1px solid #1f2a3a; cursor: pointer;
      transition: all 0.2s;
    }
    .user-option:hover {
      background: #15202e; border-color: #2563eb;
    }
    .user-option input[type="radio"] {
      cursor: pointer;
    }
    .user-label {
      display: flex; flex-direction: column; gap: 4px;
      color: #e5eef7;
    }
    .user-label strong {
      font-size: 14px;
    }
    .user-email {
      font-size: 12px; color: #64748b;
    }
    .modal-footer {
      display: flex; justify-content: flex-end;
      gap: 12px; padding: 20px;
      border-top: 1px solid #1f2a3a;
    }
    .btn-cancel, .btn-save {
      padding: 10px 20px; border-radius: 8px;
      border: 1px solid #374151; cursor: pointer;
      font-size: 14px; transition: all 0.2s;
    }
    .btn-cancel {
      background: #1f2937; color: #e5eef7;
    }
    .btn-cancel:hover {
      background: #374151;
    }
    .btn-save {
      background: #2563eb; color: #fff; border-color: #2563eb;
    }
    .btn-save:hover:not(:disabled) {
      background: #1d4ed8;
    }
    .btn-save:disabled {
      opacity: 0.5; cursor: not-allowed;
    }
    
    /* Form styles */
    .form-group {
      margin-bottom: 16px;
    }
    .form-group label {
      display: block; color: #e5eef7; margin-bottom: 8px;
      font-size: 14px; font-weight: 500;
    }
    .form-input {
      width: 100%; padding: 10px 12px;
      border-radius: 8px; border: 1px solid #1f2a3a;
      background: #0b1520; color: #e5eef7;
      font-size: 14px; transition: all 0.2s;
      box-sizing: border-box;
    }
    .form-input:focus {
      outline: none; border-color: #2563eb;
      background: #0f1b2a;
    }
    .form-input::placeholder {
      color: #64748b;
    }
    .form-input select {
      appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2394a3b8' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
      background-repeat: no-repeat; background-position: right 12px center;
      padding-right: 36px;
    }
    .form-row {
      display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
    }
    .required {
      color: #fca5a5; margin-left: 4px;
    }
    .optional {
      color: #64748b; margin-left: 4px; font-weight: normal;
    }
    .form-hint {
      display: block; color: #64748b; font-size: 11px;
      margin-top: 4px; font-style: italic;
    }
    .modal-content-large {
      max-width: 600px; max-height: 90vh;
    }
    
    /* Users management styles */
    .users-management-list {
      display: flex; flex-direction: column; gap: 8px;
    }
    .user-management-item {
      display: flex; justify-content: space-between;
      align-items: center; padding: 12px;
      border-radius: 8px; border: 1px solid #1f2a3a;
      background: #0b1520; transition: all 0.2s;
    }
    .user-management-item:hover {
      background: #0f1b2a; border-color: #2563eb;
    }
    .user-info {
      display: flex; align-items: center; gap: 12px;
      flex: 1;
    }
    .user-name-email {
      display: flex; flex-direction: column; gap: 4px;
    }
    .user-name-email strong {
      color: #e5eef7; font-size: 14px;
    }
    .user-email-small {
      color: #64748b; font-size: 12px;
    }
    .user-role-badge {
      padding: 4px 10px; border-radius: 12px;
      font-size: 11px; font-weight: 600;
      text-transform: uppercase;
    }
    .user-role-badge.role-admin {
      background: #2563eb; color: #fff;
    }
    .user-role-badge.role-user {
      background: #374151; color: #94a3b8;
    }
    .user-actions {
      display: flex; gap: 8px;
    }
    .edit-user-btn, .delete-user-btn {
      padding: 6px 12px; border-radius: 6px;
      border: 1px solid #374151; background: #1f2937;
      color: #94a3b8; cursor: pointer; font-size: 12px;
      transition: all 0.2s;
    }
    .edit-user-btn:hover:not(:disabled) {
      background: #2563eb; border-color: #2563eb;
      color: #fff;
    }
    .delete-user-btn:hover:not(:disabled) {
      background: #374151; border-color: #4b5563;
      color: #e5eef7;
    }
    .edit-user-btn:disabled, .delete-user-btn:disabled {
      opacity: 0.5; cursor: not-allowed;
    }
    .empty-users {
      padding: 24px; text-align: center; color: #94a3b8;
    }
    `,
  ],
})
export class HomeComponent implements OnInit {
  devices = signal<Device[]>([]);
  loading = signal(true);
  error = signal<string>('');
  userEmail = signal<string>('');
  isAdmin = signal(false);
  userNameMap = signal<Record<string, string>>({});
  
  // Modal state
  showModal = signal(false);
  selectedDevice = signal<Device | null>(null);
  selectedUserId = signal<string>('');
  availableUsers = signal<User[]>([]);
  loadingUsers = signal(false);
  usersError = signal<string>('');
  saving = signal(false);
  
  // Create device modal state
  showCreateModal = signal(false);
  newDeviceName = '';
  newDeviceType = '';
  creating = signal(false);
  deleting = signal<string>(''); // ID of device being deleted
  
  // Create user modal state
  showCreateUserModal = signal(false);
  newUserFirstName = '';
  newUserLastName = '';
  newUserEmail = '';
  newUserPassword = '';
  newUserRole = 'USER';
  newUserPhone = '';
  newUserAddress = '';
  newUserCity = '';
  newUserCountry = '';
  newUserDepartment = '';
  newUserJobTitle = '';
  creatingUser = signal(false);
  
  // Manage devices modal state
  showManageDevicesModal = signal(false);
  manageDevicesList = signal<Device[]>([]);
  loadingDevices = signal(false);
  devicesError = signal<string>('');
  deletingDeviceId = signal<string>(''); // ID of device being deleted
  
  // Edit device modal state
  showEditDeviceModal = signal(false);
  editingDeviceId = signal<string>(''); // ID of device being edited
  savingDevice = signal(false); // Flag to track if saving is in progress
  editDeviceName = '';
  editDeviceType = '';
  
  // Manage users modal state
  showManageUsersModal = signal(false);
  manageUsersList = signal<User[]>([]);
  deletingUserId = signal<string>(''); // ID of user being deleted
  
  // Edit user modal state
  showEditUserModal = signal(false);
  editingUserId = signal<string>(''); // ID of user being edited
  savingUser = signal(false); // Flag to track if saving is in progress
  editUserFirstName = '';
  editUserLastName = '';
  editUserEmail = '';
  editUserPassword = '';
  editUserRole = 'USER';
  editUserPhone = '';
  editUserAddress = '';
  editUserCity = '';
  editUserCountry = '';
  editUserDepartment = '';
  editUserJobTitle = '';

  constructor(
    private auth: AuthService,
    private deviceService: DeviceService,
    private userService: UserService,
    private router: Router,
    private http: HttpClient
  ) {}

  async ngOnInit(): Promise<void> {
    const email = this.auth.getUserEmail();
    const role = this.auth.getUserRole();
    
    this.userEmail.set(email || 'User');
    this.isAdmin.set(role?.toUpperCase() === 'ADMIN');

    // For ADMIN, show all devices; for USER, show only their devices
    try {
      let devs: Device[];
      if (this.isAdmin()) {
        devs = await this.deviceService.getAllDevices();
      } else {
        if (!email) {
          this.error.set('User email not found. Please login again.');
          this.loading.set(false);
          return;
        }
        
        // Find user in user-service by email to get the correct userId
        // (device-service uses userId from user-service, not auth-service)
        const user = await this.userService.getUserByEmail(email);
        if (!user || !user.id) {
          this.error.set('User not found in user-service. Please contact administrator.');
          this.loading.set(false);
          return;
        }
        
        devs = await this.deviceService.getDevicesByUser(user.id);
      }
      
      this.devices.set(devs);
      // Load user names before marking as done loading
      await this.loadUserNames(devs);
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to load devices');
    } finally {
      this.loading.set(false);
    }
  }

  async loadUserNames(devices: Device[]): Promise<void> {
    const nameMap: Record<string, string> = {};
    const userIds = [...new Set(devices.map(d => d.userId).filter(id => id !== null))] as string[];
    
    if (userIds.length === 0) {
      this.userNameMap.set({});
      return;
    }
    
    const results = await Promise.allSettled(
      userIds.map(async (userId) => {
        try {
          const user = await this.userService.getUserById(userId);
          // UserLocal has fullName, User from user-service has firstName + lastName
          const fullName = user.fullName || (user.firstName && user.lastName ? `${user.firstName} ${user.lastName}`.trim() : null);
          return { userId, name: fullName || user.email || 'Unknown User' };
        } catch (e) {
          // If user fetch fails, set a fallback name
          console.warn(`Failed to load user ${userId}:`, e);
          return { userId, name: `User ${userId.substring(0, 8)}...` };
        }
      })
    );
    
    // Process all results (both fulfilled and rejected)
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') {
        nameMap[result.value.userId] = result.value.name;
      } else {
        // If promise was rejected, set fallback for that userId
        const userId = userIds[index];
        nameMap[userId] = `User ${userId.substring(0, 8)}...`;
      }
    });
    
    // Update signal with new object to trigger Angular change detection
    this.userNameMap.set({ ...nameMap });
  }

  getUserName(userId: string | null): string | null {
    if (!userId) return null;
    const name = this.userNameMap()[userId];
    if (name) return name;
    // Return null if not loaded yet (will show Loading... in template)
    return null;
  }

  onLogout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  async onDeviceClick(device: Device): Promise<void> {
    if (!this.isAdmin()) return;
    
    this.selectedDevice.set(device);
    this.selectedUserId.set(device.userId || '');
    this.showModal.set(true);
    this.loadingUsers.set(true);
    this.usersError.set('');
    
    try {
      const users = await this.userService.getAllUsers();
      this.availableUsers.set(users);
    } catch (e: any) {
      this.usersError.set(e?.message || 'Failed to load users');
      console.error('Failed to load users:', e);
    } finally {
      this.loadingUsers.set(false);
    }
  }

  closeModal(): void {
    this.showModal.set(false);
    this.selectedDevice.set(null);
    this.selectedUserId.set('');
    this.availableUsers.set([]);
    this.usersError.set('');
  }

  async saveAssignment(): Promise<void> {
    const device = this.selectedDevice();
    if (!device) return;

    const newUserId = this.selectedUserId();
    const currentUserId = device.userId;

    // If no change, just close
    if (newUserId === (currentUserId || '')) {
      this.closeModal();
      return;
    }

    this.saving.set(true);
    try {
      let updatedDevice: Device;
      
      if (!newUserId || newUserId === '') {
        // Unassign user
        updatedDevice = await this.deviceService.unassignUserFromDevice(device.id);
      } else {
        // Assign user
        updatedDevice = await this.deviceService.assignUserToDevice(device.id, newUserId);
      }

      // Update device in the list
      const devices = this.devices();
      const index = devices.findIndex(d => d.id === device.id);
      if (index !== -1) {
        devices[index] = updatedDevice;
        this.devices.set([...devices]);
      }

      // Reload user names if needed
      await this.loadUserNames(this.devices());

      this.closeModal();
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to save assignment');
      console.error('Failed to save assignment:', e);
      // Keep modal open so user can retry
    } finally {
      this.saving.set(false);
    }
  }

  getUserDisplayName(user: User): string {
    if (user.fullName) return user.fullName;
    if (user.firstName && user.lastName) {
      return `${user.firstName} ${user.lastName}`.trim();
    }
    return user.email;
  }

  getUserRole(user: User): string {
    // Try to get role from user if available, otherwise default to USER
    return (user as any).role || 'USER';
  }

  onManageDevices(): void {
    if (!this.isAdmin()) return;
    this.showManageDevicesModal.set(true);
    this.loadManageDevices();
  }

  closeManageDevicesModal(): void {
    this.showManageDevicesModal.set(false);
    this.devicesError.set('');
  }

  async loadManageDevices(): Promise<void> {
    this.loadingDevices.set(true);
    this.devicesError.set('');
    try {
      const devices = await this.deviceService.getAllDevices();
      this.manageDevicesList.set(devices);
    } catch (e: any) {
      this.devicesError.set(e?.message || 'Failed to load devices');
      console.error('Failed to load devices:', e);
    } finally {
      this.loadingDevices.set(false);
    }
  }

  onEditDevice(device: Device): void {
    if (!this.isAdmin() || !device.id) return;
    
    // Close manage devices modal first
    this.closeManageDevicesModal();
    
    // Set editing device data
    this.editingDeviceId.set(device.id);
    this.editDeviceName = device.name || '';
    this.editDeviceType = device.type || '';
    this.showEditDeviceModal.set(true);
  }

  closeEditDeviceModal(): void {
    this.showEditDeviceModal.set(false);
    this.editingDeviceId.set('');
    this.editDeviceName = '';
    this.editDeviceType = '';
  }

  canSaveDeviceEdit(): boolean {
    return this.editDeviceName.trim().length > 0 &&
           this.editDeviceType.trim().length > 0;
  }

  async saveDeviceEdit(): Promise<void> {
    const deviceId = this.editingDeviceId();
    if (!deviceId || !this.canSaveDeviceEdit() || this.savingDevice()) return;

    this.savingDevice.set(true);
    try {
      // Get current device to preserve status and userId
      const currentDevice = this.manageDevicesList().find(d => d.id === deviceId);
      if (!currentDevice) {
        throw new Error('Device not found');
      }

      // Update device (preserve status and userId)
      const updatedDevice = await this.deviceService.updateDevice(
        deviceId,
        this.editDeviceName.trim(),
        this.editDeviceType.trim(),
        currentDevice.status,
        currentDevice.userId
      );

      // Update device in list
      const devices = this.manageDevicesList();
      const index = devices.findIndex(d => d.id === deviceId);
      if (index !== -1) {
        devices[index] = updatedDevice;
        this.manageDevicesList.set([...devices]);
      }

      // Update device in main devices list if it exists there
      const mainDevices = this.devices();
      const mainIndex = mainDevices.findIndex(d => d.id === deviceId);
      if (mainIndex !== -1) {
        mainDevices[mainIndex] = updatedDevice;
        this.devices.set([...mainDevices]);
      }

      // Close modal
      this.closeEditDeviceModal();
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to update device');
      console.error('Failed to update device:', e);
      // Keep modal open so user can retry
    } finally {
      this.savingDevice.set(false);
    }
  }

  async onDeleteDeviceFromList(device: Device): Promise<void> {
    if (!this.isAdmin() || !device.id) return;
    
    const deviceName = device.name || 'this device';
    if (!confirm(`Are you sure you want to delete "${deviceName}"? This action cannot be undone.`)) {
      return;
    }

    this.deletingDeviceId.set(device.id);
    try {
      await this.deviceService.deleteDevice(device.id);
      
      // Remove from manage devices list
      const devices = this.manageDevicesList();
      this.manageDevicesList.set(devices.filter(d => d.id !== device.id));
      
      // Remove from main devices list if it exists there
      const mainDevices = this.devices();
      this.devices.set(mainDevices.filter(d => d.id !== device.id));
      
      // If this device was selected, clear selection
      if (this.selectedDevice()?.id === device.id) {
        this.selectedDevice.set(null);
      }
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to delete device');
      console.error('Failed to delete device:', e);
    } finally {
      this.deletingDeviceId.set('');
    }
  }

  onManageUsers(): void {
    if (!this.isAdmin()) return;
    this.showManageUsersModal.set(true);
    this.loadManageUsers();
  }

  closeManageUsersModal(): void {
    this.showManageUsersModal.set(false);
    this.manageUsersList.set([]);
    this.usersError.set('');
  }

  async loadManageUsers(): Promise<void> {
    this.loadingUsers.set(true);
    this.usersError.set('');
    try {
      const users = await this.userService.getAllUsers();
      
      // Try to get roles from auth-service for better role display
      try {
        const authUsers = await firstValueFrom(
          this.http.get<Array<{ id: string; email: string; role?: string }>>(
            `/api/auth/users`
          )
        );
        
        // Merge roles into users
        const usersWithRoles = users.map(user => {
          const authUser = authUsers.find(au => au.id === user.id || au.email.toLowerCase() === user.email.toLowerCase());
          return {
            ...user,
            role: authUser?.role || 'USER'
          };
        });
        
        this.manageUsersList.set(usersWithRoles as any);
      } catch (e: any) {
        // If auth-service fails, just use users from user-service
        console.warn('Failed to load roles from auth-service:', e?.message);
        this.manageUsersList.set(users);
      }
    } catch (e: any) {
      this.usersError.set(e?.message || 'Failed to load users');
      console.error('Failed to load users:', e);
    } finally {
      this.loadingUsers.set(false);
    }
  }

  onEditUser(user: User): void {
    if (!this.isAdmin() || !user.id) return;
    
    // Close manage users modal first
    this.closeManageUsersModal();
    
    // Set editing user data
    this.editingUserId.set(user.id);
    this.editUserFirstName = user.firstName || '';
    this.editUserLastName = user.lastName || '';
    this.editUserEmail = user.email || '';
    this.editUserPassword = ''; // Leave empty by default
    this.editUserRole = this.getUserRole(user) || 'USER';
    this.editUserPhone = user.phoneNumber || '';
    this.editUserAddress = user.address || '';
    this.editUserCity = user.city || '';
    this.editUserCountry = user.country || '';
    this.editUserDepartment = user.department || '';
    this.editUserJobTitle = user.jobTitle || '';
    this.showEditUserModal.set(true);
  }

  closeEditUserModal(): void {
    this.showEditUserModal.set(false);
    this.editingUserId.set('');
    this.editUserFirstName = '';
    this.editUserLastName = '';
    this.editUserEmail = '';
    this.editUserPassword = '';
    this.editUserRole = 'USER';
    this.editUserPhone = '';
    this.editUserAddress = '';
    this.editUserCity = '';
    this.editUserCountry = '';
    this.editUserDepartment = '';
    this.editUserJobTitle = '';
  }

  canSaveUserEdit(): boolean {
    return this.editUserFirstName.trim().length > 0 &&
           this.editUserLastName.trim().length > 0 &&
           this.editUserEmail.trim().length > 0 &&
           this.editUserRole.length > 0;
  }

  async saveUserEdit(): Promise<void> {
    const userId = this.editingUserId();
    if (!userId || !this.canSaveUserEdit() || this.savingUser()) return;

    this.savingUser.set(true);
    try {
      // Update auth-service (email, password, role)
      // Only update if password is provided (backend requires password)
      const newPassword = this.editUserPassword.trim();
      if (newPassword) {
        try {
          await this.userService.updateAuthUser(
            userId,
            this.editUserEmail.trim(),
            newPassword,
            this.editUserRole,
            true // active
          );
        } catch (e: any) {
          console.warn('Failed to update auth-service (might not exist):', e?.message);
          // Continue to update user-service even if auth-service fails
        }
      } else {
        // If password is empty, we skip auth-service update
        // (Backend requires password for update)
        // User-service will be updated regardless
        console.log('Password not provided - skipping auth-service update. Only user-service will be updated.');
      }

      // Update user-service (all user details)
      const updatedUser = await this.userService.updateUser(
        userId,
        this.editUserFirstName.trim(),
        this.editUserLastName.trim(),
        this.editUserEmail.trim(),
        this.editUserPhone.trim() || undefined,
        this.editUserAddress.trim() || undefined,
        this.editUserCity.trim() || undefined,
        this.editUserCountry.trim() || undefined,
        this.editUserDepartment.trim() || undefined,
        this.editUserJobTitle.trim() || undefined,
        true // active
      );

      // Update user in list
      const users = this.manageUsersList();
      const index = users.findIndex(u => u.id === userId);
      if (index !== -1) {
        const updatedUserWithRole: User = { ...updatedUser, role: this.editUserRole };
        users[index] = updatedUserWithRole;
        this.manageUsersList.set([...users]);
      }

      // Update userNameMap with the new user name
      const updatedName = updatedUser.fullName || 
        (updatedUser.firstName && updatedUser.lastName 
          ? `${updatedUser.firstName} ${updatedUser.lastName}`.trim() 
          : updatedUser.email || 'Unknown User');
      const currentNameMap = this.userNameMap();
      if (currentNameMap[userId]) {
        // Update the entry for this user in userNameMap
        this.userNameMap.set({ ...currentNameMap, [userId]: updatedName });
      }

      // Close modal
      this.closeEditUserModal();
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to update user');
      console.error('Failed to update user:', e);
      // Keep modal open so user can retry
    } finally {
      this.savingUser.set(false);
    }
  }

  async onDeleteUser(user: User): Promise<void> {
    if (!this.isAdmin() || !user.id) return;
    
    const userName = this.getUserDisplayName(user);
    if (!confirm(`Are you sure you want to delete user "${userName}" (${user.email})? This action cannot be undone and will also delete their authentication account.`)) {
      return;
    }
    
    this.deletingUserId.set(user.id);
    try {
      // Delete from both services
      // First delete from auth-service (to revoke authentication)
      try {
        await this.userService.deleteAuthUser(user.id);
      } catch (e: any) {
        console.warn('Failed to delete from auth-service (might not exist):', e?.message);
        // Continue even if auth-service deletion fails
      }
      
      // Then delete from user-service
      await this.userService.deleteUser(user.id);
      
      // Remove user from list
      const users = this.manageUsersList();
      const filtered = users.filter(u => u.id !== user.id);
      this.manageUsersList.set(filtered);
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to delete user');
      console.error('Failed to delete user:', e);
    } finally {
      this.deletingUserId.set('');
    }
  }

  onCreateDevice(): void {
    if (!this.isAdmin()) return;
    this.newDeviceName = '';
    this.newDeviceType = '';
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.newDeviceName = '';
    this.newDeviceType = '';
  }

  canCreateDevice(): boolean {
    return this.newDeviceName.trim().length > 0 && this.newDeviceType.trim().length > 0;
  }

  async saveNewDevice(): Promise<void> {
    if (!this.canCreateDevice() || this.creating()) return;

    this.creating.set(true);
    try {
      const newDevice = await this.deviceService.createDevice(
        this.newDeviceName.trim(),
        this.newDeviceType.trim()
      );

      // Add to devices list
      const devices = this.devices();
      devices.push(newDevice);
      this.devices.set([...devices]);

      // Load user names for all devices
      await this.loadUserNames(this.devices());

      // Close modal and reset form
      this.closeCreateModal();
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to create device');
      console.error('Failed to create device:', e);
      // Keep modal open so user can retry
    } finally {
      this.creating.set(false);
    }
  }

  onCreateUser(): void {
    if (!this.isAdmin()) return;
    // Reset form
    this.newUserFirstName = '';
    this.newUserLastName = '';
    this.newUserEmail = '';
    this.newUserPassword = '';
    this.newUserRole = 'USER';
    this.newUserPhone = '';
    this.newUserAddress = '';
    this.newUserCity = '';
    this.newUserCountry = '';
    this.newUserDepartment = '';
    this.newUserJobTitle = '';
    this.showCreateUserModal.set(true);
  }

  closeCreateUserModal(): void {
    this.showCreateUserModal.set(false);
    this.newUserFirstName = '';
    this.newUserLastName = '';
    this.newUserEmail = '';
    this.newUserPassword = '';
    this.newUserRole = 'USER';
    this.newUserPhone = '';
    this.newUserAddress = '';
    this.newUserCity = '';
    this.newUserCountry = '';
    this.newUserDepartment = '';
    this.newUserJobTitle = '';
  }

  canCreateUser(): boolean {
    return this.newUserFirstName.trim().length > 0 &&
           this.newUserLastName.trim().length > 0 &&
           this.newUserEmail.trim().length > 0 &&
           this.newUserPassword.trim().length > 0 &&
           this.newUserRole.length > 0;
  }

  async onDeleteDevice(event: Event, device: Device): Promise<void> {
    event.stopPropagation(); // Prevent modal from opening when clicking delete
    
    if (!this.isAdmin()) return;
    
    if (!confirm(`Are you sure you want to delete "${device.name}"? This action cannot be undone.`)) {
      return;
    }
    
    this.deleting.set(device.id);
    try {
      await this.deviceService.deleteDevice(device.id);
      
      // Remove device from list
      const devices = this.devices();
      const filtered = devices.filter(d => d.id !== device.id);
      this.devices.set(filtered);
      
      // Reload user names if needed
      await this.loadUserNames(this.devices());
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to delete device');
      console.error('Failed to delete device:', e);
    } finally {
      this.deleting.set('');
    }
  }

  async saveNewUser(): Promise<void> {
    if (!this.canCreateUser() || this.creatingUser()) return;

    this.creatingUser.set(true);
    try {
      // First create user in auth-service (for authentication)
      const authUser = await this.userService.createAuthUser(
        this.newUserEmail.trim(),
        this.newUserPassword.trim(),
        this.newUserRole
      );

      // Then create user in user-service (for detailed information)
      await this.userService.createUser(
        this.newUserFirstName.trim(),
        this.newUserLastName.trim(),
        this.newUserEmail.trim(),
        this.newUserPhone.trim() || undefined,
        this.newUserAddress.trim() || undefined,
        this.newUserCity.trim() || undefined,
        this.newUserCountry.trim() || undefined,
        this.newUserDepartment.trim() || undefined,
        this.newUserJobTitle.trim() || undefined
      );

      // Close modal and reset form
      this.closeCreateUserModal();
      
      // Show success message (optional)
      console.log('User created successfully:', authUser.email);
    } catch (e: any) {
      this.error.set(e?.message || 'Failed to create user');
      console.error('Failed to create user:', e);
      // Keep modal open so user can retry
    } finally {
      this.creatingUser.set(false);
    }
  }
}