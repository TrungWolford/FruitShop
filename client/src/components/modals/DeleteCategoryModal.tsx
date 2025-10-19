import React, { useState } from 'react';
import { categoryService } from '../../services/categoryService';
import type { Category } from '../../types/category';
import { Button } from '../ui/Button/Button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Trash2, Loader2, AlertTriangle } from 'lucide-react';
import { toast } from 'sonner';

interface DeleteCategoryModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    category: Category | null;
}

const DeleteCategoryModal: React.FC<DeleteCategoryModalProps> = ({ isOpen, onClose, onSuccess, category }) => {
    const [loading, setLoading] = useState(false);

    const handleDeleteCategory = async () => {
        if (!category) return;

        try {
            setLoading(true);
            await categoryService.deleteCategory(category.categoryId);
            toast.success('Xóa thể loại thành công!');
            onClose();
            onSuccess();
        } catch (error) {
            console.error('Error deleting category:', error);
            toast.error('Không thể xóa thể loại. Vui lòng thử lại!');
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
                        Xác nhận xóa thể loại
                    </DialogTitle>
                    <DialogDescription>
                        Bạn có chắc chắn muốn xóa thể loại "<strong>{category?.categoryName}</strong>"? Hành động này
                        không thể hoàn tác và có thể ảnh hưởng đến các sản phẩm đang sử dụng thể loại này.
                    </DialogDescription>
                </DialogHeader>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose} disabled={loading}>
                        Hủy
                    </Button>
                    <Button variant="destructive" onClick={handleDeleteCategory} disabled={loading}>
                        {loading ? (
                            <>
                                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                Đang xóa...
                            </>
                        ) : (
                            <>
                                <Trash2 className="w-4 h-4 mr-2" />
                                Xóa thể loại
                            </>
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default DeleteCategoryModal;
