import React from 'react';
import ProductItem from './ProductItem';

interface Product {
  productId: string;
  productName: string;
  price: number;
  imageUrl?: string;
  rating?: number;
  discount?: number;
}

interface ProductGridProps {
  products: Product[];
  title?: string;
  onAddToCart?: (productId: string) => void;
  onAddToWishlist?: (productId: string) => void;
}

const ProductGrid: React.FC<ProductGridProps> = ({ 
  products, 
  title = "Sản phẩm nổi bật",
  onAddToCart,
  onAddToWishlist 
}) => {
  return (
    <div className="w-full">
      {/* Section Title */}
      {title && (
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">{title}</h2>
          <div className="w-16 h-1 bg-blue-600 rounded"></div>
        </div>
      )}

      {/* Product Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
        {products.map((product) => (
          <ProductItem
            key={product.productId}
            product={product}
            onAddToCart={() => onAddToCart?.(product.productId)}
            onAddToWishlist={() => onAddToWishlist?.(product.productId)}
          />
        ))}
      </div>

      {/* Empty State */}
      {products.length === 0 && (
        <div className="text-center py-12">
          <div className="text-gray-400 text-lg mb-2">Không có sản phẩm nào</div>
          <p className="text-gray-500">Vui lòng thử lại sau</p>
        </div>
      )}
    </div>
  );
};

export default ProductGrid;
