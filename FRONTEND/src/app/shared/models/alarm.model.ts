export type AlarmSeverity = 'Critical' | 'Major' | 'Minor' | 'Warning';

/** Active / Acknowledged / Cleared */
export type AlarmStatus = 'Active' | 'Acknowledged' | 'Cleared';

/** POST /api/alarms */
export interface AlarmRequest {
  deviceId: string;
  alarmType: string;
  severity?: AlarmSeverity;
  raisedBy?: string;
}

/** GET /api/alarms, /api/alarms/{id}, response of POST /api/alarms */
export interface AlarmResponse {
  id: number;
  deviceId: string;
  alarmType: string;
  severity: AlarmSeverity;
  status: AlarmStatus;
  groupKey?: string;
  raisedBy?: string;
  raisedAt: string;
  deduplicated?: boolean;
}

/** Response of POST /api/alarms/{id}/acknowledge */
export interface AlarmAcknowledgementResponse {
  id: number;
  alarmId: number;
  acknowledgedBy: string;
  acknowledgedAt: string;
}

/** POST /api/alarms/rules, PUT /api/alarms/rules/{id} */
export interface SeverityRuleRequest {
  alarmType: string;
  severity: AlarmSeverity;
  conditionLogic?: string;
}

/** GET /api/alarms/rules */
export interface SeverityRuleResponse {
  id: number;
  alarmType: string;
  severity: AlarmSeverity;
  conditionLogic?: string;
}

/** Query parameters for GET /api/alarms */
export interface AlarmFilterParams {
  severity?: string;
  status?: string;
  deviceId?: string;
}

