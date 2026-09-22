import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditLogResponse } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly baseUrl = `${environment.apiBaseUrl}/audit-logs`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: { action?: string; username?: string }): Observable<AuditLogResponse[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.action) params = params.set('action', filters.action);
      if (filters.username) params = params.set('username', filters.username);
    }
    return this.http.get<AuditLogResponse[]>(this.baseUrl, { params });
  }
}