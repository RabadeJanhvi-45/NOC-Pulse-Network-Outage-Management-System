import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { RoleService } from '../../../core/services/role.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserResponse, RoleResponse } from '../../../shared/models';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, LowerCasePipe],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',
})
export class UserManagementComponent {
  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  readonly users = signal<UserResponse[]>([]);
  readonly roles = signal<RoleResponse[]>([]);
  readonly loading = signal(false);
  readonly isSubmitting = signal(false);
  readonly showForm = signal(false);
  readonly editingUser = signal<UserResponse | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: [''], // required on create only — checked manually in submit()
    email: [''],
    fullName: [''],
    roleId: [0, Validators.required],
    enabled: [true],
  });

  constructor() {
    this.fetchUsers();
    this.fetchRoles();
  }

  fetchUsers(): void {
    this.loading.set(true);
    this.userService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  fetchRoles(): void {
    this.roleService.listRoles().subscribe({
      next: (roles) => this.roles.set(roles),
    });
  }

  openAddForm(): void {
    this.editingUser.set(null);
    this.form.reset({ enabled: true, roleId: this.roles()[0]?.id ?? 0 });
    this.showForm.set(true);
  }

  openEditForm(user: UserResponse): void {
    this.editingUser.set(user);
    this.form.setValue({
      username: user.username,
      password: '',
      email: user.email ?? '',
      fullName: user.fullName ?? '',
      roleId: user.roleId,
      enabled: user.enabled,
    });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingUser.set(null);
  }

  submit(): void {
    const editing = this.editingUser();

    // Password required on create; optional on update (omitting keeps the current password).
    if (!editing && !this.form.controls.password.value) {
      this.form.controls.password.setErrors({ required: true });
    }

    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const payload = {
      username: raw.username,
      email: raw.email || undefined,
      fullName: raw.fullName || undefined,
      roleId: raw.roleId,
      enabled: raw.enabled,
      ...(raw.password ? { password: raw.password } : {}),
    };

    const request$ = editing
      ? this.userService.update(editing.id, payload)
      : this.userService.create({ ...payload, password: raw.password! });

    request$.subscribe({
      next: () => {
        this.notifications.success(editing ? 'User updated.' : 'User created.');
        this.isSubmitting.set(false);
        this.showForm.set(false);
        this.editingUser.set(null);
        this.fetchUsers();
      },
      error: () => this.isSubmitting.set(false),
    });
  }

  roleNameFor(roleId: number): string {
    return this.roles().find((r) => r.id === roleId)?.name ?? '—';
  }

  toggleEnabled(user: UserResponse): void {
    this.userService
      .update(user.id, {
        username: user.username,
        email: user.email,
        fullName: user.fullName,
        roleId: user.roleId,
        enabled: !user.enabled,
      })
      .subscribe({
        next: () => {
          this.notifications.success(user.enabled ? 'User disabled.' : 'User enabled.');
          this.fetchUsers();
        },
      });
  }
}