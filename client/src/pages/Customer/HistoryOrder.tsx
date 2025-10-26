import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Search, Package, CheckCircle, ChevronDown, Truck, User, Phone, MapPin } from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { orderService } from '../../services/shippingService';
import type { OrderResponse } from '../../services/shippingService';

const HistoryReceipt: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useAppSelector((state) => state.auth);

    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
    const [showOrderDetail, setShowOrderDetail] = useState(false);
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

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
                return 'bg-red-600 text-white border-red-600'; // Huy
            case 1:
                return 'bg-blue-600 text-white border-blue-600'; // Dang van chuyen
            case 2:
                return 'bg-green-600 text-white border-green-600'; // Da hoan thanh
            default:
                return 'bg-gray-600 text-white border-gray-600';
        }
    };

    const getStatusText = (status: number) => {
        switch (status) {
            case 0:
                return 'Đã hủy';
            case 1:
                return 'Đang vận chuyển';
            case 2:
                return 'Đã hoàn thành';
            default:
                return 'Không xác định';
        }
    };

    const getShippingStatusColor = (status: number) => {
        switch (status) {
            case 0:
                return 'bg-yellow-100 text-yellow-800 border-yellow-300'; // Chờ xác nhận
            case 1:
                return 'bg-blue-100 text-blue-800 border-blue-300'; // Đang vận chuyển
            case 2:
                return 'bg-green-100 text-green-800 border-green-300'; // Đã giao hàng
            default:
                return 'bg-gray-100 text-gray-800 border-gray-300';
        }
    };

    const getShippingStatusText = (status: number) => {
        switch (status) {
            case 0:
                return 'Chờ xác nhận';
            case 1:
                return 'Đang vận chuyển';
            case 2:
                return 'Đã giao hàng';
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
        switch (status) {
            case 0:
                return 0; // Huy
            case 1:
                return 2; // Dang van chuyen
            case 2:
                return 4; // Da hoan thanh
            default:
                return 0;
        }
    };

    const getProgressColor = (currentStep: number, targetStep: number) => {
        if (currentStep >= targetStep) {
            switch (targetStep) {
                case 1:
                    return 'bg-yellow-500';
                case 2:
                    return 'bg-blue-500';
                case 3:
                    return 'bg-purple-500';
                case 4:
                    return 'bg-green-500';
                default:
                    return 'bg-green-500';
            }
        }
        return 'bg-gray-300';
    };

    const getCurrentStatusColor = (status: number) => {
        switch (status) {
            case 0:
                return 'bg-red-500'; // Huy
            case 1:
                return 'bg-blue-500'; // Dang van chuyen
            case 2:
                return 'bg-green-500'; // Da hoan thanh
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
                                                            className="text-lg font-semibold text-gray-900 cursor-pointer hover:text-blue-600 transition-colors"
                                                            onClick={() => handleOrderClick(order)}
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
                                                                <Badge className={`${getShippingStatusColor(order.shipping.status)} rounded-none text-xs`}>
                                                                    {getShippingStatusText(order.shipping.status)}
                                                                </Badge>
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
                            <h2 className="text-xl font-bold">Chi tiết đơn hàng #{selectedOrder.orderId}</h2>
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
                                        <Badge className={`${getShippingStatusColor(selectedOrder.shipping.status)} rounded-none text-xs px-3 py-1`}>
                                            {getShippingStatusText(selectedOrder.shipping.status)}
                                        </Badge>
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

            <Footer />
        </div>
    );
};

export default HistoryReceipt;
