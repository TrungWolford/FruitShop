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
import { Search, CreditCard, ChevronLeft, ChevronRight, Eye, DollarSign, CheckCircle, XCircle, Clock, RefreshCw } from 'lucide-react';
import paymentService from '../../services/paymentService';
import type { PaymentResponse } from '../../services/paymentService';

const AdminPayment: React.FC = () => {
    const [payments, setPayments] = useState<PaymentResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedPayment, setSelectedPayment] = useState<PaymentResponse | null>(null);
    const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

    // Load payments from backend
    const loadPayments = async () => {
        try {
            setLoading(true);
            let response;

            // Load all data (large size for frontend pagination)
            const largeSize = 1000;
            if (statusFilter !== 'all') {
                // Filter by status
                const status = parseInt(statusFilter);
                response = await paymentService.getPaymentsByStatus(status, 0, largeSize);
            } else {
                // Get all payments
                response = await paymentService.getAllPayments(0, largeSize, 'paymentDate', 'desc');
            }

            console.log('🔍 Backend Response:', response);

            if (response.success && response.data) {
                let allPayments = response.data;

                // Filter by search term on frontend
                if (searchTerm.trim()) {
                    const searchLower = searchTerm.toLowerCase();
                    allPayments = allPayments.filter(payment =>
                        payment.paymentId.toLowerCase().includes(searchLower) ||
                        payment.transactionId?.toLowerCase().includes(searchLower) ||
                        payment.paymentMethod.toLowerCase().includes(searchLower)
                    );
                }

                setTotalItems(allPayments.length);
                setTotalPages(Math.max(1, Math.ceil(allPayments.length / itemsPerPage)));
                
                // Paginate on frontend
                const startIndex = (currentPage - 1) * itemsPerPage;
                const endIndex = startIndex + itemsPerPage;
                setPayments(allPayments.slice(startIndex, endIndex));
            } else {
                toast.error('Không thể tải danh sách thanh toán');
                setPayments([]);
                setTotalItems(0);
                setTotalPages(1);
            }
        } catch (error) {
            console.error('Error loading payments:', error);
            toast.error('Có lỗi xảy ra khi tải thanh toán');
            setPayments([]);
            setTotalItems(0);
            setTotalPages(1);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPayments();
    }, [currentPage, statusFilter]);

    // Handle search
    useEffect(() => {
        // Reset to page 1 when search term changes
        setCurrentPage(1);
        loadPayments();
    }, [searchTerm]);

    // Get payment status text
    const getPaymentStatusText = (status: number) => {
        switch (status) {
            case 0:
                return 'Chờ xử lý';
            case 1:
                return 'Hoàn thành';
            case 2:
                return 'Thất bại';
            case 3:
                return 'Đã hoàn tiền';
            default:
                return 'Không xác định';
        }
    };

    // Get payment status color
    const getPaymentStatusColor = (status: number) => {
        switch (status) {
            case 0:
                return 'bg-yellow-100 text-yellow-800 border-yellow-300';
            case 1:
                return 'bg-green-100 text-green-800 border-green-300';
            case 2:
                return 'bg-red-100 text-red-800 border-red-300';
            case 3:
                return 'bg-blue-100 text-blue-800 border-blue-300';
            default:
                return 'bg-gray-100 text-gray-800 border-gray-300';
        }
    };

    // Get payment status icon
    const getPaymentStatusIcon = (status: number) => {
        switch (status) {
            case 0:
                return <Clock className="w-4 h-4" />;
            case 1:
                return <CheckCircle className="w-4 h-4" />;
            case 2:
                return <XCircle className="w-4 h-4" />;
            case 3:
                return <RefreshCw className="w-4 h-4" />;
            default:
                return null;
        }
    };

    // Get payment method text
    const getPaymentMethodText = (method: string) => {
        const methodMap: { [key: string]: string } = {
            'COD': 'Tiền mặt',
            'BANK_TRANSFER': 'Chuyển khoản',
            'E_WALLET': 'Ví điện tử',
            'CREDIT_CARD': 'Thẻ tín dụng',
            'MOMO': 'MoMo',
            'ZALOPAY': 'ZaloPay',
            'VNPAY': 'VNPay',
        };
        return methodMap[method] || method;
    };

    // Format price
    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    // Format date
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    // Handle view payment
    const handleViewPayment = async (payment: PaymentResponse) => {
        setSelectedPayment(payment);
        setIsViewDialogOpen(true);
    };

    // Handle update payment status
    const handleUpdateStatus = async (paymentId: string, newStatus: number) => {
        if (!confirm(`Bạn có chắc chắn muốn cập nhật trạng thái thanh toán này?`)) {
            return;
        }

        try {
            const response = await paymentService.updatePaymentStatus(paymentId, newStatus);
            if (response.success) {
                toast.success('Cập nhật trạng thái thành công!');
                loadPayments();
                if (selectedPayment?.paymentId === paymentId && response.data) {
                    setSelectedPayment(response.data);
                }
            } else {
                toast.error(response.message || 'Không thể cập nhật trạng thái');
            }
        } catch (error) {
            console.error('Error updating payment status:', error);
            toast.error('Có lỗi xảy ra khi cập nhật trạng thái');
        }
    };

    // Pagination handlers
    const handlePreviousPage = () => {
        if (currentPage > 1) {
            setCurrentPage(currentPage - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(currentPage + 1);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2">Quản lý Thanh toán</h1>
                            <p className="text-gray-600">Quản lý và theo dõi các giao dịch thanh toán</p>
                        </div>
                        <div className="flex items-center gap-3">
                            <div className="bg-white px-4 py-2 rounded-lg border shadow-sm">
                                <div className="flex items-center gap-2">
                                    <DollarSign className="w-5 h-5 text-green-600" />
                                    <div>
                                        <div className="text-xs text-gray-600">Tổng thanh toán</div>
                                        <div className="text-lg font-bold text-gray-900">{totalItems}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Filters */}
                <div className="bg-white p-4 rounded-lg shadow-sm border mb-6">
                    <div className="flex gap-4 items-center">
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <Input
                                placeholder="Tìm kiếm theo mã thanh toán, mã giao dịch..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="pl-10 border-gray-300"
                            />
                        </div>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="outline" className="min-w-[180px] justify-between">
                                    {statusFilter === 'all' && 'Tất cả trạng thái'}
                                    {statusFilter === '0' && 'Chờ xử lý'}
                                    {statusFilter === '1' && 'Hoàn thành'}
                                    {statusFilter === '2' && 'Thất bại'}
                                    {statusFilter === '3' && 'Đã hoàn tiền'}
                                    <ChevronLeft className="w-4 h-4 rotate-[-90deg]" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent className="w-[180px]">
                                <DropdownMenuItem onClick={() => setStatusFilter('all')}>
                                    Tất cả trạng thái
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('0')}>
                                    <Clock className="w-4 h-4 mr-2 text-yellow-600" />
                                    Chờ xử lý
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('1')}>
                                    <CheckCircle className="w-4 h-4 mr-2 text-green-600" />
                                    Hoàn thành
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('2')}>
                                    <XCircle className="w-4 h-4 mr-2 text-red-600" />
                                    Thất bại
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('3')}>
                                    <RefreshCw className="w-4 h-4 mr-2 text-blue-600" />
                                    Đã hoàn tiền
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-gray-50">
                                <TableHead className="font-semibold">Mã thanh toán</TableHead>
                                <TableHead className="font-semibold">Phương thức</TableHead>
                                <TableHead className="font-semibold">Số tiền</TableHead>
                                <TableHead className="font-semibold">Trạng thái</TableHead>
                                <TableHead className="font-semibold">Ngày thanh toán</TableHead>
                                <TableHead className="font-semibold">Mã giao dịch</TableHead>
                                <TableHead className="font-semibold text-right">Thao tác</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                // Loading skeleton
                                Array.from({ length: 5 }).map((_, index) => (
                                    <TableRow key={index}>
                                        <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                                        <TableCell><Skeleton className="h-6 w-24" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                                        <TableCell><Skeleton className="h-8 w-20 ml-auto" /></TableCell>
                                    </TableRow>
                                ))
                            ) : payments.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} className="text-center py-12">
                                        <CreditCard className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                                        <p className="text-gray-600 font-medium">Không tìm thấy thanh toán nào</p>
                                        <p className="text-gray-400 text-sm mt-1">
                                            {searchTerm || statusFilter !== 'all'
                                                ? 'Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm'
                                                : 'Chưa có giao dịch thanh toán nào'}
                                        </p>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                payments.map((payment) => (
                                    <TableRow key={payment.paymentId} className="hover:bg-gray-50">
                                        <TableCell className="font-mono text-sm">
                                            {payment.paymentId.substring(0, 8)}...
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant="outline" className="font-normal">
                                                {getPaymentMethodText(payment.paymentMethod)}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="font-semibold text-green-600">
                                            {formatPrice(payment.amount)}
                                        </TableCell>
                                        <TableCell>
                                            <Badge className={`${getPaymentStatusColor(payment.paymentStatus)} border`}>
                                                <span className="flex items-center gap-1">
                                                    {getPaymentStatusIcon(payment.paymentStatus)}
                                                    {getPaymentStatusText(payment.paymentStatus)}
                                                </span>
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-sm text-gray-600">
                                            {formatDate(payment.paymentDate)}
                                        </TableCell>
                                        <TableCell className="font-mono text-xs text-gray-500">
                                            {payment.transactionId || '-'}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    onClick={() => handleViewPayment(payment)}
                                                    className="flex items-center gap-1"
                                                >
                                                    <Eye className="w-4 h-4" />
                                                    Xem
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>

                    {/* Pagination */}
                    {!loading && payments.length > 0 && (
                        <div className="flex items-center justify-between p-4 border-t bg-gray-50">
                            <div className="text-sm text-gray-600">
                                Hiển thị {(currentPage - 1) * itemsPerPage + 1} -{' '}
                                {Math.min(currentPage * itemsPerPage, totalItems)} trong tổng số {totalItems} thanh toán
                            </div>
                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handlePreviousPage}
                                    disabled={currentPage === 1}
                                    className="flex items-center gap-1"
                                >
                                    <ChevronLeft className="w-4 h-4" />
                                    Trước
                                </Button>
                                <div className="flex items-center gap-1">
                                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                        let pageNum;
                                        if (totalPages <= 5) {
                                            pageNum = i + 1;
                                        } else if (currentPage <= 3) {
                                            pageNum = i + 1;
                                        } else if (currentPage >= totalPages - 2) {
                                            pageNum = totalPages - 4 + i;
                                        } else {
                                            pageNum = currentPage - 2 + i;
                                        }
                                        return (
                                            <Button
                                                key={i}
                                                variant={currentPage === pageNum ? 'default' : 'outline'}
                                                size="sm"
                                                onClick={() => setCurrentPage(pageNum)}
                                                className="w-8 h-8 p-0"
                                            >
                                                {pageNum}
                                            </Button>
                                        );
                                    })}
                                </div>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleNextPage}
                                    disabled={currentPage === totalPages}
                                    className="flex items-center gap-1"
                                >
                                    Sau
                                    <ChevronRight className="w-4 h-4" />
                                </Button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* View Payment Dialog */}
            {selectedPayment && (
                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="max-w-2xl">
                        <DialogHeader>
                            <DialogTitle className="text-xl font-bold">Chi tiết thanh toán</DialogTitle>
                            <DialogDescription>
                                Thông tin chi tiết về giao dịch thanh toán
                            </DialogDescription>
                        </DialogHeader>

                        <div className="space-y-4">
                            {/* Payment Info */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Mã thanh toán</label>
                                    <div className="font-mono text-sm bg-gray-50 p-2 rounded border">
                                        {selectedPayment.paymentId}
                                    </div>
                                </div>
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Trạng thái</label>
                                    <div>
                                        <Badge className={`${getPaymentStatusColor(selectedPayment.paymentStatus)} border`}>
                                            <span className="flex items-center gap-1">
                                                {getPaymentStatusIcon(selectedPayment.paymentStatus)}
                                                {getPaymentStatusText(selectedPayment.paymentStatus)}
                                            </span>
                                        </Badge>
                                    </div>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Phương thức thanh toán</label>
                                    <div className="flex items-center gap-2">
                                        <CreditCard className="w-4 h-4 text-gray-500" />
                                        <span className="font-medium">{getPaymentMethodText(selectedPayment.paymentMethod)}</span>
                                    </div>
                                </div>
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Số tiền</label>
                                    <div className="text-2xl font-bold text-green-600">
                                        {formatPrice(selectedPayment.amount)}
                                    </div>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Ngày thanh toán</label>
                                    <div className="text-sm">{formatDate(selectedPayment.paymentDate)}</div>
                                </div>
                                <div className="space-y-1">
                                    <label className="text-sm font-medium text-gray-600">Mã giao dịch</label>
                                    <div className="font-mono text-sm bg-gray-50 p-2 rounded border">
                                        {selectedPayment.transactionId || 'Không có'}
                                    </div>
                                </div>
                            </div>

                            {/* Status Actions */}
                            <div className="border-t pt-4">
                                <label className="text-sm font-medium text-gray-600 block mb-3">Cập nhật trạng thái</label>
                                <div className="flex gap-2 flex-wrap">
                                    {selectedPayment.paymentStatus !== 1 && (
                                        <Button
                                            size="sm"
                                            className="bg-green-600 hover:bg-green-700"
                                            onClick={() => handleUpdateStatus(selectedPayment.paymentId, 1)}
                                        >
                                            <CheckCircle className="w-4 h-4 mr-1" />
                                            Đánh dấu Hoàn thành
                                        </Button>
                                    )}
                                    {selectedPayment.paymentStatus !== 2 && (
                                        <Button
                                            size="sm"
                                            variant="outline"
                                            className="border-red-300 text-red-600 hover:bg-red-50"
                                            onClick={() => handleUpdateStatus(selectedPayment.paymentId, 2)}
                                        >
                                            <XCircle className="w-4 h-4 mr-1" />
                                            Đánh dấu Thất bại
                                        </Button>
                                    )}
                                    {selectedPayment.paymentStatus === 1 && (
                                        <Button
                                            size="sm"
                                            variant="outline"
                                            className="border-blue-300 text-blue-600 hover:bg-blue-50"
                                            onClick={() => handleUpdateStatus(selectedPayment.paymentId, 3)}
                                        >
                                            <RefreshCw className="w-4 h-4 mr-1" />
                                            Hoàn tiền
                                        </Button>
                                    )}
                                </div>
                            </div>
                        </div>

                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsViewDialogOpen(false)}>
                                Đóng
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
};

export default AdminPayment;
