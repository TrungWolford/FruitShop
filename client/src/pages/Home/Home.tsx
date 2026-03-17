// src/pages/Home/Home.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNavigation from '../../components/layout/Header/Header';
import MainBanner from './Banner/MainBanner';
import ProductGrid from '../../components/Product/ProductGrid';
import CategoryMainPage from '../../components/Category/CategoryMainPage';
import Footer from '../../components/layout/Footer/Footer';
import AgentChatBot from '@/components/ChatMessage/AiAgentic';
import HumanSupport from '@/components/ChatMessage/HumanSupport';

type activeChat = 'ai' | 'human' | null
const Home: React.FC = () => {
  const navigate = useNavigate();
  const [activeChat, setActiveChat] = useState<activeChat>(null)

  return (
    <div className="min-h-screen bg-gray-50">
      <TopNavigation />
      {/* Hero Section with Category and Banner side by side */}
      <section className="container mx-auto px-4 py-6">
        <div className="grid grid-cols-12 gap-6">
          {/* Left: Category Menu */}
          <div className="col-span-12 lg:col-span-3">
            <CategoryMainPage />
          </div>

          {/* Right: Main Banner */}
          <div className="col-span-12 lg:col-span-9">
            <MainBanner />
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="container mx-auto px-36 py-8">
        <div className="flex flex-col items-center fixed z-50 bottom-0 right-5">
          <AgentChatBot
            className={`${activeChat === 'human' ? 'hidden' : ''}`}
            isOpen={activeChat === 'ai'}
            onOpen={() => setActiveChat('ai')}
            onClose={() => setActiveChat(null)}
          />
          <HumanSupport
            className={` ${activeChat === 'ai' ? 'hidden' : ''}`}
            isOpen={activeChat === 'human'}
            onOpen={() => setActiveChat('human')}
            onClose={() => setActiveChat(null)}
          />
        </div>
        <ProductGrid
          title="Sản phẩm nổi bật"
          limit={10}
        />

        {/* Button Xem tất cả sản phẩm */}
        <div className="flex justify-center mt-8">
          <button
            onClick={() => navigate('/product')}
            className="px-8 py-3 bg-primary text-white font-semibold rounded-lg hover:bg-orange-700 transition-colors duration-200 shadow-md hover:shadow-lg"
          >
            Xem tất cả sản phẩm
          </button>
        </div>
      </section>


      <Footer />
    </div>
  );
};

export default Home;