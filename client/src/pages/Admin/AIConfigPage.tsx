import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import Header from '../../components/Admin/Header';
import LeftTaskbar from '../../components/Admin/LeftTaskbar/LeftTaskbar';
import Container from '../../components/Admin/Container';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import PersonalityTab from '../../components/Admin/FruitBot/PersonalityTab';
import RulesTab from '../../components/Admin/FruitBot/RulesTab';
import RAGTab from '../../components/Admin/FruitBot/RAGTab';
import { useAIConfig } from '../../hooks/useAIConfig';
import { toast } from 'sonner';

const TASKBAR_MARGIN_TOP = 'mt-[60px]';

const AIConfigPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, isInitialized } = useAppSelector((state) => state.adminAuth);
  const { config, updateConfig, saveConfig, saving, error } = useAIConfig();

  React.useEffect(() => {
    document.title = 'FruitBot - Admin AI';

    if (!isInitialized) {
      return;
    }

    if (!isAuthenticated || !user) {
      navigate('/admin');
      return;
    }

    const userRoles = user.roles || [];
    const isAdmin = userRoles.some(
      (role) => role.roleName === 'ADMIN' || role.roleName === 'ROLE_ADMIN'
    );

    if (!isAdmin) {
      navigate('/admin');
      return;
    }
  }, [isInitialized, isAuthenticated, user, navigate]);

  const handleSave = async () => {
    const ok = await saveConfig();
    if (ok) {
      toast.success('Da luu cau hinh AI');
    } else {
      toast.error(error || 'Khong the luu cau hinh');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <LeftTaskbar className={`${TASKBAR_MARGIN_TOP}`} />

      <Container className="px-6">
        <div className="mb-4">
          <h1 className="text-2xl font-bold text-gray-800">FruitBot Admin Panel</h1>
          <p className="text-gray-500">Quan ly tinh cach, quy tac va RAG cho chat agent.</p>
        </div>

        <Tabs defaultValue="personality" className="space-y-4">
          <TabsList className="grid grid-cols-2 md:grid-cols-3 gap-2 items-center bg-white border border-gray-200 rounded-lg p-1">
            <TabsTrigger value="personality" className="data-[state=active]:border-b-4 data-[state=active]:border-blue-600 pb-2">Tính cách AI</TabsTrigger>
            <TabsTrigger value="rules" className="data-[state=active]:border-b-4 data-[state=active]:border-blue-600 pb-2">Quy tắc trả lời</TabsTrigger>
            <TabsTrigger value="rag" className="data-[state=active]:border-b-4 data-[state=active]:border-blue-600 pb-2">RAG & Knowledge</TabsTrigger>
          </TabsList>

          <TabsContent value="personality">
            <PersonalityTab
              config={config}
              onChange={updateConfig}
              onSave={handleSave}
              saving={saving}
            />
          </TabsContent>

          <TabsContent value="rules">
            <RulesTab />
          </TabsContent>

          <TabsContent value="rag">
            <RAGTab />
          </TabsContent>

        </Tabs>
      </Container>
    </div>
  );
};

export default AIConfigPage;
