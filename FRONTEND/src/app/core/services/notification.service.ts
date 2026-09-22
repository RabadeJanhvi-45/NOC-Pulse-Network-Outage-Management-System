import { Injectable, signal } from '@angular/core';

export type NotificationLevel = 'success' | 'error' | 'info';

export interface Notification {
  id: number;
  level: NotificationLevel;
  message: string;
}

/**
 * Lightweight toast queue. The app shell (Segment 1) renders whatever is
 * in `notifications()`; this service just owns the state so interceptors
 * and any feature service can push messages without a component
 * reference.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  private readonly notificationsSignal = signal<Notification[]>([]);
  readonly notifications = this.notificationsSignal.asReadonly();

  success(message: string): void {
    this.push('success', message);
  }

  error(message: string): void {
    this.push('error', message);
  }

  info(message: string): void {
    this.push('info', message);
  }

  dismiss(id: number): void {
    this.notificationsSignal.update((list) => list.filter((n) => n.id !== id));
  }

  private push(level: NotificationLevel, message: string): void {
    const id = this.nextId++;
    this.notificationsSignal.update((list) => [...list, { id, level, message }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}
