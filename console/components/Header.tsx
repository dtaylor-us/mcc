
import React from 'react';
import { NavLink } from 'react-router-dom';
import { WrenchScrewdriverIcon, PlusCircleIcon, HomeIcon } from './icons';

const Header: React.FC = () => {
  const linkClass = "flex items-center gap-2 px-3 py-2 text-slate-300 hover:bg-slate-700 hover:text-white rounded-md transition-colors";
  const activeLinkClass = "bg-slate-700 text-white";

  return (
    <header className="bg-slate-800 shadow-md">
      <nav className="container mx-auto flex justify-between items-center p-4">
        <NavLink to="/" className="flex items-center gap-3">
          <WrenchScrewdriverIcon className="h-8 w-8 text-cyan-400" />
          <h1 className="text-xl font-bold text-white">Ops Maintenance Console</h1>
        </NavLink>
        <div className="flex items-center gap-2">
          <NavLink to="/" className={({ isActive }) => `${linkClass} ${isActive ? activeLinkClass : ''}`}>
             <HomeIcon className="h-5 w-5" />
            <span className="hidden md:inline">Home</span>
          </NavLink>
          <NavLink to="/admin/new-asset" className={({ isActive }) => `${linkClass} ${isActive ? activeLinkClass : ''}`}>
            <PlusCircleIcon className="h-5 w-5" />
            <span className="hidden md:inline">Create Asset</span>
          </NavLink>
        </div>
      </nav>
    </header>
  );
};

export default Header;
