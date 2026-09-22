import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { IncidentService } from '../../../core/services/incident.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  IncidentResponse,
  IncidentHistoryResponse,
  AssignmentResponse,
  ProgressNoteResponse,
  SlaResponse,
  IncidentStatus,
} from '../../../shared/models';

@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, RouterLink, LowerCasePipe],
  templateUrl: './incident-detail.component.html',
  styleUrl: './incident-detail.component.scss',
})
export class IncidentDetailComponent {
  private readonly incidentService = inject(IncidentService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  readonly incidentId = Number(this.route.snapshot.paramMap.get('id'));

  readonly incident = signal<IncidentResponse | null>(null);
  readonly history = signal<IncidentHistoryResponse[]>([]);
  readonly assignments = signal<AssignmentResponse[]>([]);
  readonly notes = signal<ProgressNoteResponse[]>([]);
  readonly sla = signal<SlaResponse | null>(null);

  readonly loading = signal(false);
  readonly isActionSubmitting = signal(false);

  // Modals
  readonly showResolveModal = signal(false);
  readonly showRejectAssignmentModal = signal(false);
  readonly showRejectResolutionModal = signal(false);

  readonly lifecycleStages: IncidentStatus[] = [
    'NEW',
    'ASSIGNED',
    'IN_PROGRESS',
    'RESOLVED',
    'CLOSED',
  ];

  // Forms
  readonly noteForm = this.fb.nonNullable.group({
    noteText: ['', Validators.required],
  });

  readonly resolveForm = this.fb.nonNullable.group({
    rootCause: ['', Validators.required],
    resolutionSteps: ['', Validators.required],
  });

  readonly rejectAssignmentForm = this.fb.nonNullable.group({
    reason: ['', Validators.required],
  });

  readonly rejectResolutionForm = this.fb.nonNullable.group({
    verificationNotes: ['', Validators.required],
  });

  constructor() {
    this.fetchAll();
  }

  fetchAll(): void {
    this.loading.set(true);
    this.incidentService.getById(this.incidentId).subscribe({
      next: (incident) => {
        this.incident.set(incident);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.incidentService.getHistory(this.incidentId).subscribe({
      next: (h) => this.history.set(h || []),
    });
    this.incidentService.getAssignments(this.incidentId).subscribe({
      next: (a) => this.assignments.set(a || []),
    });
    this.incidentService.getNotes(this.incidentId).subscribe({
      next: (n) => this.notes.set(n || []),
    });
    this.incidentService.getSla(this.incidentId).subscribe({
      next: (s) => this.sla.set(s),
    });
  }

  // --- Stage helpers ---

  isStageCompleted(stage: IncidentStatus): boolean {
    const current = this.incident()?.status;
    if (!current) return false;
    const currentIdx = this.lifecycleStages.indexOf(current);
    const stageIdx = this.lifecycleStages.indexOf(stage);
    return stageIdx < currentIdx;
  }

  isStageActive(stage: IncidentStatus): boolean {
    return this.incident()?.status === stage;
  }

  // --- Engineer Actions ---

  startWork(): void {
    this.isActionSubmitting.set(true);
    this.incidentService.startWork(this.incidentId).subscribe({
      next: () => {
        this.notifications.success('Started work on incident. Status is now IN_PROGRESS.');
        this.isActionSubmitting.set(false);
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  openRejectAssignmentModal(): void {
    this.rejectAssignmentForm.reset();
    this.showRejectAssignmentModal.set(true);
  }

  closeRejectAssignmentModal(): void {
    this.showRejectAssignmentModal.set(false);
  }

  submitRejectAssignment(): void {
    if (this.rejectAssignmentForm.invalid || this.isActionSubmitting()) {
      this.rejectAssignmentForm.markAllAsTouched();
      return;
    }

    this.isActionSubmitting.set(true);
    const reason = this.rejectAssignmentForm.getRawValue().reason;
    this.incidentService.rejectAssignment(this.incidentId, { reason }).subscribe({
      next: () => {
        this.notifications.success('Assignment rejection submitted for Administrator review.');
        this.isActionSubmitting.set(false);
        this.closeRejectAssignmentModal();
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  submitNote(): void {
    if (this.noteForm.invalid || this.isActionSubmitting()) {
      this.noteForm.markAllAsTouched();
      return;
    }

    this.isActionSubmitting.set(true);
    const noteText = this.noteForm.getRawValue().noteText;
    const authorId = String(this.auth.currentUser()?.userId ?? '');
    this.incidentService.addNote(this.incidentId, { noteText, authorId }).subscribe({
      next: () => {
        this.notifications.success('Progress note recorded.');
        this.noteForm.reset();
        this.isActionSubmitting.set(false);
        this.incidentService.getNotes(this.incidentId).subscribe({
          next: (n) => this.notes.set(n || []),
        });
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  openResolveModal(): void {
    this.resolveForm.reset();
    this.showResolveModal.set(true);
  }

  closeResolveModal(): void {
    this.showResolveModal.set(false);
  }

  submitResolve(): void {
    if (this.resolveForm.invalid || this.isActionSubmitting()) {
      this.resolveForm.markAllAsTouched();
      return;
    }

    this.isActionSubmitting.set(true);
    const raw = this.resolveForm.getRawValue();
    const payload = {
      resolutionNotes: `${raw.rootCause} — ${raw.resolutionSteps}`,
      rootCause: raw.rootCause,
      resolutionSteps: raw.resolutionSteps,
    };

    this.incidentService.resolve(this.incidentId, payload).subscribe({
      next: () => {
        this.notifications.success('Incident marked as RESOLVED. Awaiting NOC Operator verification.');
        this.isActionSubmitting.set(false);
        this.closeResolveModal();
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  // --- NOC Operator Actions ---

  verifyAndClose(): void {
    if (!confirm('Verify and close this incident? This completes the lifecycle and archives the ticket.')) {
      return;
    }

    this.isActionSubmitting.set(true);
    this.incidentService.verify(this.incidentId).subscribe({
      next: () => {
        this.notifications.success('Incident verified and CLOSED.');
        this.isActionSubmitting.set(false);
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  openRejectResolutionModal(): void {
    this.rejectResolutionForm.reset();
    this.showRejectResolutionModal.set(true);
  }

  closeRejectResolutionModal(): void {
    this.showRejectResolutionModal.set(false);
  }

  submitRejectResolution(): void {
    if (this.rejectResolutionForm.invalid || this.isActionSubmitting()) {
      this.rejectResolutionForm.markAllAsTouched();
      return;
    }

    this.isActionSubmitting.set(true);
    const raw = this.rejectResolutionForm.getRawValue();
    const payload = {
      reason: raw.verificationNotes,
      verificationNotes: raw.verificationNotes,
    };

    this.incidentService.rejectResolution(this.incidentId, payload).subscribe({
      next: () => {
        this.notifications.success('Resolution rejected. Returned to Engineer with status IN_PROGRESS.');
        this.isActionSubmitting.set(false);
        this.closeRejectResolutionModal();
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  // --- Admin Rejection Decisions ---

  decideRejection(approved: boolean): void {
    const actionText = approved
      ? 'Approve rejection and auto-reassign to next engineer?'
      : 'Deny rejection and keep incident assigned to this engineer?';

    if (!confirm(actionText)) return;

    this.isActionSubmitting.set(true);
    this.incidentService.decideRejection(this.incidentId, {
      decision: approved ? 'APPROVE' : 'REJECT',
      approved,
    }).subscribe({
      next: () => {
        this.notifications.success(
          approved ? 'Rejection approved. Reassigning incident.' : 'Rejection denied. Kept with engineer.',
        );
        this.isActionSubmitting.set(false);
        this.fetchAll();
      },
      error: () => this.isActionSubmitting.set(false),
    });
  }

  backToList(): void {
    this.router.navigate(['/incidents']);
  }
}