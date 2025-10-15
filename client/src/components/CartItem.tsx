import React from 'react';
import { X, Minus, Plus } from 'lucide-react';
import type { CartItem as CartItemType } from '../types/cart';

interface CartItemProps {
  item: CartItemType;
  onUpdateQuantity: (cartItemId: string, quantity: number) => void;
  onRemoveItem: (cartItemId: string) => void;
  isUpdating?: boolean;
}

const CartItem: React.FC<CartItemProps> = ({ 
  item, 
  onUpdateQuantity, 
  onRemoveItem, 
  isUpdating = false 
}) => {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  const getImageUrl = (imageUrl?: string) => {
    if (!imageUrl) return '/placeholder-image.jpg';
    
    if (imageUrl.startsWith('http')) {
      return imageUrl;
    }
    
    if (imageUrl.startsWith('/products/')) {
      return imageUrl;
    }
    
    const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
    return `/products/${cleanUrl}`;
  };



  return (
    <div className={`flex items-start gap-4 p-4 bg-white rounded-lg border border-gray-200 hover:shadow-md transition-shadow duration-200 ${
      isUpdating ? 'opacity-50 pointer-events-none' : ''
    }`}>
      {/* Product Image */}
      <div className="flex-shrink-0">
        <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden">
          {item.images && item.images.length > 0 ? (
            <img
              src={getImageUrl(item.images[0])}
              alt={item.productName}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.src = '/placeholder-image.jpg';
              }}
            />
          ) : (
            <div className="w-full h-full bg-gray-200 flex items-center justify-center">
              <span className="text-xs text-gray-400">No image</span>
            </div>
          )}
        </div>
      </div>

      {/* Product Info */}
      <div className="flex-1 min-w-0">
        {/* Product Name */}
        <h4 className="text-sm font-semibold text-gray-800 line-clamp-2 mb-2">
          <span className="text-red-700 font-bold">[{item.productId}]</span> {item.productName}
        </h4>
        
        {/* Price and Quantity Row */}
        <div className="flex items-center justify-between mb-3">
          {/* Price per unit */}
          <div className="text-sm text-gray-600">
            <span className="font-medium text-red-400">{formatPrice(item.productPrice)}</span>
          </div>

          {/* Quantity Controls */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => onUpdateQuantity(item.cartItemId, Math.max(1, item.quantity - 1))}
              disabled={isUpdating || item.quantity <= 1}
              className="w-6 h-6 rounded-md border border-gray-300 flex items-center justify-center hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Minus className="w-3 h-3 text-gray-600" />
            </button>
            <span className="text-sm font-semibold text-gray-800 min-w-[24px] text-center">
              {item.quantity}
            </span>
            <button
              onClick={() => onUpdateQuantity(item.cartItemId, item.quantity + 1)}
              disabled={isUpdating}
              className="w-6 h-6 rounded-md border border-gray-300 flex items-center justify-center hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Plus className="w-3 h-3 text-gray-600" />
            </button>
          </div>
        </div>


      </div>

      {/* Remove Button */}
      <button
        onClick={() => onRemoveItem(item.cartItemId)}
        disabled={isUpdating}
        className="flex-shrink-0 w-8 h-8 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-full flex items-center justify-center transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
        title="Xóa sản phẩm"
      >
        <X className="w-5 h-5" />
      </button>
    </div>
  );
};

export default CartItem;
