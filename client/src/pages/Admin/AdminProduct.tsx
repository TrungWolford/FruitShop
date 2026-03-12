import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { toast } from 'sonner';
// Remove mock data imports - using productService instead
import LeftTaskbar from '../../components/Admin/LeftTaskbar/LeftTaskbar';
import AddProductModal from '../../components/modals/AddProductModal';
import EditProductModal from '../../components/modals/EditProductModal';
import ViewProductModal from '../../components/modals/ViewProductModal';
import { productService } from '../../services/productService';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Skeleton } from '../../components/ui/skeleton';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../../components/ui/dropdowns/dropdown-menu';
import { Search, Package, ChevronLeft, ChevronRight, ChevronDown, BookOpen } from 'lucide-react';
import type { Product } from '../../types/product';

const AdminProduct: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, isInitialized } = useAppSelector((state) => state.adminAuth);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [isAddProductModalOpen, setIsAddProductModalOpen] = useState(false);
  const [isEditProductModalOpen, setIsEditProductModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('all');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const itemsPerPage = 10;

  // Load products from backend
  const loadProducts = async (page: number = 0) => {
    try {
      setLoading(true);
      let response;

      // Use search if searchTerm is provided
      if (searchTerm.trim()) {
        response = await productService.searchProducts(searchTerm.trim(), page, itemsPerPage);
      } else {
        response = await productService.getAllProducts(page, itemsPerPage);
      }

      // Giả sử response có cấu trúc { content: Product[], totalPages: number, totalElements: number }
      if (response.content) {
        let filteredProducts = response.content;

        // Apply status filter locally if needed
        if (statusFilter !== 'all') {
          const status = statusFilter === 'active' ? 1 : 0;
          filteredProducts = filteredProducts.filter((product: any) => product.status === status);
        }

        setProducts(filteredProducts);
        setTotalPages(response.totalPages || 1);
        setTotalItems(response.totalElements || 0);
      } else {
        let filteredProducts = response;

        // Apply status filter locally if needed
        if (statusFilter !== 'all') {
          const status = statusFilter === 'active' ? 1 : 0;
          filteredProducts = filteredProducts.filter((product: any) => product.status === status);
        }

        // Fallback nếu response không có pagination
        setProducts(filteredProducts);
        setTotalPages(1);
        setTotalItems(filteredProducts.length || 0);
      }
    } catch (error) {
      toast.error('Không thể tải danh sách sản phẩm từ server');
      setProducts([]);
      setTotalPages(1);
      setTotalItems(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    document.title = 'Vựa trái cây - Quản lý sản phẩm';

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
    const isAdmin = userRoles.some((role) => role.roleName === 'ADMIN');

    if (!isAdmin) {
      navigate('/admin');
      return;
    }

    // Load products on component mount
    loadProducts(currentPage - 1); // Convert to 0-based index
  }, [isInitialized, isAuthenticated, user, navigate]);

  useEffect(() => {
    // Debounce search to avoid too many API calls
    const timeoutId = setTimeout(() => {
      loadProducts(0); // Reset to first page when searching/filtering
      setCurrentPage(1);
    }, 500); // 500ms delay

    return () => clearTimeout(timeoutId);
  }, [searchTerm, statusFilter]);

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1); // Reset to first page when searching
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value);
    setCurrentPage(1); // Reset to first page when filtering
  };

  const getStatusFilterText = () => {
    switch (statusFilter) {
      case 'active':
        return 'Đang hoạt động';
      case 'inactive':
        return 'Ngừng hoạt động';
      default:
        return 'Tất cả';
    }
  };

  const handleEditProduct = (product: Product) => {
    setEditingProduct(product);
    setIsEditProductModalOpen(true);
  };

  const handleAddProductSuccess = () => {
    // Refresh the products list from backend
    loadProducts(currentPage - 1);
  };

  const handleEditProductSuccess = () => {
    // Show loading effect for 0.5 seconds before reloading
    setLoading(true);

    setTimeout(() => {
      // Refresh the products list from backend
      loadProducts(currentPage - 1);
    }, 500); // 0.5 seconds delay
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  // Backend returns dates formatted as 'dd/MM/yyyy HH:mm:ss' (see server application.properties)
  const parseServerDate = (value?: string | Date | number): Date | null => {
    if (!value && value !== 0) return null;

    if (value instanceof Date) return value;
    if (typeof value === 'number') return new Date(value);

    // Try native parse first (covers ISO strings)
    const isoAttempt = new Date(value as string);
    if (!Number.isNaN(isoAttempt.getTime())) return isoAttempt;

    // Try parsing 'dd/MM/yyyy HH:mm:ss' or 'dd/MM/yyyy' formats
    const str = (value as string).trim();
    const dateTimeParts = str.split(' ');
    const dateParts = dateTimeParts[0].split('/');
    if (dateParts.length === 3) {
      const day = parseInt(dateParts[0], 10);
      const month = parseInt(dateParts[1], 10) - 1; // JS months 0-based
      const year = parseInt(dateParts[2], 10);

      let hours = 0,
        minutes = 0,
        seconds = 0;

      if (dateTimeParts[1]) {
        const timeParts = dateTimeParts[1].split(':');
        hours = parseInt(timeParts[0] || '0', 10);
        minutes = parseInt(timeParts[1] || '0', 10);
        seconds = parseInt(timeParts[2] || '0', 10);
      }

      const constructed = new Date(year, month, day, hours, minutes, seconds);
      if (!Number.isNaN(constructed.getTime())) return constructed;
    }

    return null;
  };

  const formatDate = (date?: string | Date | number) => {
    const d = parseServerDate(date);
    if (!d) return '—';
    return new Intl.DateTimeFormat('vi-VN').format(d);
  };

  const truncate = (text: string | undefined, max = 30) => {
    if (!text) return '';
    return text.length > max ? `${text.slice(0, max)}...` : text;
  };

  // Function để lấy URL hình ảnh từ public/products
  const getImageUrl = (imageUrl: string) => {
    if (!imageUrl) {
      return '';
    }

    // Nếu imageUrl đã có http/https thì giữ nguyên
    if (imageUrl.startsWith('http')) {
      return imageUrl;
    }

    // Nếu không có / ở đầu thì thêm vào
    const cleanUrl = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`;
    return `/products${cleanUrl}`;
  };

  // Pagination calculations - products are already paginated from backend
  const currentProducts = products;

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    loadProducts(page - 1); // Convert to 0-based index
  };

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      loadProducts(newPage - 1); // Convert to 0-based index
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      loadProducts(newPage - 1); // Convert to 0-based index
    }
  };

  // Bỏ check authentication để truy cập trực tiếp admin
  // if (!isAuthenticated || !user || user.role !== 'ADMIN') {
  //   return null // Will redirect
  // }

  return (
    <div className="min-h-screen bg-gray-50">
      <LeftTaskbar />

      <div className="ml-64 p-4">
        {/* Header */}
        <div className="mb-3">
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <Package className="w-5 h-5 text-amber-500" />
            Quản lý sản phẩm
          </h1>
          <p className="text-gray-600 mt-0.5 text-base">Quản lý danh sách sách trong hệ thống</p>
        </div>

        {/* Search and Actions */}
        <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
          <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
            <div className="flex gap-2 flex-1">
              <div className="relative max-w-xs">
                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <Input
                  placeholder="Tìm kiếm sản phẩm..."
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
                    Ngừng hoạt động
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            <div className="flex gap-2">
              <Button
                onClick={() => setIsAddProductModalOpen(true)}
                className="bg-amber-600 hover:bg-amber-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-300 ease-in-out text-sm rounded-md"
              >
                <Package className="w-3 h-3" />
                Thêm sản phẩm mới
              </Button>
              <Button
                onClick={() => {
                  if (selectedProduct) {
                    handleEditProduct(selectedProduct);
                  } else {
                    toast.error('Vui lòng chọn một sản phẩm để sửa');
                  }
                }}
                disabled={!selectedProduct}
                className="bg-blue-600 hover:bg-blue-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-300 ease-in-out text-sm rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <BookOpen className="w-3 h-3" />
                Sửa sản phẩm
              </Button>
              <Button
                onClick={() => {
                  if (selectedProduct) {
                    setIsViewDialogOpen(true);
                  } else {
                    toast.error('Vui lòng chọn một sản phẩm để xem chi tiết');
                  }
                }}
                disabled={!selectedProduct}
                className="bg-green-600 hover:bg-green-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-300 ease-in-out text-sm rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Search className="w-3 h-3" />
                Xem chi tiết
              </Button>
            </div>
          </div>
        </div>

        {/* Products Table */}
        <div className="bg-white shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <Table className="w-full">
              <TableHeader>
                <TableRow className="bg-slate-800 hover:bg-slate-800 border-b border-slate-700">
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-24">
                    ID
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-20">
                    Hình ảnh
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-48">
                    Tên sản phẩm
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-left text-sm w-48">
                    Mô tả
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                    Giá
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-20">
                    Tồn kho
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-24">
                    Trạng thái
                  </TableHead>
                  <TableHead className="font-semibold text-white px-4 py-3 text-center text-sm w-28">
                    Ngày tạo
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading
                  ? // Show skeleton loading for each row
                  Array.from({ length: 10 }).map((_, index) => (
                    <TableRow key={`skeleton-${index}`}>
                      <TableCell className="px-4 py-3">
                        <div className="flex justify-center">
                          <Skeleton className="h-6 w-16 rounded" />
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-16 w-16 rounded" />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="space-y-2">
                          <Skeleton className="h-4 w-40" />
                          <Skeleton className="h-3 w-24" />
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-4 w-28" />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-4 w-16" />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-6 w-20 rounded-full" />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                    </TableRow>
                  ))
                  : currentProducts.map((product, index) => (
                    <TableRow
                      key={product.productId}
                      className={`transition-all duration-300 ease-in-out cursor-pointer ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                        } ${selectedProduct?.productId === product.productId
                          ? '!bg-blue-100 !border-l-4 !border-blue-600 shadow-md hover:!bg-blue-100'
                          : '!border-l-4 !border-transparent hover:!bg-transparent'
                        }`}
                      onClick={() => {
                        if (selectedProduct?.productId === product.productId) {
                          // Click lần 2: bỏ chọn
                          setSelectedProduct(null);
                        } else {
                          // Click lần 1 hoặc chọn sản phẩm khác
                          setSelectedProduct(product);
                        }
                      }}
                    >
                      <TableCell className="px-4 py-3 select-none">
                        <div className="flex justify-center">
                          <div className="px-2 py-1 bg-amber-100 rounded flex items-center justify-center">
                            <span className="text-xs font-bold text-amber-600">
                              #{product.productId}
                            </span>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div className="flex items-center">
                          {(() => {
                            // Sử dụng field images từ backend
                            const imagesArray = product.images || [];

                            if (imagesArray && imagesArray.length > 0) {
                              const firstImage = imagesArray[0];
                              const imageUrl = firstImage.imageUrl;

                              if (imageUrl) {
                                return (
                                  <img
                                    src={getImageUrl(imageUrl)}
                                    alt={product.productName}
                                    className="w-16 h-16 object-cover border border-gray-200"
                                    onError={(e) => {
                                      // Ẩn ảnh lỗi và hiển thị placeholder
                                      e.currentTarget.style.display = 'none';
                                      const placeholder = e.currentTarget.nextElementSibling as HTMLElement;
                                      if (placeholder) {
                                        placeholder.style.display = 'flex';
                                      }
                                    }}
                                  />
                                );
                              }
                            }

                            // Không có ảnh hoặc ảnh lỗi
                            return (
                              <div className="w-16 h-16 bg-gray-100 flex items-center justify-center border border-gray-200">
                                <Package className="w-6 h-6 text-gray-400" />
                              </div>
                            );
                          })()}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div>
                          <div className="font-medium text-gray-900 text-sm">
                            {product.productName}
                          </div>
                          {/* cover removed */}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div className="font-medium text-gray-700 text-sm">
                          {truncate(product.description, 30)}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div className="flex justify-center">
                          <span className="font-medium text-green-600 text-sm">
                            {formatPrice(product.price)}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div className="flex justify-center">
                          <span
                            className={`font-bold text-sm ${product.stock > 50
                              ? 'text-green-700'
                              : product.stock > 10
                                ? 'text-yellow-700'
                                : 'text-red-700'
                              }`}
                          >
                            {product.stock}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3 select-none">
                        <div className="flex justify-center">
                          <Badge
                            variant={product.status === 1 ? 'default' : 'secondary'}
                            className={`px-2 py-1 text-xs font-medium text-white ${product.status === 1
                              ? 'bg-green-700 border-green-700'
                              : 'bg-red-700 border-red-700'
                              }`}
                          >
                            {product.status === 1 ? 'Hoạt động' : 'Ngừng'}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="flex justify-center">
                          <span className="text-gray-600 text-xs">
                            {formatDate(product.createdAt)}
                          </span>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </div>

          {/* Empty State */}
          {!loading && currentProducts.length === 0 && (
            <div className="text-center py-12 px-6">
              <div className="w-16 h-16 bg-gray-100 flex items-center justify-center mx-auto mb-3">
                <Package className="w-8 h-8 text-gray-400" />
              </div>
              <h3 className="text-base font-semibold text-gray-700 mb-2">Không tìm thấy sản phẩm nào</h3>
              <p className="text-gray-500 mb-3 text-sm">
                Vui lòng thử lại với từ khóa khác hoặc thêm sản phẩm mới
              </p>
              <Button
                onClick={() => setIsAddProductModalOpen(true)}
                className="bg-amber-600 hover:bg-amber-700 text-white text-sm"
              >
                <Package className="w-4 h-4 mr-2" />
                Thêm Sản Phẩm Mới
              </Button>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="px-4 py-3 border-t border-slate-700 bg-slate-800">
              <div className="flex items-center justify-between">
                <div className="text-sm text-white">
                  Hiển thị {(currentPage - 1) * itemsPerPage + 1} đến{' '}
                  {Math.min(currentPage * itemsPerPage, totalItems)} trong tổng số {totalItems} sản
                  phẩm
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
                        className={`px-3 py-1 text-sm ${currentPage === page
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

        {/* View Product Modal */}
        <ViewProductModal
          isOpen={isViewDialogOpen}
          onClose={() => setIsViewDialogOpen(false)}
          product={selectedProduct}
        />

        {/* Add Product Modal */}
        <AddProductModal
          isOpen={isAddProductModalOpen}
          onClose={() => setIsAddProductModalOpen(false)}
          onSuccess={handleAddProductSuccess}
        />

        {/* Edit Product Modal */}
        <EditProductModal
          isOpen={isEditProductModalOpen}
          onClose={() => setIsEditProductModalOpen(false)}
          onSuccess={handleEditProductSuccess}
          product={editingProduct}
        />
      </div>
    </div>
  );
};

export default AdminProduct;
