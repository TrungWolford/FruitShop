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
import { Search, Truck, ChevronLeft, ChevronRight, Eye, User, Phone, MapPin, Package } from 'lucide-react';

// Mock Shipping Response Type
export interface ShippingResponse {
    shippingId: string;
    orderId?: string;
    accountId: string;
    accountName?: string;
    receiverName: string;
    receiverPhone: string;
    receiverAddress: string;
    city: string;
    shippingFee?: number;
    shippedAt?: string;
    status: number; // 0: Chờ xác nhận, 1: Đang vận chuyển, 2: Đã giao hàng
}

// Mock Shipping Service
const mockShippingService = {
    getAllShippings: async (
        _page: number = 0,
        _size: number = 10
    ): Promise<{ success: boolean; data?: ShippingResponse[]; message?: string }> => {
        // Simulate API delay
        await new Promise((resolve) => setTimeout(resolve, 500));

        // Mock data
        const mockData: ShippingResponse[] = [
            {
                shippingId: 'SH001',
                orderId: 'ORD001',
                accountId: 'ACC001',
                accountName: 'Nguyễn Văn A',
                receiverName: 'Nguyễn Văn A',
                receiverPhone: '0901234567',
                receiverAddress: '123 Lê Lợi',
                city: 'TP. Hồ Chí Minh',
                shippingFee: 30000,
                shippedAt: '2025-01-15T10:30:00',
                status: 2,
            },
            {
                shippingId: 'SH002',
                orderId: 'ORD002',
                accountId: 'ACC002',
                accountName: 'Trần Thị B',
                receiverName: 'Trần Thị B',
                receiverPhone: '0912345678',
                receiverAddress: '456 Nguyễn Huệ',
                city: 'Hà Nội',
                shippingFee: 25000,
                shippedAt: '2025-01-16T14:20:00',
                status: 1,
            },
            {
                shippingId: 'SH003',
                orderId: 'ORD003',
                accountId: 'ACC003',
                accountName: 'Lê Văn C',
                receiverName: 'Lê Văn C',
                receiverPhone: '0923456789',
                receiverAddress: '789 Trần Hưng Đạo',
                city: 'Đà Nẵng',
                shippingFee: 35000,
                shippedAt: '2025-01-17T09:15:00',
                status: 0,
            },
            {
                shippingId: 'SH004',
                orderId: 'ORD004',
                accountId: 'ACC004',
                accountName: 'Phạm Thị D',
                receiverName: 'Phạm Thị D',
                receiverPhone: '0934567890',
                receiverAddress: '321 Hai Bà Trưng',
                city: 'TP. Hồ Chí Minh',
                shippingFee: 30000,
                shippedAt: '2025-01-18T16:45:00',
                status: 1,
            },
            {
                shippingId: 'SH005',
                orderId: 'ORD005',
                accountId: 'ACC005',
                accountName: 'Hoàng Văn E',
                receiverName: 'Hoàng Văn E',
                receiverPhone: '0945678901',
                receiverAddress: '654 Lý Thường Kiệt',
                city: 'Cần Thơ',
                shippingFee: 40000,
                shippedAt: '2025-01-19T11:30:00',
                status: 2,
            },
        ];

        return {
            success: true,
            data: mockData,
        };
    },
};

const AdminShipping: React.FC = () => {
    const [shippings, setShippings] = useState<ShippingResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedShipping, setSelectedShipping] = useState<ShippingResponse | null>(null);
    const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

    // Load shippings from backend
    const loadShippings = async (page: number = 0) => {
        try {
            setLoading(true);
            const response = await mockShippingService.getAllShippings(page, itemsPerPage);

            console.log('🔍 Backend Response:', response);

            if (response.success && response.data) {
                let filteredShippings = response.data;

                // Apply status filter locally if needed
                if (statusFilter !== 'all') {
                    const status = parseInt(statusFilter);
                    filteredShippings = filteredShippings.filter(
                        (shipping: ShippingResponse) => shipping.status === status
                    );
                }

                setShippings(filteredShippings);
                setTotalItems(filteredShippings.length);
                setTotalPages(Math.ceil(filteredShippings.length / itemsPerPage));
            } else {
                toast.error('Không thể tải danh sách vận chuyển');
                setShippings([]);
            }
        } catch (error) {
            console.error('Error loading shippings:', error);
            toast.error('Có lỗi xảy ra khi tải vận chuyển');
            setShippings([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadShippings(currentPage - 1);
    }, [currentPage, statusFilter]);

    // Format price
    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    // Format date
    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    // Get shipping status badge
    const getShippingStatusBadge = (status: number) => {
        const statusConfig: Record<number, { text: string; color: string }> = {
            0: { text: 'Chờ xác nhận', color: 'bg-yellow-700 border-yellow-700' },
            1: { text: 'Đang vận chuyển', color: 'bg-blue-700 border-blue-700' },
            2: { text: 'Đã giao hàng', color: 'bg-green-700 border-green-700' },
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

    // Handle view detail
    const handleViewDetail = (shipping: ShippingResponse) => {
        setSelectedShipping(shipping);
        setIsViewDialogOpen(true);
    };

    // Filter shippings by search term
    const filteredShippings = shippings.filter((shipping) => {
        const matchesSearch =
            shipping.shippingId.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (shipping.receiverName && shipping.receiverName.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (shipping.receiverPhone && shipping.receiverPhone.includes(searchTerm));
        return matchesSearch;
    });

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Truck className="w-5 h-5 text-amber-500" />
                        Quản lý vận chuyển
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">Quản lý tất cả đơn vận chuyển trong hệ thống</p>
                </div>

                {/* Search and Filters */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm vận chuyển..."
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
                                                ? 'Chờ xác nhận'
                                                : statusFilter === '1'
                                                ? 'Đang giao'
                                                : 'Đã giao'}
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
                                        Chờ xác nhận
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('1')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Đang vận chuyển
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => setStatusFilter('2')}
                                        className="cursor-pointer hover:bg-gray-100"
                                    >
                                        Đã giao hàng
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                    </div>
                </div>

                {/* Stats */}
                <div className="mb-3 grid grid-cols-1 md:grid-cols-4 gap-3">
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Tổng vận chuyển</div>
                        <div className="text-2xl font-bold text-gray-900">{totalItems}</div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Chờ xác nhận</div>
                        <div className="text-2xl font-bold text-yellow-600">
                            {shippings.filter((s) => s.status === 0).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đang vận chuyển</div>
                        <div className="text-2xl font-bold text-blue-600">
                            {shippings.filter((s) => s.status === 1).length}
                        </div>
                    </div>
                    <div className="bg-white p-3 rounded-lg shadow-sm border">
                        <div className="text-sm text-gray-600">Đã giao hàng</div>
                        <div className="text-2xl font-bold text-green-600">
                            {shippings.filter((s) => s.status === 2).length}
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
                                        Mã vận chuyển
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-40">
                                        Người nhận
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-32">
                                        Số điện thoại
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-48">
                                        Địa chỉ
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                                        Phí vận chuyển
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
                                                <Skeleton className="h-4 w-28" />
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <Skeleton className="h-4 w-40" />
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="flex justify-center">
                                                    <Skeleton className="h-4 w-24" />
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
                                ) : filteredShippings.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={7} className="text-center py-12 text-gray-500">
                                            <Truck className="w-16 h-16 mx-auto mb-3 text-gray-400" />
                                            <p className="text-lg font-medium">Không tìm thấy vận chuyển nào</p>
                                            <p className="text-sm text-gray-400 mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm</p>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    filteredShippings.map((shipping, index) => (
                                        <TableRow
                                            key={shipping.shippingId}
                                            className={`transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            } hover:bg-blue-50/50`}
                                        >
                                            <TableCell className="px-4 py-3 text-center">
                                                <div className="flex justify-center">
                                                    <div className="px-2 py-1 bg-amber-100 rounded flex items-center gap-1.5">
                                                        <Truck className="w-3.5 h-3.5 text-amber-600" />
                                                        <span className="text-xs font-bold text-amber-600">
                                                            #{shipping.shippingId}
                                                        </span>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3">
                                                <div className="font-medium text-gray-900">
                                                    {shipping.receiverName}
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-sm text-gray-600">
                                                {shipping.receiverPhone}
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-sm text-gray-600">
                                                <div>{shipping.receiverAddress}</div>
                                                <div className="text-xs text-gray-500 mt-0.5">{shipping.city}</div>
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                <span className="font-semibold text-green-600">
                                                    {shipping.shippingFee ? formatPrice(shipping.shippingFee) : 'N/A'}
                                                </span>
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                {getShippingStatusBadge(shipping.status)}
                                            </TableCell>
                                            <TableCell className="px-4 py-3 text-center">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleViewDetail(shipping)}
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
                                    / <span className="font-bold text-gray-900">{totalItems}</span> vận chuyển
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
                <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle className="text-2xl">
                            Chi tiết vận chuyển #{selectedShipping?.shippingId}
                        </DialogTitle>
                        <DialogDescription>Thông tin chi tiết về đơn vận chuyển</DialogDescription>
                    </DialogHeader>

                    {selectedShipping && (
                        <div className="space-y-6">
                            {/* Shipping Info Grid */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-gray-50 p-4 rounded-lg">
                                    <p className="text-sm text-gray-600 mb-1">Mã vận chuyển</p>
                                    <p className="font-semibold">#{selectedShipping.shippingId}</p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-lg">
                                    <p className="text-sm text-gray-600 mb-1">Trạng thái</p>
                                    {getShippingStatusBadge(selectedShipping.status)}
                                </div>
                                <div className="bg-gray-50 p-4 rounded-lg">
                                    <p className="text-sm text-gray-600 mb-1">Mã đơn hàng</p>
                                    <p className="font-semibold">{selectedShipping.orderId || 'N/A'}</p>
                                </div>
                                <div className="bg-gray-50 p-4 rounded-lg">
                                    <p className="text-sm text-gray-600 mb-1">Phí vận chuyển</p>
                                    <p className="font-semibold text-green-600 text-lg">
                                        {selectedShipping.shippingFee
                                            ? formatPrice(selectedShipping.shippingFee)
                                            : 'N/A'}
                                    </p>
                                </div>
                                {selectedShipping.shippedAt && (
                                    <div className="bg-gray-50 p-4 rounded-lg col-span-2">
                                        <p className="text-sm text-gray-600 mb-1">Thời gian giao hàng</p>
                                        <p className="font-semibold">{formatDate(selectedShipping.shippedAt)}</p>
                                    </div>
                                )}
                            </div>

                            {/* Customer Info */}
                            <div>
                                <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                                    <User className="w-5 h-5 text-blue-600" />
                                    Thông tin khách hàng
                                </h3>
                                <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                                    {selectedShipping.accountName && (
                                        <p>
                                            <strong>Tài khoản:</strong> {selectedShipping.accountName}
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Receiver Info */}
                            <div>
                                <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                                    <Package className="w-5 h-5 text-green-600" />
                                    Thông tin người nhận
                                </h3>
                                <div className="bg-gradient-to-r from-green-50 to-blue-50 p-4 rounded-lg border space-y-3">
                                    <div className="flex items-start gap-3">
                                        <User className="w-5 h-5 text-green-600 mt-0.5" />
                                        <div>
                                            <p className="text-sm text-gray-600">Người nhận</p>
                                            <p className="font-semibold">{selectedShipping.receiverName}</p>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3">
                                        <Phone className="w-5 h-5 text-green-600 mt-0.5" />
                                        <div>
                                            <p className="text-sm text-gray-600">Số điện thoại</p>
                                            <p className="font-semibold">{selectedShipping.receiverPhone}</p>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3">
                                        <MapPin className="w-5 h-5 text-green-600 mt-0.5" />
                                        <div>
                                            <p className="text-sm text-gray-600">Địa chỉ</p>
                                            <p className="font-semibold">{selectedShipping.receiverAddress}</p>
                                            <p className="text-sm text-gray-600 mt-1">📍 {selectedShipping.city}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsViewDialogOpen(false)} className="rounded-md">
                            Đóng
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminShipping;
