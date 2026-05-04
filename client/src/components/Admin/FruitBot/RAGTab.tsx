import React, { useEffect, useMemo, useState } from 'react';
import { Button } from '../../ui/Button/Button';
import { Badge } from '../../ui/badge';
import { Input } from '../../ui/input';
import { adminApi, type RagSource } from '../../../api/adminApi';
import { toast } from 'sonner';

const statusColor = (status: string) => {
  if (status === 'indexed') return 'bg-green-600 border-green-600';
  if (status === 'indexing') return 'bg-amber-500 border-amber-500';
  if (status === 'failed') return 'bg-red-600 border-red-600';
  return 'bg-slate-600 border-slate-600';
};

const RAGTab: React.FC = () => {
  const [sources, setSources] = useState<RagSource[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const loadSources = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getRagSources();
      setSources(data.sources || []);
    } catch (error) {
      toast.error('Khong the tai danh sach RAG');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadSources();
  }, []);

  const metrics = useMemo(() => {
    const totalDocuments = sources.reduce((sum, s) => sum + (s.documentCount || 0), 0);
    const scored = sources.filter((s) => typeof s.relevanceScore === 'number');
    const avgScore = scored.length > 0
      ? scored.reduce((sum, s) => sum + (s.relevanceScore || 0), 0) / scored.length
      : 0;
    return { totalDocuments, avgScore };
  }, [sources]);

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error('Vui long chon file PDF');
      return;
    }
    setUploading(true);
    try {
      await adminApi.uploadRagSource(selectedFile);
      setSelectedFile(null);
      toast.success('Da tai PDF len');
      await loadSources();
    } catch (error) {
      toast.error('Khong the upload PDF');
    } finally {
      setUploading(false);
    }
  };

  const handleReindex = async (id: string) => {
    try {
      await adminApi.reindexRagSource(id);
      toast.success('Dang re-index');
      await loadSources();
    } catch (error) {
      toast.error('Khong the re-index');
    }
  };

  return (
    <div className="space-y-6">
      <div className="grid md:grid-cols-2 gap-4">
        <div className="bg-white border rounded-lg p-4">
          <p className="text-sm text-gray-500">Tong documents</p>
          <p className="text-2xl font-semibold text-gray-900">{metrics.totalDocuments}</p>
        </div>
        <div className="bg-white border rounded-lg p-4">
          <p className="text-sm text-gray-500">Relevance score trung binh</p>
          <p className="text-2xl font-semibold text-gray-900">{metrics.avgScore.toFixed(2)}</p>
        </div>
      </div>

      <div className="bg-white border rounded-lg p-4 space-y-3">
        <div className="flex flex-col md:flex-row gap-3 md:items-center">
          <Input
            type="file"
            accept="application/pdf"
            onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
          />
          <Button
            onClick={handleUpload}
            disabled={uploading}
            className="bg-amber-500 hover:bg-amber-600 text-white"
          >
            {uploading ? 'Dang tai...' : 'Upload PDF'}
          </Button>
        </div>
      </div>

      <div className="space-y-3">
        {loading && <p className="text-sm text-gray-500">Dang tai danh sach...</p>}
        {!loading && sources.length === 0 && (
          <p className="text-sm text-gray-500">Chua co nguon du lieu.</p>
        )}
        {sources.map((source) => (
          <div key={source.id} className="bg-white border rounded-lg p-4 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
            <div>
              <p className="font-medium text-gray-900">{source.name}</p>
              <p className="text-sm text-gray-500">Documents: {source.documentCount ?? 0}</p>
            </div>
            <div className="flex items-center gap-3">
              <Badge className={statusColor(source.status)}>{source.status}</Badge>
              <Button variant="outline" onClick={() => handleReindex(source.id)}>
                Re-index
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RAGTab;
