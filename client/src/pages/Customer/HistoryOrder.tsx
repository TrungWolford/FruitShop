import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Search, Package, CheckCircle, ChevronDown, Truck, User, Phone, MapPin, Star, X } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { orderService } from '../../services/orderService';
import type { OrderResponse } from '../../services/orderService';
import { ratingService } from '../../services/ratingService';
import type { CreateRatingRequest } from '../../types/rating';
import { toast } from 'sonner';

const HistoryReceipt: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useAppSelector((state) => state.auth);

    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
    const [showOrderDetail, setShowOrderDetail] = useState(false);
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Rating dialog states
    const [showRatingDialog, setShowRatingDialog] = useState(false);
    const [selectedProductForRating, setSelectedProductForRating] = useState<{
        productId: string;
        productName: string;
        productImage?: string;
        orderItemId?: string; // Add orderItemId to link rating to specific order item
    } | null>(null);
    const [ratingStars, setRatingStars] = useState(5);
    const [ratingComment, setRatingComment] = useState('');
    const [isSubmittingRating, setIsSubmittingRating] = useState(false);
    
    // Track rated order items: Map<orderDetailId, boolean>
    const [ratedOrderItems, setRatedOrderItems] = useState<Map<string, boolean>>(new Map());

    // Fetch orders từ API
    const fetchOrders = async () => {
        if (!user?.accountId) return;

        setIsLoading(true);
        try {
            const response = await orderService.getOrdersByAccount(user.accountId);
            if (response.success && response.data) {
                console.log('📦 Fetched orders:', response.data);

                // Debug: Log product images for each order
                response.data.forEach((order, orderIndex) => {
                    console.log(`📦 Order ${orderIndex + 1} (${order.orderId}):`);
                    order.orderDetails.forEach((detail, detailIndex) => {
                        console.log(`  📦 Detail ${detailIndex + 1}: ${detail.productName}`);
                        console.log(`  🖼️ Images:`, detail.productImages);
                        if (detail.productImages && detail.productImages.length > 0) {
                            detail.productImages.forEach((imageUrl, imageIndex) => {
                                console.log(
                                    `    🖼️ Image ${imageIndex + 1}: "${imageUrl}" -> "${getImageUrl(imageUrl)}"`,
                                );
                            });
                        }
                    });
                });

                setOrders(response.data);
                
                // Check which order items have been rated
                await checkRatedOrderItems(response.data);
            } else {
                console.error('Failed to fetch orders:', response.message);
                setOrders([]);
            }
        } catch (error) {
            console.error('Error fetching orders:', error);
            setOrders([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Check which order items the user has already rated
    const checkRatedOrderItems = async (orders: OrderResponse[]) => {
        if (!user?.accountId) return;

        const ratedMap = new Map<string, boolean>();
        
        // Get all order items
        const orderItems = new Map<string, { orderDetailId: string; productId: string }>();
        orders.forEach(order => {
            order.orderDetails.forEach(detail => {
                orderItems.set(detail.orderDetailId, {
                    orderDetailId: detail.orderDetailId,
                    productId: detail.productId
                });
            });
        });

        console.log('📋 Total orderItems:', orderItems.size);

        // Get unique product IDs to fetch their ratings
        const productIds = new Set<string>(
            Array.from(orderItems.values()).map(item => item.productId)
        );

        console.log('📦 Unique products to check:', productIds.size);

        // Check each product and see which orderItems are rated
        for (const productId of productIds) {
            try {
                const ratings = await ratingService.getRatingByAccountAndProduct(user.accountId, productId);
                
                console.log(`✅ Ratings for product ${productId}:`, ratings);

                // Mark orderItems that have ratings
                if (ratings && Array.isArray(ratings)) {
                    ratings.forEach(rating => {
                        if (rating.orderItemId) {
                            console.log(`⭐ Marking orderItem ${rating.orderItemId} as rated`);
                            ratedMap.set(rating.orderItemId, true);
                        } else {
                            console.warn('⚠️ Rating without orderItemId:', rating);
                        }
                    });
                }
            } catch (error) {
                // If error, continue to next product
                console.error(`❌ Error fetching ratings for product ${productId}:`, error);
            }
        }

        // Mark all orderItems not in ratedMap as not rated
        orderItems.forEach((item) => {
            if (!ratedMap.has(item.orderDetailId)) {
                ratedMap.set(item.orderDetailId, false);
            }
        });

        console.log('📊 Final rated map size:', ratedMap.size);
        console.log('📊 Rated orderItems:', Array.from(ratedMap.entries()).filter(([_, rated]) => rated));

        setRatedOrderItems(ratedMap);
    };

    // Lọc đơn hàng theo search và status
    const filteredOrders = orders.filter((order) => {
        const matchesSearch =
            order.orderId.toLowerCase().includes(searchTerm.toLowerCase()) ||
            order.orderDetails.some((detail) => detail.productName.toLowerCase().includes(searchTerm.toLowerCase()));

        if (statusFilter === 'all') return matchesSearch;
        return matchesSearch && order.status.toString() === statusFilter;
    });

    const getStatusColor = (status: number) => {
        switch (status) {
            case 0:
                return 'bg-red-600 text-white border-red-600'; // Đã hủy
            case 1:
                return 'bg-yellow-600 text-white border-yellow-600'; // Chờ xác nhận
            case 2:
                return 'bg-blue-600 text-white border-blue-600'; // Đã xác nhận
            case 3:
                return 'bg-purple-600 text-white border-purple-600'; // Đang giao
            case 4:
                return 'bg-green-600 text-white border-green-600'; // Đã giao (hoàn thành)
            default:
                return 'bg-gray-600 text-white border-gray-600';
        }
    };

    const getStatusText = (status: number) => {
        switch (status) {
            case 0:
                return 'Đã hủy';
            case 1:
                return 'Chờ xác nhận';
            case 2:
                return 'Đã xác nhận';
            case 3:
                return 'Đang giao';
            case 4:
                return 'Đã giao';
            default:
                return 'Không xác định';
        }
    };

    const getPaymentMethodText = (paymentMethod: number) => {
        switch (paymentMethod) {
            case 0:
                return 'Tiền mặt (COD)';
            case 1:
                return 'Chuyển khoản';
            default:
                return 'Không xác định';
        }
    };

    const getProgressStep = (status: number) => {
        // Map order status to progress step (1-4)
        switch (status) {
            case 0:
                return 0; // Đã hủy - không hiển thị progress
            case 1:
                return 1; // Chờ xác nhận - step 1
            case 2:
                return 2; // Đã xác nhận - step 2
            case 3:
                return 3; // Đang giao - step 3
            case 4:
                return 4; // Đã giao (hoàn thành) - step 4
            default:
                return 0;
        }
    };

    const getProgressColor = (currentStep: number, targetStep: number) => {
        if (currentStep >= targetStep) {
            switch (targetStep) {
                case 1:
                    return 'bg-yellow-500'; // Chờ xác nhận
                case 2:
                    return 'bg-blue-500'; // Đã xác nhận
                case 3:
                    return 'bg-purple-500'; // Đang giao
                case 4:
                    return 'bg-green-500'; // Đã giao
                default:
                    return 'bg-green-500';
            }
        }
        return 'bg-gray-300';
    };

    const getCurrentStatusColor = (status: number) => {
        switch (status) {
            case 0:
                return 'bg-red-500'; // Đã hủy
            case 1:
                return 'bg-yellow-500'; // Chờ xác nhận
            case 2:
                return 'bg-blue-500'; // Đã xác nhận
            case 3:
                return 'bg-purple-500'; // Đang giao
            case 4:
                return 'bg-green-500'; // Đã giao
            default:
                return 'bg-gray-500';
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    // Helper function to construct proper image URL
    const getImageUrl = (imageUrl: string) => {
        // If imageUrl is already a full URL, return as is
        if (imageUrl?.startsWith('http://') || imageUrl?.startsWith('https://') || imageUrl?.startsWith('/')) {
            return imageUrl;
        }

        // If imageUrl is just a filename, construct the full path
        return `/products/${imageUrl}`;
    };

    const handleViewOrderDetail = (order: OrderResponse) => {
        setSelectedOrder(order);
        setShowOrderDetail(true);
    };

    const handleCloseOrderDetail = () => {
        setShowOrderDetail(false);
        setSelectedOrder(null);
    };

    const handleOrderClick = (order: OrderResponse) => {
        handleViewOrderDetail(order);
    };

    const handleOpenRatingDialog = (productId: string, productName: string, productImage?: string, orderItemId?: string) => {
        setSelectedProductForRating({ productId, productName, productImage, orderItemId });
        setRatingStars(5);
        setRatingComment('');
        setShowRatingDialog(true);
    };

    const handleCloseRatingDialog = () => {
        setShowRatingDialog(false);
        setSelectedProductForRating(null);
        setRatingStars(5);
        setRatingComment('');
    };

    const handleSubmitRating = async () => {
        if (!user?.accountId || !selectedProductForRating) return;

        setIsSubmittingRating(true);
        try {
            const ratingData: CreateRatingRequest = {
                accountId: user.accountId,
                productId: selectedProductForRating.productId,
                orderItemId: selectedProductForRating.orderItemId, // Link to specific order item
                comment: ratingComment,
                ratingStar: ratingStars,
            };

            await ratingService.createRating(ratingData);
            
            // Update rated order items map immediately
            if (selectedProductForRating.orderItemId) {
                setRatedOrderItems(prev => {
                    const newMap = new Map(prev);
                    newMap.set(selectedProductForRating.orderItemId!, true);
                    return newMap;
                });
            }
            
            toast.success('Đánh giá của bạn đã được gửi thành công!', {
                duration: 3000,
            });
            
            handleCloseRatingDialog();
            
            // Reload orders to refresh rating status
            await fetchOrders();
        } catch (error) {
            console.error('Error submitting rating:', error);
            toast.error('Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại!', {
                duration: 3000,
            });
        } finally {
            setIsSubmittingRating(false);
        }
    };

    useEffect(() => {
        if (!user) {
            navigate('/');
            return;
        }
        fetchOrders();
    }, [user, navigate]);

    // Refresh orders after successful checkout
    useEffect(() => {
        const handleOrderCreated = () => {
            fetchOrders();
        };

        window.addEventListener('orderCreated', handleOrderCreated);

        return () => {
            window.removeEventListener('orderCreated', handleOrderCreated);
        };
    }, []);

    if (!user) {
        return null;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <TopNavigation />

            <main className="container mx-auto px-4 py-8">
                <div className="max-w-6xl mx-auto">
                    {/* Header */}
                    <div className="mb-8">
                        <h1 className="text-3xl font-bold text-gray-900 mb-2">Lịch sử đơn hàng</h1>
                        <p className="text-gray-600">Theo dõi và quản lý các đơn hàng của bạn</p>
                    </div>

                    {/* Search and Filter */}
                    <div className="bg-white border-2 border-gray-200 p-6 mb-6 ">
                        <div className="flex flex-col lg:flex-row gap-4 items-center justify-between">
                            <div className="flex gap-4 flex-1">
                                <div className="relative max-w-xs">
                                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                    <Input
                                        placeholder="Tìm kiếm đơn hàng..."
                                        value={searchTerm}
                                        onChange={(e) => setSearchTerm(e.target.value)}
                                        className="pl-10  border-2 border-gray-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                                    />
                                </div>
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button
                                            variant="outline"
                                            className="flex items-center gap-2  border-2 border-gray-200 hover:bg-gray-50"
                                        >
                                            <span>
                                                {statusFilter === 'all'
                                                    ? 'Tất cả trạng thái'
                                                    : statusFilter === '0'
                                                    ? 'Đã hủy'
                                                    : statusFilter === '1'
                                                    ? 'Đang vận chuyển'
                                                    : statusFilter === '2'
                                                    ? 'Đã hoàn thành'
                                                    : 'Tất cả trạng thái'}
                                            </span>
                                            <ChevronDown className="w-4 h-4" />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent className="w-48  border-2 border-gray-200">
                                        <DropdownMenuItem
                                            onClick={() => setStatusFilter('all')}
                                            className="flex items-center gap-2 cursor-pointer hover:bg-gray-50"
                                        >
                                            <div className="w-2 h-2  bg-gray-400"></div>
                                            <span>Tất cả trạng thái</span>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem
                                            onClick={() => setStatusFilter('0')}
                                            className="flex items-center gap-2 cursor-pointer hover:bg-gray-50"
                                        >
                                            <div className="w-2 h-2  bg-red-500"></div>
                                            <span>Đã hủy</span>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem
                                            onClick={() => setStatusFilter('1')}
                                            className="flex items-center gap-2 cursor-pointer hover:bg-gray-50"
                                        >
                                            <div className="w-2 h-2  bg-blue-500"></div>
                                            <span>Đang vận chuyển</span>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem
                                            onClick={() => setStatusFilter('2')}
                                            className="flex items-center gap-2 cursor-pointer hover:bg-gray-50"
                                        >
                                            <div className="w-2 h-2  bg-green-500"></div>
                                            <span>Đã hoàn thành</span>
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>
                            <div className="text-sm text-gray-600">
                                Tổng cộng: <span className="font-semibold">{filteredOrders.length}</span> đơn hàng
                            </div>
                        </div>
                    </div>

                    {/* Orders List */}
                    <div className="space-y-6">
                        {isLoading ? (
                            <Card className=" border-2 border-gray-200">
                                <CardContent className="p-12 text-center">
                                    <div className="w-16 h-16 border-4 border-blue-200 border-t-blue-600  animate-spin mx-auto mb-4"></div>
                                    <h3 className="text-lg font-semibold text-gray-700 mb-2">Đang tải đơn hàng...</h3>
                                    <p className="text-gray-500">Vui lòng chờ trong giây lát</p>
                                </CardContent>
                            </Card>
                        ) : filteredOrders.length === 0 ? (
                            <Card className=" border-2 border-gray-200">
                                <CardContent className="p-12 text-center">
                                    <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                                    <h3 className="text-lg font-semibold text-gray-700 mb-2">Không có đơn hàng nào</h3>
                                    <p className="text-gray-500 mb-4">
                                        {searchTerm || statusFilter !== 'all'
                                            ? 'Không tìm thấy đơn hàng phù hợp với bộ lọc của bạn'
                                            : 'Bạn chưa có đơn hàng nào. Hãy bắt đầu mua sắm ngay!'}
                                    </p>
                                    {!searchTerm && statusFilter === 'all' && (
                                        <Button
                                            onClick={() => navigate('/')}
                                            className="bg-blue-600 hover:bg-blue-700 "
                                        >
                                            Mua sắm ngay
                                        </Button>
                                    )}
                                </CardContent>
                            </Card>
                        ) : (
                            filteredOrders.map((order) => {
                                const currentStep = getProgressStep(order.status);
                                return (
                                    <Card
                                        key={order.orderId}
                                        className=" border-2 border-gray-200 hover:shadow-lg transition-shadow cursor-pointer"
                                        onDoubleClick={() => handleOrderClick(order)}
                                    >
                                        <CardHeader className="border-b border-gray-200">
                                            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                                                <div className="flex flex-col lg:flex-row lg:items-center gap-4 flex-1">
                                                    <div className="flex-1">
                                                        <CardTitle
                                                            className="text-lg font-semibold text-gray-900 cursor-pointer hover:text-blue-600 transition-colors truncate"
                                                            onClick={() => handleOrderClick(order)}
                                                            title={order.orderId}
                                                        >
                                                            #{order.orderId}
                                                        </CardTitle>
                                                        <CardDescription className="text-sm text-gray-600">
                                                            Đặt hàng lúc {formatDate(order.orderDate)}
                                                        </CardDescription>
                                                    </div>
                                                    <div className="flex gap-2">
                                                        <Badge className={` border ${getStatusColor(order.status)}`}>
                                                            {getStatusText(order.status)}
                                                        </Badge>
                                                    </div>
                                                </div>
                                                <div className="flex items-center gap-2">
                                                    <Badge className=" border bg-gray-100 text-gray-700 border-gray-300">
                                                        {getPaymentMethodText(order.paymentMethod)}
                                                    </Badge>
                                                </div>
                                            </div>
                                        </CardHeader>
                                        <CardContent className="p-6">
                                            {/* Progress Bar for Order Status - Full Width */}
                                            {order.status !== 0 && (
                                                <div className="mb-6">
                                                    <div className="flex items-center justify-between mb-2">
                                                        <span className="text-sm font-medium text-gray-700">
                                                            Tiến trình đơn hàng
                                                        </span>
                                                    </div>
                                                    <div className="relative">
                                                        <div className="flex items-center">
                                                            {/* Progress Line */}
                                                            <div className="flex-1 h-2 bg-gray-200  overflow-hidden">
                                                                <div
                                                                    className={`h-full transition-all duration-500 ${
                                                                        currentStep >= 1
                                                                            ? getCurrentStatusColor(order.status)
                                                                            : 'bg-gray-300'
                                                                    }`}
                                                                    style={{
                                                                        width: `${Math.max(
                                                                            0,
                                                                            Math.min(100, (currentStep / 4) * 100),
                                                                        )}%`,
                                                                    }}
                                                                ></div>
                                                            </div>
                                                        </div>

                                                        {/* Progress Steps */}
                                                        <div className="flex justify-between mt-2">
                                                            <div className="flex flex-col items-center">
                                                                <div
                                                                    className={`w-6 h-6  flex items-center justify-center text-xs font-medium ${
                                                                        currentStep >= 1
                                                                            ? getProgressColor(currentStep, 1) +
                                                                              ' text-white'
                                                                            : 'bg-gray-300 text-gray-600'
                                                                    }`}
                                                                >
                                                                    {currentStep >= 1 ? (
                                                                        <CheckCircle className="w-4 h-4" />
                                                                    ) : (
                                                                        '1'
                                                                    )}
                                                                </div>
                                                                <span className="text-xs text-gray-600 mt-1">
                                                                    Chờ xác nhận
                                                                </span>
                                                            </div>
                                                            <div className="flex flex-col items-center">
                                                                <div
                                                                    className={`w-6 h-6  flex items-center justify-center text-xs font-medium ${
                                                                        currentStep >= 2
                                                                            ? getProgressColor(currentStep, 2) +
                                                                              ' text-white'
                                                                            : 'bg-gray-300 text-gray-600'
                                                                    }`}
                                                                >
                                                                    {currentStep >= 2 ? (
                                                                        <CheckCircle className="w-4 h-4" />
                                                                    ) : (
                                                                        '2'
                                                                    )}
                                                                </div>
                                                                <span className="text-xs text-gray-600 mt-1">
                                                                    Đã xác nhận
                                                                </span>
                                                            </div>
                                                            <div className="flex flex-col items-center">
                                                                <div
                                                                    className={`w-6 h-6  flex items-center justify-center text-xs font-medium ${
                                                                        currentStep >= 3
                                                                            ? getProgressColor(currentStep, 3) +
                                                                              ' text-white'
                                                                            : 'bg-gray-300 text-gray-600'
                                                                    }`}
                                                                >
                                                                    {currentStep >= 3 ? (
                                                                        <CheckCircle className="w-4 h-4" />
                                                                    ) : (
                                                                        '3'
                                                                    )}
                                                                </div>
                                                                <span className="text-xs text-gray-600 mt-1">
                                                                    Đang giao
                                                                </span>
                                                            </div>
                                                            <div className="flex flex-col items-center">
                                                                <div
                                                                    className={`w-6 h-6  flex items-center justify-center text-xs font-medium ${
                                                                        currentStep >= 4
                                                                            ? getProgressColor(currentStep, 4) +
                                                                              ' text-white'
                                                                            : 'bg-gray-300 text-gray-600'
                                                                    }`}
                                                                >
                                                                    {currentStep >= 4 ? (
                                                                        <CheckCircle className="w-4 h-4" />
                                                                    ) : (
                                                                        '4'
                                                                    )}
                                                                </div>
                                                                <span className="text-xs text-gray-600 mt-1">
                                                                    Đã giao
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            )}

                                            {/* Two Column Layout: Order Info Left, Shipping Info Right */}
                                            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                                {/* LEFT COLUMN: Order Information */}
                                                <div className="space-y-4">
                                                    {/* Order Items Preview */}
                                                    <div>
                                                        <h3 className="font-semibold text-gray-900 mb-3">
                                                            Sản phẩm đã đặt
                                                        </h3>
                                                        <div className="space-y-3">
                                                            {/* Always show up to 3 products, maintaining consistent layout */}
                                                            {Array.from(
                                                                { length: Math.min(3, order.orderDetails.length) },
                                                                (_, index) => {
                                                                    const detail = order.orderDetails[index];
                                                                    return (
                                                                        <div
                                                                            key={
                                                                                detail?.orderDetailId ||
                                                                                `placeholder-${index}`
                                                                            }
                                                                            className="flex items-center gap-4 p-4 bg-gray-50"
                                                                        >
                                                                            <div className="relative w-20 h-20 bg-gradient-to-br from-gray-100 to-gray-200 border flex items-center justify-center overflow-hidden shadow-sm">
                                                                                {detail &&
                                                                                detail.productImages &&
                                                                                detail.productImages.length > 0 ? (
                                                                                    <>
                                                                                        <img
                                                                                            src={getImageUrl(
                                                                                                detail.productImages[0],
                                                                                            )}
                                                                                            alt={detail.productName}
                                                                                            className="w-full h-full object-cover transition-opacity duration-200"
                                                                                            onError={(e) => {
                                                                                                console.log(
                                                                                                    '❌ Image failed to load:',
                                                                                                    getImageUrl(
                                                                                                        detail
                                                                                                            .productImages[0],
                                                                                                    ),
                                                                                                );
                                                                                                const target =
                                                                                                    e.target as HTMLImageElement;
                                                                                                const fallback =
                                                                                                    target.nextElementSibling as HTMLElement;
                                                                                                target.style.display =
                                                                                                    'none';
                                                                                                if (fallback)
                                                                                                    fallback.classList.remove(
                                                                                                        'hidden',
                                                                                                    );
                                                                                            }}
                                                                                            onLoad={() => {
                                                                                                console.log(
                                                                                                    '✅ Image loaded successfully:',
                                                                                                    getImageUrl(
                                                                                                        detail
                                                                                                            .productImages[0],
                                                                                                    ),
                                                                                                );
                                                                                            }}
                                                                                        />
                                                                                        <div className="hidden absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200">
                                                                                            <Package className="w-6 h-6 text-blue-500" />
                                                                                        </div>
                                                                                    </>
                                                                                ) : (
                                                                                    <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200">
                                                                                        <Package className="w-6 h-6 text-blue-500" />
                                                                                    </div>
                                                                                )}
                                                                            </div>
                                                                            <div className="flex-1">
                                                                                <h4 className="font-medium text-gray-900 text-sm">
                                                                                    {detail?.productName ||
                                                                                        'Sản phẩm không xác định'}
                                                                                </h4>
                                                                                <p className="text-xs text-gray-600">
                                                                                    Số lượng: {detail?.quantity || 0} x{' '}
                                                                                    {formatPrice(
                                                                                        detail?.unitPrice || 0,
                                                                                    )}
                                                                                </p>
                                                                                {/* Rating button for completed orders */}
                                                                                {order.status === 4 && detail && (
                                                                                    ratedOrderItems.get(detail.orderDetailId) ? (
                                                                                        <Button
                                                                                            size="sm"
                                                                                            className="mt-2 bg-gray-400 text-white text-xs cursor-not-allowed"
                                                                                            disabled
                                                                                        >
                                                                                            <Star className="w-3 h-3 mr-1 fill-white" />
                                                                                            Đã đánh giá
                                                                                        </Button>
                                                                                    ) : (
                                                                                        <Button
                                                                                            onClick={(e) => {
                                                                                                e.stopPropagation();
                                                                                                handleOpenRatingDialog(
                                                                                                    detail.productId,
                                                                                                    detail.productName,
                                                                                                    detail.productImages?.[0],
                                                                                                    detail.orderDetailId, // Pass orderDetailId
                                                                                                );
                                                                                            }}
                                                                                            size="sm"
                                                                                            className="mt-2 bg-amber-500 hover:bg-amber-600 text-white text-xs"
                                                                                        >
                                                                                            <Star className="w-3 h-3 mr-1" />
                                                                                            Đánh giá
                                                                                        </Button>
                                                                                    )
                                                                                )}
                                                                            </div>
                                                                            <div className="text-right">
                                                                                <p className="font-semibold text-gray-900 text-sm">
                                                                                    {formatPrice(
                                                                                        detail?.totalPrice || 0,
                                                                                    )}
                                                                                </p>
                                                                            </div>
                                                                        </div>
                                                                    );
                                                                },
                                                            )}

                                                            {/* Show "more products" message if there are more than 3 */}
                                                            {order.orderDetails.length > 3 && (
                                                                <div className="text-center py-3 text-sm text-gray-600 border-t border-gray-200 bg-gray-50">
                                                                    Và {order.orderDetails.length - 3} sản phẩm khác...
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>

                                                    {/* Order Summary */}
                                                    <div className="pt-4 border-t border-gray-200">
                                                        <div className="flex justify-between items-center text-lg font-bold text-red-600">
                                                            <span>Tổng thanh toán ({order.totalItems} sản phẩm):</span>
                                                            <span>{formatPrice(order.totalAmount)}</span>
                                                        </div>
                                                    </div>
                                                </div>

                                                {/* RIGHT COLUMN: Shipping Information */}
                                                {order.shipping && (
                                                    <div className="space-y-4">
                                                        <div>
                                                            <div className="flex items-center justify-between mb-3">
                                                                <h3 className="font-semibold text-gray-900">
                                                                    Thông tin giao hàng
                                                                </h3>
                                                            </div>
                                                            <div className="space-y-3">
                                                                {/* Receiver Name */}
                                                                <div className="p-4 bg-gray-50">
                                                                    <h4 className="font-medium text-gray-900 text-sm mb-1">
                                                                        Người nhận
                                                                    </h4>
                                                                    <p className="text-sm text-gray-600">
                                                                        {order.shipping.receiverName}
                                                                    </p>
                                                                </div>

                                                                {/* Phone Number */}
                                                                <div className="p-4 bg-gray-50">
                                                                    <h4 className="font-medium text-gray-900 text-sm mb-1">
                                                                        Số điện thoại
                                                                    </h4>
                                                                    <p className="text-sm text-gray-600">
                                                                        {order.shipping.receiverPhone}
                                                                    </p>
                                                                </div>

                                                                {/* Address */}
                                                                <div className="p-4 bg-gray-50">
                                                                    <h4 className="font-medium text-gray-900 text-sm mb-1">
                                                                        Địa chỉ giao hàng
                                                                    </h4>
                                                                    <p className="text-sm text-gray-600 leading-relaxed">
                                                                        {order.shipping.receiverAddress}
                                                                    </p>
                                                                    <p className="text-sm text-gray-500 mt-1">
                                                                        {order.shipping.city}
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </CardContent>
                                    </Card>
                                );
                            })
                        )}
                    </div>
                </div>
            </main>

            {/* Order Detail Modal - Temporarily disabled until DetailOrder is updated */}
            {selectedOrder && showOrderDetail && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white  p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold truncate" title={selectedOrder.orderId}>
                                Chi tiết đơn hàng #{selectedOrder.orderId}
                            </h2>
                            <Button onClick={handleCloseOrderDetail} variant="outline" size="sm">
                                Đóng
                            </Button>
                        </div>
                        <div className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-gray-600">Ngày đặt hàng</p>
                                    <p className="font-semibold">{formatDate(selectedOrder.orderDate)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Trạng thái</p>
                                    <Badge className={`${getStatusColor(selectedOrder.status)}`}>
                                        {getStatusText(selectedOrder.status)}
                                    </Badge>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Phương thức thanh toán</p>
                                    <p className="font-semibold">{getPaymentMethodText(selectedOrder.paymentMethod)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Tổng tiền</p>
                                    <p className="font-semibold text-red-600">
                                        {formatPrice(selectedOrder.totalAmount)}
                                    </p>
                                </div>
                            </div>

                            {/* Shipping Information */}
                            {selectedOrder.shipping && (
                                <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-6  border border-blue-200 shadow-sm">
                                    <div className="flex items-center justify-between gap-3 mb-4">
                                        <div className="flex items-center gap-3">
                                            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600  flex items-center justify-center shadow-md">
                                                <Truck className="w-5 h-5 text-white" />
                                            </div>
                                            <h3 className="font-bold text-blue-900 text-lg">Thông tin giao hàng</h3>
                                        </div>
                                    </div>
                                    <div className="space-y-4">
                                        <div className="bg-white p-4  border border-blue-100">
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                <div className="space-y-3">
                                                    <div className="flex items-center gap-3">
                                                        <User className="w-5 h-5 text-blue-600" />
                                                        <div>
                                                            <p className="text-sm font-medium text-gray-600">
                                                                Người nhận
                                                            </p>
                                                            <p className="font-bold text-gray-900">
                                                                {selectedOrder.shipping.receiverName}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center gap-3">
                                                        <Phone className="w-5 h-5 text-blue-600" />
                                                        <div>
                                                            <p className="text-sm font-medium text-gray-600">
                                                                Số điện thoại
                                                            </p>
                                                            <p className="font-bold text-gray-900">
                                                                {selectedOrder.shipping.receiverPhone}
                                                            </p>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className="space-y-3">
                                                    <div className="flex items-start gap-3">
                                                        <MapPin className="w-5 h-5 text-blue-600 mt-1" />
                                                        <div>
                                                            <p className="text-sm font-medium text-gray-600">
                                                                Địa chỉ giao hàng
                                                            </p>
                                                            <p className="font-bold text-gray-900 leading-relaxed">
                                                                {selectedOrder.shipping.receiverAddress}
                                                            </p>
                                                            <p className="text-sm text-blue-700 font-medium mt-1">
                                                                📍 {selectedOrder.shipping.city}
                                                            </p>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}

                            <div>
                                <h3 className="font-semibold mb-3">Danh sách sản phẩm</h3>
                                <div className="space-y-3">
                                    {selectedOrder.orderDetails.map((detail) => (
                                        <div
                                            key={detail.orderDetailId}
                                            className="flex items-center gap-4 p-4 border bg-white shadow-sm hover:shadow-md transition-shadow"
                                        >
                                            <div className="relative w-20 h-20 bg-gradient-to-br from-gray-100 to-gray-200 border flex items-center justify-center overflow-hidden shadow-sm">
                                                {detail.productImages && detail.productImages.length > 0 ? (
                                                    <>
                                                        <img
                                                            src={getImageUrl(detail.productImages[0])}
                                                            alt={detail.productName}
                                                            className="w-full h-full object-cover transition-transform duration-200 hover:scale-105"
                                                            onError={(e) => {
                                                                console.log(
                                                                    '❌ Detail modal image failed to load:',
                                                                    getImageUrl(detail.productImages[0]),
                                                                );
                                                                const target = e.target as HTMLImageElement;
                                                                const fallback =
                                                                    target.nextElementSibling as HTMLElement;
                                                                target.style.display = 'none';
                                                                if (fallback) fallback.classList.remove('hidden');
                                                            }}
                                                            onLoad={() => {
                                                                console.log(
                                                                    '✅ Detail modal image loaded successfully:',
                                                                    getImageUrl(detail.productImages[0]),
                                                                );
                                                            }}
                                                        />
                                                        <div className="hidden absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200">
                                                            <Package className="w-8 h-8 text-blue-500" />
                                                        </div>
                                                    </>
                                                ) : (
                                                    <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200">
                                                        <Package className="w-8 h-8 text-blue-500" />
                                                    </div>
                                                )}

                                                {/* Image count indicator */}
                                                {detail.productImages && detail.productImages.length > 1 && (
                                                    <div className="absolute -top-1 -right-1 bg-blue-600 text-white text-xs  w-5 h-5 flex items-center justify-center font-bold shadow-md">
                                                        +{detail.productImages.length - 1}
                                                    </div>
                                                )}
                                            </div>
                                            <div className="flex-1">
                                                <h4 className="font-medium">{detail.productName}</h4>
                                                <p className="text-sm text-gray-600">
                                                    {detail.quantity} x {formatPrice(detail.unitPrice)}
                                                </p>
                                                {/* Rating button for completed orders in detail modal */}
                                                {selectedOrder.status === 4 && (
                                                    ratedOrderItems.get(detail.orderDetailId) ? (
                                                        <Button
                                                            size="sm"
                                                            className="mt-2 bg-gray-400 text-white text-xs cursor-not-allowed"
                                                            disabled
                                                        >
                                                            <Star className="w-3 h-3 mr-1 fill-white" />
                                                            Đã đánh giá
                                                        </Button>
                                                    ) : (
                                                        <Button
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                handleOpenRatingDialog(
                                                                    detail.productId,
                                                                    detail.productName,
                                                                    detail.productImages?.[0],
                                                                    detail.orderDetailId, // Pass orderDetailId
                                                                );
                                                            }}
                                                            size="sm"
                                                            className="mt-2 bg-amber-500 hover:bg-amber-600 text-white text-xs"
                                                        >
                                                            <Star className="w-3 h-3 mr-1" />
                                                            Đánh giá
                                                        </Button>
                                                    )
                                                )}
                                            </div>
                                            <div className="text-right">
                                                <p className="font-semibold">{formatPrice(detail.totalPrice)}</p>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Rating Dialog */}
            {showRatingDialog && selectedProductForRating && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold text-gray-900">Đánh giá sản phẩm</h2>
                            <button
                                onClick={handleCloseRatingDialog}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X className="w-6 h-6" />
                            </button>
                        </div>

                        {/* Product Info */}
                        <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg mb-6">
                            <div className="relative w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg flex items-center justify-center overflow-hidden">
                                {selectedProductForRating.productImage ? (
                                    <img
                                        src={getImageUrl(selectedProductForRating.productImage)}
                                        alt={selectedProductForRating.productName}
                                        className="w-full h-full object-cover"
                                        onError={(e) => {
                                            const target = e.target as HTMLImageElement;
                                            target.style.display = 'none';
                                            const fallback = target.nextElementSibling as HTMLElement;
                                            if (fallback) fallback.classList.remove('hidden');
                                        }}
                                    />
                                ) : null}
                                <div className={`${selectedProductForRating.productImage ? 'hidden' : ''} absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200`}>
                                    <Package className="w-6 h-6 text-blue-500" />
                                </div>
                            </div>
                            <div className="flex-1">
                                <h3 className="font-semibold text-gray-900">
                                    {selectedProductForRating.productName}
                                </h3>
                            </div>
                        </div>

                        {/* Star Rating */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Đánh giá của bạn
                            </label>
                            <div className="flex gap-2 justify-center">
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <button
                                        key={star}
                                        onClick={() => setRatingStars(star)}
                                        className="transition-transform hover:scale-110 focus:outline-none"
                                    >
                                        <Star
                                            className={`w-10 h-10 ${
                                                star <= ratingStars
                                                    ? 'fill-yellow-400 text-yellow-400'
                                                    : 'text-gray-300'
                                            }`}
                                        />
                                    </button>
                                ))}
                            </div>
                            <p className="text-center text-sm text-gray-600 mt-2">
                                {ratingStars === 1 && 'Rất không hài lòng'}
                                {ratingStars === 2 && 'Không hài lòng'}
                                {ratingStars === 3 && 'Bình thường'}
                                {ratingStars === 4 && 'Hài lòng'}
                                {ratingStars === 5 && 'Rất hài lòng'}
                            </p>
                        </div>

                        {/* Comment */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Nhận xét của bạn
                            </label>
                            <textarea
                                value={ratingComment}
                                onChange={(e) => setRatingComment(e.target.value)}
                                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
                                rows={4}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                            />
                        </div>

                        {/* Action Buttons */}
                        <div className="flex gap-3">
                            <Button
                                onClick={handleCloseRatingDialog}
                                variant="outline"
                                className="flex-1"
                                disabled={isSubmittingRating}
                            >
                                Hủy
                            </Button>
                            <Button
                                onClick={handleSubmitRating}
                                className="flex-1 bg-blue-600 hover:bg-blue-700"
                                disabled={isSubmittingRating || !ratingComment.trim()}
                            >
                                {isSubmittingRating ? 'Đang gửi...' : 'Gửi đánh giá'}
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            <Footer />
        </div>
    );
};

export default HistoryReceipt;
