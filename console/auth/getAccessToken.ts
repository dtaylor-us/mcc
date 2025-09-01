// console/auth/getAccessToken.ts
import { msalInstance } from '../authConfig';

export async function getAccessToken(scope: string): Promise<string> {
  let account = msalInstance.getActiveAccount();
  if (!account) {
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length === 0) {
      const loginRes = await msalInstance.loginPopup({ scopes: [scope] });
      account = loginRes.account!;
      msalInstance.setActiveAccount(account);
    } else {
      account = accounts[0];
      msalInstance.setActiveAccount(account);
    }
  }

  const base = { scopes: [scope], account, authority: 'https://login.microsoftonline.com/c07f229d-ff5a-4a54-9be4-1e37e3783cdd' as const };

  try {
    // Force a round-trip to the token endpoint (bypass cached AT)
    const res = await msalInstance.acquireTokenSilent({ ...base, forceRefresh: true });
    return res.accessToken;
  } catch {
    const res = await msalInstance.acquireTokenPopup({ ...base });
    return res.accessToken;
  }
}
