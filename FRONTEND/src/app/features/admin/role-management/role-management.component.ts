import { Component, inject, signal } from '@angular/core';
import { RoleService } from '../../../core/services/role.service';
import { RoleResponse } from '../../../shared/models';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [],
  templateUrl: './role-management.component.html',
  styleUrl: './role-management.component.scss',
})
export class RoleManagementComponent {
  private readonly roleService = inject(RoleService);

  readonly roles = signal<RoleResponse[]>([]);
  readonly loading = signal(false);

  constructor() {
    this.fetchRoles();
  }

  fetchRoles(): void {
    this.loading.set(true);
    this.roleService.listRoles().subscribe({
      next: (roles) => {
        this.roles.set(roles);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}