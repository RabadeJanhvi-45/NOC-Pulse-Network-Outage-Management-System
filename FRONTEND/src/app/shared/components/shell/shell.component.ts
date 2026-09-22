import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationItemResponse, RoleName } from '../../models';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  roles: RoleName[]; // empty = visible to every logged-in role
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: 'dashboard', roles: [] },
  { label: 'Devices', path: '/devices', icon: 'devices', roles: ['ADMIN', 'NOC_OPERATOR'] },
  { label: 'Alarms', path: '/alarms', icon: 'alarms', roles: ['ADMIN', 'NOC_OPERATOR'] },
  { label: 'Severity Rules', path: '/alarms/rules', icon: 'rules', roles: ['ADMIN'] },
  { label: 'Incidents', path: '/incidents', icon: 'incidents', roles: [] },
  { label: 'My Skills', path: '/profile/skills', icon: 'skills', roles: ['ENGINEER'] },
  { label: 'Register Approvals', path: '/registration-requests', icon: 'approvals', roles: ['ADMIN'] },
  { label: 'Specialization Map', path: '/admin/specializations', icon: 'map', roles: ['ADMIN'] },
  { label: 'Engineer Config', path: '/admin/engineers', icon: 'engineers', roles: ['ADMIN'] },
  { label: 'Users', path: '/admin/users', icon: 'users', roles: ['ADMIN'] },
  { label: 'Roles', path: '/admin/roles', icon: 'roles', roles: ['ADMIN'] },
  { label: 'Audit Logs', path: '/admin/audit-logs', icon: 'logs', roles: ['ADMIN'] },
  { label: 'Reports', path: '/reports', icon: 'reports', roles: ['ADMIN'] },
];

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, DatePipe, LowerCasePipe],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);

  readonly notifications = signal<NotificationItemResponse[]>([]);
  readonly showNotifications = signal(false);
  readonly unreadCount = signal(0);

  private pollTimer: any = null;

  ngOnInit(): void {
    this.fetchNotifications();
    this.pollTimer = setInterval(() => this.fetchNotifications(), 15000);
  }

  ngOnDestroy(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
  }

  protected get visibleNavItems(): NavItem[] {
    return NAV_ITEMS.filter(
      (item) => item.roles.length === 0 || this.auth.hasAnyRole(...item.roles),
    );
  }

  fetchNotifications(): void {
    if (!this.auth.isLoggedIn()) return;
    this.auth.getNotifications().subscribe({
      next: (list) => {
        this.notifications.set(list || []);
        this.unreadCount.set((list || []).filter((n) => !n.isRead).length);
      },
      error: () => {},
    });
  }

  toggleNotificationDropdown(): void {
    this.showNotifications.update((v) => !v);
  }

  markAsRead(item: NotificationItemResponse): void {
    if (item.isRead) return;
    this.auth.markNotificationAsRead(item.id).subscribe({
      next: () => {
        this.notifications.update((list) =>
          list.map((n) => (n.id === item.id ? { ...n, isRead: true } : n)),
        );
        this.unreadCount.set(this.notifications().filter((n) => !n.isRead).length);
      },
    });
  }

  logout(): void {
    this.auth.logout();
  }
}