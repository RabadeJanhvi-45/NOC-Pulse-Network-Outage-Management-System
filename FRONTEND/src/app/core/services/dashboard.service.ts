import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardSummaryResponse, GroupedCountResponse } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly baseUrl = `${environment.apiBaseUrl}/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getSummary(): Observable<DashboardSummaryResponse> {
    return this.http.get<DashboardSummaryResponse>(`${this.baseUrl}/summary`);
  }

  getAlarmsBySeverity(): Observable<GroupedCountResponse> {
    return this.http.get<GroupedCountResponse>(`${this.baseUrl}/alarms-by-severity`);
  }

  getIncidentsByStatus(): Observable<GroupedCountResponse> {
    return this.http.get<GroupedCountResponse>(`${this.baseUrl}/incidents-by-status`);
  }
}