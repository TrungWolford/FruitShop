import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { toast } from 'sonner';
import LeftTaskbar from '../../components/Admin/LeftTaskbar/LeftTaskbar';
import Header from '@/components/Admin/Header';
import Container from '@/components/Admin/Container';

const TASKBAR_MARGIN_TOP = 'mt-[60px]';

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
import { Search, RotateCcw, ChevronLeft, ChevronRight, Eye, AlertCircle, CheckCircle, XCircle } from 'lucide-react';
import refundService from '../../services/refundService';
import type { RefundResponse } from '../../services/refundService';

const AdminRefund: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, isInitialized } = useAppSelector((state) => state.adminAuth);
  const [refunds, setRefunds] = useState<RefundResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRefund, setSelectedRefund] = useState<RefundResponse | null>(null);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [isProcessing, setIsProcessing] = useState(false);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const itemsPerPage = 10;

  // Auth check
  useEffect(() => {
    document.title = 'FruitShop - Quản lý hoàn tiền';

    // Chờ auth được khởi tạo xong từ localStorage
    if (!isInitialized) {
      return;
    }

    // Check if user is authenticated and has ADMIN role
    if (!isAuthenticated || !user) {
      navigate('/admin');
      return;
    }

    const userRoles = user.roles || [];
    const isAdmin = userRoles.some(role => role.roleName === 'ADMIN');

    if (!isAdmin) {
      navigate('/admin');
      return;
    }

    loadRefunds();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isInitialized, isAuthenticated, user, navigate]);

  // Load refunds from backend
  const loadRefunds = async () => {
    try {
      setLoading(true);
      let allRefunds: RefundResponse[] = [];

      const largeSize = 1000;
      if (searchTerm.trim() && statusFilter !== 'all') {
        // Both search and filter - we'll load all and filter on frontend
        const searchResponse = await refundService.searchRefunds(searchTerm.trim(), 0, largeSize);
        if (searchResponse.success && searchResponse.data) {
          allRefunds = searchResponse.data.content.filter(r => r.refundStatus === statusFilter);
        }
      } else if (searchTerm.trim()) {
        // Only search
        const response = await refundService.searchRefunds(searchTerm.trim(), 0, largeSize);
        if (response.success && response.data) {
          allRefunds = response.data.content;
        }
      } else if (statusFilter !== 'all') {
        // Only filter
        const response = await refundService.getRefundsByStatus(statusFilter, 0, largeSize);
        if (response.success && response.data) {
          allRefunds = response.data.content;
        }
      } else {
        // No search or filter
        const response = await refundService.getAllRefunds(0, largeSize);
        if (response.success && response.data) {
          allRefunds = response.data.content;
        }
      }

      setTotalItems(allRefunds.length);
      setTotalPages(Math.max(1, Math.ceil(allRefunds.length / itemsPerPage)));

      // Paginate on frontend
      const startIndex = (currentPage - 1) * itemsPerPage;
      const endIndex = startIndex + itemsPerPage;
      setRefunds(allRefunds.slice(startIndex, endIndex));
    } catch (error) {
      toast.error('Có lỗi xảy ra khi tải danh sách hoàn tiền');
      setRefunds([]);
      setTotalItems(0);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  // Reload data when filters change (only if authenticated)
  useEffect(() => {
    if (isInitialized && isAuthenticated && user) {
      loadRefunds();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, statusFilter, searchTerm]);

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

  // Get refund status badge
  const getRefundStatusBadge = (status: string) => {
    const statusConfig: Record<string, { text: string; color: string }> = {
      'Chờ xác nhận': { text: 'Chờ xác nhận', color: 'bg-yellow-700 border-yellow-700' },
      'Đã duyệt': { text: 'Đã duyệt', color: 'bg-blue-700 border-blue-700' },
      'Từ chối': { text: 'Từ chối', color: 'bg-red-700 border-red-700' },
      'Hoàn thành': { text: 'Hoàn thành', color: 'bg-green-700 border-green-700' },
    };

    const config = statusConfig[status] || {
      text: status || 'Không xác định',
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
  const handleViewDetail = (refund: RefundResponse) => {
    setSelectedRefund(refund);
    setIsViewDialogOpen(true);
  };

  // Handle approve refund
  const handleApproveRefund = async (refundId: string) => {
    if (!confirm('Bạn có chắc chắn muốn duyệt yêu cầu hoàn tiền này?')) {
      return;
    }

    try {
      setIsProcessing(true);
      const response = await refundService.approveRefund(refundId);
      if (response.success) {
        toast.success('Đã duyệt yêu cầu hoàn tiền!');
        loadRefunds();
        if (selectedRefund?.refundId === refundId) {
          setSelectedRefund(response.data || null);
        }
      } else {
        toast.error(response.message || 'Không thể duyệt yêu cầu hoàn tiền');
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi duyệt yêu cầu hoàn tiền');
    } finally {
      setIsProcessing(false);
    }
  };

  // Handle reject refund
  const handleRejectRefund = async (refundId: string) => {
    if (!confirm('Bạn có chắc chắn muốn từ chối yêu cầu hoàn tiền này?')) {
      return;
    }

    try {
      setIsProcessing(true);
      const response = await refundService.rejectRefund(refundId);
      if (response.success) {
        toast.success('Đã từ chối yêu cầu hoàn tiền!');
        loadRefunds();
        if (selectedRefund?.refundId === refundId) {
          setSelectedRefund(response.data || null);
        }
      } else {
        toast.error(response.message || 'Không thể từ chối yêu cầu hoàn tiền');
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi từ chối yêu cầu hoàn tiền');
    } finally {
      setIsProcessing(false);
    }
  };

  // Handle complete refund
  const handleCompleteRefund = async (refundId: string) => {
    if (!confirm('Bạn có chắc chắn muốn hoàn thành yêu cầu hoàn tiền này?')) {
      return;
    }

    try {
      setIsProcessing(true);
      const response = await refundService.completeRefund(refundId);
      if (response.success) {
        toast.success('Đã hoàn thành yêu cầu hoàn tiền!');
        loadRefunds();
        if (selectedRefund?.refundId === refundId) {
          setSelectedRefund(response.data || null);
        }
      } else {
        toast.error(response.message || 'Không thể hoàn thành yêu cầu hoàn tiền');
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi hoàn thành yêu cầu hoàn tiền');
    } finally {
      setIsProcessing(false);
    }
  };

  // Handle cancel refund
  const handleCancelRefund = async (refundId: string) => {
    if (!confirm('Bạn có chắc chắn muốn hủy yêu cầu hoàn tiền này? Hành động này không thể hoàn tác.')) {
      return;
    }

    try {
      setIsProcessing(true);
      const response = await refundService.cancelRefund(refundId);
      if (response.success) {
        toast.success('Đã hủy yêu cầu hoàn tiền!');
        setIsViewDialogOpen(false);
        loadRefunds();
      } else {
        toast.error(response.message || 'Không thể hủy yêu cầu hoàn tiền');
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra khi hủy yêu cầu hoàn tiền');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <Header />
      <LeftTaskbar className={`${TASKBAR_MARGIN_TOP}`} />

      {/* body */}
      <Container className="">
        {/* Header */}
        <div className="mb-3">
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <RotateCcw className="w-5 h-5 text-amber-500" />
            Quản lý hoàn tiền
          </h1>
          <p className="text-gray-600 mt-0.5 text-base">Quản lý tất cả yêu cầu hoàn tiền trong hệ thống</p>
        </div>

        {/* Search and Filters */}
        <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
          <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
            <div className="flex gap-2 flex-1">
              <div className="relative max-w-xs">
                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <Input
                  placeholder="Tìm kiếm hoàn tiền..."
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
                        : statusFilter}
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
                    onClick={() => setStatusFilter('Chờ xác nhận')}
                    className="cursor-pointer hover:bg-gray-100"
                  >
                    Chờ xác nhận
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setStatusFilter('Đã duyệt')}
                    className="cursor-pointer hover:bg-gray-100"
                  >
                    Đã duyệt
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setStatusFilter('Từ chối')}
                    className="cursor-pointer hover:bg-gray-100"
                  >
                    Từ chối
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setStatusFilter('Hoàn thành')}
                    className="cursor-pointer hover:bg-gray-100"
                  >
                    Hoàn thành
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="mb-3 grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="bg-white p-3 rounded-lg shadow-sm border">
            <div className="text-sm text-gray-600">Tổng yêu cầu</div>
            <div className="text-2xl font-bold text-gray-900">{totalItems}</div>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm border">
            <div className="text-sm text-gray-600">Chờ xác nhận</div>
            <div className="text-2xl font-bold text-yellow-600">
              {refunds.filter((r) => r.refundStatus === 'Chờ xác nhận').length}
            </div>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm border">
            <div className="text-sm text-gray-600">Đã duyệt</div>
            <div className="text-2xl font-bold text-blue-600">
              {refunds.filter((r) => r.refundStatus === 'Đã duyệt').length}
            </div>
          </div>
          <div className="bg-white p-3 rounded-lg shadow-sm border">
            <div className="text-sm text-gray-600">Hoàn thành</div>
            <div className="text-2xl font-bold text-green-600">
              {refunds.filter((r) => r.refundStatus === 'Hoàn thành').length}
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
                    Mã hoàn tiền
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                    Mã đơn hàng
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-40">
                    Khách hàng
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-32">
                    Số tiền
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-40">
                    Ngày yêu cầu
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
                ) : refunds.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-12 text-gray-500">
                      <RotateCcw className="w-16 h-16 mx-auto mb-3 text-gray-400" />
                      <p className="text-lg font-medium">Không tìm thấy yêu cầu hoàn tiền nào</p>
                      <p className="text-sm text-gray-400 mt-1">Thử thay đổi bộ lọc hoặc tìm kiếm</p>
                    </TableCell>
                  </TableRow>
                ) : (
                  refunds.map((refund, index) => (
                    <TableRow
                      key={refund.refundId}
                      className={`transition-all duration-200 cursor-pointer ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                        } hover:bg-blue-50/50`}
                    >
                      <TableCell className="px-4 py-3 text-center">
                        <div className="flex justify-center">
                          <div
                            className="px-2 py-1 bg-amber-100 rounded flex items-center gap-1.5 cursor-pointer hover:bg-amber-200 transition-colors"
                            title={refund.refundId}
                          >
                            <RotateCcw className="w-3.5 h-3.5 text-amber-600" />
                            <span className="text-sm font-medium text-amber-700">
                              #{refund.refundId?.slice(0, 8) || 'N/A'}
                            </span>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 text-center">
                        <span className="text-sm font-medium text-gray-700">
                          #{refund.orderId?.slice(0, 8) || 'N/A'}
                        </span>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <span className="text-sm">{refund.accountName || 'N/A'}</span>
                      </TableCell>
                      <TableCell className="px-4 py-3 text-center">
                        <span className="text-sm font-semibold text-green-600">
                          {formatPrice(refund.refundAmount)}
                        </span>
                      </TableCell>
                      <TableCell className="px-4 py-3 text-center">
                        <span className="text-sm text-gray-600">
                          {formatDate(refund.requestedAt)}
                        </span>
                      </TableCell>
                      <TableCell className="px-4 py-3 text-center">
                        {getRefundStatusBadge(refund.refundStatus)}
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="flex items-center justify-center gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewDetail(refund)}
                            className="h-8 px-2 hover:bg-blue-100"
                            title="Xem chi tiết"
                          >
                            <Eye className="w-4 h-4 text-blue-600" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          {!loading && refunds.length > 0 && (
            <div className="flex items-center justify-between px-4 py-3 bg-gray-50 border-t">
              <div className="flex-1 text-sm text-gray-700">
                Hiển thị {(currentPage - 1) * itemsPerPage + 1} đến{' '}
                {Math.min(currentPage * itemsPerPage, totalItems)} trong tổng số {totalItems} yêu cầu
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                  disabled={currentPage === 1}
                  className="h-8"
                >
                  <ChevronLeft className="w-4 h-4" />
                </Button>
                <span className="text-sm text-gray-700">
                  Trang {currentPage} / {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                  disabled={currentPage === totalPages}
                  className="h-8"
                >
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          )}
        </div>
      </Container>

      {/* View Detail Dialog */}
      <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <RotateCcw className="w-5 h-5 text-amber-600" />
              Chi tiết yêu cầu hoàn tiền
            </DialogTitle>
            <DialogDescription>
              Xem và quản lý thông tin yêu cầu hoàn tiền
            </DialogDescription>
          </DialogHeader>

          {selectedRefund && (
            <div className="space-y-4">
              {/* Refund Info */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                    <AlertCircle className="w-5 h-5 text-amber-600" />
                    Thông tin hoàn tiền
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                    <div>
                      <p className="text-sm text-gray-600">Mã hoàn tiền</p>
                      <p className="font-semibold">#{selectedRefund.refundId}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Mã đơn hàng</p>
                      <p className="font-semibold">#{selectedRefund.orderId}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Khách hàng</p>
                      <p className="font-semibold">{selectedRefund.accountName || 'N/A'}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Số tiền hoàn</p>
                      <p className="font-semibold text-green-600">
                        {formatPrice(selectedRefund.refundAmount)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Trạng thái</p>
                      <div className="mt-1">
                        {getRefundStatusBadge(selectedRefund.refundStatus)}
                      </div>
                    </div>
                  </div>
                </div>

                <div>
                  <h3 className="font-semibold text-lg mb-3">Lý do & Thời gian</h3>
                  <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                    <div>
                      <p className="text-sm text-gray-600">Lý do hoàn tiền</p>
                      <p className="font-medium mt-1">{selectedRefund.reason || 'Không có lý do'}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Ngày yêu cầu</p>
                      <p className="font-semibold">{formatDate(selectedRefund.requestedAt)}</p>
                    </div>
                    {selectedRefund.processedAt && (
                      <div>
                        <p className="text-sm text-gray-600">Ngày xử lý</p>
                        <p className="font-semibold">{formatDate(selectedRefund.processedAt)}</p>
                      </div>
                    )}
                    {selectedRefund.orderDate && (
                      <div>
                        <p className="text-sm text-gray-600">Ngày đặt hàng</p>
                        <p className="font-semibold">{formatDate(selectedRefund.orderDate)}</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          <DialogFooter className="flex justify-between items-center">
            <div className="flex gap-2">
              {selectedRefund && selectedRefund.refundStatus === 'Chờ xác nhận' && (
                <>
                  <Button
                    onClick={() => handleApproveRefund(selectedRefund.refundId)}
                    className="bg-blue-600 hover:bg-blue-700 text-white"
                    disabled={isProcessing}
                  >
                    <CheckCircle className="w-4 h-4 mr-2" />
                    Duyệt
                  </Button>
                  <Button
                    onClick={() => handleRejectRefund(selectedRefund.refundId)}
                    variant="destructive"
                    disabled={isProcessing}
                  >
                    <XCircle className="w-4 h-4 mr-2" />
                    Từ chối
                  </Button>
                </>
              )}
              {selectedRefund && selectedRefund.refundStatus === 'Đã duyệt' && (
                <Button
                  onClick={() => handleCompleteRefund(selectedRefund.refundId)}
                  className="bg-green-600 hover:bg-green-700 text-white"
                  disabled={isProcessing}
                >
                  <CheckCircle className="w-4 h-4 mr-2" />
                  Hoàn thành
                </Button>
              )}
              {selectedRefund && selectedRefund.refundStatus !== 'Hoàn thành' && (
                <Button
                  onClick={() => handleCancelRefund(selectedRefund.refundId)}
                  variant="outline"
                  className="border-red-300 text-red-600 hover:bg-red-50"
                  disabled={isProcessing}
                >
                  Hủy yêu cầu
                </Button>
              )}
            </div>
            <Button
              variant="outline"
              onClick={() => setIsViewDialogOpen(false)}
              className="rounded-md"
              disabled={isProcessing}
            >
              Đóng
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminRefund;
