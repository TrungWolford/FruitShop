export interface Category {
  description: any;
  categoryId: string;
  categoryName: string;
  status: number; // 0: Hủy, 1: Đang hoạt động
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  imageOrder: number;
}

export interface Product {
  productId: string;
  productName: string;
  categories: Category[];
  images: ProductImage[]; // Array of image objects from backend
  price: number;
  stock: number;
  description: string;
  createdAt: string; // Changed to string to match API response
  status: number; // 0: Ngừng hoạt động, 1: Đang hoạt động
}

// Interface for creating a new product
export interface CreateProductRequest {
  productName: string;
  categoryIds: string[]; // Array of category IDs
  price: number;
  stock: number;
  description: string;
  status: number;
  images?: File[]; // Optional image files - will be handled separately
  imagePaths?: string[]; // Optional image paths from assets
  imageNames?: string[]; // Optional image filenames to save in database
}

// Interface for product image request (matches backend CreateProductImageRequest)
export interface ProductImageRequest {
  imageUrl: string;
  imageOrder: number;
}

// Interface for product form data
export interface ProductFormData {
  productName: string;
  selectedCategories: string[];
  price: string;
  stock: string;
  description: string;
  status: number;
  images: File[];
}

export type ProductStatus = 0 | 1;
export type CategoryStatus = 0 | 1;
