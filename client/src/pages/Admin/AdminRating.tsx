import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { toast } from 'sonner';
import LeftTaskbar from '../../components/LeftTaskbar';
import { ratingService } from '../../services/ratingService';
import type { Rating } from '../../types/rating';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Skeleton } from '../../components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from '../../components/ui/dialog';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { Search, Star, ChevronLeft, ChevronRight, ChevronDown, Eye } from 'lucide-react';

const AdminRating: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);
    const [ratings, setRatings] = useState<Rating[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedRating, setSelectedRating] = useState<Rating | null>(null);
    const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [starFilter, setStarFilter] = useState<string>('all');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const itemsPerPage = 10;

    // Load ratings from backend
    const loadRatings = async (page: number = 0) => {
        try {
            setLoading(true);
            const response = await ratingService.getAllRatings(page, itemsPerPage);

            console.log('🔍 Backend Response:', response);
            console.log('📦 Response type:', typeof response);
            console.log('📋 Response keys:', Object.keys(response));

            // Backend trả về trực tiếp Page<RatingDetailResponse>
            if (response.content) {
                let filteredRatings = response.content;

                // Apply search filter
                if (searchTerm.trim()) {
                    const term = searchTerm.toLowerCase();
                    filteredRatings = filteredRatings.filter((rating: Rating) =>
                        rating.comment?.toLowerCase().includes(term) ||
                        rating.account?.accountName?.toLowerCase().includes(term) ||
                        rating.product?.productName?.toLowerCase().includes(term)
                    );
                }

                // Apply status filter
                if (statusFilter !== 'all') {
                    const status = statusFilter === 'active' ? 1 : 0;
                    filteredRatings = filteredRatings.filter((rating: Rating) => rating.status === status);
                }

                // Apply star filter
                if (starFilter !== 'all') {
                    const star = parseInt(starFilter);
                    filteredRatings = filteredRatings.filter((rating: Rating) => rating.ratingStar === star);
                }

                setRatings(filteredRatings);
                setTotalPages(response.totalPages || 1);
                setTotalItems(response.totalElements || 0);
            } else {
                console.log('⚠️ No content in response');
                setRatings([]);
                setTotalPages(1);
                setTotalItems(0);
            }
        } catch (error) {
            console.error('❌ Error loading ratings:', error);
            toast.error('Không thể tải danh sách đánh giá từ server');
            setRatings([]);
            setTotalPages(1);
            setTotalItems(0);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        document.title = 'FruitShop - Quản lý đánh giá';

        // Check authentication
        if (!isAuthenticated || !user) {
            navigate('/');
            return;
        }

        const userRoles = user.roles || [];
        const isAdmin = userRoles.some((role) => role.roleName === 'ADMIN');

        if (!isAdmin) {
            navigate('/');
            return;
        }

        loadRatings(currentPage - 1);
    }, [isAuthenticated, user, navigate]);

    useEffect(() => {
        // Debounce search to avoid too many API calls
        const timeoutId = setTimeout(() => {
            loadRatings(0);
            setCurrentPage(1);
        }, 500);

        return () => clearTimeout(timeoutId);
    }, [searchTerm, statusFilter, starFilter]);

    // Handle status change
    const handleToggleStatus = async (rating: Rating) => {
        try {
            await ratingService.changeRatingStatus(rating.ratingId);
            toast.success(
                `${rating.status === 1 ? 'Ẩn' : 'Hiển thị'} đánh giá thành công`
            );
            loadRatings(currentPage - 1);
        } catch (error) {
            console.error('Error changing status:', error);
            toast.error('Không thể thay đổi trạng thái');
        }
    };

    // Handle search
    const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
        setCurrentPage(1);
    };

    // Handle filter changes
    const handleStatusFilterChange = (value: string) => {
        setStatusFilter(value);
        setCurrentPage(1);
    };

    const handleStarFilterChange = (value: string) => {
        setStarFilter(value);
        setCurrentPage(1);
    };

    // Get filter text
    const getStatusFilterText = () => {
        switch (statusFilter) {
            case 'active':
                return 'Đang hiển thị';
            case 'inactive':
                return 'Đã ẩn';
            default:
                return 'Tất cả trạng thái';
        }
    };

    const getStarFilterText = () => {
        switch (starFilter) {
            case '5':
                return '5 sao';
            case '4':
                return '4 sao';
            case '3':
                return '3 sao';
            case '2':
                return '2 sao';
            case '1':
                return '1 sao';
            default:
                return 'Tất cả đánh giá';
        }
    };

    // Format date
    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleDateString('vi-VN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
            });
        } catch {
            return 'N/A';
        }
    };

    // Render star rating
    const renderStars = (rating: number) => {
        return (
            <div className="flex items-center gap-0.5">
                {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                        key={star}
                        className={`w-4 h-4 ${
                            star <= rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'
                        }`}
                    />
                ))}
            </div>
        );
    };

    // Pagination handlers
    const handlePageChange = (page: number) => {
        setCurrentPage(page);
        loadRatings(page - 1);
    };

    const handlePreviousPage = () => {
        if (currentPage > 1) {
            const newPage = currentPage - 1;
            setCurrentPage(newPage);
            loadRatings(newPage - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages) {
            const newPage = currentPage + 1;
            setCurrentPage(newPage);
            loadRatings(newPage - 1);
        }
    };

    const truncate = (text: string | undefined, max = 50) => {
        if (!text) return '';
        return text.length > max ? `${text.slice(0, max)}...` : text;
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Star className="w-5 h-5 text-amber-500" />
                        Quản lý đánh giá
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">
                        Quản lý đánh giá sản phẩm từ khách hàng
                    </p>
                </div>

                {/* Search and Filters */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm đánh giá..."
                                    value={searchTerm}
                                    onChange={handleSearch}
                                    className="pl-8 py-2 text-lg border border-gray-300 bg-gray-50 text-gray-900 placeholder-gray-500 focus:border-amber-500 focus:bg-white transition-all duration-200 rounded-md"
                                />
                            </div>
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button
                                        variant="outline"
                                        className="w-40 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md flex items-center justify-between"
                                    >
                                        <div className="flex items-center">{getStatusFilterText()}</div>
                                        <ChevronDown className="w-4 h-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent className="bg-white border-gray-200 shadow-lg">
                                    <DropdownMenuItem
                                        onClick={() => handleStatusFilterChange('all')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer flex items-center"
                                    >
                                        <div className="w-2 h-2 bg-gray-400 rounded-full mr-2"></div>
                                        Tất cả trạng thái
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStatusFilterChange('active')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer flex items-center"
                                    >
                                        <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                                        Đang hiển thị
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStatusFilterChange('inactive')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer flex items-center"
                                    >
                                        <div className="w-2 h-2 bg-red-500 rounded-full mr-2"></div>
                                        Đã ẩn
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button
                                        variant="outline"
                                        className="w-40 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md flex items-center justify-between"
                                    >
                                        <div className="flex items-center">{getStarFilterText()}</div>
                                        <ChevronDown className="w-4 h-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent className="bg-white border-gray-200 shadow-lg">
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('all')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        Tất cả đánh giá
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('5')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        5 sao
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('4')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        4 sao
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('3')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        3 sao
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('2')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        2 sao
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStarFilterChange('1')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer"
                                    >
                                        1 sao
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                        <div className="flex gap-2">
                            <Button
                                onClick={() => {
                                    if (selectedRating) {
                                        setIsViewDialogOpen(true);
                                    } else {
                                        toast.error('Vui lòng chọn một đánh giá để xem chi tiết');
                                    }
                                }}
                                disabled={!selectedRating}
                                className="bg-green-600 hover:bg-green-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-300 ease-in-out text-sm rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <Eye className="w-3 h-3" />
                                Xem chi tiết
                            </Button>
                        </div>
                    </div>
                </div>

                {/* Ratings Table */}
                <div className="bg-white shadow-sm border border-gray-200 overflow-hidden">
                    <div className="overflow-x-auto">
                        <Table className="w-full">
                            <TableHeader>
                                <TableRow className="bg-slate-800 hover:bg-slate-800 border-b border-slate-700">
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-24">
                                        ID
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-40">
                                        Khách hàng
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-40">
                                        Sản phẩm
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                                        Đánh giá
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm">
                                        Nhận xét
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-24">
                                        Trạng thái
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                                        Ngày tạo
                                    </TableHead>
                                    <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                                        Thao tác
                                    </TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {loading
                                    ? Array.from({ length: 10 }).map((_, index) => (
                                          <TableRow key={`skeleton-${index}`}>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <Skeleton className="h-6 w-16 rounded" />
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <Skeleton className="h-4 w-32" />
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <Skeleton className="h-4 w-32" />
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <Skeleton className="h-4 w-24" />
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <Skeleton className="h-4 w-full" />
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <Skeleton className="h-6 w-20 rounded-full" />
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <Skeleton className="h-4 w-24" />
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <Skeleton className="h-8 w-16 rounded" />
                                                  </div>
                                              </TableCell>
                                          </TableRow>
                                      ))
                                    : ratings.map((rating, index) => (
                                          <TableRow
                                              key={rating.ratingId}
                                              className={`transition-all duration-300 ease-in-out cursor-pointer ${
                                                  index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                              } ${
                                                  selectedRating?.ratingId === rating.ratingId
                                                      ? '!bg-blue-100 !border-l-4 !border-blue-600 shadow-md hover:!bg-blue-100'
                                                      : '!border-l-4 !border-transparent hover:!bg-transparent'
                                              }`}
                                              onClick={() => {
                                                  if (selectedRating?.ratingId === rating.ratingId) {
                                                      setSelectedRating(null);
                                                  } else {
                                                      setSelectedRating(rating);
                                                  }
                                              }}
                                          >
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="flex justify-center">
                                                      <div className="px-2 py-1 bg-amber-100 rounded flex items-center justify-center">
                                                          <span className="text-xs font-bold text-amber-600">
                                                              #{rating.ratingId}
                                                          </span>
                                                      </div>
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="font-medium text-gray-900 text-sm">
                                                      {rating.account?.accountName || 'N/A'}
                                                  </div>
                                                  {rating.account?.accountPhone && (
                                                      <div className="text-xs text-gray-500">
                                                          {rating.account.accountPhone}
                                                      </div>
                                                  )}
                                              </TableCell>
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="font-medium text-gray-900 text-sm">
                                                      {rating.product?.productName || 'N/A'}
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="flex justify-center">
                                                      {renderStars(rating.ratingStar)}
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="text-gray-700 text-sm">
                                                      {truncate(rating.comment, 50)}
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3 select-none">
                                                  <div className="flex justify-center">
                                                      <Badge
                                                          variant={rating.status === 1 ? 'default' : 'secondary'}
                                                          className={`px-2 py-1 text-xs font-medium text-white ${
                                                              rating.status === 1
                                                                  ? 'bg-green-700 border-green-700'
                                                                  : 'bg-red-700 border-red-700'
                                                          }`}
                                                      >
                                                          {rating.status === 1 ? 'Hiển thị' : 'Ẩn'}
                                                      </Badge>
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <span className="text-gray-600 text-xs">
                                                          {formatDate(rating.createdAt)}
                                                      </span>
                                                  </div>
                                              </TableCell>
                                              <TableCell className="px-4 py-3">
                                                  <div className="flex justify-center">
                                                      <Button
                                                          variant="outline"
                                                          size="sm"
                                                          onClick={(e) => {
                                                              e.stopPropagation();
                                                              handleToggleStatus(rating);
                                                          }}
                                                          className={`px-2 py-1 text-xs ${
                                                              rating.status === 1
                                                                  ? 'bg-red-50 text-red-700 hover:bg-red-100 border-red-300'
                                                                  : 'bg-green-50 text-green-700 hover:bg-green-100 border-green-300'
                                                          }`}
                                                      >
                                                          {rating.status === 1 ? 'Ẩn' : 'Hiển thị'}
                                                      </Button>
                                                  </div>
                                              </TableCell>
                                          </TableRow>
                                      ))}
                            </TableBody>
                        </Table>
                    </div>

                    {/* Empty State */}
                    {!loading && ratings.length === 0 && (
                        <div className="text-center py-12 px-6">
                            <div className="w-16 h-16 bg-gray-100 flex items-center justify-center mx-auto mb-3 rounded-lg">
                                <Star className="w-8 h-8 text-gray-400" />
                            </div>
                            <h3 className="text-base font-semibold text-gray-700 mb-2">
                                Không tìm thấy đánh giá nào
                            </h3>
                            <p className="text-gray-500 mb-3 text-sm">
                                Vui lòng thử lại với từ khóa khác hoặc điều chỉnh bộ lọc
                            </p>
                        </div>
                    )}

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="px-4 py-3 border-t border-slate-700 bg-slate-800">
                            <div className="flex items-center justify-between">
                                <div className="text-sm text-white">
                                    Hiển thị {(currentPage - 1) * itemsPerPage + 1} đến{' '}
                                    {Math.min(currentPage * itemsPerPage, totalItems)} trong tổng số {totalItems} đánh
                                    giá
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={handlePreviousPage}
                                        disabled={currentPage === 1}
                                        className="px-3 py-1 text-sm border border-slate-600 text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        <ChevronLeft className="w-4 h-4" />
                                    </Button>

                                    <div className="flex items-center gap-1">
                                        {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                                            <Button
                                                key={page}
                                                variant={currentPage === page ? 'default' : 'outline'}
                                                size="sm"
                                                onClick={() => handlePageChange(page)}
                                                className={`px-3 py-1 text-sm ${
                                                    currentPage === page
                                                        ? 'bg-amber-600 text-white border-amber-600'
                                                        : 'border border-slate-600 bg-white text-black'
                                                }`}
                                            >
                                                {page}
                                            </Button>
                                        ))}
                                    </div>

                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={handleNextPage}
                                        disabled={currentPage === totalPages}
                                        className="px-3 py-1 text-sm border border-slate-600 text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        <ChevronRight className="w-4 h-4" />
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* View Rating Dialog */}
                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
                        <DialogHeader>
                            <DialogTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                                <Star className="w-5 h-5 text-amber-500" />
                                Chi tiết đánh giá
                            </DialogTitle>
                            <DialogDescription className="text-gray-600">
                                Thông tin chi tiết về đánh giá sản phẩm
                            </DialogDescription>
                        </DialogHeader>
                        {selectedRating && (
                            <div className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="text-sm font-semibold text-gray-700">Mã đánh giá</label>
                                        <p className="text-gray-900">#{selectedRating.ratingId}</p>
                                    </div>
                                    <div>
                                        <label className="text-sm font-semibold text-gray-700">Ngày tạo</label>
                                        <p className="text-gray-900">{formatDate(selectedRating.createdAt)}</p>
                                    </div>
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="text-sm font-semibold text-gray-700">Khách hàng</label>
                                        <p className="text-gray-900">{selectedRating.account?.accountName || 'N/A'}</p>
                                        {selectedRating.account?.accountPhone && (
                                            <p className="text-sm text-gray-600">
                                                {selectedRating.account.accountPhone}
                                            </p>
                                        )}
                                    </div>
                                    <div>
                                        <label className="text-sm font-semibold text-gray-700">Sản phẩm</label>
                                        <p className="text-gray-900">{selectedRating.product?.productName || 'N/A'}</p>
                                    </div>
                                </div>
                                <div>
                                    <label className="text-sm font-semibold text-gray-700">Đánh giá</label>
                                    <div className="flex items-center gap-2 mt-1">
                                        {renderStars(selectedRating.ratingStar)}
                                        <span className="text-gray-700 font-medium">
                                            {selectedRating.ratingStar}/5
                                        </span>
                                    </div>
                                </div>
                                <div>
                                    <label className="text-sm font-semibold text-gray-700">Nhận xét</label>
                                    <p className="text-gray-900 mt-1 p-3 bg-gray-50 rounded-md whitespace-pre-wrap">
                                        {selectedRating.comment || 'Không có nhận xét'}
                                    </p>
                                </div>
                                <div>
                                    <label className="text-sm font-semibold text-gray-700">Trạng thái</label>
                                    <div className="mt-1">
                                        <Badge
                                            variant={selectedRating.status === 1 ? 'default' : 'secondary'}
                                            className={`px-3 py-1 text-sm font-medium text-white ${
                                                selectedRating.status === 1
                                                    ? 'bg-green-700 border-green-700'
                                                    : 'bg-red-700 border-red-700'
                                            }`}
                                        >
                                            {selectedRating.status === 1 ? 'Đang hiển thị' : 'Đã ẩn'}
                                        </Badge>
                                    </div>
                                </div>
                                <div className="flex gap-2 pt-4">
                                    <Button
                                        onClick={() => {
                                            handleToggleStatus(selectedRating);
                                            setIsViewDialogOpen(false);
                                        }}
                                        className={`flex-1 ${
                                            selectedRating.status === 1
                                                ? 'bg-red-600 hover:bg-red-700'
                                                : 'bg-green-600 hover:bg-green-700'
                                        } text-white`}
                                    >
                                        {selectedRating.status === 1 ? 'Ẩn đánh giá' : 'Hiển thị đánh giá'}
                                    </Button>
                                    <Button
                                        variant="outline"
                                        onClick={() => setIsViewDialogOpen(false)}
                                        className="flex-1"
                                    >
                                        Đóng
                                    </Button>
                                </div>
                            </div>
                        )}
                    </DialogContent>
                </Dialog>
            </div>
        </div>
    );
};

export default AdminRating;
