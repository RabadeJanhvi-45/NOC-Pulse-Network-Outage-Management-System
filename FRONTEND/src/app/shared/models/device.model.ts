/** Active / Inactive / Faulty */
export type DeviceStatus = 'Active' | 'Inactive' | 'Faulty';

/** Healthy / Degraded / Critical */
export type DeviceHealth = 'Healthy' | 'Degraded' | 'Critical';

/** POST /api/devices (register), PUT /api/devices/{id} (edit) */
export interface DeviceRequest {
  deviceId: string;
  deviceType: string;
  name: string;
  ipAddress?: string;
  location?: string;
  region?: string;
  status?: DeviceStatus;
  health?: DeviceHealth;
  createdBy?: string;
}

/** GET /api/devices, /api/devices/{id} */
export interface DeviceResponse {
  id: number;
  deviceId: string;
  deviceType: string;
  name: string;
  ipAddress?: string;
  location?: string;
  region?: string;
  status: DeviceStatus;
  health: DeviceHealth;
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
}

/** GET /api/devices/{id}/history */
export interface DeviceHistoryResponse {
  id: number;
  deviceId: number;
  changedField: string;
  oldValue?: string;
  newValue?: string;
  changedBy?: string;
  changedAt: string;
}

/** POST /api/device-requests, GET /api/device-requests */
export interface DeviceChangeRequest {
  id?: number;
  deviceId: string;
  deviceType: string;
  name: string;
  ipAddress?: string;
  location?: string;
  region?: string;
  requestedBy?: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string | null;
  reviewedBy?: string | null;
  requestedAt?: string;
  reviewedAt?: string | null;
}

/** GET /api/device-type-specializations */
export interface DeviceTypeSpecialization {
  id: number;
  deviceType: string;
  requiredSpecialization: string;
}

/** Query parameters for GET /api/devices */
export interface DeviceFilterParams {
  region?: string;
  status?: string;
  deviceType?: string;
  search?: string;
}

