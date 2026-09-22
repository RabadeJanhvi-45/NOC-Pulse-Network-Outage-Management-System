import { Component, inject, signal } from '@angular/core';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeviceService } from '../../../core/services/device.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DeviceChangeRequest } from '../../../shared/models';

@Component({
  selector: 'app-device-requests',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LowerCasePipe],
  templateUrl: './device-requests.component.html',
  styleUrl: './device-requests.component.scss',
})
export class DeviceRequestsComponent {
  private readonly deviceService = inject(DeviceService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<DeviceChangeRequest[]>([]);
  readonly loading = signal(false);
  readonly processingId = signal<number | null>(null);

  readonly rejectingRequest = signal<DeviceChangeRequest | null>(null);
  readonly rejectForm = this.fb.nonNullable.group({
    reason: ['', [Validators.required, Validators.minLength(3)]],
  });

  constructor() {
    this.fetchRequests();
  }

  fetchRequests(): void {
    this.loading.set(true);
    this.deviceService.listDeviceRequests().subscribe({
      next: (list) => {
        this.requests.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  approve(req: DeviceChangeRequest): void {
    if (!req.id) return;
    this.processingId.set(req.id);
    this.deviceService.approveDeviceRequest(req.id).subscribe({
      next: () => {
        this.notifications.success(`Approved device request for ${req.deviceId}.`);
        this.processingId.set(null);
        this.fetchRequests();
      },
      error: () => this.processingId.set(null),
    });
  }

  openRejectModal(req: DeviceChangeRequest): void {
    this.rejectingRequest.set(req);
    this.rejectForm.reset();
  }

  closeRejectModal(): void {
    this.rejectingRequest.set(null);
    this.rejectForm.reset();
  }

  submitReject(): void {
    const req = this.rejectingRequest();
    if (!req || !req.id || this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }

    const { reason } = this.rejectForm.getRawValue();
    this.processingId.set(req.id);

    this.deviceService.rejectDeviceRequest(req.id, reason).subscribe({
      next: () => {
        this.notifications.success(`Rejected device request for ${req.deviceId}.`);
        this.processingId.set(null);
        this.closeRejectModal();
        this.fetchRequests();
      },
      error: () => this.processingId.set(null),
    });
  }
}
