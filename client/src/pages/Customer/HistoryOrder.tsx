import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Search, Package, CheckCircle, ChevronDown, Truck, User, Phone, MapPin, Star, X, XCircle, RotateCcw, Upload } from 'lucide-react';
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
import { uploadService } from '../../services/uploadService';
import type { UploadImageResponse } from '../../services/uploadService';
import refundService from '../../services/refundService';
import type { RefundResponse } from '../../services/refundService';
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

    // Track order items with return orders: Map<orderDetailId, RefundResponse>
    const [orderItemRefunds, setOrderItemRefunds] = useState<Map<string, RefundResponse>>(new Map());

    // Return order dialog states
    const [showReturnDialog, setShowReturnDialog] = useState(false);
    const [selectedItemForReturn, setSelectedItemForReturn] = useState<{
        orderItemId: string;
        orderItemName: string;
        orderItemImage?: string;
        orderId: string;
        refundAmount: number;
    } | null>(null);
    const [returnReason, setReturnReason] = useState('');
    const [returnImages, setReturnImages] = useState<UploadImageResponse[]>([]);
    const [isSubmittingReturn, setIsSubmittingReturn] = useState(false);
    const [isUploadingImages, setIsUploadingImages] = useState(false);

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
                
                // Check which order items have return orders (refunds)
                await checkOrderItemRefunds(response.data);
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

    // Check which order items have return orders (refunds)
    const checkOrderItemRefunds = async (orders: OrderResponse[]) => {
        const refundsMap = new Map<string, RefundResponse>();
        
        // Get all order items from all orders
        const orderItems = new Map<string, { orderDetailId: string; orderId: string }>();
        orders.forEach(order => {
            order.orderDetails.forEach(detail => {
                orderItems.set(detail.orderDetailId, {
                    orderDetailId: detail.orderDetailId,
                    orderId: order.orderId
                });
            });
        });

        console.log('📋 Checking refunds for order items:', orderItems.size);

        // Check each order item for refunds
        for (const [orderDetailId] of orderItems) {
            try {
                const response = await refundService.getRefundsByOrderItemId(orderDetailId);
                
                console.log(`✅ Refunds for order item ${orderDetailId}:`, response);

                if (response.success && response.data && Array.isArray(response.data) && response.data.length > 0) {
                    // Store the first refund (should only be one per item)
                    const refund = response.data[0];
                    console.log(`💰 Found refund for order item ${orderDetailId}:`, refund);
                    refundsMap.set(orderDetailId, refund);
                }
            } catch (error) {
                console.error(`❌ Error fetching refunds for order item ${orderDetailId}:`, error);
            }
        }

        console.log('📊 Total refunds found (per order item):', refundsMap.size);
        console.log('📊 Refund map keys (orderDetailIds):', Array.from(refundsMap.keys()));

        setOrderItemRefunds(refundsMap);
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

    const getRefundStatusText = (status: string) => {
        switch (status.toLowerCase()) {
            case 'pending':
            case 'chờ xác nhận':
                return 'Chờ xác nhận';
            case 'approved':
            case 'đã duyệt':
                return 'Đã duyệt';
            case 'rejected':
            case 'từ chối':
                return 'Từ chối';
            case 'completed':
            case 'hoàn thành':
                return 'Hoàn thành';
            default:
                return status;
        }
    };

    const getRefundStatusColor = (status: string) => {
        switch (status.toLowerCase()) {
            case 'pending':
            case 'chờ xác nhận':
                return 'bg-yellow-600 text-white border-yellow-600';
            case 'approved':
            case 'đã duyệt':
                return 'bg-blue-600 text-white border-blue-600';
            case 'rejected':
            case 'từ chối':
                return 'bg-red-600 text-white border-red-600';
            case 'completed':
            case 'hoàn thành':
                return 'bg-green-600 text-white border-green-600';
            default:
                return 'bg-gray-600 text-white border-gray-600';
        }
    };

    const getRefundProgressStep = (status: string) => {
        switch (status.toLowerCase()) {
            case 'pending':
            case 'chờ xác nhận':
                return 1;
            case 'approved':
            case 'đã duyệt':
                return 2;
            case 'rejected':
            case 'từ chối':
                return 0;
            case 'completed':
            case 'hoàn thành':
                return 3;
            default:
                return 0;
        }
    };

    const getRefundProgressColor = (currentStep: number, targetStep: number) => {
        if (currentStep >= targetStep) {
            switch (targetStep) {
                case 1:
                    return 'bg-yellow-500';
                case 2:
                    return 'bg-blue-500';
                case 3:
                    return 'bg-green-500';
                default:
                    return 'bg-green-500';
            }
        }
        return 'bg-gray-300';
    };

    const getCurrentRefundStatusColor = (status: string) => {
        switch (status.toLowerCase()) {
            case 'pending':
            case 'chờ xác nhận':
                return 'bg-yellow-500';
            case 'approved':
            case 'đã duyệt':
                return 'bg-blue-500';
            case 'rejected':
            case 'từ chối':
                return 'bg-red-500';
            case 'completed':
            case 'hoàn thành':
                return 'bg-green-500';
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

    // Handle cancel order
    const handleCancelOrder = async (orderId: string) => {
        if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
            return;
        }

        try {
            const response = await orderService.cancelOrder(orderId);
            if (response.success) {
                toast.success('Đơn hàng đã được hủy thành công!', {
                    duration: 3000,
                });
                // Reload orders
                await fetchOrders();
            } else {
                toast.error(response.message || 'Không thể hủy đơn hàng', {
                    duration: 3000,
                });
            }
        } catch (error) {
            console.error('Error canceling order:', error);
            toast.error('Có lỗi xảy ra khi hủy đơn hàng. Vui lòng thử lại!', {
                duration: 3000,
            });
        }
    };

    // Handle open return dialog for specific order item
    const handleOpenReturnDialog = (
        orderItemId: string, 
        orderItemName: string, 
        orderItemImage: string | undefined, 
        orderId: string,
        refundAmount: number
    ) => {
        setSelectedItemForReturn({
            orderItemId,
            orderItemName,
            orderItemImage,
            orderId,
            refundAmount
        });
        setReturnReason('');
        setReturnImages([]);
        setShowReturnDialog(true);
    };

    // Handle close return dialog
    const handleCloseReturnDialog = () => {
        setShowReturnDialog(false);
        setSelectedItemForReturn(null);
        setReturnReason('');
        setReturnImages([]);
    };

    // Handle submit return request
    const handleSubmitReturn = async () => {
        if (!selectedItemForReturn) return;

        if (!returnReason.trim()) {
            toast.error('Vui lòng nhập lý do trả hàng', {
                duration: 3000,
            });
            return;
        }

        setIsSubmittingReturn(true);
        try {
            // Extract image URLs from uploaded images
            const imageUrls = returnImages.map(img => img.url);
            
            console.log('📤 Submitting return request with:');
            console.log('  - Order Item ID:', selectedItemForReturn.orderItemId);
            console.log('  - Order ID:', selectedItemForReturn.orderId);
            console.log('  - Reason:', returnReason);
            console.log('  - Refund Amount:', selectedItemForReturn.refundAmount);
            console.log('  - Image URLs:', imageUrls);

            const response = await orderService.returnOrder(
                selectedItemForReturn.orderItemId,
                selectedItemForReturn.orderId,
                returnReason,
                selectedItemForReturn.refundAmount,
                imageUrls // Send array of URLs
            );

            if (response.success) {
                toast.success('Yêu cầu trả hàng đã được gửi thành công!', {
                    duration: 3000,
                });
                handleCloseReturnDialog();
                // Reload orders
                await fetchOrders();
            } else {
                toast.error(response.message || 'Không thể gửi yêu cầu trả hàng', {
                    duration: 3000,
                });
            }
        } catch (error) {
            console.error('Error submitting return request:', error);
            toast.error('Có lỗi xảy ra khi gửi yêu cầu trả hàng. Vui lòng thử lại!', {
                duration: 3000,
            });
        } finally {
            setIsSubmittingReturn(false);
        }
    };

    // Handle image upload for return
    const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (!files || files.length === 0) return;

        // Validate number of images (max 5)
        if (returnImages.length + files.length > 5) {
            toast.error('Chỉ được tải lên tối đa 5 hình ảnh', {
                duration: 3000,
            });
            return;
        }

        setIsUploadingImages(true);
        const toastId = toast.loading('Đang tải lên hình ảnh...');

        try {
            // Upload images to Cloudinary with folder = "refunds"
            const uploadPromises = Array.from(files).map(file => 
                uploadService.uploadImage(file, 'refunds')
            );

            const results = await Promise.all(uploadPromises);

            // Filter successful uploads
            const successfulUploads = results.filter(r => r.success && r.data);
            const failedUploads = results.filter(r => !r.success);

            if (successfulUploads.length > 0) {
                // Add successful uploads to state
                const newImages = successfulUploads.map(r => r.data!);
                
                // Log uploaded images for debugging
                console.log('📸 ========== REFUND IMAGE UPLOAD DEBUG ==========');
                console.log('📸 Total uploaded:', newImages.length);
                console.log('📸 Full response data:', newImages);
                
                newImages.forEach((img, idx) => {
                    console.log(`📸 Image ${idx + 1}:`);
                    console.log(`   - URL: "${img.url}"`);
                    console.log(`   - Public ID: "${img.publicId}"`);
                    console.log(`   - Format: "${img.format}"`);
                    console.log(`   - Width: ${img.width}`);
                    console.log(`   - Height: ${img.height}`);
                    console.log(`   - Bytes: ${img.bytes}`);
                    
                    // Test if URL is valid
                    if (!img.url || img.url === '') {
                        console.error(`❌ Image ${idx + 1} has EMPTY URL!`);
                    } else if (!img.url.startsWith('http')) {
                        console.error(`❌ Image ${idx + 1} URL is not a valid HTTP URL:`, img.url);
                    } else {
                        console.log(`✅ Image ${idx + 1} URL is valid`);
                    }
                });
                
                console.log('📸 =============================================');
                
                setReturnImages(prev => {
                    const updated = [...prev, ...newImages];
                    console.log('📸 Updated returnImages state:', updated);
                    return updated;
                });

                toast.success(
                    `Đã tải lên ${successfulUploads.length}/${files.length} hình ảnh`,
                    {
                        id: toastId,
                        duration: 3000,
                    }
                );
            }

            if (failedUploads.length > 0) {
                const errorMessage = failedUploads[0].message || 'Một số hình ảnh không thể tải lên';
                toast.error(errorMessage, {
                    id: toastId,
                    duration: 3000,
                });
            }
        } catch (error) {
            console.error('Error uploading images:', error);
            toast.error('Có lỗi xảy ra khi tải lên hình ảnh', {
                id: toastId,
                duration: 3000,
            });
        } finally {
            setIsUploadingImages(false);
            // Reset input
            e.target.value = '';
        }
    };

    // Handle remove uploaded image
    const handleRemoveImage = (index: number) => {
        setReturnImages(prev => prev.filter((_, i) => i !== index));
        toast.success('Đã xóa hình ảnh', {
            duration: 2000,
        });
    };

    useEffect(() => {
        if (!user) {
            navigate('/');
            return;
        }
        fetchOrders();
    }, [user, navigate]);

    // Handle MoMo payment callback
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const resultCode = urlParams.get('resultCode');
        const orderId = urlParams.get('orderId');
        const message = urlParams.get('message');

        if (resultCode !== null) {
            console.log('🔔 MoMo callback detected:', { resultCode, orderId, message });

            if (resultCode === '0') {
                // Payment success
                toast.success('✅ Thanh toán thành công! Đơn hàng đã được xác nhận.', {
                    duration: 5000,
                });
            } else {
                // Payment failed
                toast.error('❌ Thanh toán thất bại. Vui lòng thử lại.', {
                    duration: 5000,
                });
            }

            // Clean URL by removing query params
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
        }
    }, []);

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

                                            {/* Refund Progress Bar - Show if order has a refund */}
                                            {orderItemRefunds.has(order.orderId) && (() => {
                                                const refund = orderItemRefunds.get(order.orderId)!;
                                                const refundStep = getRefundProgressStep(refund.refundStatus);
                                                const isRejected = refund.refundStatus.toLowerCase() === 'rejected' || refund.refundStatus.toLowerCase() === 'từ chối';
                                                
                                                return (
                                                    <div className="mb-6">
                                                        <div className="flex items-center justify-between mb-2">
                                                            <span className="text-sm font-medium text-gray-700">
                                                                Tiến trình trả hàng
                                                            </span>
                                                            <Badge className={`text-xs ${getRefundStatusColor(refund.refundStatus)}`}>
                                                                {getRefundStatusText(refund.refundStatus)}
                                                            </Badge>
                                                        </div>
                                                        {!isRejected && (
                                                            <div className="relative">
                                                                <div className="flex items-center">
                                                                    <div className="flex-1 h-2 bg-gray-200 overflow-hidden">
                                                                        <div
                                                                            className={`h-full transition-all duration-500 ${
                                                                                refundStep >= 1
                                                                                    ? getCurrentRefundStatusColor(refund.refundStatus)
                                                                                    : 'bg-gray-300'
                                                                            }`}
                                                                            style={{
                                                                                width: `${Math.max(
                                                                                    0,
                                                                                    Math.min(100, (refundStep / 3) * 100),
                                                                                )}%`,
                                                                            }}
                                                                        ></div>
                                                                    </div>
                                                                </div>

                                                                {/* Refund Progress Steps */}
                                                                <div className="flex justify-between mt-2">
                                                                    <div className="flex flex-col items-center">
                                                                        <div
                                                                            className={`w-6 h-6 flex items-center justify-center text-xs font-medium ${
                                                                                refundStep >= 1
                                                                                    ? getRefundProgressColor(refundStep, 1) + ' text-white'
                                                                                    : 'bg-gray-300 text-gray-600'
                                                                            }`}
                                                                        >
                                                                            {refundStep >= 1 ? <CheckCircle className="w-4 h-4" /> : '1'}
                                                                        </div>
                                                                        <span className="text-xs text-gray-600 mt-1">
                                                                            Chờ xác nhận
                                                                        </span>
                                                                    </div>
                                                                    <div className="flex flex-col items-center">
                                                                        <div
                                                                            className={`w-6 h-6 flex items-center justify-center text-xs font-medium ${
                                                                                refundStep >= 2
                                                                                    ? getRefundProgressColor(refundStep, 2) + ' text-white'
                                                                                    : 'bg-gray-300 text-gray-600'
                                                                            }`}
                                                                        >
                                                                            {refundStep >= 2 ? <CheckCircle className="w-4 h-4" /> : '2'}
                                                                        </div>
                                                                        <span className="text-xs text-gray-600 mt-1">
                                                                            Đã duyệt
                                                                        </span>
                                                                    </div>
                                                                    <div className="flex flex-col items-center">
                                                                        <div
                                                                            className={`w-6 h-6 flex items-center justify-center text-xs font-medium ${
                                                                                refundStep >= 3
                                                                                    ? getRefundProgressColor(refundStep, 3) + ' text-white'
                                                                                    : 'bg-gray-300 text-gray-600'
                                                                            }`}
                                                                        >
                                                                            {refundStep >= 3 ? <CheckCircle className="w-4 h-4" /> : '3'}
                                                                        </div>
                                                                        <span className="text-xs text-gray-600 mt-1">
                                                                            Hoàn thành
                                                                        </span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        )}
                                                        {isRejected && (
                                                            <div className="bg-red-50 border border-red-200 rounded-lg p-3 mt-2">
                                                                <p className="text-sm text-red-700">
                                                                    ❌ Yêu cầu trả hàng đã bị từ chối
                                                                </p>
                                                            </div>
                                                        )}
                                                    </div>
                                                );
                                            })()}

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
                                                                                
                                                                                {/* Show status badge if item has been returned */}
                                                                                {detail?.status && (
                                                                                    <div className="mt-1">
                                                                                        <Badge className={`text-xs ${
                                                                                            detail.status === 'returned' 
                                                                                                ? 'bg-red-100 text-red-700 border-red-300' 
                                                                                                : detail.status === 'returning'
                                                                                                ? 'bg-orange-100 text-orange-700 border-orange-300'
                                                                                                : 'bg-gray-100 text-gray-700 border-gray-300'
                                                                                        }`}>
                                                                                            {detail.status === 'returned' && '✓ Đã trả hàng'}
                                                                                            {detail.status === 'returning' && '⏳ Đang xử lý trả hàng'}
                                                                                            {!['returned', 'returning'].includes(detail.status) && detail.status}
                                                                                        </Badge>
                                                                                    </div>
                                                                                )}
                                                                                
                                                                                {/* Buttons for completed orders */}
                                                                                {order.status === 4 && detail && !detail.status && (
                                                                                    <div className="flex gap-2 mt-2">
                                                                                        {/* Rating button */}
                                                                                        {ratedOrderItems.get(detail.orderDetailId) ? (
                                                                                            <Button
                                                                                                size="sm"
                                                                                                className="bg-gray-400 text-white text-xs cursor-not-allowed"
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
                                                                                                        detail.orderDetailId,
                                                                                                    );
                                                                                                }}
                                                                                                size="sm"
                                                                                                className="bg-amber-500 hover:bg-amber-600 text-white text-xs"
                                                                                            >
                                                                                                <Star className="w-3 h-3 mr-1" />
                                                                                                Đánh giá
                                                                                            </Button>
                                                                                        )}
                                                                                        
                                                                                        {/* Return button - disable if THIS ORDER ITEM has a refund */}
                                                                                        {orderItemRefunds.has(detail.orderDetailId) ? (
                                                                                            <Button
                                                                                                size="sm"
                                                                                                className="bg-gray-400 text-white text-xs cursor-not-allowed"
                                                                                                disabled
                                                                                            >
                                                                                                <RotateCcw className="w-3 h-3 mr-1" />
                                                                                                Đã yêu cầu trả hàng
                                                                                            </Button>
                                                                                        ) : (
                                                                                            <Button
                                                                                                onClick={(e) => {
                                                                                                    e.stopPropagation();
                                                                                                    handleOpenReturnDialog(
                                                                                                        detail.orderDetailId,
                                                                                                        detail.productName,
                                                                                                        detail.productImages?.[0],
                                                                                                        order.orderId,
                                                                                                        detail.totalPrice
                                                                                                    );
                                                                                                }}
                                                                                                size="sm"
                                                                                                className="bg-orange-500 hover:bg-orange-600 text-white text-xs"
                                                                                            >
                                                                                                <RotateCcw className="w-3 h-3 mr-1" />
                                                                                                Trả hàng
                                                                                            </Button>
                                                                                        )}
                                                                                    </div>
                                                                                )}
                                                                            </div>
                                                                            <div className="text-right">
                                                                                <p className="font-semibold text-gray-900 text-sm">
                                                                                    {formatPrice(
                                                                                        detail?.totalPrice || 0,
                                                                                    )}
                                                                                </p>
                                                                                
                                                                                {/* Show refund status under price if item has refund */}
                                                                                {detail && orderItemRefunds.has(detail.orderDetailId) && (
                                                                                    <div className="mt-1">
                                                                                        <Badge className={`text-xs ${
                                                                                            getRefundStatusColor(
                                                                                                orderItemRefunds.get(detail.orderDetailId)?.refundStatus || ''
                                                                                            )
                                                                                        }`}>
                                                                                            {getRefundStatusText(
                                                                                                orderItemRefunds.get(detail.orderDetailId)?.refundStatus || ''
                                                                                            )}
                                                                                        </Badge>
                                                                                    </div>
                                                                                )}
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
                                                        <div className="flex justify-between items-center text-lg font-bold text-red-600 mb-4">
                                                            <span>Tổng thanh toán ({order.totalItems} sản phẩm):</span>
                                                            <span>{formatPrice(order.totalAmount)}</span>
                                                        </div>

                                                        {/* Action buttons */}
                                                        <div className="flex gap-3 mt-4">
                                                            {/* Cancel button - show when status is "Chờ xác nhận" (status = 1) */}
                                                            {order.status === 1 && (
                                                                <Button
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        handleCancelOrder(order.orderId);
                                                                    }}
                                                                    variant="outline"
                                                                    className="flex-1 border-red-500 text-red-600 hover:bg-red-50"
                                                                >
                                                                    <XCircle className="w-4 h-4 mr-2" />
                                                                    Hủy đơn hàng
                                                                </Button>
                                                            )}
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
                                                
                                                {/* Show refund status under price in detail modal */}
                                                {orderItemRefunds.has(detail.orderDetailId) && (
                                                    <div className="mt-1">
                                                        <Badge className={`text-xs ${
                                                            getRefundStatusColor(
                                                                orderItemRefunds.get(detail.orderDetailId)?.refundStatus || ''
                                                            )
                                                        }`}>
                                                            {getRefundStatusText(
                                                                orderItemRefunds.get(detail.orderDetailId)?.refundStatus || ''
                                                            )}
                                                        </Badge>
                                                    </div>
                                                )}
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

            {/* Return Order Dialog */}
            {showReturnDialog && selectedItemForReturn && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl max-h-[90vh] overflow-y-auto">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold text-gray-900">Yêu cầu trả hàng</h2>
                            <button
                                onClick={handleCloseReturnDialog}
                                className="text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X className="w-6 h-6" />
                            </button>
                        </div>

                        {/* Order Item Info */}
                        <div className="p-4 bg-gray-50 rounded-lg mb-6">
                            <div className="flex items-center gap-4">
                                {/* Product Image */}
                                <div className="relative w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg flex items-center justify-center overflow-hidden">
                                    {selectedItemForReturn.orderItemImage ? (
                                        <img
                                            src={getImageUrl(selectedItemForReturn.orderItemImage)}
                                            alt={selectedItemForReturn.orderItemName}
                                            className="w-full h-full object-cover"
                                            onError={(e) => {
                                                const target = e.target as HTMLImageElement;
                                                target.style.display = 'none';
                                                const fallback = target.nextElementSibling as HTMLElement;
                                                if (fallback) fallback.classList.remove('hidden');
                                            }}
                                        />
                                    ) : null}
                                    <div className={`${selectedItemForReturn.orderItemImage ? 'hidden' : ''} absolute inset-0 flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200`}>
                                        <Package className="w-6 h-6 text-blue-500" />
                                    </div>
                                </div>
                                
                                {/* Product Info */}
                                <div className="flex-1">
                                    <h3 className="font-semibold text-gray-900 mb-1">
                                        {selectedItemForReturn.orderItemName}
                                    </h3>
                                    <p className="text-sm text-gray-600">
                                        Mã đơn hàng: <span className="font-medium">#{selectedItemForReturn.orderId.slice(0, 8)}...</span>
                                    </p>
                                    <p className="text-xs text-gray-500 mt-1">
                                        Mã sản phẩm: #{selectedItemForReturn.orderItemId.slice(0, 8)}...
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Return Reason */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Lý do trả hàng <span className="text-red-500">*</span>
                            </label>
                            <textarea
                                value={returnReason}
                                onChange={(e) => setReturnReason(e.target.value)}
                                placeholder="Vui lòng mô tả lý do bạn muốn trả hàng..."
                                rows={4}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 resize-none"
                            />
                        </div>

                        {/* Image Upload */}
                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Hình ảnh sản phẩm (tùy chọn) {returnImages.length > 0 && (
                                    <span className="text-xs text-gray-500">({returnImages.length}/5)</span>
                                )}
                            </label>
                            <div className={`border-2 border-dashed rounded-lg p-4 text-center transition-colors ${
                                isUploadingImages || returnImages.length >= 5
                                    ? 'border-gray-200 bg-gray-50 cursor-not-allowed'
                                    : 'border-gray-300 hover:border-orange-500 cursor-pointer'
                            }`}>
                                <input
                                    type="file"
                                    accept="image/jpeg,image/jpg,image/png,image/webp,image/gif"
                                    multiple
                                    onChange={handleImageUpload}
                                    className="hidden"
                                    id="return-image-upload"
                                    disabled={isUploadingImages || returnImages.length >= 5}
                                />
                                <label
                                    htmlFor="return-image-upload"
                                    className={`flex flex-col items-center ${
                                        isUploadingImages || returnImages.length >= 5 
                                            ? 'cursor-not-allowed opacity-50' 
                                            : 'cursor-pointer'
                                    }`}
                                >
                                    <Upload className="w-8 h-8 text-gray-400 mb-2" />
                                    <span className="text-sm text-gray-600">
                                        {returnImages.length >= 5 
                                            ? 'Đã đạt tối đa 5 hình ảnh'
                                            : 'Nhấn để tải lên hình ảnh'
                                        }
                                    </span>
                                    <span className="text-xs text-gray-500 mt-1">
                                        PNG, JPG, WEBP, GIF - Tối đa 5MB/ảnh
                                    </span>
                                </label>
                            </div>

                            {/* Preview uploaded images */}
                            {returnImages.length > 0 && (
                                <div className="mt-4 grid grid-cols-3 gap-2">
                                    {returnImages.map((image, index) => {
                                        // Debug logging for each image render
                                        console.log(`🖼️ Rendering preview image ${index + 1}:`, {
                                            url: image.url,
                                            hasUrl: !!image.url,
                                            urlLength: image.url?.length || 0
                                        });
                                        
                                        return (
                                            <div key={index} className="relative">
                                                <img
                                                    src={image.url}
                                                    alt={`Return ${index + 1}`}
                                                    className="w-full h-20 object-cover rounded-lg border"
                                                    onError={(e) => {
                                                        console.error('❌ FAILED to load refund preview image!');
                                                        console.error('   - Index:', index);
                                                        console.error('   - URL:', image.url);
                                                        console.error('   - Full image data:', image);
                                                        e.currentTarget.src = '/placeholder-image.svg';
                                                        e.currentTarget.style.border = '2px solid red';
                                                    }}
                                                    onLoad={() => {
                                                        console.log(`✅ SUCCESS loading refund preview image ${index + 1}:`, image.url);
                                                    }}
                                                />
                                                <button
                                                    onClick={() => handleRemoveImage(index)}
                                                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors"
                                                    type="button"
                                                >
                                                    <X className="w-3 h-3" />
                                                </button>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}

                            {/* Upload status */}
                            {isUploadingImages && (
                                <div className="mt-2 text-sm text-orange-600 flex items-center gap-2">
                                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-orange-600"></div>
                                    Đang tải lên...
                                </div>
                            )}
                        </div>

                        {/* Action Buttons */}
                        <div className="flex gap-3">
                            <Button
                                onClick={handleCloseReturnDialog}
                                variant="outline"
                                className="flex-1"
                                disabled={isSubmittingReturn}
                            >
                                Hủy
                            </Button>
                            <Button
                                onClick={handleSubmitReturn}
                                className="flex-1 bg-orange-600 hover:bg-orange-700"
                                disabled={isSubmittingReturn || !returnReason.trim()}
                            >
                                {isSubmittingReturn ? 'Đang gửi...' : 'Gửi yêu cầu'}
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
