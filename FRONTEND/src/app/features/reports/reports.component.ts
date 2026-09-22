import { DatePipe, KeyValuePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReportService } from '../../core/services/report.service';
import { DeviceService } from '../../core/services/device.service';
import {
  OutageReportResponse,
  DeviceHistoryReportResponse,
  DeviceResponse,
} from '../../shared/models';

// Formats a Date as 'YYYY-MM-DDTHH:mm:ss' — matches Spring's LocalDateTime parsing,
// with no timezone suffix (LocalDateTime has no timezone concept).
function toLocalDateTimeString(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, '0');
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, KeyValuePipe, LowerCasePipe],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss',
})
export class ReportsComponent {
  private readonly reportService = inject(ReportService);
  private readonly deviceService = inject(DeviceService);
  private readonly fb = inject(FormBuilder);

  readonly outageReport = signal<OutageReportResponse | null>(null);
  readonly outageLoading = signal(false);

  readonly devices = signal<DeviceResponse[]>([]);
  readonly deviceHistoryReport = signal<DeviceHistoryReportResponse | null>(null);
  readonly deviceHistoryLoading = signal(false);
  readonly deviceHistoryError = signal(false);

  readonly deviceLookupForm = this.fb.nonNullable.group({
    deviceId: ['', Validators.required],
  });

  // Date-range form for the outage summary — defaults to the last 30 days.
  readonly rangeForm = this.fb.nonNullable.group({
    from: [this.toDateInputValue(this.daysAgo(30))],
    to: [this.toDateInputValue(new Date())],
  });

  constructor() {
    this.fetchOutageSummary();
    this.fetchDevices();
  }

  private daysAgo(days: number): Date {
    const d = new Date();
    d.setDate(d.getDate() - days);
    return d;
  }

  // <input type="date"> needs 'YYYY-MM-DD'.
  private toDateInputValue(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  }

  fetchOutageSummary(): void {
    const raw = this.rangeForm.getRawValue();
    // from = start of day, to = end of day, so the range is inclusive of the whole "to" date.
    const from = toLocalDateTimeString(new Date(`${raw.from}T00:00:00`));
    const to = toLocalDateTimeString(new Date(`${raw.to}T23:59:59`));

    this.outageLoading.set(true);
    this.reportService.getOutageSummary(from, to).subscribe({
      next: (report) => {
        this.outageReport.set(report);
        this.outageLoading.set(false);
      },
      error: () => this.outageLoading.set(false),
    });
  }

  fetchDevices(): void {
    this.deviceService.list().subscribe({
      next: (devices) => this.devices.set(devices),
    });
  }

  lookupDeviceHistory(): void {
    if (this.deviceLookupForm.invalid) {
      this.deviceLookupForm.markAllAsTouched();
      return;
    }

    const deviceId = this.deviceLookupForm.getRawValue().deviceId;
    this.deviceHistoryLoading.set(true);
    this.deviceHistoryError.set(false);
    this.deviceHistoryReport.set(null);

    this.reportService.getDeviceHistory(deviceId).subscribe({
      next: (report) => {
        this.deviceHistoryReport.set(report);
        this.deviceHistoryLoading.set(false);
      },
      error: () => {
        this.deviceHistoryError.set(true);
        this.deviceHistoryLoading.set(false);
      },
    });
  }

  printReport(): void {
    window.print();
  }
}