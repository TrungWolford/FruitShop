import React, { useEffect, useMemo, useState } from 'react';
import { Button } from '../../ui/Button/Button';
import { Input } from '../../ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../ui/select';
import { toast } from 'sonner';
import RuleItem from './RuleItem';
import { useRules } from '../../../hooks/useRules';
import type { RuleItem as RuleItemType, RuleItemPayload } from '../../../api/adminApi';

const RulesTab: React.FC = () => {
  const { rules, loading, loadRules, createRule, toggleRule, deleteRule, reorderRules } = useRules();
  const [content, setContent] = useState('');
  const [priority, setPriority] = useState(1);
  const [scope, setScope] = useState<RuleItemPayload['scope']>('all');
  const [draggingId, setDraggingId] = useState<string | null>(null);

  useEffect(() => {
    void loadRules();
  }, [loadRules]);

  const orderedRules = useMemo(() => rules.slice().sort((a, b) => b.priority - a.priority), [rules]);

  const handleAdd = async () => {
    if (!content.trim()) {
      toast.error('Vui long nhap noi dung rule');
      return;
    }
    try {
      await createRule({ content, priority, scope, is_active: true });
      setContent('');
      setPriority(1);
      setScope('all');
      toast.success('Da them rule moi');
    } catch (error) {
      toast.error('Khong the them rule');
    }
  };

  const handleToggle = async (id: string, isActive: boolean) => {
    try {
      await toggleRule(id, isActive);
    } catch (error) {
      toast.error('Khong the cap nhat rule');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteRule(id);
      toast.success('Da xoa rule');
    } catch (error) {
      toast.error('Khong the xoa rule');
    }
  };

  const handleDragStart = (id: string) => setDraggingId(id);
  const handleDragOver = (id: string) => {
    if (!draggingId || draggingId === id) return;
    const list = [...rules];
    const fromIndex = list.findIndex((rule) => rule.id === draggingId);
    const toIndex = list.findIndex((rule) => rule.id === id);
    if (fromIndex === -1 || toIndex === -1) return;
    const [moved] = list.splice(fromIndex, 1);
    list.splice(toIndex, 0, moved);
    reorderRules(list);
  };
  const handleDrop = () => setDraggingId(null);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-800">Danh sách quy tắc</h3>
        <Button
          onClick={() => {
            if (!content.trim()) {
              toast.error('Vui lòng nhập nội dung rule');
              return;
            }
            handleAdd();
          }}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm px-4 py-2 rounded"
        >
          + Thêm quy tắc
        </Button>
      </div>

      <div className="grid lg:grid-cols-[2fr_1fr_1fr_auto] gap-2 bg-white border border-gray-200 rounded-lg p-4">
        <Input
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Nhập nội dung rule"
          className="bg-transparent text-gray-800 border-gray-300"
        />
        <Input
          type="number"
          value={priority}
          onChange={(e) => setPriority(Number(e.target.value))}
          min={1}
          className="bg-transparent text-gray-800 border-gray-300"
        />
        <Select value={scope} onValueChange={(value) => setScope(value as RuleItemPayload['scope'])}>
          <SelectTrigger className="bg-transparent text-gray-800 border-gray-300">
            <SelectValue placeholder="Scope" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="checkout">Thanh toán</SelectItem>
            <SelectItem value="product">Sản phẩm</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-2">
        {loading && <p className="text-sm text-gray-500">Đang tải rule...</p>}
        {!loading && orderedRules.length === 0 && (
          <div className="text-sm text-gray-500">Chưa có rule nào.</div>
        )}
        {orderedRules.map((rule: RuleItemType) => (
          <RuleItem
            key={rule.id}
            rule={rule}
            onToggle={handleToggle}
            onDelete={handleDelete}
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDrop={handleDrop}
          />
        ))}
      </div>
    </div>
  );
};

export default RulesTab;
