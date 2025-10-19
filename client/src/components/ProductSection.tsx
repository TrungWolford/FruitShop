import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import ProductGrid from './ProductGrid';
import { toast } from 'sonner';
import { Button } from './ui/Button/Button';
import { ArrowRight } from 'lucide-react';

interface Product {
    productId: string;
    productName: string;
    price: number;
    imageUrl?: string;
    images?: string[];
    rating?: number;
    discount?: number;
    category?: string;
    categoryCount?: number;
}

const ProductSection: React.FC = () => {
    const navigate = useNavigate();
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadProducts();
    }, []);

    const loadProducts = async () => {
        try {
            setLoading(true);
            // Lấy tất cả sản phẩm để có thể random
            const response = await productService.getAllProducts(0, 100);
            console.log('Products response:', response);

            if (response && response.content) {
                // Shuffle array và lấy 10 sản phẩm đầu tiên
                const shuffledProducts = [...response.content].sort(() => Math.random() - 0.5);
                const randomProducts = shuffledProducts.slice(0, 10);

                // Transform data to match ProductItem interface
                const transformedProducts = randomProducts.map((product: any) => {
                    // Xử lý categories - hiển thị nhiều categories nếu có
                    let category = undefined;
                    let categoryCount = 0;
                    if (product.categories && product.categories.length > 0) {
                        categoryCount = product.categories.length;
                        category = product.categories[0].categoryName; // Luôn lấy category đầu tiên
                    }

                    return {
                        productId: product.productId,
                        productName: product.productName,
                        price: product.price,
                        imageUrl:
                            product.images && product.images.length > 0
                                ? `/products/${product.images[0].imageUrl}`
                                : undefined,
                        images:
                            product.images && product.images.length > 0
                                ? product.images.map((img: any) => `/products/${img.imageUrl}`)
                                : undefined,
                        rating: 4.5, // Default rating
                        discount: undefined, // Bỏ giảm giá
                        category: category,
                        categoryCount: categoryCount,
                    };
                });

                console.log('Random featured products (10):', transformedProducts);
                setProducts(transformedProducts);
            } else {
                setProducts([]);
            }
        } catch (error) {
            console.error('Error loading products:', error);
            toast.error('Không thể tải danh sách sản phẩm');
            setProducts([]);
        } finally {
            setLoading(false);
        }
    };

    const handleAddToCart = (productId: string) => {
        toast.success('Đã thêm sản phẩm vào giỏ hàng');
        console.log('Add to cart:', productId);
    };

    const handleAddToWishlist = (productId: string) => {
        toast.success('Đã thêm sản phẩm vào yêu thích');
        console.log('Add to wishlist:', productId);
    };

    if (loading) {
        return (
            <div className="w-full">
                <div className="mb-6">
                    <div className="h-8 bg-gray-200 animate-pulse rounded w-48 mb-2"></div>
                    <div className="w-16 h-1 bg-gray-200 animate-pulse rounded"></div>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                    {Array.from({ length: 10 }).map((_, index) => (
                        <div
                            key={index}
                            className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
                        >
                            <div className="aspect-[3/4] bg-gray-200 animate-pulse"></div>
                            <div className="p-4 space-y-2">
                                <div className="h-4 bg-gray-200 animate-pulse rounded"></div>
                                <div className="h-4 bg-gray-200 animate-pulse rounded w-3/4"></div>
                                <div className="h-6 bg-gray-200 animate-pulse rounded w-1/2"></div>
                                <div className="h-8 bg-gray-200 animate-pulse rounded"></div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className="w-full">
            <ProductGrid
                products={products}
                title="Sản phẩm nổi bật"
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
