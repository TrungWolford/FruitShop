import React, { useState, useEffect } from 'react';
import { accountService } from '../../services/adminAccountService';
import type { Account, Role, UpdateAccountRequest } from '../../types/account';
import { Button } from '../ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Loader2, User, Phone, Lock, Shield } from 'lucide-react';
import { toast } from 'sonner';

interface EditAccountModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    account: Account | null;
    roles: Role[];
}

const EditAccountModal: React.FC<EditAccountModalProps> = ({ isOpen, onClose, onSuccess, account, roles }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState<UpdateAccountRequest>({
        accountName: '',
        accountPhone: '',
        password: '',
        status: 1,
        roleIds: [],
    });

    useEffect(() => {
        if (account) {
            setFormData({
                accountName: account.accountName,
                accountPhone: account.accountPhone,
                password: '', // Không hiển thị mật khẩu cũ
                status: account.status,
                roleIds: account.roles.map((role) => role.roleId),
            });
        }
    }, [account]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!account) return;

        setLoading(true);
        try {
            // Chỉ gửi các trường có giá trị
            const updateData: UpdateAccountRequest = {
                accountName: formData.accountName,
                accountPhone: formData.accountPhone,
                status: formData.status,
                roleIds: formData.roleIds,
            };

            // Chỉ thêm password nếu người dùng nhập mới
            if (formData.password && formData.password.trim()) {
                updateData.password = formData.password;
            }

            await accountService.updateAccount(account.accountId, updateData);

            toast.success('Cập nhật tài khoản thành công!');

            onSuccess();
            onClose();
        } catch (error) {
            console.error('Error updating account:', error);
            toast.error('Không thể cập nhật tài khoản. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (field: keyof UpdateAccountRequest, value: string | number | string[]) => {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <User className="w-5 h-5 text-blue-600" />
                        Sửa tài khoản
                    </DialogTitle>
                    <DialogDescription>
                        Cập nhật thông tin tài khoản. Để trống mật khẩu nếu không muốn thay đổi.
                    </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="accountName" className="flex items-center gap-2">
                            <User className="w-4 h-4" />
                            Tên tài khoản
                        </Label>
                        <Input
                            id="accountName"
                            value={formData.accountName}
                            onChange={(e) => handleInputChange('accountName', e.target.value)}
                            placeholder="Nhập tên tài khoản"
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="accountPhone" className="flex items-center gap-2">
                            <Phone className="w-4 h-4" />
                            Số điện thoại
                        </Label>
                        <Input
                            id="accountPhone"
                            value={formData.accountPhone}
                            onChange={(e) => handleInputChange('accountPhone', e.target.value)}
                            placeholder="Nhập số điện thoại"
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="password" className="flex items-center gap-2">
                            <Lock className="w-4 h-4" />
                            Mật khẩu mới (để trống nếu không đổi)
                        </Label>
                        <Input
                            id="password"
                            type="password"
                            value={formData.password}
                            onChange={(e) => handleInputChange('password', e.target.value)}
                            placeholder="Nhập mật khẩu mới"
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="status" className="flex items-center gap-2">
                            <Shield className="w-4 h-4" />
                            Trạng thái
                        </Label>
                        <Select
                            value={(formData.status || 1).toString()}
                            onValueChange={(value) => handleInputChange('status', parseInt(value))}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Chọn trạng thái" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="1">Hoạt động</SelectItem>
                                <SelectItem value="0">Tạm ngưng</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="roleIds" className="flex items-center gap-2">
                            <Shield className="w-4 h-4" />
                            Vai trò
                        </Label>
                        <Select
                            value={formData.roleIds?.[0] || ''}
                            onValueChange={(value) => handleInputChange('roleIds', [value])}
                        >
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

                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
                            Hủy
                        </Button>
                        <Button type="submit" disabled={loading} className="bg-blue-600 hover:bg-blue-700">
                            {loading ? (
                                <>
                                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                    Đang cập nhật...
                                </>
                            ) : (
                                'Cập nhật tài khoản'
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default EditAccountModal;
