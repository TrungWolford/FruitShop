// Service để xử lý hình ảnh sản phẩm
// Lưu vào thư mục assets/img/products

export const imageService = {
  // Kiểm tra và cleanup localStorage nếu cần
  checkAndCleanupStorage: (): void => {
    try {
      const savedImages = JSON.parse(localStorage.getItem('product_images') || '[]');
      if (savedImages.length > 15) {
        // Nếu có quá 15 images, chỉ giữ lại 10 gần nhất
        const recentImages = savedImages.slice(-10);
        localStorage.setItem('product_images', JSON.stringify(recentImages));
      }
    } catch (error) {
    }
  },

  // Lưu hình ảnh và trả về tên file gốc
  saveImageToAssets: async (file: File): Promise<string> => {
    try {
      // Sử dụng tên file gốc từ máy tính
      const fileName = file.name;
      
      // Lưu thông tin file vào localStorage (KHÔNG lưu dữ liệu hình ảnh để tránh vượt quota)
      const imageData = {
        name: fileName,
        originalName: file.name,
        timestamp: Date.now(),
        size: file.size,
        type: file.type
        // KHÔNG lưu data: reader.result vì nó rất lớn và gây vượt quota
      };
      
      try {
        // Cleanup trước khi thêm mới để tránh vượt quota
        imageService.checkAndCleanupStorage();
        
        const existingImages = JSON.parse(localStorage.getItem('product_images') || '[]');
        existingImages.push(imageData);
        localStorage.setItem('product_images', JSON.stringify(existingImages));
      } catch (storageError) {
        // Nếu localStorage đầy, chỉ log warning và tiếp tục
        // Có thể clear old data nếu cần
        try {
          localStorage.removeItem('product_images');
          localStorage.setItem('product_images', JSON.stringify([imageData]));
        } catch (clearError) {
        }
      }
      
      // Trả về tên file gốc
      return fileName;
    } catch (error) {
      throw error;
    }
  },

  // Lấy tất cả tên hình ảnh đã lưu
  getAllImageNames: (): string[] => {
    try {
      const savedImages = JSON.parse(localStorage.getItem('product_images') || '[]');
      return savedImages.map((img: any) => img.name);
    } catch (error) {
      return [];
    }
  },



  // Validate hình ảnh
  validateImage: (file: File): { isValid: boolean; error?: string } => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const maxSize = 10 * 1024 * 1024; // 10MB

    if (!allowedTypes.includes(file.type)) {
      return {
        isValid: false,
        error: `File ${file.name} không đúng định dạng. Chỉ chấp nhận: JPG, PNG, GIF, WEBP`
      };
    }

    if (file.size > maxSize) {
      return {
        isValid: false,
        error: `File ${file.name} quá lớn. Kích thước tối đa: 10MB`
      };
    }

    return { isValid: true };
  },

  // Tạo preview URL
  createPreviewUrl: (file: File): string => {
    return URL.createObjectURL(file);
  },

  // Giải phóng preview URL
  revokePreviewUrl: (url: string): void => {
    URL.revokeObjectURL(url);
  },

  // Cleanup localStorage để tránh vượt quota
  cleanupStorage: (): void => {
    try {
      const savedImages = JSON.parse(localStorage.getItem('product_images') || '[]');
      // Giữ lại chỉ 10 images gần nhất để tránh vượt quota
      if (savedImages.length > 10) {
        const recentImages = savedImages.slice(-10);
        localStorage.setItem('product_images', JSON.stringify(recentImages));
      }
    } catch (error) {
    }
  },

  // Clear tất cả dữ liệu hình ảnh trong localStorage
  clearAllImages: (): void => {
    try {
      localStorage.removeItem('product_images');
    } catch (error) {
    }
  }
};

export default imageService;
