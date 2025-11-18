import React from 'react';
import { useNavigate } from 'react-router-dom';
import ProductGrid from './ProductGrid';
import { toast } from 'sonner';
import { Button } from './ui/Button/Button';
import { ArrowRight } from 'lucide-react';

const ProductSection: React.FC = () => {
    const navigate = useNavigate();

    const handleAddToCart = (productId: string) => {
        toast.success('Đã thêm sản phẩm vào giỏ hàng');
        console.log('Add to cart:', productId);
    };

    const handleAddToWishlist = (productId: string) => {
        toast.success('Đã thêm sản phẩm vào yêu thích');
        console.log('Add to wishlist:', productId);
    };

    return (
        <div className="w-full">
            <ProductGrid
                title="Sản phẩm nổi bật"
                limit={8}
                onAddToCart={handleAddToCart}
                onAddToWishlist={handleAddToWishlist}
            />

            {/* View More Button */}
            <div className="mt-8 text-center">
                <Button
                    onClick={() => {
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                        navigate('/product');
                    }}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-lg font-semibold transition-colors duration-200 flex items-center gap-2 mx-auto"
                >
                    Xem thêm sản phẩm
                    <ArrowRight className="w-4 h-4" />
                </Button>
            </div>
        </div>
    );
};

export default ProductSection;
