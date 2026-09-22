import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { IncidentService } from '../../../core/services/incident.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  IncidentResponse,
  IncidentPriority,
  IncidentStatus,
  IncidentFilterParams,
} from '../../../shared/models';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, RouterLink, LowerCasePipe],
  templateUrl: './incident-list.component.html',
  styleUrl: './incident-list.component.scss',
})
export class IncidentListComponent {
  private readonly incidentService = inject(IncidentService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  readonly incidents = signal<IncidentResponse[]>([]);
  readonly loading = signal(false);

  readonly statusFilter = signal<IncidentStatus | 'All'>('All');
  readonly priorityFilter = signal<string>('');

  readonly statusOptions: (IncidentStatus | 'All')[] = [
    'All',
    'NEW',
    'ASSIGNED',
    'IN_PROGRESS',
    'RESOLVED',
    'CLOSED',
  ];

  readonly priorityOptions: string[] = ['All', 'P1', 'P2', 'P3', 'P4'];

  constructor() {
    this.fetchIncidents();
  }

  fetchIncidents(): void {
    this.loading.set(true);
    const filters: IncidentFilterParams = {};
    if (this.statusFilter() !== 'All') {
      filters.status = this.statusFilter();
    }
    if (this.priorityFilter() && this.priorityFilter() !== 'All') {
      filters.priority = this.priorityFilter();
    }

    this.incidentService.list(filters).subscribe({
      next: (incidents) => {
        console.log('[Incident Queue] Fetched incidents for user:', {
          currentUser: this.auth.currentUser()?.username,
          role: this.auth.role(),
          incidentCount: incidents.length,
          incidents: incidents.map(i => ({
            id: i.id,
            deviceId: i.deviceId,
            alarmId: i.alarmId,
            priority: i.priority,
            status: i.status,
            createdBy: i.createdBy,
            createdAt: i.createdAt
          }))
        });
        this.incidents.set(incidents);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[Incident Queue] Error fetching incidents:', err);
        this.loading.set(false);
      },
    });
  }

  setStatusFilter(status: IncidentStatus | 'All'): void {
    this.statusFilter.set(status);
    this.fetchIncidents();
  }

  setPriorityFilter(priority: string): void {
    this.priorityFilter.set(priority);
    this.fetchIncidents();
  }

  openIncident(incident: IncidentResponse): void {
    this.router.navigate(['/incidents', incident.id]);
  }
}

