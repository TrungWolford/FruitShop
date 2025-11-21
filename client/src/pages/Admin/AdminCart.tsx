import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { toast } from 'sonner';
import LeftTaskbar from '../../components/Admin/LeftTaskbar';
import { cartService } from '../../services/cartService';
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
import { Search, ShoppingCart, ChevronLeft, ChevronRight, ChevronDown, Ban, CheckCircle } from 'lucide-react';
import type { Cart } from '../../types/cart';

interface CartWithAccount extends Cart {
    accountName?: string;
    accountPhone?: string;
}

const AdminCart: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);
    const [carts, setCarts] = useState<CartWithAccount[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;

    useEffect(() => {
        document.title = 'BookCity - Quản lý giỏ hàng';
        
        // Check if user is authenticated and has ADMIN role
        if (!isAuthenticated || !user) {
            navigate('/');
            return;
        }
        
        const userRoles = user.roles || [];
        const isAdmin = userRoles.some(role => role.roleName === 'ADMIN');
        
        if (!isAdmin) {
            navigate('/');
            return;
        }

        loadCartsData();
    }, [isAuthenticated, user, navigate]);

    const loadCartsData = async () => {
        try {
            setLoading(true);
            
            // Use the new getAllCarts endpoint for better performance
            const cartsResponse = await cartService.getAllCarts(0, 1000);
            
            if (cartsResponse.success && cartsResponse.data) {
                const cartsData = cartsResponse.data;
                
                // Handle paginated response
                let cartsList: Cart[] = [];
                
                // Check if it's a paginated response with content property
                if (typeof cartsData === 'object' && cartsData !== null && 'content' in cartsData) {
                    const paginatedData = cartsData as any;
                    cartsList = Array.isArray(paginatedData.content) ? paginatedData.content : [];
                } 
                // Check if it's an array of carts
                else if (Array.isArray(cartsData) && cartsData.length > 0 && 'cartId' in cartsData[0]) {
                    cartsList = cartsData as unknown as Cart[];
                }
                // Check if it's a single cart
                else if (typeof cartsData === 'object' && 'cartId' in cartsData) {
                    cartsList = [cartsData as Cart];
                }
                
                // Load account details for each cart
                const cartsWithAccountInfo: CartWithAccount[] = cartsList.map((cart: Cart) => {
                    // Get account info from the cart's account object
                    if (cart.account) {
                        return {
                            ...cart,
                            accountName: cart.account.accountName || 'N/A',
                            accountPhone: cart.account.accountPhone || 'N/A'
                        };
                    }
                    return cart as CartWithAccount;
                });
                
                setCarts(cartsWithAccountInfo);
            } else {
                toast.error(cartsResponse.message || 'Lỗi khi tải danh sách giỏ hàng');
            }
            
        } catch (error) {
            console.error('Error loading carts:', error);
            toast.error('Lỗi khi tải danh sách giỏ hàng');
        } finally {
            setLoading(false);
        }
    };

    const handleDisableCart = async (cartId: string) => {
        try {
            const result = await cartService.disableCart(cartId);
            if (result.success) {
                toast.success('Vô hiệu hóa giỏ hàng thành công');
                loadCartsData(); // Reload data
            } else {
                toast.error(result.message || 'Lỗi khi vô hiệu hóa giỏ hàng');
            }
        } catch (error) {
            console.error('Error disabling cart:', error);
            toast.error('Lỗi khi vô hiệu hóa giỏ hàng');
        }
    };

    const handleEnableCart = async (cartId: string) => {
        try {
            const result = await cartService.enableCart(cartId);
            if (result.success) {
                toast.success('Kích hoạt giỏ hàng thành công');
                loadCartsData(); // Reload data
            } else {
                toast.error(result.message || 'Lỗi khi kích hoạt giỏ hàng');
            }
        } catch (error) {
            console.error('Error enabling cart:', error);
            toast.error('Lỗi khi kích hoạt giỏ hàng');
        }
    };

    // Filter carts
    const filteredCarts = carts.filter(cart => {
        const matchesSearch = !searchTerm || 
            cart.accountName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            cart.accountPhone?.includes(searchTerm) ||
            cart.cartId.toLowerCase().includes(searchTerm.toLowerCase());
        
        const matchesStatus = statusFilter === 'all' || 
            (statusFilter === 'active' && cart.status === 1) ||
            (statusFilter === 'disabled' && cart.status === 0);
        
        return matchesSearch && matchesStatus;
    });

    // Pagination
    const totalPages = Math.ceil(filteredCarts.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentCarts = filteredCarts.slice(startIndex, endIndex);

    const formatDate = (date: Date) => {
        return new Intl.DateTimeFormat('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).format(new Date(date));
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />
            
            <div className="ml-64 p-8">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-800 flex items-center gap-2">
                        <ShoppingCart className="w-8 h-8 text-amber-500" />
                        Quản lý giỏ hàng
                    </h1>
                    <p className="text-gray-600 mt-2">Quản lý và theo dõi giỏ hàng của khách hàng</p>
                </div>

                {/* Filter and Search */}
                <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                    <div className="flex flex-col md:flex-row gap-4">
                        {/* Search */}
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <Input
                                type="text"
                                placeholder="Tìm kiếm theo tên, số điện thoại, ID giỏ hàng..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="pl-10"
                            />
                        </div>

                        {/* Status Filter */}
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="outline" className="min-w-[200px] justify-between">
                                    <span>
                                        {statusFilter === 'all' && 'Tất cả trạng thái'}
                                        {statusFilter === 'active' && 'Đang hoạt động'}
                                        {statusFilter === 'disabled' && 'Đã vô hiệu hóa'}
                                    </span>
                                    <ChevronDown className="w-4 h-4 ml-2" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-[200px]">
                                <DropdownMenuItem onClick={() => setStatusFilter('all')}>
                                    Tất cả trạng thái
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('active')}>
                                    Đang hoạt động
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => setStatusFilter('disabled')}>
                                    Đã vô hiệu hóa
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>

                {/* Carts Table */}
                <div className="bg-white rounded-lg shadow-md overflow-hidden">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID Giỏ hàng</TableHead>
                                <TableHead>Khách hàng</TableHead>
                                <TableHead>Số điện thoại</TableHead>
                                <TableHead>Số sản phẩm</TableHead>
                                <TableHead>Tổng giá trị</TableHead>
                                <TableHead>Ngày tạo</TableHead>
                                <TableHead>Trạng thái</TableHead>
                                <TableHead className="text-right">Thao tác</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                Array.from({ length: 5 }).map((_, index) => (
                                    <TableRow key={index}>
                                        <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-40" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-16" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                                        <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                                        <TableCell><Skeleton className="h-8 w-24 ml-auto" /></TableCell>
                                    </TableRow>
                                ))
                            ) : currentCarts.length > 0 ? (
                                currentCarts.map((cart) => (
                                    <TableRow key={cart.cartId}>
                                        <TableCell className="font-mono text-sm">{cart.cartId.slice(0, 8)}...</TableCell>
                                        <TableCell className="font-medium">{cart.accountName || 'N/A'}</TableCell>
                                        <TableCell>{cart.accountPhone || 'N/A'}</TableCell>
                                        <TableCell>{cart.items?.length || 0}</TableCell>
                                        <TableCell className="font-semibold text-green-600">
                                            {formatPrice(cart.totalAmount || 0)}
                                        </TableCell>
                                        <TableCell className="text-sm text-gray-600">
                                            {cart.createdAt ? formatDate(cart.createdAt) : 'N/A'}
                                        </TableCell>
                                        <TableCell>
                                            {cart.status === 1 ? (
                                                <Badge variant="default" className="bg-green-500">
                                                    Hoạt động
                                                </Badge>
                                            ) : (
                                                <Badge variant="destructive">
                                                    Vô hiệu hóa
                                                </Badge>
                                            )}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {cart.status === 1 ? (
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={() => handleDisableCart(cart.cartId)}
                                                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                                                >
                                                    <Ban className="w-4 h-4 mr-1" />
                                                    Vô hiệu hóa
                                                </Button>
                                            ) : (
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={() => handleEnableCart(cart.cartId)}
                                                    className="text-green-600 hover:text-green-700 hover:bg-green-50"
                                                >
                                                    <CheckCircle className="w-4 h-4 mr-1" />
                                                    Kích hoạt
                                                </Button>
                                            )}
                                        </TableCell>
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={8} className="text-center py-12">
                                        <ShoppingCart className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                                        <p className="text-gray-500 text-lg">Không tìm thấy giỏ hàng nào</p>
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>

                {/* Pagination */}
                {!loading && filteredCarts.length > 0 && (
                    <div className="mt-6 flex items-center justify-between">
                        <p className="text-sm text-gray-600">
                            Hiển thị {startIndex + 1} - {Math.min(endIndex, filteredCarts.length)} trong tổng số {filteredCarts.length} giỏ hàng
                        </p>
                        
                        <div className="flex items-center gap-2">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                                disabled={currentPage === 1}
                            >
                                <ChevronLeft className="w-4 h-4" />
                                Trước
                            </Button>
                            
                            <div className="flex items-center gap-1">
                                {Array.from({ length: totalPages }, (_, i) => i + 1)
                                    .filter(page => {
                                        return page === 1 || 
                                               page === totalPages || 
                                               Math.abs(page - currentPage) <= 1;
                                    })
                                    .map((page, index, array) => (
                                        <React.Fragment key={page}>
                                            {index > 0 && array[index - 1] !== page - 1 && (
                                                <span className="px-2">...</span>
                                            )}
                                            <Button
                                                variant={currentPage === page ? "default" : "outline"}
                                                size="sm"
                                                onClick={() => setCurrentPage(page)}
                                                className="min-w-[40px]"
                                            >
                                                {page}
                                            </Button>
                                        </React.Fragment>
                                    ))}
                            </div>
                            
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                                disabled={currentPage === totalPages}
                            >
                                Sau
                                <ChevronRight className="w-4 h-4" />
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminCart;
