import { useMemo, useState } from 'react';
import { adminApi, type AiConfigPayload, type AiLanguage } from '../api/adminApi';

const DEFAULT_CONFIG: AiConfigPayload = {
  name: 'FruitBot',
  systemPrompt: '',
  style: 60,
  length: 50,
  sales: 40,
  language: 'vi'
};

export function useAIConfig() {
  const [config, setConfig] = useState<AiConfigPayload>(DEFAULT_CONFIG);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateConfig = (next: Partial<AiConfigPayload>) => {
    setConfig((prev) => ({ ...prev, ...next }));
  };

  const saveConfig = async () => {
    setSaving(true);
    setError(null);
    try {
      await adminApi.saveAiConfig(config);
      return true;
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Không thể lưu cấu hình');
      return false;
    } finally {
      setSaving(false);
    }
  };

  const languageOptions: { label: string; value: AiLanguage }[] = useMemo(
    () => [
      { label: 'Tiếng Việt', value: 'vi' },
      { label: 'English', value: 'en' },
      { label: 'Tự động', value: 'auto' }
    ],
    []
  );

  return {
    config,
    updateConfig,
    saveConfig,
    saving,
    error,
    languageOptions
  };
}
