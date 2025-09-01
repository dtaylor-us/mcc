import { Asset, WorkLog } from '../types';
import { authorizedFetch } from '../lib/authorizedFetch';
import { SCOPES } from '../constants/scopes';

const MCP_BASE_URL = process.env.MCP_BASE_URL || 'http://localhost:8081';
const AGENT_BASE_URL = process.env.AGENT_BASE_URL || 'http://localhost:8080';

const handleResponse = async <T,>(response: Response): Promise<T> => {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`API Error: ${response.status} ${response.statusText} - ${errorText}`);
  }
  return response.json() as Promise<T>;
};

// ---------- MCP Server (Assets + WorkLogs) ----------
export const mcpService = {
  createAsset: async (data: Omit<Asset, 'id' | 'qrCode' | 'qrImageUrl'>): Promise<Asset> => {
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/assets/v1`, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: { 'Content-Type': 'application/json' },
    }, SCOPES.MCP_READ);
    return handleResponse<Asset>(res);
  },

  getAssetById: async (id: string): Promise<Asset> => {
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/assets/v1/${id}`, {}, SCOPES.MCP_READ);
    return handleResponse<Asset>(res);
  },

  getAssetByQr: async (qrCode: string): Promise<Asset> => {
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/assets/v1/by-qr/${qrCode}`, {}, SCOPES.MCP_READ);
    return handleResponse<Asset>(res);
  },

  listAssets: async (query = '', page = 0, size = 20): Promise<Asset[]> => {
    const params = new URLSearchParams({ query, page: String(page), size: String(size) });
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/assets/v1?${params}`, {}, SCOPES.MCP_READ);
    return handleResponse<Asset[]>(res);
  },

  listWorkLogsForAsset: async (assetId: string): Promise<WorkLog[]> => {
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/worklogs/v1?assetId=${assetId}`, {}, SCOPES.MCP_READ);
    return handleResponse<WorkLog[]>(res);
  },

  createWorkLog: async (data: Omit<WorkLog, 'id' | 'createdAt'>): Promise<WorkLog> => {
    const res = await authorizedFetch(`${MCP_BASE_URL}/api/worklogs/v1`, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: { 'Content-Type': 'application/json' },
    }, SCOPES.MCP_READ);
    return handleResponse<WorkLog>(res);
  },
};

// ---------- Agent App (Chat) ----------
export const agentService = {
  askAgent: async (userMessage: string, conversationId: string): Promise<{ content: string; toolCalls: any[] }> => {
    const res = await authorizedFetch(`${AGENT_BASE_URL}/agent/ask`, {
      method: 'POST',
      body: JSON.stringify({ userMessage, conversationId }),
      headers: { 'Content-Type': 'application/json' },
    }, SCOPES.AGENT_READ);
    return handleResponse<{ content: string; toolCalls: any[] }>(res);
  },
};
