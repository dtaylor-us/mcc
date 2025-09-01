// console/components/LoginButton.tsx
import React from 'react';
import { useMsal } from '@azure/msal-react';
import { LoginIcon, LogoutIcon } from './icons';

const LoginButton: React.FC = () => {
  const { instance } = useMsal();
  const activeAccount = instance.getActiveAccount();

  const handleLogin = async () => {
    const result = await instance.loginPopup({
      scopes: [
        'api://26b96d20-8366-4eb8-a0fb-401b77582830/Read.access',
        'api://3f06f8f3-5bf1-48e3-a48e-c3f070c6be5f/MCP.Read',
      ],
    });
    if (result?.account) {
      instance.setActiveAccount(result.account);
    }
  };

  const handleLogout = async () => {
    await instance.logoutPopup({ postLogoutRedirectUri: window.location.origin });
  };

  return activeAccount ? (
    <button
      onClick={handleLogout}
      className="flex items-center gap-2 px-4 py-2 ml-6 rounded-md bg-red-600 hover:bg-red-500 text-white font-medium transition-colors"
    >
      <LogoutIcon className="h-5 w-5" />
      <span className="hidden md:inline">Sign&nbsp;Out</span>
    </button>
  ) : (
    <button
      onClick={handleLogin}
      className="flex items-center gap-2 px-4 py-2 ml-6 rounded-md bg-emerald-600 hover:bg-emerald-500 text-white font-medium transition-colors"
    >
      <LoginIcon className="h-5 w-5" />
      <span className="hidden md:inline">Sign&nbsp;In</span>
    </button>
  );
};

export default LoginButton;
