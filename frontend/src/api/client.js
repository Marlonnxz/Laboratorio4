// API Client configured for Gateway communication with automatic cloud resolution

function resolveDefaultGatewayUrl() {
  if (typeof window !== 'undefined' && window.__GATEWAY_URL__) {
    return window.__GATEWAY_URL__;
  }
  const saved = typeof window !== 'undefined' ? localStorage.getItem('custom_gateway_url') : null;
  if (saved) return saved;

  const envUrl = import.meta.env.VITE_API_GATEWAY_URL;
  if (envUrl && envUrl !== 'http://localhost:8080') {
    return envUrl;
  }

  // Automatic cloud detection for Azure Container Apps
  if (typeof window !== 'undefined' && window.location) {
    const host = window.location.hostname;
    if (host.includes('.azurecontainerapps.io') && host.startsWith('frontend.')) {
      return window.location.origin.replace('frontend.', 'api-gateway.');
    }
  }

  return envUrl || 'http://localhost:8080';
}

let gatewayBaseUrl = resolveDefaultGatewayUrl();

// Clean trailing slash
if (gatewayBaseUrl.endsWith('/')) {
  gatewayBaseUrl = gatewayBaseUrl.slice(0, -1);
}

export function getGatewayUrl() {
  return gatewayBaseUrl;
}

export function setGatewayUrl(newUrl) {
  gatewayBaseUrl = newUrl.endsWith('/') ? newUrl.slice(0, -1) : newUrl;
  localStorage.setItem('custom_gateway_url', gatewayBaseUrl);
}

export const TOKEN_KEY = 'acad_jwt_token';
export const USER_KEY = 'acad_user_info';

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function getStoredUser() {
  const raw = sessionStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function storeAuth(token, user) {
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

// Telemetry listener for X-Cache and latency diagnostics
const telemetryListeners = new Set();

export function onTelemetry(callback) {
  telemetryListeners.add(callback);
  return () => telemetryListeners.delete(callback);
}

function broadcastTelemetry(event) {
  telemetryListeners.forEach((listener) => {
    try {
      listener(event);
    } catch (e) {
      console.error('Telemetry listener error:', e);
    }
  });
}

export async function apiRequest(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${gatewayBaseUrl}${endpoint}`;
  const token = getStoredToken();

  const headers = new Headers(options.headers || {});

  // Attach JWT if available
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  // Do not set Content-Type if uploading FormData (browser sets boundary)
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const startTime = performance.now();
  let response;
  let durationMs = 0;
  let errorObj = null;

  try {
    response = await fetch(url, {
      ...options,
      headers,
    });
    durationMs = Math.round(performance.now() - startTime);
  } catch (err) {
    durationMs = Math.round(performance.now() - startTime);
    broadcastTelemetry({
      method: options.method || 'GET',
      url,
      status: 0,
      statusText: 'Network Error',
      durationMs,
      cacheHeader: 'ERR',
      timestamp: new Date().toLocaleTimeString(),
    });
    throw new Error(`Error al conectar con el Gateway (${url}): ${err.message}`);
  }

  const cacheHeader = response.headers.get('x-cache') || response.headers.get('X-Cache') || 'NONE';

  // Broadcast to telemetry bar
  broadcastTelemetry({
    method: options.method || 'GET',
    url,
    status: response.status,
    statusText: response.statusText,
    durationMs,
    cacheHeader,
    timestamp: new Date().toLocaleTimeString(),
  });

  // Parse JSON or text
  let responseData = null;
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    try {
      responseData = await response.json();
    } catch {
      responseData = null;
    }
  } else {
    responseData = await response.text();
  }

  if (!response.ok) {
    const errorMsg = responseData?.message || responseData?.error || `HTTP ${response.status}: ${response.statusText}`;
    const error = new Error(errorMsg);
    error.status = response.status;
    error.data = responseData;
    error.cacheHeader = cacheHeader;
    error.durationMs = durationMs;
    throw error;
  }

  return {
    status: response.status,
    data: responseData,
    headers: response.headers,
    cacheHeader,
    durationMs,
  };
}

