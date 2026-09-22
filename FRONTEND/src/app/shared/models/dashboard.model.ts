import { AlarmResponse } from './alarm.model';
import { IncidentResponse } from './incident.model';

/** GET /api/dashboard/summary */
export interface DashboardSummaryResponse {
  totalDevices: number;
  activeAlarms: number;
  openIncidents: number;
  pendingRegistrationRequests?: number;
  pendingDeviceRequests?: number;
  generatedAt?: string;
}

/**
 * GET /api/dashboard/alarms-by-severity, /api/dashboard/incidents-by-status
 */
export interface GroupedCountResponse {
  groupedBy?: string;
  counts: Record<string, number>;
  total?: number;
}

/** GET /api/reports/outages */
export interface OutageReportResponse {
  from?: string;
  to?: string;
  totalAlarmsRaised?: number;
  alarmsBySeverity?: Record<string, number>;
  totalIncidentsCreated?: number;
  incidentsResolved?: number;
  incidentsByStatus?: Record<string, number>;
  generatedAt?: string;
  [key: string]: any;
}

/** GET /api/reports/device-history/{deviceId} */
export interface DeviceHistoryReportResponse {
  deviceId: string;
  deviceName?: string;
  deviceStatus?: string;
  alarms?: AlarmResponse[];
  incidents?: IncidentResponse[];
  [key: string]: any;
}

