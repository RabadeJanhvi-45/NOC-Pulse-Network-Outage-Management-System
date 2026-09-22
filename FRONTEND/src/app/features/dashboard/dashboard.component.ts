import { DatePipe, KeyValuePipe, LowerCasePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { IncidentService } from '../../core/services/incident.service';
import { AlarmService } from '../../core/services/alarm.service';
import {
  DashboardSummaryResponse,
  GroupedCountResponse,
  IncidentResponse,
  AlarmResponse,
  SkillResponse,
  EngineerResponse,
} from '../../shared/models';

export interface PieSlice {
  label: string;
  value: number;
  percentage: number;
  color: string;
  strokeDasharray: string;
  strokeDashoffset: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, KeyValuePipe, RouterLink, LowerCasePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly incidentService = inject(IncidentService);
  private readonly alarmService = inject(AlarmService);
  protected readonly auth = inject(AuthService);

  readonly summary = signal<DashboardSummaryResponse | null>(null);
  readonly alarmsBySeverity = signal<GroupedCountResponse | null>(null);
  readonly incidentsByStatus = signal<GroupedCountResponse | null>(null);
  readonly allAlarms = signal<AlarmResponse[]>([]);
  readonly allIncidents = signal<IncidentResponse[]>([]);
  readonly myIncidents = signal<IncidentResponse[]>([]);
  readonly mySkills = signal<SkillResponse[]>([]);
  readonly mySpecialization = signal<string>('Routing & Switching');
  readonly totalEngineersCount = signal<number>(0);
  readonly pendingApprovalsCount = signal<number>(0);
  readonly loading = signal(false);
  readonly hoveredSlice = signal<PieSlice | null>(null);

  private readonly severityOrder = ['Critical', 'Major', 'Minor', 'Warning'];
  private readonly statusOrder = ['NEW', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

  readonly orderedSeverityCounts = computed(() => this.orderEntries(this.alarmsBySeverity(), this.severityOrder));
  readonly orderedStatusCounts = computed(() => this.orderEntries(this.incidentsByStatus(), this.statusOrder));

  readonly myOpenIncidents = computed(() => this.myIncidents().filter((i) => i.status !== 'CLOSED'));
  readonly myActiveCount = computed(() => this.myIncidents().filter((i) => i.status === 'ASSIGNED' || i.status === 'IN_PROGRESS').length);
  readonly myInProgressCount = computed(() => this.myIncidents().filter((i) => i.status === 'IN_PROGRESS').length);
  readonly myResolvedCount = computed(() => this.myIncidents().filter((i) => i.status === 'RESOLVED').length);
  readonly myClosedCount = computed(() => this.myIncidents().filter((i) => i.status === 'CLOSED').length);

  // --- Donut & Pie Chart Color Palettes (Warm Editorial System) ---
  private readonly severityColorMap: Record<string, string> = {
    CRITICAL: '#D9534F',
    Critical: '#D9534F',
    MAJOR: '#E67E22',
    Major: '#E67E22',
    MINOR: '#4A3040',
    Minor: '#4A3040',
    WARNING: '#B98A73',
    Warning: '#B98A73',
  };

  private readonly statusColorMap: Record<string, string> = {
    NEW: '#8C7A70',
    ASSIGNED: '#B98A73',
    IN_PROGRESS: '#E67E22',
    RESOLVED: '#4A3040',
    CLOSED: '#3B7A57',
  };

  readonly severityPieSlices = computed<PieSlice[]>(() =>
    this.calculatePieSlices(this.orderedSeverityCounts(), this.severityColorMap)
  );

  readonly statusPieSlices = computed<PieSlice[]>(() =>
    this.calculatePieSlices(this.orderedStatusCounts(), this.statusColorMap)
  );

  readonly totalAlarmsCount = computed(() =>
    this.orderedSeverityCounts().reduce((sum, item) => sum + item.value, 0)
  );

  readonly totalIncidentsCount = computed(() =>
    this.orderedStatusCounts().reduce((sum, item) => sum + item.value, 0)
  );

  private calculatePieSlices(
    items: { key: string; value: number }[],
    colorMap: Record<string, string>,
    circumference = 408.41
  ): PieSlice[] {
    const total = items.reduce((sum, item) => sum + item.value, 0);
    if (total === 0) return [];

    let accumulatedOffset = 0;
    return items.map((item) => {
      const percentage = Math.round((item.value / total) * 100);
      const strokeLength = (item.value / total) * circumference;
      const strokeDasharray = `${strokeLength.toFixed(2)} ${circumference.toFixed(2)}`;
      const strokeDashoffset = -accumulatedOffset;
      accumulatedOffset += strokeLength;

      return {
        label: item.key,
        value: item.value,
        percentage,
        color: colorMap[item.key.toUpperCase()] || colorMap[item.key] || '#4A3040',
        strokeDasharray,
        strokeDashoffset,
      };
    });
  }

  constructor() {
    this.fetchAll();
  }

  fetchAll(): void {
    this.loading.set(true);

    if (this.auth.hasAnyRole('ENGINEER') && !this.auth.hasAnyRole('ADMIN')) {
      this.incidentService.list().subscribe({
        next: (list) => {
          this.myIncidents.set(list || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });

      this.auth.getMySkills().subscribe({
        next: (skills: SkillResponse[]) => this.mySkills.set(skills || []),
        error: () => {},
      });

      this.auth.getEngineers().subscribe({
        next: (engineers: EngineerResponse[]) => {
          const current = engineers.find(
            (e) => e.userId === this.auth.currentUser()?.userId || e.username === this.auth.currentUser()?.username,
          );
          if (current?.primarySpecialization) {
            this.mySpecialization.set(current.primarySpecialization);
          }
        },
        error: () => {},
      });
      return;
    }

    this.dashboardService.getSummary().subscribe({
      next: (s) => this.summary.set(s),
    });
    this.dashboardService.getAlarmsBySeverity().subscribe({
      next: (a) => this.alarmsBySeverity.set(a),
    });
    this.dashboardService.getIncidentsByStatus().subscribe({
      next: (i) => {
        this.incidentsByStatus.set(i);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.alarmService.list({}).subscribe({
      next: (alarms) => this.allAlarms.set(alarms || []),
      error: () => {},
    });

    this.incidentService.list().subscribe({
      next: (incidents) => this.allIncidents.set(incidents || []),
      error: () => {},
    });

    if (this.auth.hasAnyRole('ADMIN')) {
      this.auth.getRegistrationRequests().subscribe({
        next: (reqs) => this.pendingApprovalsCount.set((reqs || []).filter((r) => r.status === 'PENDING').length),
        error: () => {},
      });
      this.auth.getEngineers().subscribe({
        next: (engs) => this.totalEngineersCount.set((engs || []).length),
        error: () => {},
      });
    }
  }

  private orderEntries(
    grouped: GroupedCountResponse | null,
    preferredOrder: string[],
  ): { key: string; value: number }[] {
    if (!grouped || !grouped.counts) {
      return [];
    }
    const entries = Object.entries(grouped.counts).map(([key, value]) => ({ key, value }));
    entries.sort((a, b) => {
      const ai = preferredOrder.indexOf(a.key);
      const bi = preferredOrder.indexOf(b.key);
      return (ai === -1 ? preferredOrder.length : ai) - (bi === -1 ? preferredOrder.length : bi);
    });
    return entries;
  }

  barWidth(value: number, group: { key: string; value: number }[]): number {
    const max = Math.max(...group.map((g) => g.value), 1);
    return Math.round((value / max) * 100);
  }
}