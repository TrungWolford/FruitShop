import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';
import { Badge } from '../ui/badge';
import { Button } from '../ui/Button/Button';
import { Package, Calendar, User, Tag, DollarSign, Box, FileText, Eye } from 'lucide-react';
import type { Product } from '../../types/product';

interface ViewProductModalProps {
    isOpen: boolean;
    onClose: () => void;
    product: Product | null;
}

const ViewProductModal: React.FC<ViewProductModalProps> = ({ isOpen, onClose, product }) => {
    if (!product) return null;

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    const parseServerDate = (value?: string | Date | number): Date | null => {
        if (!value && value !== 0) return null;
        if (value instanceof Date) return value;
        if (typeof value === 'number') return new Date(value);

        const isoAttempt = new Date(value as string);
        if (!Number.isNaN(isoAttempt.getTime())) return isoAttempt;

        const str = (value as string).trim();
        const dateTimeParts = str.split(' ');
        const dateParts = dateTimeParts[0].split('/');
        if (dateParts.length === 3) {
            const day = parseInt(dateParts[0], 10);
            const month = parseInt(dateParts[1], 10) - 1;
            const year = parseInt(dateParts[2], 10);

            let hours = 0,
                minutes = 0;

            if (dateTimeParts[1]) {
                const timeParts = dateTimeParts[1].split(':');
                hours = parseInt(timeParts[0] || '0', 10);
                minutes = parseInt(timeParts[1] || '0', 10);
            }

            const constructed = new Date(year, month, day, hours, minutes);
            if (!Number.isNaN(constructed.getTime())) return constructed;
        }

        return null;
    };

    const formatDate = (date?: string | Date | number) => {
        const d = parseServerDate(date);
        if (!d) return '—';
        return new Intl.DateTimeFormat('vi-VN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        }).format(d);
    };

    const getImageUrl = (imageUrl: string) => {
        if (!imageUrl) return '/placeholder-image.jpg';

        if (imageUrl.startsWith('http')) {
            return imageUrl;
        }

        const cleanUrl = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`;
        return `/products${cleanUrl}`;
    };

    const getStockStatus = (stock: number) => {
        if (stock > 50) return { color: 'text-green-600', bg: 'bg-green-50', text: 'Còn hàng' };
        if (stock > 10) return { color: 'text-yellow-600', bg: 'bg-yellow-50', text: 'Sắp hết' };
        return { color: 'text-red-600', bg: 'bg-red-50', text: 'Sắp hết' };
    };

    const numberToVietnameseWords = (num: number): string => {
        const ones = ['', 'một', 'hai', 'ba', 'bốn', 'năm', 'sáu', 'bảy', 'tám', 'chín'];
        const teens = [
            'mười',
            'mười một',
            'mười hai',
            'mười ba',
            'mười bốn',
            'mười lăm',
            'mười sáu',
            'mười bảy',
            'mười tám',
            'mười chín',
        ];
        const tens = [
            '',
            '',
            'hai mươi',
            'ba mươi',
            'bốn mươi',
            'năm mươi',
            'sáu mươi',
            'bảy mươi',
            'tám mươi',
            'chín mươi',
        ];

        if (num === 0) return 'không';
        if (num < 10) return ones[num];
        if (num < 20) return teens[num - 10];
        if (num < 100) {
            const ten = Math.floor(num / 10);
            const one = num % 10;
            if (one === 0) return tens[ten];
            if (one === 1) return `${tens[ten]} mốt`;
            if (one === 5) return `${tens[ten]} lăm`;
            return `${tens[ten]} ${ones[one]}`;
        }
        if (num < 1000) {
            const hundred = Math.floor(num / 100);
            const remainder = num % 100;
            if (remainder === 0) return `${ones[hundred]} trăm`;
            return `${ones[hundred]} trăm ${numberToVietnameseWords(remainder)}`;
        }
        if (num < 1000000) {
            const thousand = Math.floor(num / 1000);
            const remainder = num % 1000;
            if (remainder === 0) return `${numberToVietnameseWords(thousand)} nghìn`;
            return `${numberToVietnameseWords(thousand)} nghìn ${numberToVietnameseWords(remainder)}`;
        }
        if (num < 1000000000) {
            const million = Math.floor(num / 1000000);
            const remainder = num % 1000000;
            if (remainder === 0) return `${numberToVietnameseWords(million)} triệu`;
            return `${numberToVietnameseWords(million)} triệu ${numberToVietnameseWords(remainder)}`;
        }
        return 'quá lớn';
    };

    const stockStatus = getStockStatus(product.stock);

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
                <DialogHeader className="border-b border-gray-200 pb-4">
                    <DialogTitle className="flex items-center gap-3 text-xl">
                        <div className="p-2 bg-blue-100 rounded-lg">
                            <Eye className="w-6 h-6 text-blue-600" />
                        </div>
                        <span>Chi tiết sản phẩm</span>
                    </DialogTitle>
                </DialogHeader>

                <div className="pt-4 space-y-6">
                    {/* Main Content Grid */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Left Column - Product Image */}
                        <div className="lg:col-span-1">
                            <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                                <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
                                    <Package className="w-4 h-4" />
                                    Hình ảnh sản phẩm
                                </h3>
                                <div className="space-y-3">
                                    {product.images && product.images.length > 0 ? (
                                        <>
                                            {/* Main Image */}
                                            <div className="relative">
                                                <img
                                                    src={getImageUrl(product.images[0].imageUrl)}
                                                    alt={product.productName}
                                                    className="w-full h-64 object-cover rounded-lg border border-gray-200"
                                                    onError={(e) => {
                                                        e.currentTarget.src = '/placeholder-image.jpg';
                                                    }}
                                                />
                                            </div>
                                            {/* Thumbnail Images */}
                                            {product.images.length > 1 && (
                                                <div className="grid grid-cols-4 gap-2">
                                                    {product.images.slice(1, 5).map((image, index) => (
                                                        <img
                                                            key={index}
                                                            src={getImageUrl(image.imageUrl)}
                                                            alt={`${product.productName} ${index + 2}`}
                                                            className="w-full h-16 object-cover rounded border border-gray-200"
                                                            onError={(e) => {
                                                                e.currentTarget.src = '/placeholder-image.jpg';
                                                            }}
                                                        />
                                                    ))}
                                                </div>
                                            )}
                                        </>
                                    ) : (
                                        <div className="w-full h-64 bg-gray-100 flex items-center justify-center rounded-lg border border-gray-200">
                                            <div className="text-center">
                                                <Package className="w-12 h-12 text-gray-400 mx-auto mb-2" />
                                                <p className="text-sm text-gray-500">Không có hình ảnh</p>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Right Column - Product Details */}
                        <div className="lg:col-span-2">
                            <div className="bg-white rounded-lg border border-gray-200 p-6 space-y-4">
                                {/* Product Name, ID and Status - Same Row */}
                                <div>
                                    <label className="text-xs font-medium text-gray-600 mb-1 block">
                                        Thông tin sản phẩm
                                    </label>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <span className="text-base font-bold text-red-600">
                                                [Mã SP: {product.productId}]
                                            </span>
                                            <span className="text-lg font-bold text-blue-800">
                                                - {product.productName}
                                            </span>
                                        </div>
                                        <Badge
                                            variant={product.status === 1 ? 'default' : 'secondary'}
                                            className={`px-3 py-1 text-sm font-medium ${
                                                product.status === 1
                                                    ? 'bg-green-100 text-green-700 border-green-200'
                                                    : 'bg-red-100 text-red-700 border-red-200'
                                            }`}
                                        >
                                            {product.status === 1 ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                        </Badge>
                                    </div>
                                </div>

                                {/* Author and Cover */}
                                {/* author and cover removed from view */}

                                {/* Categories */}
                                <div>
                                    <label className="text-xs font-medium text-gray-600 mb-1 block flex items-center gap-1">
                                        <Tag className="w-3 h-3" />
                                        Danh mục
                                    </label>
                                    <div className="flex flex-wrap gap-2">
                                        {product.categories.map((category) => (
                                            <Badge
                                                key={category.categoryId}
                                                variant="outline"
                                                className="bg-blue-50 text-blue-700 border-blue-200 px-2 py-1 text-xs"
                                            >
                                                {category.categoryName}
                                            </Badge>
                                        ))}
                                    </div>
                                </div>

                                {/* Price and Stock */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label className="text-xs font-medium text-gray-600 mb-1 block">Giá bán</label>
                                        <p className="text-lg font-bold text-red-700">
                                            Giá: {formatPrice(product.price)}
                                        </p>
                                        <p className="text-xs text-gray-500 mt-1">
                                            ({numberToVietnameseWords(product.price)} đồng)
                                        </p>
                                    </div>
                                    <div>
                                        <label className="text-xs font-medium text-gray-600 mb-1 block flex items-center gap-1">
                                            <Box className="w-3 h-3" />
                                            Tồn kho
                                        </label>
                                        <p className="text-lg font-bold text-gray-900">{product.stock}</p>
                                    </div>
                                </div>

                                {/* Description */}
                                <div>
                                    <label className="text-xs font-medium text-gray-600 mb-1 block flex items-center gap-1">
                                        <FileText className="w-3 h-3" />
                                        Mô tả sản phẩm
                                    </label>
                                    <div className="bg-gray-50 p-2 rounded-lg">
                                        <p className="text-sm text-gray-700 leading-relaxed">{product.description}</p>
                                    </div>
                                </div>

                                {/* Created Date */}
                                <div>
                                    <label className="text-xs font-medium text-gray-600 mb-1 block flex items-center gap-1">
                                        <Calendar className="w-3 h-3" />
                                        Ngày tạo
                                    </label>
                                    <p className="text-sm text-gray-900 font-medium">{formatDate(product.createdAt)}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default ViewProductModal;
