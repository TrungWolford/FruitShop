import React, { useEffect, useState } from 'react';
import { toast } from 'sonner';
import LeftTaskbar from '../../components/LeftTaskbar';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Skeleton } from '../../components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../../components/ui/dialog';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { Search, Package, ChevronLeft, ChevronRight, Eye, Truck, User, Phone, MapPin } from 'lucide-react';
import { orderService } from '../../services/orderService';
import type { OrderResponse } from '../../services/orderService';
import shippingService from '../../services/shippingService';

const AdminOrder: React.FC = () => {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
    const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // Shipper dialog state
    const [isShipperDialogOpen, setIsShipperDialogOpen] = useState(false);
    const [shipperName, setShipperName] = useState('');
    const [isCreatingShipping, setIsCreatingShipping] = useState(false);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

    // Load orders from backend
    const loadOrders = async (page: number = 0) => {
        try {
            setLoading(true);
            let response;

            // Use search and filter API
            if (searchTerm.trim() && statusFilter !== 'all') {
                // Both search and filter
                const status = parseInt(statusFilter);
                response = await orderService.searchAndFilterOrders(searchTerm.trim(), status, page, itemsPerPage);
            } else if (searchTerm.trim()) {
                // Only search
                response = await orderService.searchOrders(searchTerm.trim(), page, itemsPerPage);
            } else if (statusFilter !== 'all') {
                // Only filter
                const status = parseInt(statusFilter);
                response = await orderService.filterOrdersByStatus(status, page, itemsPerPage);
            } else {
                // No search or filter
                response = await orderService.getAllOrders(page, itemsPerPage);
            }

            console.log('🔍 Backend Response:', response);

            if (response.success && response.data) {
                setOrders(response.data);
                setTotalItems(response.data.length);
                setTotalPages(Math.ceil(response.data.length / itemsPerPage));
            } else {
                toast.error('Không thể tải danh sách đơn hàng');
                setOrders([]);
            }
        } catch (error) {
            console.error('Error loading orders:', error);
            toast.error('Có lỗi xảy ra khi tải đơn hàng');
            setOrders([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadOrders(currentPage - 1);
    }, [currentPage, statusFilter, searchTerm]);

    // Format price
    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    // Format date
    const formatDate = (dateString: string) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    // Get order status badge
    const getOrderStatusBadge = (status: number) => {
        const statusConfig: Record<number, { text: string; color: string }> = {
            0: { text: 'Đã hủy', color: 'bg-red-700 border-red-700' },
            1: { text: 'Chờ xác nhận', color: 'bg-yellow-700 border-yellow-700' },
            2: { text: 'Đã xác nhận', color: 'bg-blue-700 border-blue-700' },
            3: { text: 'Đang giao', color: 'bg-purple-700 border-purple-700' },
            4: { text: 'Giao thành công', color: 'bg-green-700 border-green-700' },
        };

        const config = statusConfig[status] || {
            text: 'Không xác định',
            color: 'bg-gray-700 border-gray-700',
        };

        return (
            <Badge
                variant="default"
                className={`px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap ${config.color}`}
            >
                {config.text}
            </Badge>
        );
    };

    // Get shipping status badge
    const getShippingStatusBadge = (status?: number) => {
        if (status === undefined) return <Badge className="bg-gray-100 text-gray-800 rounded-none">N/A</Badge>;

        const statusConfig: Record<number, { text: string; color: string }> = {
            1: { text: 'Chờ xác nhận', color: 'bg-yellow-100 text-yellow-800 border-yellow-300' },
            2: { text: 'Đã xác nhận', color: 'bg-orange-100 text-orange-800 border-orange-300' },
            3: { text: 'Đang giao', color: 'bg-blue-100 text-blue-800 border-blue-300' },
            4: { text: 'Đã giao', color: 'bg-green-100 text-green-800 border-green-300' },
        };

        const config = statusConfig[status] || { text: 'Không xác định', color: 'bg-gray-100 text-gray-800 border-gray-300' };
        return <Badge className={`${config.color} rounded-none`}>{config.text}</Badge>;
    };

    // Handle view detail
    const handleViewDetail = (order: OrderResponse) => {
        setSelectedOrder(order);
        setIsViewDialogOpen(true);
    };

    // Get payment method text
    const getPaymentMethodText = (method: number) => {
        return method === 0 ? 'Tiền mặt (COD)' : 'Chuyển khoản';
    };

    // Handle confirm order (status 1 -> 2)
    const handleConfirmOrder = async (orderId: string) => {
        try {
            const response = await orderService.confirmOrder(orderId);
            if (response.success) {
                toast.success('Đã xác nhận đơn hàng thành công!');
                loadOrders(currentPage - 1);
                if (selectedOrder?.orderId === orderId) {
                    setSelectedOrder(response.data || null);
                }
            } else {
                toast.error(response.message || 'Không thể xác nhận đơn hàng');
            }
        } catch (error) {
            console.error('Error confirming order:', error);
            toast.error('Có lỗi xảy ra khi xác nhận đơn hàng');
        }
    };

    // Handle start delivery (status 2 -> 3)
    const handleStartDelivery = async () => {
        // Open dialog to input shipper name
        setIsShipperDialogOpen(true);
    };

    // Handle start delivery - update shipping status to "Đang giao"
    const handleCreateShippingAndStartDelivery = async () => {
        if (!selectedOrder) return;

        if (!shipperName.trim()) {
            toast.error('Vui lòng nhập tên người giao hàng!');
            return;
        }

        try {
            setIsCreatingShipping(true);

            // Update shipping with shipper name (shipping already exists from createOrder)
            if (selectedOrder.shipping) {
                const updateResponse = await shippingService.updateShipping(selectedOrder.shipping.shippingId, {
                    receiverName: selectedOrder.shipping.receiverName,
                    receiverPhone: selectedOrder.shipping.receiverPhone,
                    receiverAddress: selectedOrder.shipping.receiverAddress,
                    city: selectedOrder.shipping.city,
                    shipperName: shipperName.trim(),
                    shippingFee: selectedOrder.shipping.shippingFee,
                });

                if (!updateResponse.success) {
                    toast.error('Không thể cập nhật thông tin giao hàng');
                    return;
                }
            } else {
                toast.error('Không tìm thấy thông tin giao hàng cho đơn hàng này');
                return;
            }

            // Start delivery - backend will update order status to 3 and shipping status to 3 (Đang giao)
            const response = await orderService.startDelivery(selectedOrder.orderId);
            if (response.success) {
                toast.success('Đã bắt đầu giao hàng!');
                setIsShipperDialogOpen(false);
                setShipperName('');
                loadOrders(currentPage - 1);
                if (selectedOrder?.orderId) {
                    setSelectedOrder(response.data || null);
                }
            } else {
                toast.error(response.message || 'Không thể bắt đầu giao hàng');
            }
        } catch (error) {
            console.error('Error starting delivery:', error);
            toast.error('Có lỗi xảy ra khi bắt đầu giao hàng');
        } finally {
            setIsCreatingShipping(false);
        }
    };

    // Handle complete order (status 3 -> 4)
    const handleCompleteOrder = async (orderId: string) => {
        try {
            const response = await orderService.completeOrder(orderId);
            if (response.success) {
                // Backend updates shipping status to 4 (Đã giao) in completeOrder
                toast.success('Đã hoàn thành đơn hàng!');
                loadOrders(currentPage - 1);
                if (selectedOrder?.orderId === orderId) {
                    setSelectedOrder(response.data || null);
                }
            } else {
                toast.error(response.message || 'Không thể hoàn thành đơn hàng');
            }
        } catch (error) {
            console.error('Error completing order:', error);
            toast.error('Có lỗi xảy ra khi hoàn thành đơn hàng');
        }
    };

    // Handle cancel order
    const handleCancelOrder = async (orderId: string) => {
        if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
            return;
        }

        try {
            const response = await orderService.cancelOrder(orderId);
            if (response.success) {
                toast.success('Đã hủy đơn hàng thành công!');
                loadOrders(currentPage - 1);
                if (selectedOrder?.orderId === orderId) {
                    setSelectedOrder(response.data || null);
                }
            } else {
                toast.error(response.message || 'Không thể hủy đơn hàng');
            }
        } catch (error) {
            console.error('Error canceling order:', error);
            toast.error('Có lỗi xảy ra khi hủy đơn hàng');
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Package className="w-5 h-5 text-amber-500" />
                        Quản lý đơn hàng
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">Quản lý tất cả đơn hàng trong hệ thống</p>
                </div>

                {/* Search and Filters */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm đơn hàng..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-8 py-2 text-lg border border-gray-300 bg-gray-50 text-gray-900 placeholder-gray-500 focus:border-amber-500 focus:bg-white transition-all duration-200 rounded-md"
                                />
                            </div>

                            {/* Status Filter */}
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button
                                        variant="outline"
                                        className="w-44 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md flex items-center justify-between"
                                    >
                                        <div className="flex items-center">
                                            {statusFilter === 'all'
                                                ? 'Tất cả'
                                                : statusFilter === '0'
                                                ? 'Đã hủy'
                                                : statusFilter === '1'
                                                ? 'Chờ xác nhận'
                                                : statusFilter === '2'
                                                ? 'Đã xác nhận'
                                                : statusFilter === '3'
                                                ? 'Đang giao'
                                                : 'Giao thành công'}
                                        </div>
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent className="bg-white border-gray-200 shadow-lg">
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('all')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Tất cả trạng thái
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('0')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Đã hủy
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('1')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Chờ xác nhận
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('2')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Đã xác nhận
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('3')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Đang giao
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('4')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Giao thành công
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                    </div>
                </div>                {/* Stats */}
                <div className="mb-3 grid grid-cols-1 md:grid-cols-5 gap-3">
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Tổng đơn hàng</div>
                        <div className="text-2xl font-bold text-gray-900">{totalItems}</div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Chờ xác nhận</div>
                        <div className="text-2xl font-bold text-yellow-600">
                            {orders.filter((o) => o.status === 1).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đã xác nhận</div>
                        <div className="text-2xl font-bold text-blue-600">
                            {orders.filter((o) => o.status === 2).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đang giao</div>
                        <div className="text-2xl font-bold text-purple-600">
                            {orders.filter((o) => o.status === 3).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Thành công</div>
                        <div className="text-2xl font-bold text-green-600">
                            {orders.filter((o) => o.status === 4).length}
                        </div>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white shadow-sm border border-gray-200 overflow-hidden">
                    <div className="overflow-x-auto">
                        <Table className="w-full">
                            <TableHeader>
                                <TableRow className="bg-slate-800 hover:bg-slate-800 border-b border-slate-700">
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                                        Mã đơn hàng
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-40">
                                        Khách hàng
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                                        Tổng tiền
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-40">
                                        Ngày đặt
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                                        Trạng thái
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                                        Thao tác
                                    </TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {loading ? (
                                    Array.from({ length: 10 }).map((_, index) => (
                                        <TableRow key={`skeleton-${index}`}>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-6 w-24 rounded" />
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <Skeleton className="h-4 w-32" />
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-4 w-28" />
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-4 w-32" />
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-6 w-24 rounded-full" />
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-8 w-20 rounded" />
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                ) : orders.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={6} className="text-center py-12 text-gray-500">
                                            <Package className="w-16 h-16 mx-auto mb-3 text-gray-400" />
                                            <p className="text-lg font-medium">Không tìm thấy đơn hàng nào</p>
                                            <p className="text-sm text-gray-400 mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm</p>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    orders.map((order, index) => (
                                        <TableRow
                                            key={order.orderId}
                                            className={`transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            } hover:bg-blue-50/50`}
                                        >
                                            <TableCell className="px-4 py-3 text-center">
                                                <div className="flex justify-center">
                                                    <div 
                                                        className="px-2 py-1 bg-amber-100 rounded flex items-center gap-1.5 cursor-pointer hover:bg-amber-200 transition-colors"
                                                        title={order.orderId}
                                                    >
                                                        <Package className="w-3.5 h-3.5 text-amber-600" />
                                                        <span className="text-xs font-bold text-amber-600">
                                                            #{order.orderId.length > 8 ? order.orderId.substring(0, 8) + '...' : order.orderId}
                                                        </span>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="font-medium text-gray-900">
                                                    {order.accountName || 'Khách vãng lai'}
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                <span className="font-semibold text-green-600">
                                                    {formatPrice(order.totalAmount)}
                                                </span>
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center text-sm text-gray-600">
                                                {formatDate(order.orderDate)}
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                {getOrderStatusBadge(order.status)}
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleViewDetail(order)}
                                                    className="bg-blue-50 hover:bg-blue-100 text-blue-600 px-3 py-1.5 rounded-md transition-all duration-200"
                                                >
                                                    <Eye className="w-3.5 h-3.5 mr-1.5" />
                                                    Chi tiết
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>

                        {/* Pagination */}
                        {!loading && totalPages > 1 && (
                            <div className="flex items-center justify-between px-6 py-4 border-t bg-gray-50/50">
                                <div className="text-sm text-gray-600 font-medium">
                                    Hiển thị{' '}
                                    <span className="font-bold text-gray-900">
                                        {(currentPage - 1) * itemsPerPage + 1}
                                    </span>{' '}
                                    -{' '}
                                    <span className="font-bold text-gray-900">
                                        {Math.min(currentPage * itemsPerPage, totalItems)}
                                    </span>{' '}
                                    / <span className="font-bold text-gray-900">{totalItems}</span> đơn hàng
                                </div>
                                <div className="flex gap-2">
                                    <Button
                                        variant="outline"
                                        onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                                        disabled={currentPage === 1}
                                        className="border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed rounded-md"
                                    >
                                        <ChevronLeft className="w-4 h-4" />
                                    </Button>
                                    <div className="flex items-center px-4 bg-white border border-gray-300 rounded-md font-medium text-sm">
                                        Trang <span className="font-bold text-amber-600 mx-1">{currentPage}</span> /{' '}
                                        {totalPages}
                                    </div>
                                    <Button
                                        variant="outline"
                                        onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                                        disabled={currentPage === totalPages}
                                        className="border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed rounded-md"
                                    >
                                        <ChevronRight className="w-4 h-4" />
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Detail Dialog */}
            <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle className="text-2xl flex items-center gap-2">
                            <span>Chi tiết đơn hàng</span>
                            <span 
                                className="text-amber-600 cursor-pointer hover:text-amber-700"
                                title={selectedOrder?.orderId}
                            >
                                #{selectedOrder?.orderId && selectedOrder.orderId.length > 8 
                                    ? selectedOrder.orderId.substring(0, 8) + '...' 
                                    : selectedOrder?.orderId}
                            </span>
                        </DialogTitle>
                        <DialogDescription>Thông tin chi tiết về đơn hàng</DialogDescription>
                    </DialogHeader>

                    {selectedOrder && (
                        <div className="space-y-6">
                            {/* Order Info Grid */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Mã đơn hàng</p>
                                    <p className="font-semibold text-amber-600" title={selectedOrder.orderId}>
                                        #{selectedOrder.orderId}
                                    </p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Ngày đặt hàng</p>
                                    <p className="font-semibold">{formatDate(selectedOrder.orderDate)}</p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Trạng thái</p>
                                    {getOrderStatusBadge(selectedOrder.status)}
                                </div>
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Phương thức thanh toán</p>
                                    <p className="font-semibold">{getPaymentMethodText(selectedOrder.paymentMethod)}</p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Số lượng sản phẩm</p>
                                    <p className="font-semibold text-blue-600">
                                        {selectedOrder.totalItems || selectedOrder.orderDetails?.length || 0} sản phẩm
                                    </p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-none">
                                    <p className="text-sm text-gray-600 mb-1">Tổng tiền</p>
                                    <p className="font-semibold text-green-600 text-lg">
                                        {formatPrice(selectedOrder.totalAmount)}
                                    </p>
                                </div>
                            </div>

                            {/* Customer Info */}
                            <div>
                                <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                                    <User className="w-5 h-5 text-blue-600" />
                                    Thông tin khách hàng
                                </h3>
                                <div className="bg-gray-50 p-4 rounded-none space-y-2">
                                    <p>
                                        <strong>Tên:</strong> {selectedOrder.accountName}
                                    </p>
                                    <p>
                                        <strong>ID:</strong> <span className="text-gray-600 text-sm">{selectedOrder.accountId}</span>
                                    </p>
                                </div>
                            </div>

                            {/* Shipping Info */}
                            {selectedOrder.shipping && (
                                <div>
                                    <div className="flex items-center justify-between mb-3">
                                        <h3 className="font-semibold text-lg flex items-center gap-2">
                                            <Truck className="w-5 h-5 text-green-600" />
                                            Thông tin giao hàng
                                        </h3>
                                        {getShippingStatusBadge(selectedOrder.shipping.status)}
                                    </div>
                                    <div className="bg-gradient-to-r from-green-50 to-blue-50 p-4 rounded-none border space-y-3">
                                        <div className="flex items-start gap-3">
                                            <User className="w-5 h-5 text-green-600 mt-0.5" />
                                            <div>
                                                <p className="text-sm text-gray-600">Người nhận</p>
                                                <p className="font-semibold">{selectedOrder.shipping.receiverName}</p>
                                            </div>
                                        </div>
                                        <div className="flex items-start gap-3">
                                            <Phone className="w-5 h-5 text-green-600 mt-0.5" />
                                            <div>
                                                <p className="text-sm text-gray-600">Số điện thoại</p>
                                                <p className="font-semibold">{selectedOrder.shipping.receiverPhone}</p>
                                            </div>
                                        </div>
                                        <div className="flex items-start gap-3">
                                            <MapPin className="w-5 h-5 text-green-600 mt-0.5" />
                                            <div>
                                                <p className="text-sm text-gray-600">Địa chỉ</p>
                                                <p className="font-semibold">{selectedOrder.shipping.receiverAddress}</p>
                                                <p className="text-sm text-gray-600 mt-1">
                                                    📍 {selectedOrder.shipping.city}
                                                </p>
                                            </div>
                                        </div>
                                        {selectedOrder.shipping.shipperName && (
                                            <div className="flex items-start gap-3">
                                                <Truck className="w-5 h-5 text-purple-600 mt-0.5" />
                                                <div>
                                                    <p className="text-sm text-gray-600">Người giao hàng</p>
                                                    <p className="font-semibold text-purple-700">{selectedOrder.shipping.shipperName}</p>
                                                </div>
                                            </div>
                                        )}
                                        {selectedOrder.shipping.shippedAt && (
                                            <div className="flex items-start gap-3 pt-3 border-t">
                                                <div>
                                                    <p className="text-sm text-gray-600">Ngày giao hàng</p>
                                                    <p className="font-semibold">
                                                        {formatDate(selectedOrder.shipping.shippedAt)}
                                                    </p>
                                                </div>
                                            </div>
                                        )}
                                        {selectedOrder.shipping.shippingFee && (
                                            <div className="flex items-start gap-3 pt-3 border-t">
                                                <div>
                                                    <p className="text-sm text-gray-600">Phí vận chuyển</p>
                                                    <p className="font-semibold text-green-600">
                                                        {formatPrice(selectedOrder.shipping.shippingFee)}
                                                    </p>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Order Items */}
                            {selectedOrder.orderDetails && selectedOrder.orderDetails.length > 0 && (
                                <div>
                                    <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                                        <Package className="w-5 h-5 text-amber-600" />
                                        Sản phẩm đã đặt ({selectedOrder.orderDetails.length})
                                    </h3>
                                    <div className="border rounded-none overflow-hidden">
                                        <table className="w-full">
                                            <thead className="bg-gray-100">
                                                <tr>
                                                    <th className="px-4 py-3 text-left text-sm font-semibold">
                                                        Sản phẩm
                                                    </th>
                                                    <th className="px-4 py-3 text-right text-sm font-semibold">
                                                        Số lượng
                                                    </th>
                                                    <th className="px-4 py-3 text-right text-sm font-semibold">
                                                        Đơn giá
                                                    </th>
                                                    <th className="px-4 py-3 text-right text-sm font-semibold">
                                                        Thành tiền
                                                    </th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {selectedOrder.orderDetails.map((detail, index) => (
                                                    <tr key={index} className="border-t hover:bg-gray-50">
                                                        <td className="px-4 py-3">
                                                            <div className="flex items-center gap-3">
                                                                {/* Product Image */}
                                                                {detail.productImages && detail.productImages.length > 0 ? (
                                                                    <img
                                                                        src={`/products/${detail.productImages[0]}`}
                                                                        alt={detail.productName}
                                                                        className="w-12 h-12 object-cover rounded border"
                                                                        onError={(e) => {
                                                                            const target = e.target as HTMLImageElement;
                                                                            target.src = '/placeholder.png';
                                                                        }}
                                                                    />
                                                                ) : (
                                                                    <div className="w-12 h-12 bg-gray-200 rounded border flex items-center justify-center">
                                                                        <Package className="w-6 h-6 text-gray-400" />
                                                                    </div>
                                                                )}
                                                                <div>
                                                                    <p className="font-medium">{detail.productName || 'N/A'}</p>
                                                                    <p className="text-xs text-gray-500">ID: {detail.productId}</p>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td className="px-4 py-3 text-right">{detail.quantity}</td>
                                                        <td className="px-4 py-3 text-right">
                                                            {formatPrice(detail.unitPrice)}
                                                        </td>
                                                        <td className="px-4 py-3 text-right font-semibold">
                                                            {formatPrice(detail.unitPrice * detail.quantity)}
                                                        </td>
                                                    </tr>
                                                ))}
                                                <tr className="border-t bg-gray-50 font-semibold">
                                                    <td colSpan={3} className="px-4 py-3 text-right">
                                                        Tổng cộng:
                                                    </td>
                                                    <td className="px-4 py-3 text-right text-green-600 text-lg">
                                                        {formatPrice(selectedOrder.totalAmount)}
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    <DialogFooter className="flex justify-between items-center">
                        <div className="flex gap-2">
                            {selectedOrder && selectedOrder.status === 1 && (
                                <>
                                    <Button
                                        onClick={() => handleConfirmOrder(selectedOrder.orderId)}
                                        className="bg-blue-600 hover:bg-blue-700 text-white"
                                    >
                                        ✓ Xác nhận đơn
                                    </Button>
                                    <Button
                                        onClick={() => handleCancelOrder(selectedOrder.orderId)}
                                        variant="destructive"
                                    >
                                        ✕ Hủy đơn
                                    </Button>
                                </>
                            )}
                            {selectedOrder && selectedOrder.status === 2 && (
                                <Button
                                    onClick={() => handleStartDelivery()}
                                    className="bg-purple-600 hover:bg-purple-700 text-white"
                                >
                                    🚚 Bắt đầu giao hàng
                                </Button>
                            )}
                            {selectedOrder && selectedOrder.status === 3 && (
                                <Button
                                    onClick={() => handleCompleteOrder(selectedOrder.orderId)}
                                    className="bg-green-600 hover:bg-green-700 text-white"
                                >
                                    ✓ Hoàn thành
                                </Button>
                            )}
                        </div>
                        <Button variant="outline" onClick={() => setIsViewDialogOpen(false)} className="rounded-md">
                            Đóng
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Shipper Dialog */}
            <Dialog open={isShipperDialogOpen} onOpenChange={setIsShipperDialogOpen}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            <Truck className="w-5 h-5 text-purple-600" />
                            Bắt đầu giao hàng
                        </DialogTitle>
                        <DialogDescription>
                            Nhập tên người giao hàng để bắt đầu quá trình giao hàng
                        </DialogDescription>
                    </DialogHeader>
                    
                    <div className="space-y-4 py-4">
                        {/* Order Info */}
                        {selectedOrder && (
                            <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-600">Mã đơn hàng:</span>
                                    <span className="font-semibold">#{selectedOrder.orderId}</span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-600">Khách hàng:</span>
                                    <span className="font-semibold">{selectedOrder.accountName}</span>
                                </div>
                                {selectedOrder.shipping && (
                                    <>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-600">Người nhận:</span>
                                            <span className="font-semibold">{selectedOrder.shipping.receiverName}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-600">Địa chỉ:</span>
                                            <span className="font-semibold text-right max-w-xs">{selectedOrder.shipping.receiverAddress}, {selectedOrder.shipping.city}</span>
                                        </div>
                                    </>
                                )}
                            </div>
                        )}

                        {/* Shipper Name Input */}
                        <div className="space-y-2">
                            <label htmlFor="shipperName" className="text-sm font-medium text-gray-700">
                                Tên người giao hàng <span className="text-red-500">*</span>
                            </label>
                            <Input
                                id="shipperName"
                                value={shipperName}
                                onChange={(e) => setShipperName(e.target.value)}
                                placeholder="Nhập tên người giao hàng..."
                                className="w-full"
                                disabled={isCreatingShipping}
                            />
                        </div>
                    </div>

                    <DialogFooter className="gap-2">
                        <Button
                            variant="outline"
                            onClick={() => {
                                setIsShipperDialogOpen(false);
                                setShipperName('');
                            }}
                            disabled={isCreatingShipping}
                        >
                            Hủy
                        </Button>
                        <Button
                            onClick={handleCreateShippingAndStartDelivery}
                            className="bg-purple-600 hover:bg-purple-700 text-white"
                            disabled={isCreatingShipping || !shipperName.trim()}
                        >
                            {isCreatingShipping ? (
                                <>
                                    <span className="animate-spin mr-2">⏳</span>
                                    Đang xử lý...
                                </>
                            ) : (
                                <>
                                    <Truck className="w-4 h-4 mr-2" />
                                    Bắt đầu giao hàng
                                </>
                            )}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminOrder;
