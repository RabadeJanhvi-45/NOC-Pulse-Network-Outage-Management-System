import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SkillResponse } from '../../../shared/models';

@Component({
  selector: 'app-engineer-skills',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './engineer-skills.component.html',
  styleUrl: './engineer-skills.component.scss',
})
export class EngineerSkillsComponent {
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly skills = signal<SkillResponse[]>([]);
  readonly loading = signal(false);
  readonly isAdding = signal(false);

  readonly form = this.fb.nonNullable.group({
    skillName: ['', [Validators.required, Validators.minLength(2)]],
  });

  constructor() {
    this.fetchSkills();
  }

  fetchSkills(): void {
    this.loading.set(true);
    this.auth.getMySkills().subscribe({
      next: (list) => {
        this.skills.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  canAddMore(): boolean {
    return this.skills().length < 3;
  }

  addSkill(): void {
    if (this.form.invalid || this.isAdding() || !this.canAddMore()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isAdding.set(true);
    const { skillName } = this.form.getRawValue();

    this.auth.addSkill(skillName).subscribe({
      next: () => {
        this.notifications.success('Skill added successfully.');
        this.form.reset();
        this.isAdding.set(false);
        this.fetchSkills();
      },
      error: () => this.isAdding.set(false),
    });
  }

  deleteSkill(skill: SkillResponse): void {
    if (!confirm(`Remove skill "${skill.skillName}"?`)) return;

    this.auth.deleteSkill(skill.id).subscribe({
      next: () => {
        this.notifications.success('Skill removed.');
        this.fetchSkills();
      },
    });
  }
}
