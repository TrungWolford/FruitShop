// src/components/ProductGrid.tsx
import React, { useEffect, useState } from 'react';
import ProductItem from './ProductItem';
import { useNavigate } from 'react-router-dom';
import { Button } from './ui/Button/Button';
import { ArrowRight } from 'lucide-react';
import { productService } from '../services/productService';
import type { Product as ApiProduct } from '../types/product';

interface ProductGridProps {
  title?: string;
  categoryId?: string;
  limit?: number;
  onAddToCart?: (productId: string) => void;
  onAddToWishlist?: (productId: string) => void;
  showViewAll?: boolean;
  viewAllLink?: string;
}

const ProductGrid: React.FC<ProductGridProps> = ({
  title = 'Sản phẩm nổi bật',
  categoryId,
  limit,
  onAddToCart,
  onAddToWishlist,
  showViewAll = true,
  viewAllLink,
}) => {
  const navigate = useNavigate();

  const [products, setProducts] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalProducts, setTotalProducts] = useState(0);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      try {
        setLoading(true);
        // Request first page with size large enough (or you can implement pagination later)
        const response: any = await productService.getAllProducts(0, limit || 20);

        // If backend returns paginated { content, totalElements }
        if (response && response.content) {
          if (!mounted) return;
          setProducts(response.content as ApiProduct[]);
          setTotalProducts(response.totalElements || response.content.length || 0);
        } else if (Array.isArray(response)) {
          if (!mounted) return;
          setProducts(response as ApiProduct[]);
          setTotalProducts(response.length);
        } else if (response && response.data) {
          // Some endpoints wrap in data
          const data = response.data;
          if (Array.isArray(data)) {
            setProducts(data as ApiProduct[]);
            setTotalProducts(data.length);
          }
        }
      } catch (error) {
        console.error('Error loading products for ProductGrid:', error);
        setProducts([]);
        setTotalProducts(0);
      } finally {
        setLoading(false);
      }
    };

    load();
    return () => {
      mounted = false;
    };
  }, [categoryId, limit]);

  // Filter and slice locally
  let filteredProducts = products.filter((p) => p.status === 1);
  if (categoryId) {
    filteredProducts = filteredProducts.filter((product) =>
      product.categories?.some((cat) => cat.categoryId === categoryId),
    );
  }

  const isLimited = limit && filteredProducts.length > limit;
  if (limit) {
    filteredProducts = filteredProducts.slice(0, limit);
  }

  const transformedProducts = filteredProducts.map((product) => ({
    productId: product.productId,
    productName: product.productName,
    price: product.price,
    images: product.images,
    category: product.categories?.[0]?.categoryName || 'Không phân loại',
    categoryCount: product.categories?.length || 0,
    rating: undefined,
    discount: undefined,
  }));

  const handleViewAll = () => {
    if (viewAllLink) {
      navigate(viewAllLink);
    } else if (categoryId) {
      navigate(`/category/${categoryId}`);
    } else {
      navigate('/products');
    }
  };

  return (
    <div className="w-full">
      {/* Section Title */}
      {title && (
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">{title}</h2>
            <div className="w-16 h-1 bg-blue-600 rounded"></div>
          </div>

          {/* View All Button - Desktop */}
        </div>
      )}

      {/* Product Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
        {loading
          ? Array.from({ length: limit || 8 }).map((_, i) => (
              <div key={i} className="h-64 bg-gray-100 animate-pulse rounded-lg" />
            ))
          : transformedProducts.map((product) => (
              <ProductItem
                key={product.productId}
                product={product}
                onAddToCart={() => onAddToCart?.(product.productId)}
                onAddToWishlist={() => onAddToWishlist?.(product.productId)}
              />
            ))}
      </div>

      {/* View All Button - Mobile & Bottom */}
      {showViewAll && isLimited && (
        <div className="mt-8 text-center">
          <Button
            onClick={handleViewAll}
            className="bg-primary hover:bg-primary/90 text-white px-8 py-3 rounded-lg font-semibold inline-flex items-center gap-2"
          >
            Xem tất cả {totalProducts} sản phẩm
            <ArrowRight className="w-5 h-5" />
          </Button>

          {/* Progress indicator */}
          <div className="mt-4 flex items-center justify-center gap-2 text-sm text-gray-500">
            <span>
              Đang hiển thị {transformedProducts.length} / {totalProducts} sản phẩm
            </span>
          </div>
        </div>
      )}

      {/* Empty State */}
      {transformedProducts.length === 0 && (
        <div className="text-center py-12">
          <div className="text-gray-400 text-lg mb-2">Không có sản phẩm nào</div>
          <p className="text-gray-500">Vui lòng thử lại sau</p>
        </div>
      )}
    </div>
  );
};

export default ProductGrid;
