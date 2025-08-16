
import React, { useState, createContext, useContext, ReactNode, useMemo } from 'react';
import { HashRouter, Routes, Route, useParams } from 'react-router-dom';
import Header from './components/Header';
import HomeScreen from './screens/HomeScreen';
import AssetDetailScreen from './screens/AssetDetailScreen';
import AdminScreen from './screens/AdminScreen';
import { Asset } from './types';

interface AppContextType {
  currentAsset: Asset | null;
  setCurrentAsset: (asset: Asset | null) => void;
  conversationId: string;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};

const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [currentAsset, setCurrentAsset] = useState<Asset | null>(null);
  const conversationId = useMemo(() => `ops-session-${Math.random().toString(36).substring(2, 10)}`, []);

  const value = {
    currentAsset,
    setCurrentAsset,
    conversationId,
  };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

function App() {
  return (
    <AppProvider>
      <HashRouter>
        <div className="min-h-screen flex flex-col">
          <Header />
          <main className="flex-grow container mx-auto p-4 md:p-6">
            <Routes>
              <Route path="/" element={<HomeScreen />} />
              <Route path="/asset/id/:id" element={<AssetDetailScreen />} />
              <Route path="/asset/qr/:qrCode" element={<AssetDetailScreen />} />
              <Route path="/admin/new-asset" element={<AdminScreen />} />
            </Routes>
          </main>
        </div>
      </HashRouter>
    </AppProvider>
  );
}

export default App;
