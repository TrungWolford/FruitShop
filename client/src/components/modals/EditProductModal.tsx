import React, { useState, useEffect } from 'react';
import { Button } from '../ui/Button/Button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Badge } from '../ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '../ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { toast } from 'sonner';
import { productService } from '../../services/productService';
import { categoryService } from '../../services/categoryService';
import { imageService } from '../../services/imageService';
import { cloudinaryService } from '../../services/cloudinaryService';
import { SUCCESS_MESSAGES, ERROR_MESSAGES } from '../../config/constants';
import type { ProductFormData, CreateProductRequest, Product } from '../../types/product';
import type { Category } from '../../types/category';
import { Upload, X, BookOpen } from 'lucide-react';

interface EditProductModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    product: Product | null;
}

const EditProductModal: React.FC<EditProductModalProps> = ({ isOpen, onClose, onSuccess, product }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [categories, setCategories] = useState<Category[]>([]);
    const [imagePreviewUrls, setImagePreviewUrls] = useState<string[]>([]);
    const [originalImageUrls, setOriginalImageUrls] = useState<string[]>([]); // Track original Cloudinary URLs
    const [removedExistingImages, setRemovedExistingImages] = useState<Set<number>>(new Set()); // Track which existing images were removed

    // Helper function to count current images accurately
    const getCurrentImageCount = () => {
        let count = 0;
        for (let i = 0; i < 5; i++) {
            const hasNewImage = imagePreviewUrls[i];
            const hasExistingImage = product?.images && product.images[i] && !removedExistingImages.has(i);
            if (hasNewImage || hasExistingImage) {
                count++;
            }
        }

        // Debug log
        console.log('Image count debug:', {
            total: count,
            newImages: imagePreviewUrls.length,
            existingImages: product?.images?.length || 0,
            removedCount: removedExistingImages.size,
            removedIndices: Array.from(removedExistingImages),
        });

        return count;
    };

    // Handle clicking on existing image to replace it
    const handleExistingImageClick = (index: number) => {
        console.log(`🖱️ Clicked on existing image at slot ${index + 1}`);
        // The file input will be triggered by the label
    };

    const [formData, setFormData] = useState<ProductFormData>({
        productName: '',
        selectedCategories: [],
        price: '',
        stock: '',
        description: '',
        status: 1,
        images: [],
    });

    // Load categories and populate form when product changes
    useEffect(() => {
        if (isOpen && product) {
            loadCategories();
            populateFormData();
        }
    }, [isOpen, product]);

    // Cleanup localStorage when component unmounts
    useEffect(() => {
        return () => {
            // Cleanup localStorage khi component unmount để tránh vượt quota
            imageService.cleanupStorage();
        };
    }, []);

    const loadCategories = async () => {
        try {
            const response = await categoryService.getAllCategories();
            setCategories(response.content || []);
        } catch (error) {
            console.error('Error loading categories:', error);

            // Fallback: Use mock categories when API is not available
            const mockCategories = [
                { categoryId: '1', categoryName: 'Tiểu thuyết', status: 1 },
                { categoryId: '2', categoryName: 'Khoa học', status: 1 },
                { categoryId: '3', categoryName: 'Lịch sử', status: 1 },
                { categoryId: '4', categoryName: 'Văn học', status: 1 },
                { categoryId: '5', categoryName: 'Kinh tế', status: 1 },
            ];

            setCategories(mockCategories);

            toast.warning('Không thể kết nối server. Sử dụng danh mục mẫu.');
        }
    };

    const populateFormData = () => {
        if (!product) return;

        console.log('🔄 Populating form data for product:', product.productId);
        console.log('📦 Product data:', {
            productName: product.productName,
            categories: product.categories?.length || 0,
            images: product.images?.length || 0,
        });

        if (product.images) {
            product.images.forEach((img, index) => {
                console.log(`  Image ${index + 1}: ${img.imageUrl} (order: ${img.imageOrder})`);
            });
        }

        // Populate form with existing product data
        setFormData({
            productName: product.productName,
            selectedCategories: product.categories.map((cat) => cat.categoryId),
            price: product.price.toString(),
            stock: product.stock.toString(),
            description: product.description,
            status: product.status,
            images: [],
        });

        // Set existing image URLs (Cloudinary URLs from database)
        if (product.images && product.images.length > 0) {
            const existingImageUrls = product.images.map((img) => img.imageUrl);
            setOriginalImageUrls(existingImageUrls); // Keep track of original Cloudinary URLs
        } else {
            setOriginalImageUrls([]);
        }

        // Clear image previews and reset removed images tracking
        setImagePreviewUrls([]);
        setRemovedExistingImages(new Set());
    };

    const handleInputChange = (field: keyof ProductFormData, value: string | number | string[]) => {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleSingleImageChange = async (file: File, slotIndex: number) => {
        if (!product) return;

        // Check if slot is already occupied by new image
        const hasNewImage = imagePreviewUrls[slotIndex];

        if (hasNewImage) {
            toast.error('Ô này đã có hình ảnh mới. Vui lòng xóa hình ảnh hiện tại trước.');
            return;
        }

        // Validate image
        const validation = imageService.validateImage(file);
        if (!validation.isValid) {
            toast.error(validation.error);
            return;
        }

        try {
            const previewUrl = imageService.createPreviewUrl(file);

            // Check if this slot has an existing image that needs to be replaced
            const hasExistingImage =
                product.images && product.images[slotIndex] && !removedExistingImages.has(slotIndex);

            if (hasExistingImage) {
                // Mark existing image as removed (will be replaced with new one)
                console.log(`🔄 Marking existing image at slot ${slotIndex + 1} for replacement`);
                setRemovedExistingImages((prev) => new Set([...prev, slotIndex]));
            }

            // Add file to formData images array
            setFormData((prev) => ({
                ...prev,
                images: [...prev.images, file],
            }));

            // Set preview URL at specific slot
            setImagePreviewUrls((prev) => {
                const newUrls = [...prev];
                newUrls[slotIndex] = previewUrl;
                return newUrls;
            });

            toast.success(
                hasExistingImage
                    ? `Sẽ thay thế hình ảnh ở ô ${slotIndex + 1} khi lưu`
                    : `Đã thêm hình ảnh vào ô ${slotIndex + 1}`,
            );

            // Reset file input
            const fileInput = document.getElementById(`image-upload-slot-${slotIndex}`) as HTMLInputElement;
            if (fileInput) {
                fileInput.value = '';
            }
        } catch (error) {
            console.error('Error preparing image:', error);
            toast.error('Không thể xử lý hình ảnh');
        }
    };

    const removeNewImage = (index: number) => {
        // Remove preview URL
        if (imagePreviewUrls[index]) {
            imageService.revokePreviewUrl(imagePreviewUrls[index]);
        }

        // Clear preview at this slot
        setImagePreviewUrls((prev) => {
            const newUrls = [...prev];
            newUrls[index] = '';
            return newUrls;
        });

        // Remove file from formData.images
        // Note: This is simplified - in production you'd need to track which file corresponds to which slot
        setFormData((prev) => ({
            ...prev,
            images: prev.images.filter((_, i) => i !== index),
        }));

        toast.info('Đã xóa hình ảnh mới');
    };

    const removeExistingImage = (index: number) => {
        // Mark this existing image as removed
        setRemovedExistingImages((prev) => new Set([...prev, index]));
        toast.info('Hình ảnh sẽ bị xóa khi lưu sản phẩm');
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!product) return;

        // Validation
        if (!formData.productName.trim()) {
            toast.error('Vui lòng nhập tên sản phẩm');
            return;
        }

        if (formData.selectedCategories.length === 0) {
            toast.error('Vui lòng chọn ít nhất một danh mục');
            return;
        }

        if (!formData.price || parseFloat(formData.price) <= 0) {
            toast.error('Vui lòng nhập giá hợp lệ');
            return;
        }

        if (!formData.stock || parseInt(formData.stock) < 0) {
            toast.error('Vui lòng nhập số lượng tồn kho hợp lệ');
            return;
        }

        if (!formData.description.trim()) {
            toast.error('Vui lòng nhập mô tả sản phẩm');
            return;
        }

        setIsLoading(true);

        try {
            // ========== STEP 1: Upload NEW images to Cloudinary ==========
            console.log('=== STARTING FILE UPLOADS FOR EDIT ===');
            let uploadedImageUrls: string[] = [];

            // Upload new images to Cloudinary
            if (formData.images.length > 0) {
                console.log('Uploading', formData.images.length, 'new images to Cloudinary...');
                
                for (let i = 0; i < formData.images.length; i++) {
                    const image = formData.images[i];
                    console.log(`Uploading new image ${i + 1}/${formData.images.length}:`, image.name);
                    
                    try {
                        const result = await cloudinaryService.uploadImage(image, {
                            folder: 'products/images'
                        });
                        
                        if (result.success && result.data) {
                            uploadedImageUrls.push(result.data.url);
                            console.log(`✅ New image ${i + 1} uploaded successfully:`, result.data.url);
                            
                            toast.success(`Đã tải lên hình ảnh mới ${i + 1}/${formData.images.length}`, {
                                duration: 2000,
                                position: 'top-right',
                            });
                        } else {
                            console.error(`❌ New image ${i + 1} upload failed:`, result.message);
                            toast.error(`Không thể tải lên hình ảnh ${i + 1}: ${result.message}`, {
                                duration: 4000,
                                position: 'top-right',
                            });
                            throw new Error(`Failed to upload image ${i + 1}: ${result.message}`);
                        }
                    } catch (error) {
                        console.error(`❌ Error uploading new image ${i + 1}:`, error);
                        toast.error(`Lỗi khi tải lên hình ảnh ${i + 1}`, {
                            duration: 4000,
                            position: 'top-right',
                        });
                        throw error;
                    }
                }
                
                console.log('✅ All new images uploaded successfully:', uploadedImageUrls);
            }

            // ========== STEP 2: Combine existing and new image URLs ==========
            // Keep existing images that weren't removed
            const remainingOriginalUrls = originalImageUrls.filter((_, index) => 
                !removedExistingImages.has(index)
            );
            
            // Combine remaining original URLs with newly uploaded URLs
            const allImageUrls = [...remainingOriginalUrls, ...uploadedImageUrls];
            
            // Check if images have changed
            const imagesChanged = 
                remainingOriginalUrls.length !== originalImageUrls.length || // Some removed
                uploadedImageUrls.length > 0; // Or new ones added

            console.log('📊 Image comparison:', {
                original: originalImageUrls.length,
                remaining: remainingOriginalUrls.length,
                newUploaded: uploadedImageUrls.length,
                total: allImageUrls.length,
                removed: Array.from(removedExistingImages),
                imagesChanged
            });

            // ========== STEP 3: Update product with combined URLs ==========
            console.log('=== UPDATING PRODUCT IN DATABASE ===');
            
            const productData: CreateProductRequest = {
                productName: formData.productName.trim(),
                categoryIds: formData.selectedCategories,
                price: parseFloat(formData.price),
                stock: parseInt(formData.stock),
                description: formData.description.trim(),
                status: formData.status,
                // Only send images if they have changed
                imageNames: imagesChanged ? allImageUrls : undefined,
            };

            console.log('📦 Updating product with data:', {
                productName: productData.productName,
                price: productData.price,
                stock: productData.stock,
                categories: productData.categoryIds,
                imagesChanged,
                imageCount: allImageUrls.length,
                imageUrls: allImageUrls,
            });

            await productService.updateProduct(product.productId, productData);

            toast.success(
                `${SUCCESS_MESSAGES.PRODUCT_UPDATED} ${
                    allImageUrls.length > 0 ? `với ${allImageUrls.length} hình ảnh` : ''
                }`,
                {
                    duration: 3000,
                    position: 'top-right',
                    style: {
                        background: '#10b981',
                        color: '#fff',
                        borderRadius: '8px',
                        padding: '12px 16px',
                    },
                }
            );

            // Reset form
            setFormData({
                productName: '',
                selectedCategories: [],
                price: '',
                stock: '',
                description: '',
                status: 1,
                images: [],
            });
            setImagePreviewUrls([]);
            setOriginalImageUrls([]);
            setRemovedExistingImages(new Set());

            // Cleanup localStorage
            imageService.cleanupStorage();

            // Close modal first
            onClose();

            // Add delay for loading effect and then reload table
            setTimeout(() => {
                onSuccess();
            }, 500);
        } catch (error: any) {
            console.error('Error updating product:', error);
            const errorMessage = error.response?.data?.message || error.message || ERROR_MESSAGES.PRODUCT_UPDATE_FAILED;
            toast.error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const handleClose = () => {
        // Reset form state
        setFormData({
            productName: '',
            selectedCategories: [],
            price: '',
            stock: '',
            description: '',
            status: 1,
            images: [],
        });
        setImagePreviewUrls([]);
        setOriginalImageUrls([]);
        setRemovedExistingImages(new Set());

        // Cleanup localStorage
        imageService.cleanupStorage();

        onClose();
    };

    if (!product) return null;

    return (
        <Dialog open={isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-w-5xl max-h-[95vh] overflow-y-auto">
                <DialogHeader className="border-b border-gray-200 pb-4">
                    <DialogTitle className="flex items-center gap-3 text-xl">
                        <div className="p-2 bg-amber-100 rounded-lg">
                            <BookOpen className="w-6 h-6 text-amber-600" />
                        </div>
                        <span>Sửa sản phẩm</span>
                    </DialogTitle>
                    <DialogDescription className="text-gray-600 mt-2">
                        Cập nhật thông tin sản phẩm trong hệ thống. Các trường có dấu * là bắt buộc.
                    </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-8 pt-4">
                    {/* Basic Information Section */}
                    <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 bg-amber-500 rounded-full"></div>
                            Thông tin cơ bản
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-2">
                                <Label htmlFor="productName" className="text-sm font-medium text-gray-700">
                                    Tên sản phẩm <span className="text-red-500">*</span>
                                </Label>
                                <Input
                                    id="productName"
                                    value={formData.productName}
                                    onChange={(e) => handleInputChange('productName', e.target.value)}
                                    placeholder="Nhập tên sản phẩm"
                                    className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20"
                                />
                            </div>

                            {/* author field removed */}
                        </div>
                    </div>

                    {/* Categories and Cover Section */}
                    <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                            Phân loại và bìa sách
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-2">
                                <Label className="text-sm font-medium text-gray-700">
                                    Danh mục <span className="text-red-500">*</span>
                                </Label>
                                <Select
                                    value={formData.selectedCategories[0] || ''}
                                    onValueChange={(value) => handleInputChange('selectedCategories', [value])}
                                >
                                    <SelectTrigger className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20">
                                        <SelectValue placeholder="Chọn danh mục" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {categories.map((category) => (
                                            <SelectItem key={category.categoryId} value={category.categoryId}>
                                                {category.categoryName}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            {/* cover field removed */}
                        </div>
                    </div>

                    {/* Price and Stock Section */}
                    <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                            Giá cả và tồn kho
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-2">
                                <Label htmlFor="price" className="text-sm font-medium text-gray-700">
                                    Giá bán (VNĐ) <span className="text-red-500">*</span>
                                </Label>
                                <Input
                                    id="price"
                                    type="number"
                                    value={formData.price}
                                    onChange={(e) => handleInputChange('price', e.target.value)}
                                    placeholder="Nhập giá bán"
                                    className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="stock" className="text-sm font-medium text-gray-700">
                                    Tồn kho <span className="text-red-500">*</span>
                                </Label>
                                <Input
                                    id="stock"
                                    type="number"
                                    value={formData.stock}
                                    onChange={(e) => handleInputChange('stock', e.target.value)}
                                    placeholder="Nhập số lượng tồn kho"
                                    className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Description and Status Section */}
                    <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 bg-purple-500 rounded-full"></div>
                            Mô tả và trạng thái
                        </h3>
                        <div className="space-y-6">
                            <div className="space-y-2">
                                <Label htmlFor="description" className="text-sm font-medium text-gray-700">
                                    Mô tả sản phẩm <span className="text-red-500">*</span>
                                </Label>
                                <Textarea
                                    id="description"
                                    value={formData.description}
                                    onChange={(e) => handleInputChange('description', e.target.value)}
                                    placeholder="Nhập mô tả chi tiết về sản phẩm..."
                                    rows={4}
                                    className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20 resize-none"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label className="text-sm font-medium text-gray-700">Trạng thái</Label>
                                <Select
                                    value={formData.status.toString()}
                                    onValueChange={(value) => handleInputChange('status', parseInt(value))}
                                >
                                    <SelectTrigger className="border-gray-300 focus:border-amber-500 focus:ring-amber-500/20">
                                        <SelectValue placeholder="Chọn trạng thái" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="1">Đang hoạt động</SelectItem>
                                        <SelectItem value="0">Ngừng hoạt động</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                    </div>

                    {/* Image Upload Section */}
                    <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                            <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                            Hình ảnh sản phẩm
                        </h3>

                        {/* 5 Image Slots */}
                        <div className="space-y-4">
                            <div className="flex items-center gap-2">
                                <h4 className="text-sm font-medium text-gray-700">Hình ảnh sản phẩm:</h4>
                                <Badge variant="outline" className="text-xs bg-blue-50 text-blue-700 border-blue-200">
                                    {getCurrentImageCount()}/5 hình
                                </Badge>
                                <span className="text-xs text-gray-500">(Click vào hình ảnh để thay thế)</span>
                            </div>

                            <div className="grid grid-cols-5 gap-4">
                                {Array.from({ length: 5 }, (_, index) => {
                                    const hasNewImage = imagePreviewUrls[index];
                                    const hasExistingImage =
                                        product.images && product.images[index] && !removedExistingImages.has(index);

                                    return (
                                        <div
                                            key={index}
                                            className={`relative group bg-white rounded-lg overflow-hidden shadow-sm border-2 border-dashed ${
                                                hasNewImage || hasExistingImage ? 'border-gray-200' : 'border-gray-300'
                                            } min-h-[120px] flex items-center justify-center`}
                                        >
                                            {hasNewImage ? (
                                                // New uploaded image
                                                <>
                                                    <img
                                                        src={imagePreviewUrls[index]}
                                                        alt={`Preview ${index + 1}`}
                                                        className="w-full h-full object-cover"
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => removeNewImage(index)}
                                                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-md"
                                                    >
                                                        <X className="w-3 h-3" />
                                                    </button>
                                                    <div className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white text-xs p-1 truncate">
                                                        Hình ảnh mới
                                                    </div>
                                                </>
                                            ) : hasExistingImage ? (
                                                // Existing image - clickable to replace
                                                <>
                                                    <label
                                                        htmlFor={`image-upload-slot-${index}`}
                                                        className="relative w-full h-full cursor-pointer"
                                                        onClick={() => handleExistingImageClick(index)}
                                                    >
                                                        <img
                                                            src={getImageUrl(product.images![index].imageUrl)}
                                                            alt={`Existing ${index + 1}`}
                                                            className="w-full h-full object-cover"
                                                        />
                                                        {/* Overlay to show it's clickable */}
                                                        <div className="absolute inset-0 bg-black bg-opacity-0 hover:bg-opacity-20 transition-all duration-200 flex items-center justify-center">
                                                            <div className="bg-white bg-opacity-90 rounded-full p-2 opacity-0 hover:opacity-100 transition-opacity">
                                                                <Upload className="w-4 h-4 text-gray-700" />
                                                            </div>
                                                        </div>
                                                        {/* Click indicator */}
                                                        <div className="absolute top-2 left-2 bg-blue-500 text-white text-xs px-2 py-1 rounded opacity-0 hover:opacity-100 transition-opacity">
                                                            Thay thế
                                                        </div>
                                                    </label>
                                                    <button
                                                        type="button"
                                                        onClick={() => removeExistingImage(index)}
                                                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-md z-10"
                                                    >
                                                        <X className="w-3 h-3" />
                                                    </button>
                                                    <div className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white text-xs p-1 truncate">
                                                        {product.images![index].imageUrl}
                                                    </div>
                                                </>
                                            ) : (
                                                // Empty slot - clickable for upload
                                                <label
                                                    htmlFor={`image-upload-slot-${index}`}
                                                    className="text-center p-4 cursor-pointer hover:bg-gray-50 transition-colors w-full h-full flex flex-col items-center justify-center"
                                                >
                                                    <div className="w-8 h-8 bg-amber-100 rounded-full mx-auto mb-2 flex items-center justify-center">
                                                        <Upload className="w-4 h-4 text-amber-600" />
                                                    </div>
                                                    <p className="text-xs text-gray-600 font-medium">Thêm ảnh</p>
                                                </label>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Hidden file inputs for each slot */}
                            {Array.from({ length: 5 }, (_, index) => {
                                return (
                                    <input
                                        key={index}
                                        type="file"
                                        accept="image/*"
                                        onChange={(e) => {
                                            if (e.target.files && e.target.files[0]) {
                                                handleSingleImageChange(e.target.files[0], index);
                                            }
                                        }}
                                        className="hidden"
                                        id={`image-upload-slot-${index}`}
                                    />
                                );
                            })}
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleClose}
                            disabled={isLoading}
                            className="border-gray-300 text-gray-700 hover:bg-gray-50"
                        >
                            Hủy
                        </Button>
                        <Button
                            type="submit"
                            disabled={isLoading}
                            className="bg-amber-600 hover:bg-amber-700 text-white"
                        >
                            {isLoading ? (
                                <div className="flex items-center gap-2">
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                    Đang cập nhật...
                                </div>
                            ) : (
                                'Cập nhật sản phẩm'
                            )}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
};

// Helper function to get image URL
const getImageUrl = (imageUrl: string) => {
    if (!imageUrl) return '/placeholder-image.jpg';

    if (imageUrl.startsWith('http')) {
        return imageUrl;
    }

    // Remove leading slash if exists and add /products/ prefix
    const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
    return `/products/${cleanUrl}`;
};

export default EditProductModal;
