import { useCallback, useState } from 'react';
import { adminApi, type RuleItem, type RuleItemPayload } from '../api/adminApi';

export function useRules() {
  const [rules, setRules] = useState<RuleItem[]>([]);
  const [loading, setLoading] = useState(false);

  const loadRules = useCallback(async () => {
    setLoading(true);
    try {
      const data = await adminApi.getRules();
      setRules(data);
    } finally {
      setLoading(false);
    }
  }, []);

  const createRule = async (payload: RuleItemPayload) => {
    const created = await adminApi.createRule(payload);
    setRules((prev) => [created, ...prev]);
  };

  const toggleRule = async (id: string, isActive: boolean) => {
    const updated = await adminApi.toggleRule(id, isActive);
    setRules((prev) => prev.map((rule) => (rule.id === id ? updated : rule)));
  };

  const deleteRule = async (id: string) => {
    await adminApi.deleteRule(id);
    setRules((prev) => prev.filter((rule) => rule.id !== id));
  };

  const reorderRules = (ordered: RuleItem[]) => {
    setRules(ordered);
  };

  return {
    rules,
    loading,
    loadRules,
    createRule,
    toggleRule,
    deleteRule,
    reorderRules
  };
}
