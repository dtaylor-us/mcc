
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useApp } from '../App';
import { mcpService, agentService } from '../services/apiService';
import { Asset, WorkLog, ChatMessage, ManualPreview } from '../types';
import { PaperAirplaneIcon } from '../components/icons';

const AssetDetailScreen: React.FC = () => {
  const { id, qrCode } = useParams<{ id?: string, qrCode?: string }>();
  const navigate = useNavigate();
  const { currentAsset, setCurrentAsset, conversationId } = useApp();
  
  const [asset, setAsset] = useState<Asset | null>(currentAsset);
  const [workLogs, setWorkLogs] = useState<WorkLog[]>([]);
  const [manualPreview, setManualPreview] = useState<ManualPreview | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAssetDetails = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);

      let fetchedAsset: Asset;
      if (asset) {
        fetchedAsset = asset;
      } else if (id) {
        fetchedAsset = await mcpService.getAssetById(id);
      } else if (qrCode) {
        fetchedAsset = await mcpService.getAssetByQr(qrCode);
      } else {
        throw new Error("No asset identifier provided.");
      }

      setAsset(fetchedAsset);
      if (!currentAsset || currentAsset.id !== fetchedAsset.id) {
        setCurrentAsset(fetchedAsset);
      }

      const [logs, preview] = await Promise.all([
        mcpService.listWorkLogsForAsset(fetchedAsset.id),
        mcpService.getManualPreview(fetchedAsset.id)
      ]);
      setWorkLogs(logs);
      setManualPreview(preview);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred.');
      setAsset(null);
    } finally {
      setIsLoading(false);
    }
  }, [id, qrCode, asset, currentAsset, setCurrentAsset]);

  useEffect(() => {
    fetchAssetDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, qrCode]);

  const refreshWorkLogs = async () => {
    if (asset) {
      const logs = await mcpService.listWorkLogsForAsset(asset.id);
      setWorkLogs(logs);
    }
  };

  if (isLoading) return <div className="text-center p-8">Loading asset details...</div>;
  if (error) return <div className="bg-red-900 border border-red-700 text-red-200 px-4 py-3 rounded-md">{error}</div>;
  if (!asset) return <div className="text-center p-8">Asset not found. <button onClick={() => navigate('/')} className="text-cyan-400 hover:underline">Go Home</button></div>;

  return (
    <div className="space-y-6">
      <AssetInfo asset={asset} />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <WorkLogForm assetId={asset.id} onLogCreated={refreshWorkLogs} />
          <WorkLogTable workLogs={workLogs} />
        </div>
        <div className="lg:col-span-1 space-y-6">
          <ManualCard manualPreview={manualPreview} manualPath={asset.manualPath} />
          <AgentChat />
        </div>
      </div>
    </div>
  );
};

// Sub-components defined outside the main component body to prevent re-renders
const AssetInfo: React.FC<{asset: Asset}> = ({ asset }) => (
    <div className="bg-slate-800 p-6 rounded-lg shadow-lg flex flex-col md:flex-row items-start gap-6">
        <img src={asset.qrImageUrl} alt="QR Code" className="w-32 h-32 rounded-md bg-white p-1" />
        <div className="flex-grow">
            <h1 className="text-3xl font-bold text-white">{asset.name}</h1>
            <p className="text-lg text-slate-300">{asset.model}</p>
            <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-2 text-slate-400">
                <p><span className="font-semibold">Serial:</span> {asset.serialNumber}</p>
                <p><span className="font-semibold">Location:</span> {asset.location}</p>
            </div>
        </div>
    </div>
);

const ManualCard: React.FC<{manualPreview: ManualPreview | null, manualPath: string}> = ({ manualPreview, manualPath }) => (
  <div className="bg-slate-800 p-4 rounded-lg shadow-lg">
    <h3 className="text-lg font-semibold mb-2 text-white">Manual Preview</h3>
    <div className="bg-slate-900 p-3 rounded-md max-h-40 overflow-y-auto text-sm text-slate-300 mb-3">
      {manualPreview ? <pre className="whitespace-pre-wrap font-mono">{manualPreview.preview}</pre> : 'No preview available.'}
    </div>
    <a href={manualPath} target="_blank" rel="noopener noreferrer" className="text-cyan-400 hover:underline text-sm">Open Full Manual</a>
  </div>
);

const WorkLogForm: React.FC<{assetId: string; onLogCreated: () => void;}> = ({ assetId, onLogCreated }) => {
    const [formData, setFormData] = useState({ action: '', technician: '', durationMinutes: '', notes: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string|null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);
        try {
            await mcpService.createWorkLog({
                assetId,
                action: formData.action,
                technician: formData.technician,
                durationMinutes: Number(formData.durationMinutes),
                notes: formData.notes
            });
            setFormData({ action: '', technician: '', durationMinutes: '', notes: '' });
            onLogCreated();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to create work log");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="bg-slate-800 p-4 rounded-lg shadow-lg">
            <h3 className="text-lg font-semibold mb-4 text-white">Log Work</h3>
            {error && <div className="bg-red-900 text-red-200 p-2 rounded mb-4 text-sm">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <input name="action" value={formData.action} onChange={handleChange} placeholder="Action Performed" required className="bg-slate-700 p-2 rounded-md w-full" />
                    <input name="technician" value={formData.technician} onChange={handleChange} placeholder="Technician" required className="bg-slate-700 p-2 rounded-md w-full" />
                    <input name="durationMinutes" type="number" value={formData.durationMinutes} onChange={handleChange} placeholder="Duration (min)" required className="bg-slate-700 p-2 rounded-md w-full" />
                </div>
                <textarea name="notes" value={formData.notes} onChange={handleChange} placeholder="Notes..." rows={3} className="bg-slate-700 p-2 rounded-md w-full"></textarea>
                <button type="submit" disabled={isSubmitting} className="bg-cyan-600 hover:bg-cyan-500 text-white font-bold py-2 px-4 rounded-lg transition-colors disabled:bg-slate-500 w-full">
                    {isSubmitting ? 'Logging...' : 'Log Work'}
                </button>
            </form>
        </div>
    );
};

const WorkLogTable: React.FC<{workLogs: WorkLog[]}> = ({ workLogs }) => (
    <div className="bg-slate-800 p-4 rounded-lg shadow-lg">
        <h3 className="text-lg font-semibold mb-4 text-white">Work History</h3>
        <div className="overflow-x-auto max-h-96">
            <table className="min-w-full divide-y divide-slate-700">
                <thead className="bg-slate-700/50 sticky top-0">
                    <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-slate-300 uppercase">When</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-slate-300 uppercase">Tech</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-slate-300 uppercase">Action</th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-slate-300 uppercase">Min</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-700">
                    {workLogs.length > 0 ? workLogs.map(log => (
                        <tr key={log.id} className="hover:bg-slate-700">
                            <td className="px-4 py-2 text-sm text-slate-300">{new Date(log.createdAt).toLocaleString()}</td>
                            <td className="px-4 py-2 text-sm">{log.technician}</td>
                            <td className="px-4 py-2 text-sm">{log.action}</td>
                            <td className="px-4 py-2 text-sm">{log.durationMinutes}</td>
                        </tr>
                    )) : <tr><td colSpan={4} className="text-center py-4 text-slate-400">No work history found.</td></tr>}
                </tbody>
            </table>
        </div>
    </div>
);


const AgentChat: React.FC = () => {
    const { conversationId } = useApp();
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState('');
    const [isThinking, setIsThinking] = useState(false);

    const handleSend = async () => {
        if (!input.trim()) return;
        const userMessage: ChatMessage = { from: 'user', text: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsThinking(true);
        try {
            const response = await agentService.askAgent(input, conversationId);
            const assistantMessage: ChatMessage = { from: 'assistant', text: response.content, toolCalls: response.toolCalls };
            setMessages(prev => [...prev, assistantMessage]);
        } catch (error) {
            const errorMessage: ChatMessage = { from: 'assistant', text: 'Sorry, I encountered an error.' };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsThinking(false);
        }
    };

    return (
        <div className="bg-slate-800 rounded-lg shadow-lg flex flex-col h-[32rem]">
            <h3 className="text-lg font-semibold p-4 border-b border-slate-700 text-white">Ask the Ops Agent</h3>
            <div className="flex-grow p-4 space-y-4 overflow-y-auto">
                {messages.map((msg, index) => (
                    <div key={index} className={`flex ${msg.from === 'user' ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-xs md:max-w-sm rounded-lg px-4 py-2 ${msg.from === 'user' ? 'bg-cyan-700 text-white' : 'bg-slate-700 text-slate-200'}`}>
                            {msg.text}
                        </div>
                    </div>
                ))}
                {isThinking && <div className="text-slate-400 text-sm">Agent is thinking...</div>}
            </div>
            <div className="p-4 border-t border-slate-700 flex gap-2">
                <input
                    type="text"
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyPress={e => e.key === 'Enter' && handleSend()}
                    placeholder="e.g., Log filter change..."
                    className="flex-grow bg-slate-700 p-2 rounded-md"
                    disabled={isThinking}
                />
                <button onClick={handleSend} disabled={isThinking} className="bg-cyan-600 hover:bg-cyan-500 text-white p-2 rounded-md disabled:bg-slate-500">
                    <PaperAirplaneIcon className="h-5 w-5" />
                </button>
            </div>
        </div>
    );
};

export default AssetDetailScreen;
