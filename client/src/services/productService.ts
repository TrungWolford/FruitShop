import axiosInstance from '../libs/axios';
import { API } from '../config/constants';
import type { CreateProductRequest } from '../types/product';
import { mockProducts, getProductById as mockGetProductById } from '@/apis/mockData';

export const productService = {
  // Lấy tất cả sản phẩm từ backend
  getAllProducts: async (page: number = 0, size: number = 10) => {
    try {
      const response = await axiosInstance.get(`${API.GET_ALL_PRODUCTS}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.warn('Backend không phản hồi, dùng mock data cho getAllProducts');
      // Fallback: phân trang mock data
      const start = page * size;
      const end = start + size;
      const paginatedProducts = mockProducts.filter((p: any) => p.status === 1).slice(start, end);
      return {
        content: paginatedProducts,
        totalElements: mockProducts.filter((p: any) => p.status === 1).length,
        totalPages: Math.ceil(mockProducts.filter((p: any) => p.status === 1).length / size),
        size,
        number: page,
        first: page === 0,
        last: end >= mockProducts.filter((p: any) => p.status === 1).length,
        empty: paginatedProducts.length === 0,
      };
    }
  },

  // Lấy sản phẩm theo ID
  getProductById: async (productId: string) => {
    try {
      const response = await axiosInstance.get(`${API.GET_PRODUCT_BY_ID(productId)}`);
      return {
        success: true,
        data: response.data,
        message: 'Product fetched successfully'
      };
    } catch (error: any) {
      // Fallback: tìm trong mock data
      const mockProduct = mockGetProductById(productId);
      if (mockProduct) {
        console.warn('⚠️ Backend không phản hồi, dùng mock data cho getProductById');
        return {
          success: true,
          data: mockProduct,
          message: 'Product fetched from mock data'
        };
      }
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || error.response?.data || error.message || 'Failed to fetch product'
      };
    }
  },

  // Tạo sản phẩm mới
  createProduct: async (productData: CreateProductRequest) => {
    try {
      const requestData = {
        productName: productData.productName,
        price: productData.price,
        stock: productData.stock,
        description: productData.description,
        status: productData.status,
        categoryIds: productData.categoryIds,
        images: productData.imageNames?.map((fileName, index) => ({
          imageUrl: fileName,
          imageOrder: index + 1
        })) || [],
      };
      const response = await axiosInstance.post(`${API.CREATE_PRODUCT}`, requestData, {
        headers: { 'Content-Type': 'application/json' },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Cập nhật sản phẩm
  updateProduct: async (productId: string, productData: Partial<CreateProductRequest>) => {
    try {
      const requestData = {
        productName: productData.productName,
        price: productData.price,
        stock: productData.stock,
        description: productData.description,
        status: productData.status,
        categoryIds: productData.categoryIds,
        ...(productData.imageNames && {
          images: productData.imageNames.map((fileName, index) => ({
            imageUrl: fileName,
            imageOrder: index + 1
          }))
        }),
      };
      const response = await axiosInstance.put(`${API.UPDATE_PRODUCT(productId)}`, requestData, {
        headers: { 'Content-Type': 'application/json' },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Xóa sản phẩm
  deleteProduct: async (productId: string) => {
    try {
      const response = await axiosInstance.delete(`${API.DELETE_PRODUCT(productId)}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Tìm kiếm sản phẩm
  searchProducts: async (
    keywords: string,
    page: number = 0,
    size: number = 10,
    minPrice?: number,
    maxPrice?: number,
    status?: number
  ) => {
    try {
      const params: any = { keywords, page, size };
      if (minPrice !== undefined) params.minPrice = minPrice;
      if (maxPrice !== undefined) params.maxPrice = maxPrice;
      if (status !== undefined) params.status = status;
      const response = await axiosInstance.get(`${API.SEARCH_PRODUCTS}`, { params });
      return response.data;
    } catch (error) {
      console.warn('⚠️ Backend không phản hồi, dùng mock data cho searchProducts');
      // Fallback: tìm kiếm trong mock data
      const keyword = keywords.toLowerCase();
      let filtered = mockProducts.filter((p: any) =>
        p.productName.toLowerCase().includes(keyword) ||
        p.description?.toLowerCase().includes(keyword)
      );
      if (status !== undefined) filtered = filtered.filter((p: any) => p.status === status);
      if (minPrice !== undefined) filtered = filtered.filter((p: any) => p.price >= minPrice);
      if (maxPrice !== undefined) filtered = filtered.filter((p: any) => p.price <= maxPrice);

      const start = page * size;
      const paginatedProducts = filtered.slice(start, start + size);
      return {
        content: paginatedProducts,
        totalElements: filtered.length,
        totalPages: Math.ceil(filtered.length / size),
        size,
        number: page,
        first: page === 0,
        last: start + size >= filtered.length,
        empty: paginatedProducts.length === 0,
      };
    }
  },

  // Lọc sản phẩm
  filterProducts: async (filters: {
    categoryId?: string;
    status?: number;
    minPrice?: number;
    maxPrice?: number;
    page?: number;
    size?: number;
  }) => {
    try {
      const response = await axiosInstance.get(`${API.FILTER_PRODUCTS}`, {
        params: filters
      });
      return response.data;
    } catch (error) {
      console.warn('⚠️ Backend không phản hồi, dùng mock data cho filterProducts');
      // Fallback: lọc trong mock data
      const { categoryId, status, minPrice, maxPrice, page = 0, size = 10 } = filters;
      let filtered = [...mockProducts] as any[];

      if (categoryId) filtered = filtered.filter((p: any) => p.categories?.some((c: any) => c.categoryId === categoryId));
      if (status !== undefined) filtered = filtered.filter((p: any) => p.status === status);
      if (minPrice !== undefined) filtered = filtered.filter((p: any) => p.price >= minPrice);
      if (maxPrice !== undefined) filtered = filtered.filter((p: any) => p.price <= maxPrice);

      const start = page * size;
      const paginatedProducts = filtered.slice(start, start + size);
      return {
        content: paginatedProducts,
        totalElements: filtered.length,
        totalPages: Math.ceil(filtered.length / size),
        size,
        number: page,
        first: page === 0,
        last: start + size >= filtered.length,
        empty: paginatedProducts.length === 0,
      };
    }
  }
};
