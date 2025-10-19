import React, { useState } from 'react';
import { roleService } from '../../services/adminAccountService';
import { Button } from '../ui/Button/Button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Plus, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface AddRoleModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const AddRoleModal: React.FC<AddRoleModalProps> = ({ isOpen, onClose, onSuccess }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        roleName: '',
    });

    const resetForm = () => {
        setFormData({
            roleName: '',
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleCreateRole = async () => {
        try {
            setLoading(true);
            await roleService.createRole(formData);
            toast.success('Tạo vai trò thành công!');
            handleClose();
            onSuccess();
        } catch (error) {
            console.error('Error creating role:', error);
            toast.error('Không thể tạo vai trò. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const isFormValid = formData.roleName.trim().length > 0;

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle>Thêm vai trò mới</DialogTitle>
                    <DialogDescription>Tạo vai trò mới cho hệ thống</DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                        <Label htmlFor="roleName">Tên vai trò *</Label>
                        <Input
                            id="roleName"
                            value={formData.roleName}
                            onChange={(e) =>
                                setFormData({
                                    ...formData,
                                    roleName: e.target.value,
                                })
                            }
                            placeholder="Nhập tên vai trò (VD: ADMIN, CUSTOMER)"
                        />
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={handleClose} disabled={loading}>
                        Hủy
                    </Button>
                    <Button
                        onClick={handleCreateRole}
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
                                Tạo vai trò
                            </>
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default AddRoleModal;
