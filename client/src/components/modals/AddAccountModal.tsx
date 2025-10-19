import React, { useState } from 'react';
import { accountService } from '../../services/adminAccountService';
import type { CreateAccountRequest, Role } from '../../types/account';
import { Button } from '../ui/Button/Button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Plus, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface AddAccountModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    roles: Role[];
}

const AddAccountModal: React.FC<AddAccountModalProps> = ({ isOpen, onClose, onSuccess, roles }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState<CreateAccountRequest>({
        accountName: '',
        accountPhone: '',
        password: '',
        status: 1,
        roleIds: [],
    });

    const resetForm = () => {
        setFormData({
            accountName: '',
            accountPhone: '',
            password: '',
            status: 1,
            roleIds: [],
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleCreateAccount = async () => {
        try {
            setLoading(true);
            await accountService.createAccount(formData);
            toast.success('Tạo tài khoản thành công!');
            handleClose();
            onSuccess();
        } catch (error) {
            console.error('Error creating account:', error);
            toast.error('Không thể tạo tài khoản. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = (roleId: string) => {
        setFormData({
            ...formData,
            roleIds: [roleId], // Backend nhận Set<String> nhưng UI chỉ chọn 1 role
        });
    };

    const isFormValid =
        formData.accountName && formData.accountPhone && formData.password && formData.roleIds.length > 0;

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle>Thêm tài khoản mới</DialogTitle>
                    <DialogDescription>Tạo tài khoản mới cho hệ thống</DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                        <Label htmlFor="accountName">Tên tài khoản *</Label>
                        <Input
                            id="accountName"
                            value={formData.accountName}
                            onChange={(e) =>
                                setFormData({
                                    ...formData,
                                    accountName: e.target.value,
                                })
                            }
                            placeholder="Nhập tên tài khoản"
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label htmlFor="accountPhone">Số điện thoại *</Label>
                        <Input
                            id="accountPhone"
                            value={formData.accountPhone}
                            onChange={(e) =>
                                setFormData({
                                    ...formData,
                                    accountPhone: e.target.value,
                                })
                            }
                            placeholder="Nhập số điện thoại"
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label htmlFor="password">Mật khẩu *</Label>
                        <Input
                            id="password"
                            type="password"
                            value={formData.password}
                            onChange={(e) =>
                                setFormData({
                                    ...formData,
                                    password: e.target.value,
                                })
                            }
                            placeholder="Nhập mật khẩu"
                        />
                    </div>

                    <div className="grid gap-2">
                        <Label htmlFor="roleId">Vai trò *</Label>
                        <Select value={formData.roleIds[0] || ''} onValueChange={handleRoleChange}>
                            <SelectTrigger>
                                <SelectValue placeholder="Chọn vai trò" />
                            </SelectTrigger>
                            <SelectContent>
                                {roles.map((role) => (
                                    <SelectItem key={role.roleId} value={role.roleId}>
                                        {role.roleName}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="grid gap-2">
                        <Label htmlFor="status">Trạng thái</Label>
                        <Select
                            value={formData.status.toString()}
                            onValueChange={(value) =>
                                setFormData({
                                    ...formData,
                                    status: parseInt(value),
                                })
                            }
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="1">Hoạt động</SelectItem>
                                <SelectItem value="0">Tạm ngưng</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={handleClose} disabled={loading}>
                        Hủy
                    </Button>
                    <Button
                        onClick={handleCreateAccount}
                        disabled={loading || !isFormValid}
                        className="bg-blue-600 hover:bg-blue-700"
                    >
                        {loading ? (
                            <>
                                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                Đang tạo...
                            </>
                        ) : (
                            <>
                                <Plus className="w-4 h-4 mr-2" />
                                Tạo tài khoản
                            </>
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default AddAccountModal;
