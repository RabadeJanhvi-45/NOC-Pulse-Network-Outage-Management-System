export type IncidentStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type IncidentPriority = 'P1' | 'P2' | 'P3' | 'P4' | 'Critical' | 'High' | 'Medium' | 'Low';

/**
  * Incident creation/update DTO.
  */
 export interface IncidentRequest {
  deviceId?: string;
  alarmId?: number;
  description?: string;
  priority: IncidentPriority;
  status?: IncidentStatus;
  resolutionNotes?: string;
}

/** GET /api/incidents, /api/incidents/{id} */
export interface IncidentResponse {
  id: number;
  deviceId: string;
  alarmId?: number;
  description?: string;
  priority: IncidentPriority;
  status: IncidentStatus;
  resolutionNotes?: string;
  rootCause?: string;
  resolutionSteps?: string;
  assignedEngineer?: string;
  assigneeId?: string;
  assignedEngineerUsername?: string;
  assignedEngineerSpecialization?: string;
  createdBy?: string;
  createdAt: string;
  closedAt?: string;
  closedBy?: string;
}

/** GET /api/incidents/{id}/history */
export interface IncidentHistoryResponse {
  id: number;
  incidentId: number;
  fieldChanged: string;
  oldValue?: string;
  newValue?: string;
  changedBy?: string;
  changedAt: string;
}

/** GET /api/incidents/{id}/assignments */
export interface AssignmentResponse {
  id: number;
  incidentId: number;
  assigneeId: string;
  assigneeType: string;
  assignedAt: string;
  isCurrent: boolean;
}

/** POST /api/incidents/{id}/notes */
export interface ProgressNoteRequest {
  noteText: string;
  authorId?: string;
}

/** GET /api/incidents/{id}/notes */
export interface ProgressNoteResponse {
  id: number;
  incidentId: number;
  noteText: string;
  authorId: string;
  createdAt: string;
}

/** POST /api/incidents/{id}/resolve */
export interface ResolveIncidentRequest {
  resolutionNotes: string;
  rootCause?: string;
  resolutionSteps?: string;
}

/** POST /api/incidents/{id}/reject-assignment */
export interface RejectAssignmentRequest {
  reason: string;
}

/** POST /api/incidents/{id}/reject-resolution */
export interface RejectResolutionRequest {
  reason: string;
  verificationNotes?: string;
}

/** POST /api/incidents/{id}/decide-rejection */
export interface DecideRejectionRequest {
  decision?: 'APPROVE' | 'REJECT';
  approved?: boolean;
}

/** GET /api/incidents/{id}/sla */
export interface SlaResponse {
  incidentId: number;
  slaThreshold?: number;
  resolutionTime?: number;
  isBreached?: boolean;
}

/** Query parameters for GET /api/incidents */
export interface IncidentFilterParams {
  status?: string;
  priority?: string;
}

