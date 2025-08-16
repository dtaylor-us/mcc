
import { Asset, WorkLog, ManualPreview } from '../types';

const MCP_BASE_URL = process.env.MCP_BASE_URL || 'http://localhost:8081';
const AGENT_BASE_URL = process.env.AGENT_BASE_URL || 'http://localhost:8080';
const AGENT_USER = process.env.AGENT_USER || 'agent';
const AGENT_PASS = process.env.AGENT_PASS || 'password';

const handleResponse = async <T,>(response: Response): Promise<T> => {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`API Error: ${response.status} ${response.statusText} - ${errorText}`);
  }
  return response.json() as Promise<T>;
};

// MCP Server (Assets)
export const mcpService = {
  createAsset: (data: Omit<Asset, 'id' | 'qrCode' | 'qrImageUrl'>): Promise<Asset> => {
    return fetch(`${MCP_BASE_URL}/api/assets/v1`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<Asset>(res));
  },

  getAssetById: (id: string): Promise<Asset> => {
    return fetch(`${MCP_BASE_URL}/api/assets/v1/${id}`).then(res => handleResponse<Asset>(res));
  },

  getAssetByQr: (qrCode: string): Promise<Asset> => {
    return fetch(`${MCP_BASE_URL}/api/assets/v1/by-qr/${qrCode}`).then(res => handleResponse<Asset>(res));
  },

  listAssets: (query: string = '', page: number = 0, size: number = 20): Promise<Asset[]> => {
    const params = new URLSearchParams({ query, page: page.toString(), size: size.toString() });
    return fetch(`${MCP_BASE_URL}/api/assets/v1?${params}`).then(res => handleResponse<Asset[]>(res));
  },

  listWorkLogsForAsset: (assetId: string): Promise<WorkLog[]> => {
    return fetch(`${MCP_BASE_URL}/api/worklogs/v1?assetId=${assetId}`).then(res => handleResponse<WorkLog[]>(res));
  },

  createWorkLog: (data: Omit<WorkLog, 'id' | 'createdAt'>): Promise<WorkLog> => {
    return fetch(`${MCP_BASE_URL}/api/worklogs/v1`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<WorkLog>(res));
  },

  getManualPreview: (assetId: string): Promise<ManualPreview> => {
    // This endpoint is inferred from the blueprint
    return fetch(`${MCP_BASE_URL}/api/assets/v1/${assetId}/manual/preview`)
      .then(res => handleResponse<ManualPreview>(res));
  },
};

// Agent App (Chat)
export const agentService = {
  askAgent: (userMessage: string, conversationId: string): Promise<{ content: string, toolCalls: any[] }> => {
    const headers = new Headers();
    headers.append('Content-Type', 'application/json');
    headers.append('Authorization', 'Basic ' + btoa(`${AGENT_USER}:${AGENT_PASS}`));

    return fetch(`${AGENT_BASE_URL}/agent/ask`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ userMessage, conversationId }),
    }).then(res => handleResponse<{ content: string, toolCalls: any[] }>(res));
  },
};
