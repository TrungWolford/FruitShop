import axiosInstance from '../libs/axios';
import { API } from '../config/constants';
import type { CreateProductRequest } from '../types/product';

export const productService = {
  // Lấy tất cả sản phẩm từ backend
  getAllProducts: async (page: number = 0, size: number = 10) => {
    const response = await axiosInstance.get(`${API.GET_ALL_PRODUCTS}`, {
      params: { page, size }
    });
    return response.data;
  },

  // Lấy sản phẩm theo ID
  getProductById: async (productId: string) => {
    const response = await axiosInstance.get(`${API.GET_PRODUCT_BY_ID(productId)}`);
    return {
      success: true,
      data: response.data,
      message: 'Product fetched successfully'
    };
  },

  // Tạo sản phẩm mới
  createProduct: async (productData: CreateProductRequest) => {
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
  },

  // Cập nhật sản phẩm
  updateProduct: async (productId: string, productData: Partial<CreateProductRequest>) => {
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
  },

  // Xóa sản phẩm
  deleteProduct: async (productId: string) => {
    const response = await axiosInstance.delete(`${API.DELETE_PRODUCT(productId)}`);
    return response.data;
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
    const params: any = { keywords, page, size };
    if (minPrice !== undefined) params.minPrice = minPrice;
    if (maxPrice !== undefined) params.maxPrice = maxPrice;
    if (status !== undefined) params.status = status;
    const response = await axiosInstance.get(`${API.SEARCH_PRODUCTS}`, { params });
    return response.data;
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
    const response = await axiosInstance.get(`${API.FILTER_PRODUCTS}`, {
      params: filters
    });
    return response.data;
  }
};
