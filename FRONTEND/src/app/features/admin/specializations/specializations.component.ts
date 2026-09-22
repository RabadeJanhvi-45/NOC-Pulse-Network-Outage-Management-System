import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeviceService } from '../../../core/services/device.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DeviceTypeSpecialization } from '../../../shared/models';

@Component({
  selector: 'app-specializations',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './specializations.component.html',
  styleUrl: './specializations.component.scss',
})
export class SpecializationsComponent {
  private readonly deviceService = inject(DeviceService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly mappings = signal<DeviceTypeSpecialization[]>([]);
  readonly loading = signal(false);
  readonly isSubmitting = signal(false);

  readonly showForm = signal(false);
  readonly editingMapping = signal<DeviceTypeSpecialization | null>(null);

  readonly form = this.fb.nonNullable.group({
    deviceType: ['', Validators.required],
    requiredSpecialization: ['', Validators.required],
  });

  constructor() {
    this.fetchMappings();
  }

  fetchMappings(): void {
    this.loading.set(true);
    this.deviceService.getDeviceTypeSpecializations().subscribe({
      next: (list) => {
        this.mappings.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openAdd(): void {
    this.editingMapping.set(null);
    this.form.reset();
    this.showForm.set(true);
  }

  openEdit(m: DeviceTypeSpecialization): void {
    this.editingMapping.set(m);
    this.form.setValue({
      deviceType: m.deviceType,
      requiredSpecialization: m.requiredSpecialization,
    });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingMapping.set(null);
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const data = this.form.getRawValue();
    const editing = this.editingMapping();

    const request$ = editing
      ? this.deviceService.updateDeviceTypeSpecialization(editing.id, data)
      : this.deviceService.createDeviceTypeSpecialization(data);

    request$.subscribe({
      next: () => {
        this.notifications.success(editing ? 'Mapping updated.' : 'Mapping created.');
        this.isSubmitting.set(false);
        this.closeForm();
        this.fetchMappings();
      },
      error: () => this.isSubmitting.set(false),
    });
  }

  deleteMapping(m: DeviceTypeSpecialization): void {
    if (!confirm(`Delete specialization mapping for "${m.deviceType}"?`)) return;

    this.deviceService.deleteDeviceTypeSpecialization(m.id).subscribe({
      next: () => {
        this.notifications.success('Mapping deleted.');
        this.fetchMappings();
      },
    });
  }
}
