import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, ShoppingBag } from 'lucide-react';
import { useAppSelector } from '../../hooks/redux';
import { toast } from 'sonner';

import { localStorageCartService } from '@/services/localStorageCartService';
import { cartService } from '../../services/cartService';

interface ProductItemProps {
  product: {
    productId: string;
    productName: string;
    price: number;
    imageUrl?: string;
    images?: Array<{ imageUrl: string }> | string[]; // Support both formats
    rating?: number;
    discount?: number;
    category?: string;
    categoryCount?: number; // Số lượng categories
  };
  onAddToCart?: () => void;
  onAddToWishlist?: () => void;
}

const ProductItem: React.FC<ProductItemProps> = ({ product, onAddToCart, onAddToWishlist }) => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [isAddingToCart, setIsAddingToCart] = useState(false);

  // State cho slide hình ảnh
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [isHovering, setIsHovering] = useState(false);

  // Xử lý slide hình ảnh khi hover
  useEffect(() => {
    if (!isHovering || !product.images || product.images.length <= 1) {
      return;
    }

    const interval = setInterval(() => {
      setCurrentImageIndex((prevIndex) => {
        const newIndex = (prevIndex + 1) % product.images!.length;
        return newIndex;
      });
    }, 1500); // 1.5 giây để có thời gian transition

    return () => {
      clearInterval(interval);
    };
  }, [isHovering, product.images]);

  // Reset index khi không hover
  useEffect(() => {
    if (!isHovering) {
      setCurrentImageIndex(0);
    }
  }, [isHovering]);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const formatDiscountPrice = (price: number, discount: number) => {
    const discountedPrice = price * (1 - discount / 100);
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(discountedPrice);
  };

  // Helper function to get image URL
  const getImageUrl = (imageUrl?: string) => {
    if (!imageUrl) return '/placeholder-image.svg';

    if (imageUrl.startsWith('http')) {
      return imageUrl;
    }

    // Ensure the URL starts with /products/
    if (imageUrl.startsWith('/products/')) {
      return imageUrl;
    }

    // Remove leading slash if exists and add /products/ prefix
    const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
    return `/products/${cleanUrl}`;
  };

  // Xử lý click vào sản phẩm để đi tới trang chi tiết
  const handleProductClick = () => {
    // Chỉ truyền productId vào URL
    navigate(`/product/${product.productId}`);
  };

  // Xử lý thêm vào giỏ hàng với logic createCart trước
  const handleAddToCart = async () => {
    setIsAddingToCart(true);
    try {
      if (isAuthenticated && user) {
        // User đã đăng nhập → thêm qua API
        // console.log('🛒 Starting cart process for product:', product.productId, 'user:', user.accountId);

        const addToCartResponse = await cartService.addToCart({
          productId: product.productId,
          quantity: 1,
          accountId: user.accountId,
        });
        // console.log('✅ Add to cart response:', addToCartResponse);

        if (addToCartResponse.success) {
          toast.success('Đã thêm sản phẩm vào giỏ hàng');

          // Gọi callback nếu có để refresh cart
          if (onAddToCart) {
            onAddToCart();
          }

          // Dispatch event để Cart component có thể refresh
          window.dispatchEvent(new CustomEvent('cartUpdated'));
        } else {
          // console.error('❌ Failed to add to cart:', addToCartResponse);
          toast.error(addToCartResponse.message || 'Không thể thêm sản phẩm vào giỏ hàng');
        }
      } else {
        // Guest user → thêm vào localStorage
        // console.log('🛒 Adding to localStorage cart for guest user');

        // Lấy imageUrl cho localStorage
        let imageUrl = product.imageUrl;
        if (!imageUrl && product.images && product.images.length > 0) {
          const firstImage = product.images[0];
          imageUrl = typeof firstImage === 'string' ? firstImage : firstImage.imageUrl;
        }

        localStorageCartService.addToCart({
          productId: product.productId,
          productName: product.productName,
          productPrice: product.price,
          images: imageUrl ? [imageUrl] : [],
          quantity: 1,
        });

        toast.success('Đã thêm sản phẩm vào giỏ hàng');

        // Gọi callback nếu có
        if (onAddToCart) {
          onAddToCart();
        }

        // Event đã được dispatch tự động trong localStorageCartService.addToCart()
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // console.error('💥 Error adding to cart:', error);
      // console.error('Error details:', error.response?.data);
      toast.error(error.response?.data?.message || error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
    } finally {
      setIsAddingToCart(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow duration-200">
      {/* Product Image */}
      <div
        className="relative aspect-[3/4] bg-gray-100 overflow-hidden cursor-pointer"
        onMouseEnter={() => setIsHovering(true)}
        onMouseLeave={() => setIsHovering(false)}
        onClick={handleProductClick}
      >
        {product.images && product.images.length > 0 ? (
          <div className="image-slide-container">
            {product.images.map((image, index) => {
              const imageUrl = typeof image === 'string' ? image : image.imageUrl;
              return (
                <img
                  key={index}
                  src={getImageUrl(imageUrl)}
                  alt={product.productName}
                  className={`image-slide-item object-cover ${
                    index === currentImageIndex ? 'active' : index < currentImageIndex ? 'prev' : 'next'
                  }`}
                  onError={(e) => {
                    // Thay thế bằng placeholder khi ảnh lỗi
                    e.currentTarget.src = '/placeholder-image.svg';
                  }}
                />
              );
            })}
            {/* Hiển thị số thứ tự hình ảnh */}
            {product.images.length > 1 && (
              <div className="absolute bottom-2 right-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded z-10">
                {currentImageIndex + 1}/{product.images.length}
              </div>
            )}
          </div>
        ) : product.imageUrl ? (
          <img
            src={getImageUrl(product.imageUrl)}
            alt={product.productName}
            className="w-full h-full object-cover"
            onError={(e) => {
              // Thay thế bằng placeholder khi ảnh lỗi
              e.currentTarget.src = '/placeholder-image.svg';
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gray-200">
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-2 bg-gray-300 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <span className="text-gray-400 text-sm">Không có ảnh</span>
            </div>
          </div>
        )}

        {/* Discount Badge */}
        {product.discount && (
          <div className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded">
            -{product.discount}%
          </div>
        )}

        {/* Wishlist Button */}
        <button
          onClick={onAddToWishlist}
          className="absolute top-2 right-2 w-8 h-8 bg-white bg-opacity-80 hover:bg-opacity-100 rounded-full flex items-center justify-center transition-all duration-200 hover:scale-110"
        >
          <Heart className="w-4 h-4 text-gray-600 hover:text-red-500" />
        </button>
      </div>

      {/* Product Info */}
      <div className="p-4">
        {/* Category */}
        <div className="flex items-center justify-between mb-1">
          <div className="text-xs text-gray-500 uppercase font-medium h-8 line-clamp-2">{product.category || 'Không phân loại'}</div>
          {product.categoryCount && product.categoryCount > 1 && (
            <div className="text-xs text-blue-600 font-medium">+{product.categoryCount - 1}</div>
          )}
        </div>

        {/* Product Name */}
        <div className="mb-1 h-12 text-sm overflow-hidden font-normal text-lower">
          <h3
            className="text-sm font-semibold text-gray-900  line-clamp-2 hover:text-blue-600 transition-colors cursor-pointer"
            onClick={handleProductClick}
            title={product.productName}
          >
            {product.productName}
          </h3>
        </div>

        {/* Price */}
        <div className="flex items-center gap-2 mb-3 h-5">
          {product.discount ? (
            <>
              <span className="text-lg font-bold text-red-600">
                {formatDiscountPrice(product.price, product.discount)}
              </span>
              <span className="text-sm text-gray-500 line-through">{formatPrice(product.price)}</span>
            </>
          ) : (
            <span className="text-lg font-bold text-gray-900">{formatPrice(product.price)}</span>
          )}
        </div>

        {/* Add to Cart Button */}
        <button
          onClick={handleAddToCart}
          disabled={isAddingToCart}
          className={`leading-2 w-full bg-white hover:bg-orange-500 text-black hover:text-white text-sm font-semibold py-2 px-2 rounded-2xl hover:rounded-3xl flex items-center justify-center  transition-all duration-200 ${
            isAddingToCart ? 'opacity-50 cursor-not-allowed' : ''
          }`}
        >
          <div
            className={`w-8 h-8 rounded-full flex items-center justify-center mr-1 ${
              isAddingToCart ? 'bg-gray-400' : 'bg-primary'
            }`}
          >
            {isAddingToCart ? (
              <div className="w-4 h-4 border-2 mx-1 border-white border-t-transparent rounded-full animate-spin "></div>
            ) : (
              <ShoppingBag className="w-4 h-4 text-white" />
            )}
          </div>
          {isAddingToCart ? 'ĐANG THÊM...' : 'THÊM VÀO GIỎ HÀNG'}
        </button>
      </div>
    </div>
  );
};

export default ProductItem;
