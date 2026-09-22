import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { DeviceService } from '../../../core/services/device.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EngineerResponse } from '../../../shared/models';

@Component({
  selector: 'app-engineer-management',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './engineer-management.component.html',
  styleUrl: './engineer-management.component.scss',
})
export class EngineerManagementComponent {
  private readonly auth = inject(AuthService);
  private readonly deviceService = inject(DeviceService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly engineers = signal<EngineerResponse[]>([]);
  readonly loading = signal(false);
  readonly isSaving = signal(false);

  readonly totalCount = computed(() => this.engineers().length);
  readonly assignedCount = computed(() => this.engineers().filter((e) => !!e.primarySpecialization).length);
  readonly unassignedCount = computed(() => this.engineers().filter((e) => !e.primarySpecialization).length);

  readonly defaultSpecializationOptions: string[] = [
    'Routing & Switching',
    'Firewall & Security',
    'Cloud Networking',
    'Wireless & Mobility',
    'Data Center Infrastructure',
    'Network Automation & DevOps',
    'VoIP & Unified Communications',
  ];

  readonly specializationOptions = signal<string[]>(this.defaultSpecializationOptions);

  readonly editingEngineer = signal<EngineerResponse | null>(null);
  readonly specForm = this.fb.nonNullable.group({
    specialization: ['', [Validators.required, Validators.minLength(2)]],
  });

  constructor() {
    this.fetchEngineers();
    this.fetchSpecializations();
  }

  fetchSpecializations(): void {
    this.deviceService.getDeviceTypeSpecializations().subscribe({
      next: (mappings) => {
        const set = new Set<string>(this.defaultSpecializationOptions);
        mappings.forEach((m) => {
          if (m.requiredSpecialization?.trim()) {
            set.add(m.requiredSpecialization.trim());
          }
        });
        this.specializationOptions.set(Array.from(set));
      },
      error: () => {},
    });
  }

  fetchEngineers(): void {
    this.loading.set(true);
    this.auth.getEngineers().subscribe({
      next: (list) => {
        this.engineers.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openEditSpec(eng: EngineerResponse): void {
    this.editingEngineer.set(eng);
    this.specForm.setValue({
      specialization: eng.primarySpecialization || '',
    });
  }

  closeModal(): void {
    this.editingEngineer.set(null);
    this.specForm.reset();
  }

  saveSpecialization(): void {
    const eng = this.editingEngineer();
    if (!eng || this.specForm.invalid || this.isSaving()) {
      this.specForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    const { specialization } = this.specForm.getRawValue();

    this.auth.setEngineerSpecialization(eng.userId, specialization).subscribe({
      next: () => {
        this.notifications.success(`Primary specialization updated for ${eng.username || 'Engineer'}.`);
        this.isSaving.set(false);
        this.closeModal();
        this.fetchEngineers();
      },
      error: () => this.isSaving.set(false),
    });
  }
}
