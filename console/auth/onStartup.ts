import { msalInstance } from '../authConfig';

export function initMsalActiveAccount() {
  const active = msalInstance.getActiveAccount();
  if (!active) {
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length > 0) {
      msalInstance.setActiveAccount(accounts[0]);
    }
  }
}
