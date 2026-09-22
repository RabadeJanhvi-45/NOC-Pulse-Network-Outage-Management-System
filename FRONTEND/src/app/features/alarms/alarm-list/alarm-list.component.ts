import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AlarmService } from '../../../core/services/alarm.service';
import { DeviceService } from '../../../core/services/device.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AlarmResponse, AlarmSeverity, DeviceResponse, AlarmFilterParams } from '../../../shared/models';

@Component({
  selector: 'app-alarm-list',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LowerCasePipe],
  templateUrl: './alarm-list.component.html',
  styleUrl: './alarm-list.component.scss',
})
export class AlarmListComponent {
  private readonly alarmService = inject(AlarmService);
  private readonly deviceService = inject(DeviceService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);

  readonly alarms = signal<AlarmResponse[]>([]);
  readonly devices = signal<DeviceResponse[]>([]);
  readonly loading = signal(false);
  readonly isSubmitting = signal(false);
  readonly acknowledgingId = signal<number | null>(null);
  readonly clearingId = signal<number | null>(null);

  readonly showForm = signal(false);

  readonly severityOptions: AlarmSeverity[] = ['Critical', 'Major', 'Minor', 'Warning'];

  readonly alarmTypeOptions: string[] = [
    'INTERFACE_DOWN',
    'LINK_FLAP',
    'BGP_SESSION_DOWN',
    'OSPF_NEIGHBOR_DOWN',
    'HIGH_CPU_UTILIZATION',
    'HIGH_MEMORY_USAGE',
    'PACKET_LOSS_HIGH',
    'LATENCY_DEGRADATION',
    'POWER_SUPPLY_FAILURE',
    'FAN_FAILURE',
    'TEMPERATURE_CRITICAL',
    'PORT_SECURITY_VIOLATION',
    'OPTICAL_SIGNAL_DEGRADATION',
    'CONFIGURATION_MISMATCH',
    'AUTHENTICATION_FAILURE',
    'CIRCUIT_FAILOVER',
    'BANDWIDTH_SATURATION',
    'HARDWARE_FAULT',
    'SYSTEM_RELOAD_UNEXPECTED',
  ];

  readonly filterForm = this.fb.nonNullable.group({
    severity: [''],
    status: [''],
    deviceId: [''],
  });

  readonly form = this.fb.nonNullable.group({
    deviceId: ['', Validators.required],
    alarmType: ['', Validators.required],
    severity: ['' as AlarmSeverity | ''],
  });

  constructor() {
    this.fetchAlarms();
    this.fetchDevices();
  }

  canWrite(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'NOC_OPERATOR');
  }

  canAcknowledgeOrClear(): boolean {
    return this.auth.hasAnyRole('ADMIN');
  }

  fetchAlarms(): void {
    this.loading.set(true);
    const raw = this.filterForm.getRawValue();
    const filters: AlarmFilterParams = {};
    if (raw.severity) filters.severity = raw.severity;
    if (raw.status) filters.status = raw.status;
    if (raw.deviceId) filters.deviceId = raw.deviceId;

    this.alarmService.list(filters).subscribe({
      next: (alarms) => {
        console.log('[Alarm Management] Fetched alarms for user:', {
          currentUser: this.auth.currentUser()?.username,
          role: this.auth.role(),
          alarmCount: alarms.length,
          alarms: alarms.map(a => ({
            id: a.id,
            deviceId: a.deviceId,
            alarmType: a.alarmType,
            severity: a.severity,
            status: a.status,
            raisedBy: a.raisedBy,
            raisedAt: a.raisedAt
          }))
        });
        this.alarms.set(alarms);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[Alarm Management] Error fetching alarms:', err);
        this.loading.set(false);
      },
    });
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.fetchAlarms();
  }

  fetchDevices(): void {
    this.deviceService.list().subscribe({
      next: (devices) => this.devices.set(devices),
    });
  }

  openForm(): void {
    this.form.reset({ severity: '' });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const currentUsername = this.auth.currentUser()?.username ?? 'operator1';

    const payload = {
      deviceId: raw.deviceId,
      alarmType: raw.alarmType,
      ...(raw.severity ? { severity: raw.severity as AlarmSeverity } : {}),
      raisedBy: currentUsername,
    };

    console.log('[Alarm Management] Raising alarm with payload:', payload);

    this.alarmService.raise(payload).subscribe({
      next: (alarm) => {
        console.log('[Alarm Management] Raise Alarm Response:', alarm);
        this.notifications.success(
          alarm.deduplicated
            ? 'An active alarm already exists for this device/type. Deduplicated.'
            : 'Alarm raised. Incident generated and Engineer assigned automatically.',
        );
        this.isSubmitting.set(false);
        this.showForm.set(false);
        this.fetchAlarms();
      },
      error: (err) => {
        console.error('[Alarm Management] Error raising alarm:', err);
        this.isSubmitting.set(false);
      },
    });
  }

  acknowledge(alarm: AlarmResponse): void {
    this.acknowledgingId.set(alarm.id);
    this.alarmService.acknowledge(alarm.id).subscribe({
      next: () => {
        this.notifications.success('Alarm acknowledged.');
        this.acknowledgingId.set(null);
        this.fetchAlarms();
      },
      error: () => this.acknowledgingId.set(null),
    });
  }

  clearAlarm(alarm: AlarmResponse): void {
    this.clearingId.set(alarm.id);
    this.alarmService.clear(alarm.id).subscribe({
      next: () => {
        this.notifications.success('Alarm cleared and removed from active list.');
        this.clearingId.set(null);
        this.fetchAlarms();
      },
      error: () => this.clearingId.set(null),
    });
  }
}