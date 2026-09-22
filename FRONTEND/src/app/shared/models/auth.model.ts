import { RoleName } from './common.model';

/** POST /api/auth/login */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Response from POST /api/auth/login */
export interface LoginResponse {
  token: string;
  tokenType?: string; // "Bearer"
  userId: number;
  username: string;
  fullName?: string;
  roleName: RoleName;
  expiresAt?: string;
}

/** POST /api/auth/register (NOC Operator Self-Registration) */
export interface NocRegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

/** POST /api/auth/register/engineer (Engineer Self-Registration) */
export interface EngineerRegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
  specialization?: string;
}

/** GET /api/registration-requests, POST /api/auth/register response */
export interface RegistrationRequestResponse {
  id: number;
  userId: number;
  username: string;
  email?: string;
  fullName?: string;
  roleName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string | null;
  reviewedBy?: string | null;
  requestedAt: string;
  reviewedAt?: string | null;
}

/** POST /api/registration-requests/{id}/reject */
export interface RejectRegistrationRequest {
  reason: string;
}

/** GET /api/engineers */
export interface EngineerResponse {
  id?: number;
  userId: number;
  username?: string;
  fullName?: string;
  primarySpecialization?: string;
  activeTaskLimit?: number;
  skills?: SkillResponse[];
}

/** GET /api/engineers/me/skills */
export interface SkillResponse {
  id: number;
  userId: number;
  skillName: string;
}

/** POST /api/engineers/me/skills */
export interface AddSkillRequest {
  skillName: string;
}

/** PUT /api/engineers/{userId}/specialization */
export interface SpecializationRequest {
  specialization: string;
}

/** GET /api/notifications */
export interface NotificationItemResponse {
  id: number;
  userId: number;
  type: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

/** POST /api/users, PUT /api/users/{id} */
export interface UserRequest {
  username: string;
  password?: string;
  email?: string;
  fullName?: string;
  roleId: number;
  enabled?: boolean;
}

/** GET /api/users, /api/users/{id} */
export interface UserResponse {
  id: number;
  username: string;
  email?: string;
  fullName?: string;
  roleId: number;
  roleName: RoleName;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/** POST /api/roles, PUT /api/roles/{id} */
export interface RoleRequest {
  name: string;
  description?: string;
}

/** GET /api/roles, /api/roles/{id} */
export interface RoleResponse {
  id: number;
  name: string;
  description?: string;
}

/** GET /api/audit-logs */
export interface AuditLogResponse {
  id: number;
  userId: number;
  username: string;
  action: string;
  details?: string;
  ipAddress?: string;
  createdAt: string;
}

