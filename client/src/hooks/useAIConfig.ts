import { useEffect, useMemo, useState } from 'react';
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load current config from ai-service when admin opens the page
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    adminApi
      .getAiConfig()
      .then((data) => {
        if (!cancelled) setConfig(data);
      })
      .catch(() => {
        // Silently fall back to defaults if ai-service is unreachable
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

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
    loading,
    error,
    languageOptions
  };
}
