import React, { useState, useEffect } from 'react';
import { roleService } from '../../services/adminAccountService';
import type { Role } from '../../types/account';
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
import { Loader2, Shield } from 'lucide-react';
import { toast } from 'sonner';

interface EditRoleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    role: Role | null;
}

const EditRoleModal: React.FC<EditRoleModalProps> = ({ isOpen, onClose, onSuccess, role }) => {
    const [loading, setLoading] = useState(false);
    const [roleName, setRoleName] = useState('');

    useEffect(() => {
        if (role) {
            setRoleName(role.roleName);
        }
    }, [role]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!role) return;

        if (!roleName.trim()) {
            toast.error('Vui lòng nhập tên vai trò');
            return;
        }

        setLoading(true);
        try {
            await roleService.updateRole(role.roleId, { roleName: roleName.trim() });

            toast.success('Cập nhật vai trò thành công!');

            onSuccess();
            onClose();
            setRoleName('');
        } catch (error) {
            console.error('Error updating role:', error);
            toast.error('Không thể cập nhật vai trò. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        onClose();
        setRoleName('');
    };

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <Shield className="w-5 h-5 text-purple-600" />
                        Sửa vai trò
                    </DialogTitle>
                    <DialogDescription>Cập nhật thông tin vai trò trong hệ thống</DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="roleName" className="flex items-center gap-2">
                            <Shield className="w-4 h-4" />
                            Tên vai trò
                        </Label>
                        <Input
                            id="roleName"
                            value={roleName}
                            onChange={(e) => setRoleName(e.target.value)}
                            placeholder="Nhập tên vai trò"
                            required
                            disabled={loading}
                        />
                    </div>

                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={handleClose} disabled={loading}>
                            Hủy
                        </Button>
                        <Button
                            type="submit"
                            disabled={loading || !roleName.trim()}
                            className="bg-purple-600 hover:bg-purple-700"
                        >
                            {loading ? (
                                <>
                                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                    Đang cập nhật...
                                </>
                            ) : (
                                'Cập nhật vai trò'
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default EditRoleModal;
