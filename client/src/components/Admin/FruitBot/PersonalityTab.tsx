import React from 'react';
import { Input } from '../../ui/input';
import { Textarea } from '../../ui/input/textarea';
import { Button } from '../../ui/Button/Button';
import type { AiConfigPayload } from '../../../api/adminApi';

interface PersonalityTabProps {
  config: AiConfigPayload;
  onChange: (next: Partial<AiConfigPayload>) => void;
  onSave: () => void;
  saving: boolean;
}

const PersonalityTab: React.FC<PersonalityTabProps> = ({ config, onChange, onSave, saving }) => {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-6">
        <div>
          <label className="text-sm font-medium text-gray-700">Tên AI</label>
          <Input
            value={config.name}
            onChange={(e) => onChange({ name: e.target.value })}
            placeholder="Cam — trợ lý trái cây"
            className="mt-2 bg-transparent text-gray-800"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-700">Ngôn ngữ mặc định</label>
          <select
            value={config.language}
            onChange={(e) => onChange({ language: e.target.value as AiConfigPayload['language'] })}
            className="mt-2 w-full border border-gray-300 rounded-md px-3 py-2 text-sm bg-transparent text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="vi">Tiếng Việt</option>
            <option value="en">English</option>
            <option value="auto">Tự động</option>
          </select>
        </div>
      </div>

      <div>
        <label className="text-sm font-medium text-gray-700">System prompt — tính cách & phong cách</label>
        <Textarea
          value={config.systemPrompt}
          onChange={(e) => onChange({ systemPrompt: e.target.value })}
          placeholder="Bạn là Cam, trợ lý thân thiện của cửa hàng trái cây tươi..."
          className="mt-2 min-h-[160px] text-sm text-gray-800 border-gray-300 bg-transparent"
        />
      </div>

      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-4">Thanh điều chỉnh phong cách</h3>
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <span className="w-28 text-sm text-gray-600 font-medium">Thân thiện</span>
            <input
              type="range"
              min={0}
              max={100}
              value={config.style}
              onChange={(e) => onChange({ style: Number(e.target.value) })}
              className="flex-1 accent-gray-600"
            />
            <span className="w-8 text-sm text-blue-600 font-semibold text-right">{config.style}</span>
          </div>

          <div className="flex items-center gap-4">
            <span className="w-28 text-sm text-gray-600 font-medium">Độ dài</span>
            <input
              type="range"
              min={0}
              max={100}
              value={config.length}
              onChange={(e) => onChange({ length: Number(e.target.value) })}
              className="flex-1 accent-gray-600"
            />
            <span className="w-8 text-sm text-blue-600 font-semibold text-right">{config.length}</span>
          </div>

          <div className="flex items-center gap-4">
            <span className="w-28 text-sm text-gray-600 font-medium">Chủ động bán</span>
            <input
              type="range"
              min={0}
              max={100}
              value={config.sales}
              onChange={(e) => onChange({ sales: Number(e.target.value) })}
              className="flex-1 accent-gray-600"
            />
            <span className="w-8 text-sm text-blue-600 font-semibold text-right">{config.sales}</span>
          </div>
        </div>
      </div>

      <div className="flex justify-end pt-4">
        <Button
          onClick={onSave}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 rounded-md"
          disabled={saving}
        >
          {saving ? 'Đang lưu...' : 'Lưu cấu hình'}
        </Button>
      </div>
    </div>
  );
};

export default PersonalityTab;
