import React from 'react';
import { Button } from '../../ui/Button/Button';
import { Checkbox } from '../../ui/input/checkbox';
import { Trash2 } from 'lucide-react';
import type { RuleItem as RuleItemType } from '../../../api/adminApi';

interface RuleItemProps {
  rule: RuleItemType;
  onToggle: (id: string, isActive: boolean) => void;
  onDelete: (id: string) => void;
  onDragStart: (id: string) => void;
  onDragOver: (id: string) => void;
  onDrop: (id: string) => void;
}

const scopeLabel: Record<RuleItemType['scope'], string> = {
  all: 'Tat ca',
  checkout: 'Thanh toan',
  product: 'San pham'
};

const RuleItem: React.FC<RuleItemProps> = ({
  rule,
  onToggle,
  onDelete,
  onDragStart,
  onDragOver,
  onDrop
}) => {
  return (
    <div
      className="flex items-center gap-3 border border-gray-600 rounded-lg p-4 bg-gray-800 hover:bg-gray-750 transition"
      draggable
      onDragStart={() => onDragStart(rule.id)}
      onDragOver={(e) => {
        e.preventDefault();
        onDragOver(rule.id);
      }}
      onDrop={() => onDrop(rule.id)}
    >
      <div className={`w-3 h-3 rounded-full flex-shrink-0 ${rule.is_active ? 'bg-teal-400' : 'bg-gray-500'}`} />
      
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-gray-100">{rule.content}</p>
        <p className="text-xs text-gray-400 mt-1">Ưu tiên: {rule.priority === 1 ? 'Cao' : rule.priority === 2 ? 'Trung' : 'Thấp'} - Áp dụng: {scopeLabel[rule.scope]}</p>
      </div>
      
      <div className="flex items-center gap-3 flex-shrink-0">
        <span className={`text-xs font-medium px-2 py-1 rounded ${rule.is_active ? 'text-green-400' : 'text-gray-400'}`}>
          {rule.is_active ? 'Bật' : 'Tắt'}
        </span>
        <label className="flex items-center gap-2">
          <Checkbox
            checked={rule.is_active}
            onCheckedChange={(checked) => onToggle(rule.id, Boolean(checked))}
            className="w-4 h-4"
          />
        </label>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onDelete(rule.id)}
          className="text-gray-400 hover:text-red-400 hover:bg-gray-700 p-1"
        >
          <Trash2 className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
}

export default RuleItem;
