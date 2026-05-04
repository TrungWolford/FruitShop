import axiosInstance from '../libs/axios';

const ADMIN_BASE = '/api/admin';
const AI_SERVICE_BASE = '/ai-service/api/admin';

export type AiLanguage = 'vi' | 'en' | 'auto';

export interface AiConfigPayload {
  name: string;
  systemPrompt: string;
  style: number;
  length: number;
  sales: number;
  language: AiLanguage;
}

export interface RuleItemPayload {
  content: string;
  priority: number;
  scope: 'all' | 'checkout' | 'product';
  is_active: boolean;
}

export interface RuleItem extends RuleItemPayload {
  id: string;
}

export interface RagSource {
  id: string;
  name: string;
  status: 'indexed' | 'indexing' | 'failed' | string;
  documentCount?: number;
  relevanceScore?: number;
  updatedAt?: string;
}

export interface RagSourcesResponse {
  sources: RagSource[];
  totalDocuments?: number;
  averageRelevanceScore?: number;
}

export interface TestChatResponse {
  reply: string;
  tools_called?: string[];
  rag_sources_used?: string[];
  latency_ms?: number;
  config_name?: string;
}

export const adminApi = {
  saveAiConfig: async (payload: AiConfigPayload) => {
    const response = await axiosInstance.put(`${ADMIN_BASE}/ai-config`, payload);
    return response.data;
  },

  getRules: async (): Promise<RuleItem[]> => {
    const response = await axiosInstance.get(`${ADMIN_BASE}/rules`);
    if (Array.isArray(response.data)) {
      return response.data as RuleItem[];
    }
    if (response.data && Array.isArray(response.data.content)) {
      return response.data.content as RuleItem[];
    }
    return [];
  },

  createRule: async (payload: RuleItemPayload) => {
    const response = await axiosInstance.post(`${ADMIN_BASE}/rules`, payload);
    return response.data as RuleItem;
  },

  toggleRule: async (id: string, isActive: boolean) => {
    const response = await axiosInstance.patch(`${ADMIN_BASE}/rules/${id}`, {
      is_active: isActive
    });
    return response.data as RuleItem;
  },

  deleteRule: async (id: string) => {
    await axiosInstance.delete(`${ADMIN_BASE}/rules/${id}`);
  },

  getRagSources: async (): Promise<RagSourcesResponse> => {
    const response = await axiosInstance.get(`${AI_SERVICE_BASE}/rag-sources`);
    if (Array.isArray(response.data)) {
      return { sources: response.data as RagSource[] };
    }
    if (response.data && Array.isArray(response.data.sources)) {
      return response.data as RagSourcesResponse;
    }
    return { sources: [] };
  },

  uploadRagSource: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axiosInstance.post(`${AI_SERVICE_BASE}/rag-sources/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  reindexRagSource: async (id: string) => {
    const response = await axiosInstance.post(`${AI_SERVICE_BASE}/rag-sources/${id}/reindex`);
    return response.data;
  },

  testChat: async (message: string, useCurrentConfig: boolean) => {
    const response = await axiosInstance.post(`${AI_SERVICE_BASE}/test-chat`, {
      message,
      useCurrentConfig
    });
    return response.data as TestChatResponse;
  }
};
