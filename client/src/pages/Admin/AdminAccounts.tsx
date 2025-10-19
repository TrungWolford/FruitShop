import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { accountService, roleService } from '../../services/adminAccountService';
import type { Account, Role, PaginatedResponse } from '../../types/account';
import LeftTaskbar from '../../components/LeftTaskbar';
import AddAccountModal from '../../components/modals/AddAccountModal';
import EditAccountModal from '../../components/modals/EditAccountModal';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Label } from '../../components/ui/label';
import {
    Users,
    Search,
    Plus,
    Eye,
    Trash2,
    ChevronLeft,
    ChevronRight,
    Loader2,
    AlertTriangle,
    Edit,
} from 'lucide-react';
import { toast } from 'sonner';

const AdminAccounts: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);

    // State for accounts data
    const [accounts, setAccounts] = useState<Account[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);
    const [loading, setLoading] = useState(false);

    // State for search and filters
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [roleFilter, setRoleFilter] = useState<string>('all');

    // State for modals
    const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    // State for roles
    const [roles, setRoles] = useState<Role[]>([]);

    useEffect(() => {
        document.title = 'BookCity - Quản lý tài khoản';

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

        loadRoles();
        loadAccounts();
    }, [currentPage, statusFilter, roleFilter, isAuthenticated, user, navigate]);

    useEffect(() => {
        if (searchTerm.trim()) {
            handleSearch();
        } else {
            loadAccounts();
        }
    }, [searchTerm]);

    const loadRoles = async () => {
        try {
            const rolesData = await roleService.getAllRoles();
            setRoles(rolesData);
        } catch (error) {
            console.error('Error loading roles:', error);
            toast.error('Không thể tải danh sách vai trò');
        }
    };

    const loadAccounts = async () => {
        setLoading(true);
        try {
            let response: PaginatedResponse<Account>;

            if (statusFilter === 'all' && roleFilter === 'all') {
                response = await accountService.getAllAccounts(currentPage, pageSize);
            } else if (statusFilter !== 'all') {
                const status = statusFilter === 'active' ? 1 : 0;
                response = await accountService.getAccountsByStatus(status, currentPage, pageSize);
            } else {
                response = await accountService.getAllAccounts(currentPage, pageSize);
            }

            setAccounts(response.content);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error loading accounts:', error);
            toast.error('Không thể tải danh sách tài khoản');
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) return;

        setLoading(true);
        try {
            const response = await accountService.searchAccountsByName(searchTerm, currentPage, pageSize);
            setAccounts(response.content);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error searching accounts:', error);
            toast.error('Không thể tìm kiếm tài khoản');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (!selectedAccount) return;

        try {
            setLoading(true);
            await accountService.deleteAccount(selectedAccount.accountId);
            toast.success('Xóa tài khoản thành công!');
            setIsDeleteModalOpen(false);
            setSelectedAccount(null);
            loadAccounts();
        } catch (error) {
            console.error('Error deleting account:', error);
            toast.error('Không thể xóa tài khoản');
        } finally {
            setLoading(false);
        }
    };

    const handleEditAccount = (account: Account) => {
        setSelectedAccount(account);
        setIsEditModalOpen(true);
    };

    const handlePageChange = (newPage: number) => {
        setCurrentPage(newPage);
    };

    const getRoleName = (roleId: string) => {
        const role = roles.find((r) => r.roleId === roleId);
        return role?.roleName || 'Unknown';
    };

    const getRoleBadge = (roleId: string) => {
        const roleName = getRoleName(roleId);

        if (roleName.includes('ADMIN')) {
            return (
                <Badge
                    variant="default"
                    className="px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap bg-purple-700 border-purple-700"
                >
                    Admin
                </Badge>
            );
        } else if (roleName.includes('CUSTOMER')) {
            return (
                <Badge
                    variant="default"
                    className="px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap bg-blue-700 border-blue-700"
                >
                    Khách hàng
                </Badge>
            );
        }

        return (
            <Badge
                variant="secondary"
                className="px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap bg-gray-700 border-gray-700"
            >
                {roleName}
            </Badge>
        );
    };

    const getStatusBadge = (status: number) => {
        if (status === 1) {
            return (
                <Badge
                    variant="default"
                    className="px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap bg-green-700 border-green-700"
                >
                    Hoạt động
                </Badge>
            );
        } else {
            return (
                <Badge
                    variant="secondary"
                    className="px-1.5 py-0.5 text-xs font-medium text-white whitespace-nowrap bg-red-700 border-red-700"
                >
                    Tạm ngưng
                </Badge>
            );
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Users className="w-5 h-5 text-blue-500" />
                        Quản Lý Tài Khoản
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">
                        Quản lý tài khoản người dùng và phân quyền hệ thống
                    </p>
                </div>

                {/* Search and Actions Bar */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm tài khoản..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-8 py-2 text-lg border border-gray-300 bg-gray-50 text-gray-900 placeholder-gray-500 focus:border-blue-500 focus:bg-white transition-all duration-200 rounded-md"
                                />
                            </div>
                            <Select value={statusFilter} onValueChange={setStatusFilter}>
                                <SelectTrigger className="w-40 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md">
                                    <SelectValue placeholder="Trạng thái" />
                                </SelectTrigger>
                                <SelectContent className="bg-white border-gray-200 shadow-lg">
                                    <SelectItem value="all" className="text-gray-900 hover:bg-gray-100">
                                        Tất cả
                                    </SelectItem>
                                    <SelectItem value="active" className="text-gray-900 hover:bg-gray-100">
                                        Hoạt động
                                    </SelectItem>
                                    <SelectItem value="inactive" className="text-gray-900 hover:bg-gray-100">
                                        Tạm ngưng
                                    </SelectItem>
                                </SelectContent>
                            </Select>

                            <Select value={roleFilter} onValueChange={setRoleFilter}>
                                <SelectTrigger className="w-40 border border-gray-300 bg-gray-50 text-gray-900 hover:bg-gray-100 rounded-md">
                                    <SelectValue placeholder="Vai trò" />
                                </SelectTrigger>
                                <SelectContent className="bg-white border-gray-200 shadow-lg">
                                    <SelectItem value="all" className="text-gray-900 hover:bg-gray-100">
                                        Tất cả
                                    </SelectItem>
                                    {roles.map((role) => (
                                        <SelectItem
                                            key={role.roleId}
                                            value={role.roleId}
                                            className="text-gray-900 hover:bg-gray-100"
                                        >
                                            {role.roleName}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <Button
                            onClick={() => setIsCreateModalOpen(true)}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-200 text-sm rounded-md"
                        >
                            <Plus className="w-3 h-3" />
                            Thêm Tài Khoản
                        </Button>
                    </div>
                </div>

                {/* Table Container */}
                <div className="bg-white shadow-sm border border-gray-200 overflow-hidden">
                    <div className="overflow-x-auto">
                        {loading ? (
                            <div className="flex items-center justify-center py-8">
                                <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
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
                                            Tên tài khoản
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-left text-sm w-32">
                                            Số điện thoại
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-24">
                                            Vai trò
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-20">
                                            Trạng thái
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-32">
                                            Thao tác
                                        </TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {accounts.map((account, index) => (
                                        <TableRow
                                            key={account.accountId}
                                            className={`hover:bg-gray-50 transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            }`}
                                        >
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center">
                                                    <div className="w-6 h-6 bg-blue-100 flex items-center justify-center">
                                                        <span className="text-xs font-bold text-blue-600">
                                                            #{account.accountId.slice(-6)}
                                                        </span>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="font-medium text-gray-900 text-sm">
                                                    {account.accountName}
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2 text-sm text-gray-600">
                                                {account.accountPhone}
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex justify-center">
                                                    {getRoleBadge(account.roles[0]?.roleId || '')}
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex justify-center">
                                                    {getStatusBadge(account.status)}
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center gap-1">
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            setSelectedAccount(account);
                                                            setIsViewModalOpen(true);
                                                        }}
                                                        className="h-7 w-7 p-0"
                                                        title="Xem chi tiết"
                                                    >
                                                        <Eye className="w-3 h-3" />
                                                    </Button>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => handleEditAccount(account)}
                                                        className="h-7 w-7 p-0 text-blue-600 hover:text-blue-700"
                                                        title="Sửa tài khoản"
                                                    >
                                                        <Edit className="w-3 h-3" />
                                                    </Button>

                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            setSelectedAccount(account);
                                                            setIsDeleteModalOpen(true);
                                                        }}
                                                        className="text-red-600 hover:text-red-700 h-7 w-7 p-0"
                                                        title="Xóa tài khoản"
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

                        {/* Pagination */}
                        {totalPages > 1 && (
                            <div className="flex items-center justify-between p-4 border-t">
                                <div className="text-sm text-gray-700">
                                    Trang {currentPage + 1} / {totalPages} - Tổng {totalElements} tài khoản
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
                        {!loading && accounts.length === 0 && (
                            <div className="text-center py-12 px-6">
                                <div className="w-16 h-16 bg-gray-100 flex items-center justify-center mx-auto mb-3">
                                    <Users className="w-8 h-8 text-gray-400" />
                                </div>
                                <h3 className="text-base font-semibold text-gray-700 mb-2">
                                    Không tìm thấy tài khoản nào
                                </h3>
                                <p className="text-gray-500 mb-3 text-sm">
                                    Vui lòng thử lại với từ khóa khác hoặc thêm tài khoản mới
                                </p>
                                <Button
                                    onClick={() => setIsCreateModalOpen(true)}
                                    className="bg-blue-600 hover:bg-blue-700 text-white text-sm"
                                >
                                    <Plus className="w-4 h-4 mr-2" />
                                    Thêm Tài Khoản Mới
                                </Button>
                            </div>
                        )}
                    </div>
                </div>

                {/* Create Account Modal */}
                <AddAccountModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    onSuccess={loadAccounts}
                    roles={roles}
                />

                {/* Edit Account Modal */}
                <EditAccountModal
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    onSuccess={loadAccounts}
                    account={selectedAccount}
                    roles={roles}
                />

                {/* View Account Modal */}
                <Dialog open={isViewModalOpen} onOpenChange={setIsViewModalOpen}>
                    <DialogContent className="max-w-2xl">
                        <DialogHeader>
                            <DialogTitle>Chi tiết tài khoản</DialogTitle>
                            <DialogDescription>Thông tin chi tiết về tài khoản</DialogDescription>
                        </DialogHeader>

                        {selectedAccount && (
                            <div className="grid gap-4 py-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <Label className="text-sm font-medium text-gray-700">ID tài khoản</Label>
                                        <p className="text-sm font-mono bg-gray-100 p-2 rounded">
                                            {selectedAccount.accountId}
                                        </p>
                                    </div>
                                    <div>
                                        <Label className="text-sm font-medium text-gray-700">Trạng thái</Label>
                                        <div className="mt-1">{getStatusBadge(selectedAccount.status)}</div>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <Label className="text-sm font-medium text-gray-700">Tên tài khoản</Label>
                                        <p className="text-sm bg-gray-100 p-2 rounded">{selectedAccount.accountName}</p>
                                    </div>
                                    <div>
                                        <Label className="text-sm font-medium text-gray-700">Số điện thoại</Label>
                                        <p className="text-sm bg-gray-100 p-2 rounded">
                                            {selectedAccount.accountPhone}
                                        </p>
                                    </div>
                                </div>

                                <div>
                                    <Label className="text-sm font-medium text-gray-700">Vai trò</Label>
                                    <div className="mt-1">
                                        {selectedAccount.roles && selectedAccount.roles.length > 0 ? (
                                            getRoleBadge(selectedAccount.roles[0].roleId)
                                        ) : (
                                            <Badge variant="secondary">Chưa có vai trò</Badge>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}

                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsViewModalOpen(false)}>
                                Đóng
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                {/* Delete Confirmation Modal */}
                <Dialog open={isDeleteModalOpen} onOpenChange={setIsDeleteModalOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle className="flex items-center gap-2 text-red-600">
                                <AlertTriangle className="w-5 h-5" />
                                Xác nhận xóa tài khoản
                            </DialogTitle>
                            <DialogDescription>
                                Bạn có chắc chắn muốn xóa tài khoản "{selectedAccount?.accountName}"? Hành động này
                                không thể hoàn tác.
                            </DialogDescription>
                        </DialogHeader>

                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsDeleteModalOpen(false)} disabled={loading}>
                                Hủy
                            </Button>
                            <Button variant="destructive" onClick={handleDeleteAccount} disabled={loading}>
                                {loading ? (
                                    <>
                                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                        Đang xóa...
                                    </>
                                ) : (
                                    <>
                                        <Trash2 className="w-4 h-4 mr-2" />
                                        Xóa tài khoản
                                    </>
                                )}
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>
        </div>
    );
};

export default AdminAccounts;
