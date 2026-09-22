import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AlarmRequest,
  AlarmResponse,
  AlarmAcknowledgementResponse,
  SeverityRuleRequest,
  SeverityRuleResponse,
  AlarmFilterParams,
} from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class AlarmService {
  private readonly baseUrl = `${environment.apiBaseUrl}/alarms`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: AlarmFilterParams): Observable<AlarmResponse[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.severity) params = params.set('severity', filters.severity);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.deviceId) params = params.set('deviceId', filters.deviceId);
    }
    return this.http.get<AlarmResponse[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<AlarmResponse> {
    return this.http.get<AlarmResponse>(`${this.baseUrl}/${id}`);
  }

  raise(request: AlarmRequest): Observable<AlarmResponse> {
    return this.http.post<AlarmResponse>(this.baseUrl, request);
  }

  acknowledge(id: number): Observable<AlarmAcknowledgementResponse> {
    return this.http.post<AlarmAcknowledgementResponse>(`${this.baseUrl}/${id}/acknowledge`, {});
  }

  clear(id: number): Observable<AlarmResponse> {
    return this.http.post<AlarmResponse>(`${this.baseUrl}/${id}/clear`, {});
  }

  getRules(): Observable<SeverityRuleResponse[]> {
    return this.http.get<SeverityRuleResponse[]>(`${this.baseUrl}/rules`);
  }

  createRule(request: SeverityRuleRequest): Observable<SeverityRuleResponse> {
    return this.http.post<SeverityRuleResponse>(`${this.baseUrl}/rules`, request);
  }

  updateRule(id: number, request: SeverityRuleRequest): Observable<SeverityRuleResponse> {
    return this.http.put<SeverityRuleResponse>(`${this.baseUrl}/rules/${id}`, request);
  }

  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/rules/${id}`);
  }
}