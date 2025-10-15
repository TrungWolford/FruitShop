// Category interfaces theo backend CategoryController và DTOs

export interface Category {
  categoryId: string;
  categoryName: string;
  status: number; // 0 = INACTIVE, 1 = ACTIVE
  description?: string; // Thêm field này cho UI (có thể bỏ qua khi gửi API)
}

export interface CreateCategoryRequest {
  categoryName: string;
  status?: number; // Default = 1 trong backend
}

export interface UpdateCategoryRequest {
  categoryName?: string;
  status?: number;
}

export interface CategoryResponse {
  categoryId: string;
  categoryName: string;
  status: number;
}

export interface PaginatedCategoryResponse {
  content: Category[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export type CategoryStatus = 0 | 1; // 0 = INACTIVE, 1 = ACTIVE
