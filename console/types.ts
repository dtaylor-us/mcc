
export interface Asset {
  id: string;
  qrCode: string;
  name: string;
  model: string;
  serialNumber: string;
  location: string;
  manualPath: string;
  qrImageUrl: string;
}

export interface WorkLog {
  id: string;
  assetId: string;
  action: string;
  technician: string;
  durationMinutes: number;
  notes: string;
  createdAt: string;
}

export interface ChatMessage {
  from: 'user' | 'assistant';
  text: string;
  toolCalls?: any[];
}
