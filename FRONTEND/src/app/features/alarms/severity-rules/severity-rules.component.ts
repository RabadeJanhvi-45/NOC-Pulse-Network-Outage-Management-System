import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AlarmService } from '../../../core/services/alarm.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AlarmSeverity, SeverityRuleResponse } from '../../../shared/models';

@Component({
  selector: 'app-severity-rules',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './severity-rules.component.html',
  styleUrl: './severity-rules.component.scss',
})
export class SeverityRulesComponent {
  private readonly alarmService = inject(AlarmService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly rules = signal<SeverityRuleResponse[]>([]);
  readonly loading = signal(false);
  readonly isSubmitting = signal(false);
  readonly showForm = signal(false);
  readonly editingRule = signal<SeverityRuleResponse | null>(null);

  readonly severityOptions: AlarmSeverity[] = ['Critical', 'Major', 'Minor', 'Warning'];

  readonly form = this.fb.nonNullable.group({
    alarmType: ['', Validators.required],
    severity: ['Warning' as AlarmSeverity, Validators.required],
    conditionLogic: [''],
  });

  constructor() {
    this.fetchRules();
  }

  fetchRules(): void {
    this.loading.set(true);
    this.alarmService.getRules().subscribe({
      next: (rules) => {
        this.rules.set(rules);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openForm(existing?: SeverityRuleResponse): void {
    if (existing) {
      this.editingRule.set(existing);
      this.form.setValue({
        alarmType: existing.alarmType,
        severity: existing.severity,
        conditionLogic: existing.conditionLogic ?? '',
      });
    } else {
      this.editingRule.set(null);
      this.form.reset({ severity: 'Warning' });
    }
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingRule.set(null);
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const payload = this.form.getRawValue();
    const editing = this.editingRule();

    const request$ = editing
      ? this.alarmService.updateRule(editing.id, payload)
      : this.alarmService.createRule(payload);

    request$.subscribe({
      next: () => {
        this.notifications.success('Severity rule saved.');
        this.isSubmitting.set(false);
        this.showForm.set(false);
        this.editingRule.set(null);
        this.fetchRules();
      },
      error: () => this.isSubmitting.set(false),
    });
  }

  deleteRule(rule: SeverityRuleResponse): void {
    if (!confirm(`Delete rule for alarm type "${rule.alarmType}"?`)) return;

    this.alarmService.deleteRule(rule.id).subscribe({
      next: () => {
        this.notifications.success('Severity rule deleted.');
        this.fetchRules();
      },
    });
  }
}