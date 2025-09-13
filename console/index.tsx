// console/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { MsalProvider } from '@azure/msal-react';
import { msalInstance } from './authConfig';
import { initMsalActiveAccount } from './auth/onStartup';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Could not find root element to mount to');
}

async function bootstrap() {
  // Initialise MSAL and set the active account if one exists
  await initMsalActiveAccount();

  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <MsalProvider instance={msalInstance}>
        <App />
      </MsalProvider>
    </React.StrictMode>
  );
}

bootstrap().catch(err => {
  console.error('Error initialising MSAL', err);
});
