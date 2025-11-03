import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const mode = (localStorage.getItem('auth_mode') || 'default').toLowerCase();
  let authReq = req;

  // Skip authentication for public endpoints (mode, login, validate-basic)
  const publicEndpoints = ['/api/auth/users/mode', '/api/auth/users/login', '/api/auth/validate-basic'];
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));

  if (req.url.startsWith('/api') && !isPublicEndpoint) {
    if (mode === 'jwt') {
      const token = localStorage.getItem('jwt_token');
      if (token) {
        authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
        console.debug('[AuthInterceptor] Added JWT token to request:', req.url.substring(0, 50));
      } else {
        console.warn('[AuthInterceptor] No JWT token found for request:', req.url);
      }
    } else if (mode === 'basic') {
      const b64 = localStorage.getItem('basic_b64');
      if (b64) {
        authReq = req.clone({ setHeaders: { Authorization: `Basic ${b64}` } });
      }
    }
  }

  return next(authReq);
};


