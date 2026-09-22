import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  IncidentRequest,
  IncidentResponse,
  IncidentHistoryResponse,
  AssignmentResponse,
  ProgressNoteRequest,
  ProgressNoteResponse,
  ResolveIncidentRequest,
  RejectAssignmentRequest,
  RejectResolutionRequest,
  DecideRejectionRequest,
  SlaResponse,
  IncidentFilterParams,
} from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly baseUrl = `${environment.apiBaseUrl}/incidents`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: IncidentFilterParams): Observable<IncidentResponse[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.status) params = params.set('status', filters.status);
      if (filters.priority) params = params.set('priority', filters.priority);
    }
    return this.http.get<IncidentResponse[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<IncidentResponse> {
    return this.http.get<IncidentResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: IncidentRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(this.baseUrl, request);
  }

  update(id: number, request: Partial<IncidentRequest>): Observable<IncidentResponse> {
    return this.http.put<IncidentResponse>(`${this.baseUrl}/${id}`, request);
  }

  // --- Engineer Workflow Actions ---

  startWork(id: number): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/start`, {});
  }

  addNote(id: number, request: ProgressNoteRequest): Observable<ProgressNoteResponse> {
    return this.http.post<ProgressNoteResponse>(`${this.baseUrl}/${id}/notes`, request);
  }

  resolve(id: number, request: ResolveIncidentRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/resolve`, request);
  }

  rejectAssignment(id: number, request: RejectAssignmentRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/reject-assignment`, request);
  }

  // --- NOC Operator Workflow Actions ---

  verify(id: number): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/verify`, {});
  }

  rejectResolution(id: number, request: RejectResolutionRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/reject-resolution`, request);
  }

  // --- Admin Workflow Actions ---

  decideRejection(id: number, request: DecideRejectionRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.baseUrl}/${id}/decide-rejection`, request);
  }

  // --- Supporting Details ---

  getHistory(id: number): Observable<IncidentHistoryResponse[]> {
    return this.http.get<IncidentHistoryResponse[]>(`${this.baseUrl}/${id}/history`);
  }

  getAssignments(id: number): Observable<AssignmentResponse[]> {
    return this.http.get<AssignmentResponse[]>(`${this.baseUrl}/${id}/assignments`);
  }

  getNotes(id: number): Observable<ProgressNoteResponse[]> {
    return this.http.get<ProgressNoteResponse[]>(`${this.baseUrl}/${id}/notes`);
  }

  getSla(id: number): Observable<SlaResponse> {
    return this.http.get<SlaResponse>(`${this.baseUrl}/${id}/sla`);
  }
}