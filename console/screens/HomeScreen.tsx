import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import QrScanner from '../components/QrScanner';
import { mcpService } from '../services/apiService';
import { Asset } from '../types';
import { useApp } from '../App';

const HomeScreen: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Asset[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { setCurrentAsset } = useApp();

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      const results = await mcpService.listAssets(searchQuery);
      setSearchResults(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred during search.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleScan = useCallback(async (qrCode: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const asset = await mcpService.getAssetByQr(qrCode);
      setCurrentAsset(asset);
      navigate(`/asset/id/${asset.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to find asset for the scanned QR code.');
      setIsLoading(false);
    }
  }, [navigate, setCurrentAsset]);

  const handleRowClick = (asset: Asset) => {
    setCurrentAsset(asset);
    navigate(`/asset/id/${asset.id}`);
  };

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <QrScanner onScan={handleScan} onError={setError} />

        <div className="p-4 bg-slate-800 rounded-lg">
          <h2 className="text-2xl font-bold mb-4 text-slate-100">Search Assets</h2>
          <form onSubmit={handleSearch} className="space-y-4">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by name, model, serial, or brand"
              className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-cyan-600 hover:bg-cyan-500 text-white font-bold py-2 px-4 rounded-lg transition-colors disabled:bg-slate-500"
            >
              {isLoading ? 'Searching...' : 'Search'}
            </button>
          </form>
        </div>
      </div>

      {error && <div className="bg-red-900 border border-red-700 text-red-200 px-4 py-3 rounded-md">{error}</div>}

      <div>
        <h3 className="text-xl font-semibold mb-4">Search Results</h3>
        {isLoading && searchResults.length === 0 && <p>Loading...</p>}
        {!isLoading && searchResults.length === 0 && <p className="text-slate-400">No assets found. Try a new search.</p>}

        {searchResults.length > 0 && (
          <div className="bg-slate-800 rounded-lg">
            {/* Desktop/tablet table */}
            <div className="hidden md:block overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-700">
                <thead className="bg-slate-700/50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Name</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Model</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Serial Number</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">brand</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700">
                  {searchResults.map((asset) => (
                    <tr key={asset.id} className="hover:bg-slate-700">
                      {/* Make the full row a real button for reliable taps/clicks */}
                      <td colSpan={4} className="p-0">
                        <button
                          type="button"
                          onClick={() => handleRowClick(asset)}
                          className="w-full text-left px-6 py-4 flex flex-wrap gap-x-6 gap-y-1 items-center focus:outline-none focus:ring-2 focus:ring-cyan-500"
                        >
                          <span className="text-sm font-medium text-white min-w-[10rem] truncate">{asset.name}</span>
                          <span className="text-sm font-medium text-white min-w-[10rem] truncate">{asset.assetType}</span>
                          <span className="text-sm text-slate-300 min-w-[10rem] truncate">{asset.brand}</span>
                          <span className="text-sm text-slate-300 min-w-[8rem] truncate">{asset.model}</span>
                          <span className="text-sm text-slate-300 min-w-[10rem] truncate">{asset.serialNumber}</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile columnar list */}
            <ul className="md:hidden divide-y divide-slate-700">
              {searchResults.map((asset) => (
                <li key={asset.id} className="p-0">
                  <button
                    type="button"
                    onClick={() => handleRowClick(asset)}
                    className="w-full text-left px-4 py-4 min-h-[48px] active:bg-slate-700/70 focus:outline-none focus:ring-2 focus:ring-cyan-500"
                    aria-label={`Open asset ${asset.name}`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="text-white font-semibold truncate">{asset.name}</div>
                        <div className="text-white font-semibold truncate">{asset.assetType}</div>
                        <div className="text-slate-300 text-sm truncate">
                          {asset.model ?? '—'} · {asset.serialNumber ?? '—'}
                        </div>
                        <div className="text-slate-400 text-xs truncate">{asset.brand ?? '—'}</div>
                      </div>
                      <span className="shrink-0 text-slate-400 text-sm">View ›</span>
                    </div>
                  </button>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomeScreen;
