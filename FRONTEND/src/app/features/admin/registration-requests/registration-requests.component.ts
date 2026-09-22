import { Component, inject, signal, computed } from '@angular/core';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RegistrationRequestResponse } from '../../../shared/models';

@Component({
  selector: 'app-registration-requests',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LowerCasePipe],
  templateUrl: './registration-requests.component.html',
  styleUrl: './registration-requests.component.scss',
})
export class RegistrationRequestsComponent {
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly requests = signal<RegistrationRequestResponse[]>([]);
  readonly loading = signal(false);
  readonly processingId = signal<number | null>(null);

  readonly totalCount = computed(() => this.requests().length);
  readonly pendingCount = computed(() => this.requests().filter((r) => r.status === 'PENDING').length);
  readonly approvedCount = computed(() => this.requests().filter((r) => r.status === 'APPROVED').length);
  readonly rejectedCount = computed(() => this.requests().filter((r) => r.status === 'REJECTED').length);

  readonly rejectingRequest = signal<RegistrationRequestResponse | null>(null);
  readonly rejectForm = this.fb.nonNullable.group({
    reason: ['', [Validators.required, Validators.minLength(3)]],
  });

  constructor() {
    this.fetchRequests();
  }

  fetchRequests(): void {
    this.loading.set(true);
    this.auth.getRegistrationRequests().subscribe({
      next: (list) => {
        this.requests.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  approve(req: RegistrationRequestResponse): void {
    this.processingId.set(req.id);
    this.auth.approveRegistrationRequest(req.id).subscribe({
      next: () => {
        this.notifications.success(`Approved registration for ${req.username}.`);
        this.processingId.set(null);
        this.fetchRequests();
      },
      error: () => this.processingId.set(null),
    });
  }

  openRejectModal(req: RegistrationRequestResponse): void {
    this.rejectingRequest.set(req);
    this.rejectForm.reset();
  }

  closeRejectModal(): void {
    this.rejectingRequest.set(null);
    this.rejectForm.reset();
  }

  submitReject(): void {
    const req = this.rejectingRequest();
    if (!req || this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }

    const { reason } = this.rejectForm.getRawValue();
    this.processingId.set(req.id);

    this.auth.rejectRegistrationRequest(req.id, reason).subscribe({
      next: () => {
        this.notifications.success(`Rejected registration request for ${req.username}.`);
        this.processingId.set(null);
        this.closeRejectModal();
        this.fetchRequests();
      },
      error: () => this.processingId.set(null),
    });
  }
}
