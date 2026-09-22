import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  LoginResponse,
  NocRegisterRequest,
  EngineerRegisterRequest,
  RegistrationRequestResponse,
  RejectRegistrationRequest,
  EngineerResponse,
  SkillResponse,
  AddSkillRequest,
  SpecializationRequest,
  NotificationItemResponse,
  RoleName,
} from '../../shared/models';

const TOKEN_KEY = 'nod_token';
const USER_KEY = 'nod_user';

export interface StoredUser {
  userId: number;
  username: string;
  fullName?: string;
  roleName: RoleName;
  expiresAt?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<StoredUser | null>(this.readStoredUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUserSignal());
  readonly role = computed<RoleName | null>(() => this.currentUserSignal()?.roleName ?? null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, request).pipe(
      tap((response) => {
        const user: StoredUser = {
          userId: response.userId,
          username: response.username,
          fullName: response.fullName,
          roleName: response.roleName,
          expiresAt: response.expiresAt,
        };
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUserSignal.set(user);
      }),
    );
  }

  logout(): void {
    this.http.post(`${environment.apiBaseUrl}/auth/logout`, {}).subscribe({
      complete: () => this.clearSession(),
      error: () => this.clearSession(),
    });
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  hasAnyRole(...roles: RoleName[]): boolean {
    const current = this.role();
    return !!current && roles.includes(current);
  }

  // --- Registration Requests (NOC Operator self-registration & Admin review) ---

  registerNocOperator(request: NocRegisterRequest): Observable<RegistrationRequestResponse> {
    return this.http.post<RegistrationRequestResponse>(`${environment.apiBaseUrl}/auth/register`, { ...request, role: 'NOC_OPERATOR' });
  }

  registerEngineer(request: EngineerRegisterRequest): Observable<RegistrationRequestResponse> {
    return this.http.post<RegistrationRequestResponse>(`${environment.apiBaseUrl}/auth/register`, { ...request, role: 'ENGINEER' });
  }

  getRegistrationRequests(): Observable<RegistrationRequestResponse[]> {
    return this.http.get<RegistrationRequestResponse[]>(`${environment.apiBaseUrl}/registration-requests`);
  }

  approveRegistrationRequest(id: number): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/registration-requests/${id}/approve`, {});
  }

  rejectRegistrationRequest(id: number, reason: string): Observable<void> {
    const body: RejectRegistrationRequest = { reason };
    return this.http.post<void>(`${environment.apiBaseUrl}/registration-requests/${id}/reject`, body);
  }

  // --- Engineer Skills & Specializations ---

  getEngineers(): Observable<EngineerResponse[]> {
    return this.http.get<EngineerResponse[]>(`${environment.apiBaseUrl}/engineers`);
  }

  setEngineerSpecialization(userId: number, specialization: string): Observable<void> {
    const body: SpecializationRequest = { specialization };
    return this.http.put<void>(`${environment.apiBaseUrl}/engineers/${userId}/specialization`, body);
  }

  getMySkills(): Observable<SkillResponse[]> {
    return this.http.get<SkillResponse[]>(`${environment.apiBaseUrl}/engineers/me/skills`);
  }

  addSkill(skillName: string): Observable<SkillResponse> {
    const body: AddSkillRequest = { skillName };
    return this.http.post<SkillResponse>(`${environment.apiBaseUrl}/engineers/me/skills`, body);
  }

  deleteSkill(skillId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/engineers/me/skills/${skillId}`);
  }

  // --- Notification Center ---

  getNotifications(): Observable<NotificationItemResponse[]> {
    return this.http.get<NotificationItemResponse[]>(`${environment.apiBaseUrl}/notifications`);
  }

  markNotificationAsRead(id: number): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/notifications/${id}/read`, {});
  }

  private readStoredUser(): StoredUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredUser;
    } catch {
      return null;
    }
  }
}

