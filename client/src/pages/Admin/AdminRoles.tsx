import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import { roleService } from '../../services/adminAccountService';
import type { Role } from '../../types/account';
import LeftTaskbar from '../../components/LeftTaskbar';
import AddRoleModal from '../../components/modals/AddRoleModal';
import EditRoleModal from '../../components/modals/EditRoleModal';
import DeleteRoleModal from '../../components/modals/DeleteRoleModal';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';

import { Shield, Search, Plus, Eye, Edit, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const AdminRoles: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);

    // State for roles data
    const [roles, setRoles] = useState<Role[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    // State for modals
    const [selectedRole, setSelectedRole] = useState<Role | null>(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    useEffect(() => {
        document.title = 'BookCity - Quản lý vai trò';

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
    }, [isAuthenticated, user, navigate]);

    const loadRoles = async () => {
        setLoading(true);
        try {
            const rolesData = await roleService.getAllRoles();
            setRoles(rolesData);
        } catch (error) {
            console.error('Error loading roles:', error);
            toast.error('Không thể tải danh sách vai trò');
        } finally {
            setLoading(false);
        }
    };

    const filteredRoles = roles.filter((role) => role.roleName.toLowerCase().includes(searchTerm.toLowerCase()));

    const getRoleBadge = (roleName: string) => {
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

    return (
        <div className="min-h-screen bg-gray-50">
            <LeftTaskbar />

            <div className="ml-64 p-4">
                {/* Header */}
                <div className="mb-3">
                    <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                        <Shield className="w-5 h-5 text-purple-500" />
                        Quản Lý Vai Trò
                    </h1>
                    <p className="text-gray-600 mt-0.5 text-base">Quản lý các vai trò và quyền hạn trong hệ thống</p>
                </div>

                {/* Search and Actions Bar */}
                <div className="bg-slate-800 shadow-sm border border-slate-700 p-3 mb-3 rounded-lg">
                    <div className="flex flex-col lg:flex-row gap-2 items-center justify-between">
                        <div className="flex gap-2 flex-1">
                            <div className="relative max-w-xs">
                                <Search className="absolute left-2.5 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="Tìm kiếm vai trò..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-8 py-2 text-lg border border-gray-300 bg-gray-50 text-gray-900 placeholder-gray-500 focus:border-purple-500 focus:bg-white transition-all duration-200 rounded-md"
                                />
                            </div>
                        </div>
                        <Button
                            onClick={() => setIsCreateModalOpen(true)}
                            className="bg-purple-600 hover:bg-purple-700 text-white px-2.5 py-1 flex items-center gap-1 transition-all duration-200 text-sm rounded-md"
                        >
                            <Plus className="w-3 h-3" />
                            Thêm Vai Trò
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
                                            Tên Vai Trò
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-24">
                                            Hiển Thị
                                        </TableHead>
                                        <TableHead className="font-semibold text-white px-2 py-2 text-center text-sm w-32">
                                            Thao Tác
                                        </TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {filteredRoles.map((role, index) => (
                                        <TableRow
                                            key={role.roleId}
                                            className={`hover:bg-gray-50 transition-all duration-200 cursor-pointer ${
                                                index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                                            }`}
                                        >
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center">
                                                    <div className="w-6 h-6 bg-purple-100 flex items-center justify-center">
                                                        <span className="text-xs font-bold text-purple-600">
                                                            #{role.roleId.slice(-6)}
                                                        </span>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="font-medium text-gray-900 text-sm">{role.roleName}</div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex justify-center">{getRoleBadge(role.roleName)}</div>
                                            </TableCell>
                                            <TableCell className="px-2 py-2">
                                                <div className="flex items-center justify-center gap-1">
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            // View role details - có thể thêm modal view sau
                                                            console.log('View role:', role);
                                                        }}
                                                        className="h-7 w-7 p-0"
                                                        title="Xem chi tiết"
                                                    >
                                                        <Eye className="w-3 h-3" />
                                                    </Button>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            setSelectedRole(role);
                                                            setIsEditModalOpen(true);
                                                        }}
                                                        className="h-7 w-7 p-0 text-blue-600 hover:text-blue-700"
                                                        title="Sửa vai trò"
                                                    >
                                                        <Edit className="w-3 h-3" />
                                                    </Button>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            setSelectedRole(role);
                                                            setIsDeleteModalOpen(true);
                                                        }}
                                                        className="text-red-600 hover:text-red-700 h-7 w-7 p-0"
                                                        title="Xóa vai trò"
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

                        {/* Empty State */}
                        {!loading && filteredRoles.length === 0 && (
                            <div className="text-center py-12 px-6">
                                <div className="w-16 h-16 bg-gray-100 flex items-center justify-center mx-auto mb-3">
                                    <Shield className="w-8 h-8 text-gray-400" />
                                </div>
                                <h3 className="text-base font-semibold text-gray-700 mb-2">
                                    {searchTerm ? 'Không tìm thấy vai trò nào' : 'Chưa có vai trò nào'}
                                </h3>
                                <p className="text-gray-500 mb-3 text-sm">
                                    {searchTerm
                                        ? 'Vui lòng thử lại với từ khóa khác'
                                        : 'Thêm vai trò đầu tiên cho hệ thống'}
                                </p>
                                {!searchTerm && (
                                    <Button
                                        onClick={() => setIsCreateModalOpen(true)}
                                        className="bg-purple-600 hover:bg-purple-700 text-white text-sm"
                                    >
                                        <Plus className="w-4 h-4 mr-2" />
                                        Thêm Vai Trò Mới
                                    </Button>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                {/* Create Role Modal */}
                <AddRoleModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    onSuccess={loadRoles}
                />

                {/* Edit Role Modal */}
                <EditRoleModal
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    onSuccess={loadRoles}
                    role={selectedRole}
                />

                {/* Delete Role Modal */}
                <DeleteRoleModal
                    isOpen={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onSuccess={loadRoles}
                    role={selectedRole}
                />
            </div>
        </div>
    );
};

export default AdminRoles;
