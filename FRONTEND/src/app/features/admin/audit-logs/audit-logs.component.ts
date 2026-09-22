import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AuditLogService } from '../../../core/services/audit-log.service';
import { AuditLogResponse } from '../../../shared/models';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [DatePipe, LowerCasePipe, ReactiveFormsModule],
  templateUrl: './audit-logs.component.html',
  styleUrl: './audit-logs.component.scss',
})
export class AuditLogsComponent {
  private readonly auditLogService = inject(AuditLogService);
  private readonly fb = inject(FormBuilder);

  readonly logs = signal<AuditLogResponse[]>([]);
  readonly loading = signal(false);

  readonly standardActions: string[] = [
    'LOGIN',
    'LOGIN_FAILURE',
    'LOGOUT',
    'USER_CREATE',
    'USER_UPDATE',
    'ROLE_CREATE',
    'ROLE_UPDATE',
    'DEVICE_REGISTER',
    'DEVICE_UPDATE',
    'DEVICE_DELETE',
    'ALARM_RAISED',
    'ALARM_ACKNOWLEDGED',
    'ALARM_CLEARED',
    'INCIDENT_CREATED',
    'INCIDENT_ASSIGNED',
    'INCIDENT_START_WORK',
    'INCIDENT_RESOLVED',
    'INCIDENT_VERIFIED_CLOSED',
    'INCIDENT_REASSIGNED',
  ];

  readonly filterForm = this.fb.nonNullable.group({
    search: [''],
    action: [''],
    username: [''],
  });

  // Client-side search & filtering for instant, typo-tolerant, multi-field searching
  readonly filteredLogs = computed(() => {
    const list = this.logs();
    const { search, action, username } = this.filterForm.getRawValue();

    const searchLower = search.trim().toLowerCase();
    const actionLower = action.trim().toLowerCase();
    const userLower = username.trim().toLowerCase();

    return list.filter((item) => {
      // Action match
      if (actionLower && (!item.action || !item.action.toLowerCase().includes(actionLower))) {
        return false;
      }

      // Username match
      if (userLower && (!item.username || !item.username.toLowerCase().includes(userLower))) {
        return false;
      }

      // Free-text search match across action, username, details, and ipAddress
      if (searchLower) {
        const matchesAction = item.action?.toLowerCase().includes(searchLower);
        const matchesUser = item.username?.toLowerCase().includes(searchLower);
        const matchesDetails = item.details?.toLowerCase().includes(searchLower);
        const matchesIp = item.ipAddress?.toLowerCase().includes(searchLower);

        if (!matchesAction && !matchesUser && !matchesDetails && !matchesIp) {
          return false;
        }
      }

      return true;
    });
  });

  constructor() {
    this.fetchLogs();
  }

  fetchLogs(): void {
    this.loading.set(true);
    this.auditLogService.list({}).subscribe({
      next: (logs) => {
        this.logs.set(logs || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  resetFilters(): void {
    this.filterForm.reset();
  }
}