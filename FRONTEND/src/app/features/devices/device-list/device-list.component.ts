import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { DeviceService } from '../../../core/services/device.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  DeviceRequest,
  DeviceResponse,
  DeviceHistoryResponse,
  DeviceStatus,
  DeviceHealth,
  DeviceChangeRequest,
  DeviceFilterParams,
} from '../../../shared/models';

@Component({
  selector: 'app-device-list',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LowerCasePipe],
  templateUrl: './device-list.component.html',
  styleUrl: './device-list.component.scss',
})
export class DeviceListComponent {
  private readonly deviceService = inject(DeviceService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);

  readonly devices = signal<DeviceResponse[]>([]);
  readonly loading = signal(false);
  readonly isSubmitting = signal(false);

  // Forms
  readonly showForm = signal(false);
  readonly editingDevice = signal<DeviceResponse | null>(null);
  readonly viewMode = signal<'grid' | 'table'>('grid');

  readonly historyFor = signal<DeviceResponse | null>(null);
  readonly history = signal<DeviceHistoryResponse[]>([]);
  readonly historyLoading = signal(false);

  readonly statusOptions: DeviceStatus[] = ['Active', 'Inactive', 'Faulty'];
  readonly healthOptions: DeviceHealth[] = ['Healthy', 'Degraded', 'Critical'];
  readonly deviceTypeOptions: string[] = [
    'Router',
    'Switch',
    'Firewall',
    'Load Balancer',
    'Gateway / SD-WAN',
    'Access Point',
    'Optical / DWDM',
    'Server / Appliance',
  ];

  // Analytics Computed Signals
  readonly healthStats = computed(() => {
    const list = this.devices();
    const total = list.length;
    if (total === 0) return { healthy: 0, degraded: 0, critical: 0, total: 0, healthyPct: 0, degradedPct: 0, criticalPct: 0 };
    const healthy = list.filter(d => d.health === 'Healthy').length;
    const degraded = list.filter(d => d.health === 'Degraded').length;
    const critical = list.filter(d => d.health === 'Critical').length;
    return {
      healthy,
      degraded,
      critical,
      total,
      healthyPct: Math.round((healthy / total) * 100),
      degradedPct: Math.round((degraded / total) * 100),
      criticalPct: Math.round((critical / total) * 100),
    };
  });

  readonly typeStats = computed(() => {
    const list = this.devices();
    const map = new Map<string, number>();
    for (const d of list) {
      const t = d.deviceType || 'Other';
      map.set(t, (map.get(t) || 0) + 1);
    }
    return Array.from(map.entries())
      .map(([type, count]) => ({
        type,
        count,
        percent: list.length > 0 ? Math.round((count / list.length) * 100) : 0,
      }))
      .sort((a, b) => b.count - a.count);
  });

  readonly activeRate = computed(() => {
    const list = this.devices();
    if (list.length === 0) return 100;
    const active = list.filter(d => d.status === 'Active').length;
    return Math.round((active / list.length) * 100);
  });

  getDeviceImage(type?: string): string {
    const t = (type || '').toLowerCase();
    // 1. Switch
    if (t.includes('switch')) {
      return 'assets/images/devices/switch.png';
    }
    // 2. Firewall
    if (t.includes('firewall') || t.includes('security')) {
      return 'assets/images/devices/firewall.png';
    }
    // 3. Load Balancer
    if (t.includes('balancer') || t.includes('load')) {
      return 'assets/images/devices/load-balancer.png';
    }
    // 4. Access Point / WiFi / Wireless
    if (t.includes('access') || t.includes('wifi') || t.includes('wireless') || t.includes('ap')) {
      return 'assets/images/devices/access-point.png';
    }
    // 5. Gateway / SD-WAN / Router
    if (t.includes('gateway') || t.includes('sd-wan') || t.includes('router')) {
      return 'assets/images/devices/gateway-sd-wan.png';
    }
    // 6. Optical DWDM / Optical Transport
    if (t.includes('optical') || t.includes('dwdm') || t.includes('fiber')) {
      return 'assets/images/devices/optical-dwdm.png';
    }
    // 7. Server / Compute / Default
    return 'assets/images/devices/server.png';
  }

  setViewMode(mode: 'grid' | 'table'): void {
    this.viewMode.set(mode);
  }

  // Filter Form
  readonly filterForm = this.fb.nonNullable.group({
    search: [''],
    region: [''],
    status: [''],
    deviceType: [''],
  });

  // Device Form (Create & Edit)
  readonly form = this.fb.nonNullable.group({
    deviceId: ['', Validators.required],
    deviceType: ['', Validators.required],
    name: ['', Validators.required],
    ipAddress: [''],
    location: [''],
    region: [''],
    status: ['Active' as DeviceStatus],
    health: ['Healthy' as DeviceHealth],
  });

  constructor() {
    this.fetchDevices();
  }

  fetchDevices(): void {
    this.loading.set(true);
    const raw = this.filterForm.getRawValue();
    const filters: DeviceFilterParams = {};
    if (raw.search) filters.search = raw.search;
    if (raw.region) filters.region = raw.region;
    if (raw.status) filters.status = raw.status;
    if (raw.deviceType) filters.deviceType = raw.deviceType;

    this.deviceService.list(filters).subscribe({
      next: (devices) => {
        console.log('[Device Inventory] Fetched devices:', {
          currentUser: this.auth.currentUser()?.username,
          role: this.auth.role(),
          deviceCount: devices.length,
          devices: devices.map(d => ({
            id: d.id,
            deviceId: d.deviceId,
            name: d.name,
            deviceType: d.deviceType,
            createdBy: d.createdBy,
            status: d.status,
            health: d.health,
            ipAddress: d.ipAddress,
            location: d.location
          }))
        });
        this.devices.set(devices);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[Device Inventory] Error fetching devices:', err);
        this.loading.set(false);
      },
    });
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.fetchDevices();
  }

  // --- Direct Add / Edit ---

  openAddForm(): void {
    this.editingDevice.set(null);
    this.form.reset({ status: 'Active', health: 'Healthy' });
    this.showForm.set(true);
  }

  openEditForm(device: DeviceResponse): void {
    this.editingDevice.set(device);
    this.form.setValue({
      deviceId: device.deviceId,
      deviceType: device.deviceType,
      name: device.name,
      ipAddress: device.ipAddress ?? '',
      location: device.location ?? '',
      region: device.region ?? '',
      status: device.status,
      health: device.health,
    });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingDevice.set(null);
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const editing = this.editingDevice();
    const currentUsername = this.auth.currentUser()?.username ?? 'operator1';

    const payload: DeviceRequest = {
      ...this.form.getRawValue(),
      createdBy: editing ? editing.createdBy : currentUsername,
    };

    console.log('[Device Inventory] Submitting device payload:', { editing: !!editing, payload });

    const request$ = editing
      ? this.deviceService.update(editing.id, payload)
      : this.deviceService.create(payload);

    request$.subscribe({
      next: (res) => {
        console.log('[Device Inventory] Device Save Response:', res);
        this.notifications.success(editing ? 'Device updated successfully.' : 'Device registered successfully.');
        this.isSubmitting.set(false);
        this.showForm.set(false);
        this.editingDevice.set(null);
        this.fetchDevices();
      },
      error: (err) => {
        console.error('[Device Inventory] Device Save Error:', err);
        this.isSubmitting.set(false);
      },
    });
  }

  confirmDelete(device: DeviceResponse): void {
    if (!confirm(`Delete device "${device.name}" (${device.deviceId})? This cannot be undone.`)) {
      return;
    }

    this.deviceService.delete(device.id).subscribe({
      next: () => {
        this.notifications.success('Device deleted.');
        this.fetchDevices();
      },
      error: () => {},
    });
  }

  viewHistory(device: DeviceResponse): void {
    this.historyFor.set(device);
    this.historyLoading.set(true);
    this.deviceService.getHistory(device.id).subscribe({
      next: (history) => {
        this.history.set(history);
        this.historyLoading.set(false);
      },
      error: () => this.historyLoading.set(false),
    });
  }

  closeHistory(): void {
    this.historyFor.set(null);
    this.history.set([]);
  }
}