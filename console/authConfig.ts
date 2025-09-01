// console/authConfig.ts
import { PublicClientApplication } from '@azure/msal-browser';

export const msalConfig = {
  auth: {
    clientId: '3917e656-52e0-4c9d-a81c-a6e27dd98eb7',                                  // SPA registration
    authority: 'https://login.microsoftonline.com/c07f229d-ff5a-4a54-9be4-1e37e3783cdd',   // Tenant
    redirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
  },
};

export const msalInstance = new PublicClientApplication(msalConfig);
