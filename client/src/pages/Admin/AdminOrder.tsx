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

const AdminOrder: React.FC = () => {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
    const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

    // Load orders from backend
    const loadOrders = async (page: number = 0) => {
        try {
            setLoading(true);
            const response = await orderService.getAllOrders(page, itemsPerPage);

            console.log('🔍 Backend Response:', response);

            if (response.success && response.data) {
                let filteredOrders = response.data;

                // Apply status filter locally if needed
                if (statusFilter !== 'all') {
                    const status = parseInt(statusFilter);
                    filteredOrders = filteredOrders.filter((order: OrderResponse) => order.status === status);
                }

                setOrders(filteredOrders);
                setTotalItems(filteredOrders.length);
                setTotalPages(Math.ceil(filteredOrders.length / itemsPerPage));
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
    }, [currentPage, statusFilter]);

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
        return (
            <Badge
                variant={status === 1 ? 'default' : 'secondary'}
                className={`px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap ${
                    status === 1 ? 'bg-green-700 border-green-700' : 'bg-red-700 border-red-700'
                }`}
            >
                {status === 1 ? 'Đã hoàn thành' : 'Đã hủy'}
            </Badge>
        );
    };

    // Get shipping status badge
    const getShippingStatusBadge = (status?: number) => {
        if (status === undefined) return <Badge className="bg-gray-100 text-gray-800 rounded-none">N/A</Badge>;

        const statusConfig: Record<number, { text: string; color: string }> = {
            0: { text: 'Chờ xác nhận', color: 'bg-yellow-100 text-yellow-800 border-yellow-300' },
            1: { text: 'Đang vận chuyển', color: 'bg-blue-100 text-blue-800 border-blue-300' },
            2: { text: 'Đã giao hàng', color: 'bg-green-100 text-green-800 border-green-300' },
        };

        const config = statusConfig[status] || { text: 'Không xác định', color: 'bg-gray-100 text-gray-800 border-gray-300' };
        return <Badge className={`${config.color} rounded-none`}>{config.text}</Badge>;
    };

    // Handle view detail
    const handleViewDetail = (order: OrderResponse) => {
        setSelectedOrder(order);
        setIsViewDialogOpen(true);
    };

    // Filter orders by search term
    const filteredOrders = orders.filter((order) => {
        const matchesSearch =
            order.orderId.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (order.accountName && order.accountName.toLowerCase().includes(searchTerm.toLowerCase()));
        return matchesSearch;
    });

    // Get payment method text
    const getPaymentMethodText = (method: number) => {
        return method === 0 ? 'Tiền mặt (COD)' : 'Chuyển khoản';
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
                                        className="w-40 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md flex items-center justify-between"
                                    >
                                        <div className="flex items-center">
                                            {statusFilter === 'all'
                                                ? 'Tất cả'
                                                : statusFilter === '0'
                                                ? 'Đã hủy'
                                                : 'Hoàn thành'}
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
                                        Đã hoàn thành
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                    </div>
                </div>                {/* Stats */}
                <div className="mb-3 grid grid-cols-1 md:grid-cols-3 gap-3">
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Tổng đơn hàng</div>
                        <div className="text-2xl font-bold text-gray-900">{totalItems}</div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đã hoàn thành</div>
                        <div className="text-2xl font-bold text-green-600">
                            {orders.filter((o) => o.status === 1).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đã hủy</div>
                        <div className="text-2xl font-bold text-red-600">
                            {orders.filter((o) => o.status === 0).length}
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
                                ) : filteredOrders.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={6} className="text-center py-12 text-gray-500">
                                            <Package className="w-16 h-16 mx-auto mb-3 text-gray-400" />
                                            <p className="text-lg font-medium">Không tìm thấy đơn hàng nào</p>
                                            <p className="text-sm text-gray-400 mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm</p>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    filteredOrders.map((order, index) => (
                                        <TableRow
                                            key={order.orderId}
                                            className={`transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            } hover:bg-blue-50/50`}
                                        >
                                            <TableCell className="px-4 py-3 text-center">
                                                <div className="flex justify-center">
                                                    <div className="px-2 py-1 bg-amber-100 rounded flex items-center gap-1.5">
                                                        <Package className="w-3.5 h-3.5 text-amber-600" />
                                                        <span className="text-xs font-bold text-amber-600">
                                                            #{order.orderId.substring(0, 8)}
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
                        <DialogTitle className="text-2xl">
                            Chi tiết đơn hàng #{selectedOrder?.orderId.substring(0, 8)}
                        </DialogTitle>
                        <DialogDescription>Thông tin chi tiết về đơn hàng</DialogDescription>
                    </DialogHeader>

                    {selectedOrder && (
                        <div className="space-y-6">
                            {/* Order Info Grid */}
                            <div className="grid grid-cols-2 gap-4">
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
                                    <h3 className="font-semibold text-lg mb-3">Sản phẩm đã đặt</h3>
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
                                                        <td className="px-4 py-3">{detail.productName || 'N/A'}</td>
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

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsViewDialogOpen(false)} className="rounded-none">
                            Đóng
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminOrder;
