import React, { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Badge } from '../ui/badge';
import { 
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '../ui/dialog';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
import { toast } from 'sonner';
import { productService } from '../../services/productService';
import { categoryService } from '../../services/categoryService';
import { imageService } from '../../services/imageService';

import { VALIDATION_RULES, SUCCESS_MESSAGES, ERROR_MESSAGES } from '../../config/constants';
import type { ProductFormData, CreateProductRequest } from '../../types/product';
import type { Category } from '../../types/category';
import { Upload, X, BookOpen } from 'lucide-react';

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const AddProductModal: React.FC<AddProductModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [imagePreviewUrls, setImagePreviewUrls] = useState<string[]>(new Array(5).fill(''));
  const [imageNames, setImageNames] = useState<string[]>(new Array(5).fill('')); // Track image names for database
  
  const [formData, setFormData] = useState<ProductFormData>({
    productName: '',
    selectedCategories: [],
    author: '',
    cover: 'Bìa mềm',
    price: '',
    stock: '',
    description: '',
    status: 1,
    images: [],
  });

  // Load categories on component mount
  useEffect(() => {
    if (isOpen) {
      loadCategories();
    }
  }, [isOpen]);

  const loadCategories = async () => {
    try {
              const response = await categoryService.getAllCategories();
        setCategories(response.content);
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
      
      toast.warning("Không thể kết nối server. Sử dụng danh mục mẫu.");
    }
  };

  const handleInputChange = (field: keyof ProductFormData, value: string | number | string[]) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleImageChange = async (event: React.ChangeEvent<HTMLInputElement>, index: number) => {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    const file = files[0]; // Only take the first file for single upload

    // Validate file using imageService
    const validation = imageService.validateImage(file);
    if (!validation.isValid) {
      toast.error(validation.error);
      return;
    }

    // Save image to assets and create preview URL
    try {
      // Save to assets and get filename
      const fileName = await imageService.saveImageToAssets(file);
      
      // Create preview URL
      const previewUrl = imageService.createPreviewUrl(file);

      // Update the specific index
      setFormData(prev => ({
        ...prev,
        images: prev.images.map((_, i) => i === index ? file : prev.images[i])
      }));
      
      setImagePreviewUrls(prev => {
        const newUrls = [...prev];
        newUrls[index] = previewUrl;
        return newUrls;
      });
      
      setImageNames(prev => {
        const newNames = [...prev];
        newNames[index] = fileName;
        return newNames;
      });
      
      toast.success(`Đã thêm hình ảnh ${index + 1}. Lưu tên file gốc vào database.`);
    } catch (error) {
      console.error('Error saving image:', error);
      toast.error("Không thể lưu hình ảnh");
    }
  };

  const removeImage = (index: number) => {
    // Revoke the object URL to free memory
    if (imagePreviewUrls[index]) {
      imageService.revokePreviewUrl(imagePreviewUrls[index]);
    }
    
    // Clear the specific index
    setImagePreviewUrls(prev => {
      const newUrls = [...prev];
      newUrls[index] = '';
      return newUrls;
    });
    
    setImageNames(prev => {
      const newNames = [...prev];
      newNames[index] = '';
      return newNames;
    });
    
    setFormData(prev => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index)
    }));
  };

  const handleCategoryToggle = (categoryId: string) => {
    setFormData(prev => ({
      ...prev,
      selectedCategories: prev.selectedCategories.includes(categoryId)
        ? prev.selectedCategories.filter(id => id !== categoryId)
        : [...prev.selectedCategories, categoryId]
    }));
  };

  const validateForm = (): boolean => {
    if (!formData.productName.trim()) {
      toast.error("Vui lòng nhập tên sản phẩm");
      return false;
    }

    if (formData.productName.length < VALIDATION_RULES.PRODUCT_NAME.MIN_LENGTH) {
      toast.error(VALIDATION_RULES.PRODUCT_NAME.MESSAGE);
      return false;
    }

    if (!formData.author.trim()) {
      toast.error("Vui lòng nhập tác giả");
      return false;
    }

    if (!formData.price || parseFloat(formData.price) <= 0) {
      toast.error(VALIDATION_RULES.PRICE.MESSAGE);
      return false;
    }

    if (!formData.stock || parseInt(formData.stock) < 0) {
      toast.error(VALIDATION_RULES.QUANTITY.MESSAGE);
      return false;
    }

    if (!formData.description.trim()) {
      toast.error("Vui lòng nhập mô tả sản phẩm");
      return false;
    }

    if (formData.selectedCategories.length === 0) {
      toast.error("Vui lòng chọn ít nhất một danh mục");
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setIsLoading(true);

    try {
      const productData: CreateProductRequest = {
        productName: formData.productName.trim(),
        categoryIds: formData.selectedCategories,
        author: formData.author.trim(),
        cover: formData.cover,
        price: parseFloat(formData.price),
        stock: parseInt(formData.stock),
        description: formData.description.trim(),
        status: formData.status,
        imageNames: imageNames.length > 0 ? imageNames : undefined, // Send image filenames
      };

      console.log('📦 Creating product with data:', {
        productName: productData.productName,
        author: productData.author,
        price: productData.price,
        stock: productData.stock,
        categories: productData.categoryIds,
        imageCount: imageNames.length,
        imageNames: imageNames
      });

      await productService.createProduct(productData);

      toast.success(`${SUCCESS_MESSAGES.PRODUCT_CREATED} ${imageNames.length > 0 ? `với ${imageNames.length} hình ảnh` : ''}`);

      // Reset form
      setFormData({
        productName: '',
        selectedCategories: [],
        author: '',
        cover: 'Bìa mềm',
        price: '',
        stock: '',
        description: '',
        status: 1,
        images: [],
      });

      // Clear image previews and names
      imagePreviewUrls.forEach(url => {
        if (url) imageService.revokePreviewUrl(url);
      });
      setImagePreviewUrls(new Array(5).fill(''));
      setImageNames(new Array(5).fill(''));

      onSuccess();
      onClose();
    } catch (error: any) {
      console.error('Error creating product:', error);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          ERROR_MESSAGES.PRODUCT_CREATE_FAILED;
      
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    // Clean up image preview URLs and names
    imagePreviewUrls.forEach(url => imageService.revokePreviewUrl(url));
    setImagePreviewUrls([]);
    setImageNames([]);
    
    // Reset form
    setFormData({
      productName: '',
      selectedCategories: [],
      author: '',
      cover: 'Bìa mềm',
      price: '',
      stock: '',
      description: '',
      status: 1,
      images: [],
    });
    
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-amber-500" />
            Thêm sản phẩm mới
          </DialogTitle>
          <DialogDescription>
            Điền thông tin chi tiết để tạo sản phẩm mới trong hệ thống
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="productName">Tên sản phẩm *</Label>
              <Input
                id="productName"
                value={formData.productName}
                onChange={(e) => handleInputChange('productName', e.target.value)}
                placeholder="Nhập tên sản phẩm..."
                className="w-full"
                maxLength={VALIDATION_RULES.PRODUCT_NAME.MAX_LENGTH}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="author">Tác giả *</Label>
              <Input
                id="author"
                value={formData.author}
                onChange={(e) => handleInputChange('author', e.target.value)}
                placeholder="Nhập tên tác giả..."
                className="w-full"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="cover">Loại bìa</Label>
              <Select value={formData.cover} onValueChange={(value) => handleInputChange('cover', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Chọn loại bìa" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Bìa mềm">Bìa mềm</SelectItem>
                  <SelectItem value="Bìa cứng">Bìa cứng</SelectItem>
                  <SelectItem value="Bìa bóng">Bìa bóng</SelectItem>
                  <SelectItem value="Bìa matte">Bìa matte</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="price">Giá bán (VNĐ) *</Label>
              <Input
                id="price"
                type="number"
                value={formData.price}
                onChange={(e) => handleInputChange('price', e.target.value)}
                placeholder="0"
                min="0"
                step="1000"
                className="w-full"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="stock">Tồn kho *</Label>
              <Input
                id="stock"
                type="number"
                value={formData.stock}
                onChange={(e) => handleInputChange('stock', e.target.value)}
                placeholder="0"
                min="0"
                className="w-full"
              />
            </div>
          </div>

          {/* Categories */}
          <div className="space-y-2">
            <Label>Danh mục *</Label>
            <div className="border border-gray-200 rounded-md p-3 max-h-32 overflow-y-auto">
              {categories.length === 0 ? (
                <p className="text-gray-500 text-sm">Đang tải danh mục...</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {categories.map((category) => (
                    <Badge
                      key={category.categoryId}
                      variant={formData.selectedCategories.includes(category.categoryId) ? "default" : "outline"}
                      className={`cursor-pointer transition-all ${
                        formData.selectedCategories.includes(category.categoryId)
                          ? 'bg-amber-600 text-white border-amber-600'
                          : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                      }`}
                      onClick={() => handleCategoryToggle(category.categoryId)}
                    >
                      {category.categoryName}
                    </Badge>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Mô tả sản phẩm *</Label>
            <Textarea
              id="description"
              value={formData.description}
              onChange={(e) => handleInputChange('description', e.target.value)}
              placeholder="Nhập mô tả chi tiết về sản phẩm..."
              rows={4}
              className="w-full"
            />
          </div>

          {/* Image Upload */}
          <div className="space-y-2">
            <Label>Hình ảnh sản phẩm</Label>
            <div className="grid grid-cols-5 gap-3">
              {Array.from({ length: 5 }).map((_, index) => (
                <div key={index} className="relative">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => handleImageChange(e, index)}
                    className="hidden"
                    id={`image-upload-${index}`}
                  />
                  <label 
                    htmlFor={`image-upload-${index}`} 
                    className="block w-full h-24 border-2 border-dashed border-gray-300 rounded-lg p-2 text-center cursor-pointer hover:border-amber-400 hover:bg-amber-50 transition-colors"
                  >
                    {imagePreviewUrls[index] ? (
                      <div className="relative w-full h-full">
                        <img
                          src={imagePreviewUrls[index]}
                          alt={`Preview ${index + 1}`}
                          className="w-full h-full object-cover rounded-md"
                        />
                        <button
                          type="button"
                          onClick={(e) => {
                            e.preventDefault();
                            removeImage(index);
                          }}
                          className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-red-600"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </div>
                    ) : (
                      <div className="flex flex-col items-center justify-center h-full">
                        <Upload className="w-6 h-6 text-gray-400 mb-1" />
                        <span className="text-xs text-gray-500">Hình {index + 1}</span>
                      </div>
                    )}
                  </label>
                </div>
              ))}
            </div>
            

          </div>

          {/* Status */}
          <div className="space-y-2">
            <Label htmlFor="status">Trạng thái</Label>
            <Select value={formData.status.toString()} onValueChange={(value) => handleInputChange('status', parseInt(value))}>
              <SelectTrigger>
                <SelectValue placeholder="Chọn trạng thái" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">Đang hoạt động</SelectItem>
                <SelectItem value="0">Ngừng hoạt động</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Form Actions */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              disabled={isLoading}
              className="bg-amber-600 hover:bg-amber-700 text-white"
            >
              {isLoading ? 'Đang tạo...' : 'Tạo sản phẩm'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AddProductModal;
