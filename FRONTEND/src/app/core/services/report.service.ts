import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OutageReportResponse, DeviceHistoryReportResponse } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly baseUrl = `${environment.apiBaseUrl}/reports`;

  constructor(private readonly http: HttpClient) {}

  getOutages(from?: string, to?: string): Observable<OutageReportResponse> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<OutageReportResponse>(`${this.baseUrl}/outage-summary`, { params });
  }

  getOutageSummary(from?: string, to?: string): Observable<OutageReportResponse> {
    return this.getOutages(from, to);
  }

  getDeviceHistory(deviceId: string): Observable<DeviceHistoryReportResponse> {
    return this.http.get<DeviceHistoryReportResponse>(`${this.baseUrl}/device/${deviceId}/history`);
  }
}