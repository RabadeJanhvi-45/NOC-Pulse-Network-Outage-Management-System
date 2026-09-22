/** The three seeded roles according to NOC Pulse specification */
export type RoleName = 'ADMIN' | 'NOC_OPERATOR' | 'ENGINEER';

/** Shape returned by GlobalExceptionHandler across every backend service. */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  path?: string;
  timestamp?: string;
  fieldErrors?: Record<string, string>;
}

