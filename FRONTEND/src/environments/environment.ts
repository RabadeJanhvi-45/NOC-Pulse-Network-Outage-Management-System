export const environment = {
  production: false,
  // api-gateway (port 8080) — every request goes through here, never
  // directly to a business service. See backend README route map.
  apiBaseUrl: 'http://localhost:8080/api',
};
