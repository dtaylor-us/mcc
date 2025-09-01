import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { PlusCircleIcon, HomeIcon, ChatMessageIcon } from './icons';
import AgentChatDock from '../components/AgentChatDock';
import LoginButton from './LoginButton';

const Header: React.FC = () => {
  const linkClass =
    "relative flex items-center gap-2 px-3 py-2 text-slate-300 hover:bg-slate-700 hover:text-white rounded-md transition-colors";
  const activeLinkClass = "bg-slate-700 text-white";

  const [isChatOpen, setIsChatOpen] = useState(false);
  const [unread, setUnread] = useState(0);

return (
  <header className="bg-slate-800 shadow-md">
    <nav className="container mx-auto flex justify-between items-center p-4">
      <NavLink to="/" className="flex items-center gap-3">
        <h1 className="text-xl font-bold text-white">Ops Maintenance Console</h1>
      </NavLink>
      {/* group nav links and chat button */}
      <div className="flex items-center gap-2">
        <NavLink
          to="/"
          className={({ isActive }) =>
            `${linkClass} ${isActive ? activeLinkClass : ''}`
          }
        >
          <HomeIcon className="h-5 w-5" />
          <span className="hidden md:inline">Home</span>
        </NavLink>
        <NavLink
          to="/admin/new-asset"
          className={({ isActive }) =>
            `${linkClass} ${isActive ? activeLinkClass : ''}`
          }
        >
          <PlusCircleIcon className="h-5 w-5" />
          <span className="hidden md:inline">Create Asset</span>
        </NavLink>
        <button
          onClick={() => setIsChatOpen(true)}
          className="relative flex items-center gap-2 px-4 py-2 rounded-md bg-cyan-600 hover:bg-cyan-500 text-white font-medium transition-colors"
          aria-haspopup="dialog"
          aria-expanded={isChatOpen}
          aria-controls="agent-chat-dock"
        >
          <ChatMessageIcon className="h-5 w-5" />
          <span className="hidden md:inline">Ask Agent</span>
          {unread > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-600 text-white text-xs font-bold rounded-full px-2 py-0.5">
              {unread > 99 ? '99+' : unread}
            </span>
          )}
        </button>
      </div>
      {/* login/logout button on the far right */}
      <LoginButton />
    </nav>
    <AgentChatDock
      id="agent-chat-dock"
      isOpen={isChatOpen}
      onClose={() => setIsChatOpen(false)}
      onUnreadChange={setUnread}
    />
  </header>
);
};

export default Header;
