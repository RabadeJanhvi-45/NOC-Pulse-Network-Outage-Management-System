import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DeviceRequest,
  DeviceResponse,
  DeviceHistoryResponse,
  DeviceChangeRequest,
  DeviceTypeSpecialization,
  DeviceFilterParams,
} from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class DeviceService {
  private readonly baseUrl = `${environment.apiBaseUrl}/devices`;
  private readonly requestsUrl = `${environment.apiBaseUrl}/device-requests`;
  private readonly specMapUrl = `${environment.apiBaseUrl}/device-type-specializations`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: DeviceFilterParams): Observable<DeviceResponse[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.region) params = params.set('region', filters.region);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.deviceType) params = params.set('deviceType', filters.deviceType);
      if (filters.search) params = params.set('search', filters.search);
    }
    return this.http.get<DeviceResponse[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<DeviceResponse> {
    return this.http.get<DeviceResponse>(`${this.baseUrl}/${id}`);
  }

  getByDeviceId(deviceId: string): Observable<DeviceResponse> {
    return this.http.get<DeviceResponse>(`${this.baseUrl}/by-device-id/${deviceId}`);
  }

  create(request: DeviceRequest): Observable<DeviceResponse> {
    return this.http.post<DeviceResponse>(this.baseUrl, request);
  }

  update(id: number, request: DeviceRequest): Observable<DeviceResponse> {
    return this.http.put<DeviceResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getHistory(id: number): Observable<DeviceHistoryResponse[]> {
    return this.http.get<DeviceHistoryResponse[]>(`${this.baseUrl}/${id}/history`);
  }

  // --- Device Registration & Change Requests ---

  submitDeviceRequest(request: DeviceChangeRequest): Observable<DeviceChangeRequest> {
    return this.http.post<DeviceChangeRequest>(this.requestsUrl, request);
  }

  listDeviceRequests(): Observable<DeviceChangeRequest[]> {
    return this.http.get<DeviceChangeRequest[]>(this.requestsUrl);
  }

  approveDeviceRequest(id: number): Observable<void> {
    return this.http.post<void>(`${this.requestsUrl}/${id}/approve`, {});
  }

  rejectDeviceRequest(id: number, reason: string): Observable<void> {
    return this.http.post<void>(`${this.requestsUrl}/${id}/reject`, { reason });
  }

  // --- Device Type to Specialization Mapping ---

  getDeviceTypeSpecializations(): Observable<DeviceTypeSpecialization[]> {
    return this.http.get<DeviceTypeSpecialization[]>(this.specMapUrl);
  }

  createDeviceTypeSpecialization(data: { deviceType: string; requiredSpecialization: string }): Observable<DeviceTypeSpecialization> {
    return this.http.post<DeviceTypeSpecialization>(this.specMapUrl, data);
  }

  updateDeviceTypeSpecialization(id: number, data: { deviceType: string; requiredSpecialization: string }): Observable<DeviceTypeSpecialization> {
    return this.http.put<DeviceTypeSpecialization>(`${this.specMapUrl}/${id}`, data);
  }

  deleteDeviceTypeSpecialization(id: number): Observable<void> {
    return this.http.delete<void>(`${this.specMapUrl}/${id}`);
  }
}