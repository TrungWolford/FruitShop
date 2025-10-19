import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { categoryService } from '../../services/categoryService';
import type { Category, PaginatedCategoryResponse } from '../../types/category';
import LeftTaskbar from '../../components/LeftTaskbar';
import AddCategoryModal from '../../components/modals/AddCategoryModal';
import EditCategoryModal from '../../components/modals/EditCategoryModal';
import DeleteCategoryModal from '../../components/modals/DeleteCategoryModal';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { Tags, Search, Edit, Trash2, Plus, ChevronDown, Eye, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { toast } from 'sonner';

const AdminCategory: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);

    // State for categories data
    const [categories, setCategories] = useState<Category[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);
    const [loading, setLoading] = useState(false);

    // State for search and filters
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // State for modals
    const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    useEffect(() => {
        document.title = 'BookCity - Quản lý thể loại';

        // Check if user is authenticated and has ADMIN role
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

        loadCategories();
    }, [currentPage, statusFilter, isAuthenticated, user, navigate]);

    useEffect(() => {
        if (searchTerm.trim()) {
            handleSearch();
        } else {
            loadCategories();
        }
    }, [searchTerm]);

    const loadCategories = async () => {
        setLoading(true);
        try {
            const response: PaginatedCategoryResponse = await categoryService.getAllCategories(currentPage, pageSize);

            let filteredContent = response.content;

            // Apply status filter
            if (statusFilter !== 'all') {
                const status = statusFilter === 'active' ? 1 : 0;
                filteredContent = response.content.filter((cat) => cat.status === status);
            }

            setCategories(filteredContent);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error loading categories:', error);
            toast.error('Không thể tải danh sách thể loại. Vui lòng kiểm tra backend server!');

            // Fallback: Sử dụng dữ liệu mock khi backend không khả dụng
            const mockCategories: Category[] = [
                {
                    categoryId: 'CAT001',
                    categoryName: 'Tiểu thuyết',
                    status: 1,
                },
                {
                    categoryId: 'CAT002',
                    categoryName: 'Khoa học',
                    status: 1,
                },
                {
                    categoryId: 'CAT003',
                    categoryName: 'Lịch sử',
                    status: 0,
                },
            ];

            let filteredMock = mockCategories;
            if (statusFilter !== 'all') {
                const status = statusFilter === 'active' ? 1 : 0;
                filteredMock = mockCategories.filter((cat) => cat.status === status);
            }

            setCategories(filteredMock);
            setTotalElements(filteredMock.length);
            setTotalPages(1);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) return;

        setLoading(true);
        try {
            const response = await categoryService.searchCategories(searchTerm, currentPage, pageSize);
            setCategories(response.content);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error searching categories:', error);
            toast.error('Không thể tìm kiếm thể loại');
        } finally {
            setLoading(false);
        }
    };

    const handleEditCategory = (category: Category) => {
        setSelectedCategory(category);
        setIsEditModalOpen(true);
    };

    const handleDeleteCategory = (category: Category) => {
        setSelectedCategory(category);
        setIsDeleteModalOpen(true);
    };

    const handlePageChange = (newPage: number) => {
        setCurrentPage(newPage);
    };

    const getStatusFilterText = () => {
        switch (statusFilter) {
            case 'active':
                return 'Đang hoạt động';
            case 'inactive':
                return 'Tạm ngưng';
            default:
                return 'Tất cả';
        }
    };

    const handleStatusFilterChange = (value: string) => {
        setStatusFilter(value);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />
            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Tags className="w-5 h-5 text-purple-500" />
                        Quản Lý Thể Loại
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">Quản lý và tìm kiếm các thể loại sách</p>
                </div>

                {/* Search and Actions Bar */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm thể loại..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-8 py-2 text-lg border border-gray-300 bg-gray-50 text-gray-900 placeholder-gray-500 focus:border-purple-500 focus:bg-white transition-all duration-200 rounded-md"
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
                                        Tất cả
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStatusFilterChange('active')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer flex items-center"
                                    >
                                        <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                                        Đang hoạt động
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => handleStatusFilterChange('inactive')}
                                        className="text-gray-900 hover:bg-gray-100 cursor-pointer flex items-center"
                                    >
                                        <div className="w-2 h-2 bg-red-500 rounded-full mr-2"></div>
                                        Tạm ngưng
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        </div>
                        <Button
                            onClick={() => setIsCreateModalOpen(true)}
                            className="bg-purple-600 hover:bg-purple-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-200 text-sm rounded-md"
                        >
                            <Plus className="w-3 h-3" />
                            Thêm Thể Loại
                        </Button>
                    </div>
                </div>

                {/* Table Container */}
                <div className="bg-white shadow-sm border border-gray-200 overflow-hidden">
                    <div className="overflow-x-auto">
                        {loading ? (
                            <div className="flex items-center justify-center py-8">
                                <Loader2 className="w-8 h-8 animate-spin text-purple-600" />
                                <span className="ml-2">Đang tải...</span>
                            </div>
                        ) : (
                            <Table className="w-full">
                                <TableHeader>
                                    <TableRow className="bg-slate-800 hover:bg-slate-800 border-b border-slate-700">
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-16">
                                            Mã
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-left text-sm w-32">
                                            Tên Thể Loại
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-20">
                                            Trạng Thái
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-32">
                                            Thao Tác
                                        </TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {categories.map((category, index) => (
                                        <TableRow
                                            key={category.categoryId}
                                            className={`hover:bg-gray-50 transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            }`}
                                        >
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center">
                                                    <div className="w-6 h-6 bg-purple-100 flex items-center justify-center">
                                                        <span className="text-xs font-bold text-purple-600">
                                                            #{category.categoryId.slice(-6)}
                                                        </span>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div>
                                                    <div className="font-medium text-gray-900 text-sm">
                                                        {category.categoryName}
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex justify-center">
                                                    <Badge
                                                        variant={category.status === 1 ? 'default' : 'secondary'}
                                                        className={`px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap ${
                                                            category.status === 1
                                                                ? 'bg-green-700 border-green-700'
                                                                : 'bg-red-700 border-red-700'
                                                        }`}
                                                    >
                                                        {category.status === 1 ? 'Hoạt động' : 'Tạm ngưng'}
                                                    </Badge>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center gap-1">
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            // View category details - có thể thêm modal view sau
                                                            console.log('View category:', category);
                                                        }}
                                                        className="h-7 w-7 p-0"
                                                        title="Xem chi tiết"
                                                    >
                                                        <Eye className="w-3 h-3" />
                                                    </Button>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => handleEditCategory(category)}
                                                        className="h-7 w-7 p-0 text-blue-600 hover:text-blue-700"
                                                        title="Sửa thể loại"
                                                    >
                                                        <Edit className="w-3 h-3" />
                                                    </Button>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => handleDeleteCategory(category)}
                                                        className="text-red-600 hover:text-red-700 h-7 w-7 p-0"
                                                        title="Xóa thể loại"
                                                    >
                                                        <Trash2 className="w-3 h-3" />
                                                    </Button>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        )}
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center justify-between p-4 border-t">
                            <div className="text-sm text-gray-700">
                                Trang {currentPage + 1} / {totalPages} - Tổng {totalElements} thể loại
                            </div>
                            <div className="flex items-center gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 0}
                                >
                                    <ChevronLeft className="w-4 h-4" />
                                    Trước
                                </Button>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage >= totalPages - 1}
                                >
                                    Sau
                                    <ChevronRight className="w-4 h-4" />
                                </Button>
                            </div>
                        </div>
                    )}

                    {/* Empty State */}
                    {!loading && categories.length === 0 && (
                        <div className="text-center py-12 px-6">
                            <div className="w-16 h-16 bg-gray-100 flex items-center justify-center mx-auto mb-3">
                                <Tags className="w-8 h-8 text-gray-400" />
                            </div>
                            <h3 className="text-base font-semibold text-gray-700 mb-2">Không tìm thấy thể loại nào</h3>
                            <p className="text-gray-500 mb-3 text-sm">
                                Vui lòng thử lại với từ khóa khác hoặc thêm thể loại mới
                            </p>
                            <Button
                                onClick={() => setIsCreateModalOpen(true)}
                                className="bg-purple-600 hover:bg-purple-700 text-white text-sm"
                            >
                                <Plus className="w-4 h-4 mr-2" />
                                Thêm Thể Loại Mới
                            </Button>
                        </div>
                    )}
                </div>

                {/* Create Category Modal */}
                <AddCategoryModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    onSuccess={loadCategories}
                />

                {/* Edit Category Modal */}
                <EditCategoryModal
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    onSuccess={loadCategories}
                    category={selectedCategory}
                />

                {/* Delete Category Modal */}
                <DeleteCategoryModal
                    isOpen={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onSuccess={loadCategories}
                    category={selectedCategory}
                />
            </div>
        </div>
    );
};

export default AdminCategory;
