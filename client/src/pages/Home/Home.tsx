// src/pages/Home/Home.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import TopNavigation from '../../components/ui/Header/Header';
import MainBanner from '../../components/MainBanner';
import ProductGrid from '../../components/ProductGrid';
import CategoryMainPage from '../../components/CategoryMainPage';
import Footer from '../../components/ui/Footer/Footer';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const handleAddToCart = (productId: string) => {
    console.log('Add to cart:', productId);
    // TODO: Implement add to cart logic
  };

  const handleAddToWishlist = (productId: string) => {
    console.log('Add to wishlist:', productId);
    // TODO: Implement wishlist logic
  };

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
        <ProductGrid 
          title="Sản phẩm nổi bật"
          limit={10}
          onAddToCart={handleAddToCart}
          onAddToWishlist={handleAddToWishlist}
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