import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

import { ShellComponent } from './shared/components/shell/shell.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { EngineerRegisterComponent } from './features/auth/engineer-register/engineer-register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { DeviceListComponent } from './features/devices/device-list/device-list.component';
import { DeviceRequestsComponent } from './features/admin/device-requests/device-requests.component';
import { AlarmListComponent } from './features/alarms/alarm-list/alarm-list.component';
import { SeverityRulesComponent } from './features/alarms/severity-rules/severity-rules.component';
import { IncidentListComponent } from './features/incidents/incident-list/incident-list.component';
import { IncidentDetailComponent } from './features/incidents/incident-detail/incident-detail.component';
import { RegistrationRequestsComponent } from './features/admin/registration-requests/registration-requests.component';
import { SpecializationsComponent } from './features/admin/specializations/specializations.component';
import { EngineerManagementComponent } from './features/admin/engineers/engineer-management.component';
import { EngineerSkillsComponent } from './features/profile/skills/engineer-skills.component';
import { ReportsComponent } from './features/reports/reports.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { RoleManagementComponent } from './features/admin/role-management/role-management.component';
import { AuditLogsComponent } from './features/admin/audit-logs/audit-logs.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => LoginComponent,
    data: { title: 'Login' },
  },
  {
    path: 'register',
    loadComponent: () => RegisterComponent,
    data: { title: 'NOC Operator Registration' },
  },
  {
    path: 'register-engineer',
    loadComponent: () => EngineerRegisterComponent,
    data: { title: 'Engineer Registration' },
  },

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => ShellComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => DashboardComponent,
        data: { title: 'Dashboard' },
      },

      {
        path: 'devices',
        canActivate: [roleGuard],
        loadComponent: () => DeviceListComponent,
        data: { title: 'Device Inventory', roles: ['ADMIN', 'NOC_OPERATOR'] },
      },

      {
        path: 'device-requests',
        canActivate: [roleGuard],
        loadComponent: () => DeviceRequestsComponent,
        data: { title: 'Device Registration Requests', roles: ['ADMIN'] },
      },

      {
        path: 'alarms',
        canActivate: [roleGuard],
        loadComponent: () => AlarmListComponent,
        data: { title: 'Alarm Management', roles: ['ADMIN', 'NOC_OPERATOR'] },
      },

      {
        path: 'alarms/rules',
        canActivate: [roleGuard],
        loadComponent: () => SeverityRulesComponent,
        data: { title: 'Severity Rules', roles: ['ADMIN'] },
      },

      {
        path: 'incidents',
        loadComponent: () => IncidentListComponent,
        data: { title: 'Incidents' },
      },

      {
        path: 'incidents/:id',
        loadComponent: () => IncidentDetailComponent,
        data: { title: 'Incident Resolution Workspace' },
      },

      {
        path: 'registration-requests',
        canActivate: [roleGuard],
        loadComponent: () => RegistrationRequestsComponent,
        data: { title: 'NOC Operator Approvals', roles: ['ADMIN'] },
      },

      {
        path: 'admin/specializations',
        canActivate: [roleGuard],
        loadComponent: () => SpecializationsComponent,
        data: { title: 'Specializations Map', roles: ['ADMIN'] },
      },

      {
        path: 'admin/engineers',
        canActivate: [roleGuard],
        loadComponent: () => EngineerManagementComponent,
        data: { title: 'Engineer Configuration', roles: ['ADMIN'] },
      },

      {
        path: 'profile/skills',
        canActivate: [roleGuard],
        loadComponent: () => EngineerSkillsComponent,
        data: { title: 'My Secondary Skills', roles: ['ENGINEER'] },
      },

      {
        path: 'reports',
        canActivate: [roleGuard],
        loadComponent: () => ReportsComponent,
        data: { title: 'Outage & Health Reporting', roles: ['ADMIN'] },
      },

      {
        path: 'admin/users',
        canActivate: [roleGuard],
        loadComponent: () => UserManagementComponent,
        data: { title: 'User Management', roles: ['ADMIN'] },
      },

      {
        path: 'admin/roles',
        canActivate: [roleGuard],
        loadComponent: () => RoleManagementComponent,
        data: { title: 'Roles & Access', roles: ['ADMIN'] },
      },

      {
        path: 'admin/audit-logs',
        canActivate: [roleGuard],
        loadComponent: () => AuditLogsComponent,
        data: { title: 'Audit Logs', roles: ['ADMIN'] },
      },

      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
    ],
  },

  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

