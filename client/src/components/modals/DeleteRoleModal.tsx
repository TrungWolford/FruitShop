import React from 'react';
import { roleService } from '../../services/adminAccountService';
import type { Role } from '../../types/account';
import { Button } from '../ui/Button/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Trash2, Loader2, AlertTriangle } from 'lucide-react';
import { toast } from 'sonner';
import { useState } from 'react';

interface DeleteRoleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    role: Role | null;
}

const DeleteRoleModal: React.FC<DeleteRoleModalProps> = ({ isOpen, onClose, onSuccess, role }) => {
    const [loading, setLoading] = useState(false);

    const handleDeleteRole = async () => {
        if (!role) return;

        try {
            setLoading(true);
            await roleService.deleteRole(role.roleId);
            toast.success('Xóa vai trò thành công!');
            onClose();
            onSuccess();
        } catch (error) {
            console.error('Error deleting role:', error);
            toast.error('Không thể xóa vai trò. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-red-600">
                        <AlertTriangle className="w-5 h-5" />
                        Xác nhận xóa vai trò
                    </DialogTitle>
                    <DialogDescription>
                        Bạn có chắc chắn muốn xóa vai trò "{role?.roleName}"? Hành động này không thể hoàn tác và có thể
                        ảnh hưởng đến các tài khoản đang sử dụng vai trò này.
                    </DialogDescription>
                </DialogHeader>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose} disabled={loading}>
                        Hủy
                    </Button>
                    <Button variant="destructive" onClick={handleDeleteRole} disabled={loading}>
                        {loading ? (
                            <>
                                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                Đang xóa...
                            </>
                        ) : (
                            <>
                                <Trash2 className="w-4 h-4 mr-2" />
                                Xóa vai trò
                            </>
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default DeleteRoleModal;
