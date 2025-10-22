import axiosInstance from '../libs/axios';
import { API } from '../config/constants';
import type { CreateProductRequest } from '../types/product';

export const productService = {
  // Lấy tất cả sản phẩm từ backend
  getAllProducts: async (page: number = 0, size: number = 10) => {
    try {
      const response = await axiosInstance.get(`${API.GET_ALL_PRODUCTS}`, {
        params: {
          page,
          size
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching products:', error);
      throw error;
    }
  },

  // Lấy sản phẩm theo ID
  getProductById: async (productId: string) => {
    try {
      console.log('🔍 Fetching product with ID:', productId);
      console.log('📡 API URL:', `${API.GET_PRODUCT_BY_ID(productId)}`);
      
      const response = await axiosInstance.get(`${API.GET_PRODUCT_BY_ID(productId)}`);
      
      console.log('✅ Product fetched successfully:', response.data);
      
      return {
        success: true,
        data: response.data,
        message: 'Product fetched successfully'
      };
    } catch (error: any) {
      console.error('❌ Error fetching product:', error);
      console.error('📋 Error details:', {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: error.config?.url,
      });
      
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
      // Prepare JSON data for API
      const requestData = {
        productName: productData.productName,
        price: productData.price,
        stock: productData.stock,
        description: productData.description,
        status: productData.status,
        categoryIds: productData.categoryIds,
        // Convert imageNames to proper format for backend
        images: productData.imageNames?.map((fileName, index) => ({
          imageUrl: fileName,
          imageOrder: index + 1
        })) || [], // Convert imageNames to proper format
      };

      console.log('🚀 Sending product data to API:', {
        ...requestData,
        imageCount: productData.imageNames?.length || 0,
        imageNames: productData.imageNames || [] // For logging clarity
      });

      const response = await axiosInstance.post(`${API.CREATE_PRODUCT}`, requestData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error creating product:', error);
      throw error;
    }
  },

  // Cập nhật sản phẩm
  updateProduct: async (productId: string, productData: Partial<CreateProductRequest>) => {
    try {
      // Prepare JSON data for API - same format as createProduct
      const requestData = {
        productName: productData.productName,
        price: productData.price,
        stock: productData.stock,
        description: productData.description,
        status: productData.status,
        categoryIds: productData.categoryIds,
        // Only include images if imageNames is provided (meaning images changed)
        ...(productData.imageNames && {
          images: productData.imageNames.map((fileName, index) => ({
            imageUrl: fileName,
            imageOrder: index + 1
          }))
        }),
      };

      console.log('🚀 Updating product with data:', {
        productId,
        ...requestData,
        imageCount: productData.imageNames?.length || 0,
        imageNames: productData.imageNames || []
      });

      const response = await axiosInstance.put(`${API.UPDATE_PRODUCT(productId)}`, requestData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error updating product:', error);
      throw error;
    }
  },

  // Xóa sản phẩm
  deleteProduct: async (productId: string) => {
    try {
      const response = await axiosInstance.delete(`${API.DELETE_PRODUCT(productId)}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting product:', error);
      throw error;
    }
  },

  // Tìm kiếm sản phẩm
  searchProducts: async (
    keywords: string, 
    page: number = 0, 
    size: number = 10,
    minPrice?: number,
    maxPrice?: number
  ) => {
    try {
      const params: any = {
        keywords,
        page,
        size
      };
      
      if (minPrice !== undefined) params.minPrice = minPrice;
      if (maxPrice !== undefined) params.maxPrice = maxPrice;
      
      console.log('📡 productService.searchProducts called with:', params);
      console.log('📡 API endpoint:', API.SEARCH_PRODUCTS);
      
      const response = await axiosInstance.get(`${API.SEARCH_PRODUCTS}`, { params });
      
      console.log('✅ Search API response:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ Error searching products:', error);
      throw error;
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
      console.error('Error filtering products:', error);
      throw error;
    }
  }
};
