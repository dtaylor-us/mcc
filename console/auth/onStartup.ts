// console/auth/onStartup.ts
import { msalInstance } from '../authConfig';

export async function initMsalActiveAccount() {
  await msalInstance.initialize();

  const active = msalInstance.getActiveAccount();
  if (!active) {
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length > 0) {
      msalInstance.setActiveAccount(accounts[0]);
    }
  }
}
