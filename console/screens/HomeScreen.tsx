
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
              placeholder="Search by name, model, serial, or location"
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
          <div className="overflow-x-auto bg-slate-800 rounded-lg">
            <table className="min-w-full divide-y divide-slate-700">
              <thead className="bg-slate-700/50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Name</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Model</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Serial Number</th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Location</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700">
                {searchResults.map((asset) => (
                  <tr key={asset.id} onClick={() => handleRowClick(asset)} className="hover:bg-slate-700 cursor-pointer">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-white">{asset.name}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-300">{asset.model}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-300">{asset.serialNumber}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-300">{asset.location}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomeScreen;
