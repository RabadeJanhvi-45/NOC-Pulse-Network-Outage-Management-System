import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RoleRequest, RoleResponse } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private readonly rolesUrl = `${environment.apiBaseUrl}/roles`;

  constructor(private readonly http: HttpClient) {}

  listRoles(): Observable<RoleResponse[]> {
    return this.http.get<RoleResponse[]>(this.rolesUrl);
  }

  createRole(request: RoleRequest): Observable<RoleResponse> {
    return this.http.post<RoleResponse>(this.rolesUrl, request);
  }

  updateRole(id: number, request: RoleRequest): Observable<RoleResponse> {
    return this.http.put<RoleResponse>(`${this.rolesUrl}/${id}`, request);
  }
}