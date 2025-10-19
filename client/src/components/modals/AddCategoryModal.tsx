import React, { useState } from 'react';
import { categoryService } from '../../services/categoryService';
import type { CreateCategoryRequest } from '../../types/category';
import { Button } from '../ui/Button/Button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Loader2, Tags, Plus } from 'lucide-react';
import { toast } from 'sonner';

interface AddCategoryModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const AddCategoryModal: React.FC<AddCategoryModalProps> = ({ isOpen, onClose, onSuccess }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState<CreateCategoryRequest>({
        categoryName: '',
        status: 1, // Default active
    });

    const resetForm = () => {
        setFormData({
            categoryName: '',
            status: 1,
        });
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.categoryName.trim()) {
            toast.error('Vui lòng nhập tên thể loại');
            return;
        }

        setLoading(true);
        try {
            await categoryService.createCategory({
                ...formData,
                status: formData.status || 1,
            });
            toast.success('Tạo thể loại thành công!');
            handleClose();
            onSuccess();
        } catch (error) {
            console.error('Error creating category:', error);
            toast.error('Không thể tạo thể loại. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (field: keyof CreateCategoryRequest, value: string | number) => {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <Tags className="w-5 h-5 text-purple-600" />
                        Thêm thể loại mới
                    </DialogTitle>
                    <DialogDescription>Tạo thể loại mới cho hệ thống quản lý sách</DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="categoryName" className="flex items-center gap-2">
                            <Tags className="w-4 h-4" />
                            Tên thể loại
                        </Label>
                        <Input
                            id="categoryName"
                            value={formData.categoryName}
                            onChange={(e) => handleInputChange('categoryName', e.target.value)}
                            placeholder="Nhập tên thể loại"
                            required
                            disabled={loading}
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="status">Trạng thái</Label>
                        <Select
                            value={formData.status?.toString()}
                            onValueChange={(value) => handleInputChange('status', parseInt(value))}
                            disabled={loading}
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

                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={handleClose} disabled={loading}>
                            Hủy
                        </Button>
                        <Button
                            type="submit"
                            disabled={loading || !formData.categoryName.trim()}
                            className="bg-purple-600 hover:bg-purple-700"
                        >
                            {loading ? (
                                <>
                                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                    Đang tạo...
                                </>
                            ) : (
                                <>
                                    <Plus className="w-4 h-4 mr-2" />
                                    Tạo thể loại
                                </>
                            )}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};

export default AddCategoryModal;
