import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { Search, ChevronRight, Menu, ChevronLeft } from 'lucide-react';
import type { Product, Category } from '../../types/product';
import { toast } from 'sonner';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { categoryService } from '../../services/categoryService';
import { productService } from '../../services/productService';
import ProductItem from '../../components/Product/ProductItem';

// Mock data for products
const mockProducts: Product[] = [
  {
    productId: '1',
    productName: 'Đắc Nhân Tâm',
    categories: [{ categoryId: '1', categoryName: 'Sách Kỹ Năng', status: 1, description: 'Sách về kỹ năng sống và phát triển bản thân' }],
    images: [{ id: 1, imageUrl: '/products/dac-nhan-tam.jpg', imageOrder: 1 }],
    price: 89000,
    stock: 50,
    description: 'Cuốn sách kinh điển về nghệ thuật giao tiếp và thuyết phục',
    createdAt: '2024-01-15',
    status: 1
  },
  {
    productId: '2',
    productName: 'Tư Duy Nhanh Và Chậm',
    categories: [{ categoryId: '2', categoryName: 'Sách Tâm Lý', status: 1, description: 'Sách về tâm lý học và hành vi con người' }],
    images: [{ id: 2, imageUrl: '/products/tu-duy-nhanh-va-cham.jpg', imageOrder: 1 }],
    price: 199000,
    stock: 30,
    description: 'Khám phá cách bộ não con người đưa ra quyết định',
    createdAt: '2024-01-20',
    status: 1
  },
  {
    productId: '3',
    productName: 'Sapiens',
    categories: [{ categoryId: '3', categoryName: 'Sách Lịch Sử', status: 1, description: 'Sách về lịch sử và văn hóa' }],
    images: [{ id: 3, imageUrl: '/products/sapiens.jpg', imageOrder: 1 }],
    price: 249000,
    stock: 25,
    description: 'Lược sử loài người từ thời tiền sử đến hiện tại',
    createdAt: '2024-01-25',
    status: 1
  },
  {
    productId: '4',
    productName: 'Atomic Habits',
    categories: [{ categoryId: '1', categoryName: 'Sách Kỹ Năng', status: 1, description: 'Sách về kỹ năng sống và phát triển bản thân' }],
    images: [{ id: 4, imageUrl: '/products/atomic-habits.jpg', imageOrder: 1 }],
    price: 179000,
    stock: 40,
    description: 'Xây dựng thói quen tốt và phá vỡ thói quen xấu',
    createdAt: '2024-02-01',
    status: 1
  },
  {
    productId: '5',
    productName: 'Thinking, Fast and Slow',
    categories: [{ categoryId: '2', categoryName: 'Sách Tâm Lý', status: 1, description: 'Sách về tâm lý học và hành vi con người' }],
    images: [{ id: 5, imageUrl: '/products/thinking-fast-slow.jpg', imageOrder: 1 }],
    price: 219000,
    stock: 20,
    description: 'Bản tiếng Anh của cuốn Tư Duy Nhanh Và Chậm',
    createdAt: '2024-02-05',
    status: 1
  },
  {
    productId: '6',
    productName: 'Lịch Sử Việt Nam',
    categories: [{ categoryId: '3', categoryName: 'Sách Lịch Sử', status: 1, description: 'Sách về lịch sử và văn hóa' }],
    images: [{ id: 6, imageUrl: '/products/lich-su-viet-nam.jpg', imageOrder: 1 }],
    price: 299000,
    stock: 15,
    description: 'Toàn bộ lịch sử Việt Nam từ thời dựng nước đến nay',
    createdAt: '2024-02-10',
    status: 1
  }
];


const ProductPage: React.FC = () => {
  const { categoryName } = useParams<{ categoryName: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const searchQuery = searchParams.get('q'); // Lấy search query từ URL
  
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Pagination states
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 12; // 12 sản phẩm mỗi trang
  
  // Price filter states
  const [selectedPriceRange, setSelectedPriceRange] = useState<string | null>(null);
  const priceRanges = [
    { label: 'Dưới 200k', value: 'under200', minPrice: 0, maxPrice: 200000 },
    { label: '200k - 600k', value: '200to600', minPrice: 200000, maxPrice: 600000 },
    { label: 'Trên 600k', value: 'over600', minPrice: 600000, maxPrice: 999999999 },
  ];

  useEffect(() => {
    loadCategories();
    // Scroll to top when component mounts
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  useEffect(() => {
    if (categories.length > 0) {
      loadProducts();
    }
    // Scroll to top when category or search query changes
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [categoryName, categories, currentPage, searchQuery, selectedPriceRange]); // Add selectedPriceRange dependency

  useEffect(() => {
    // Luôn filter để chỉ hiển thị sản phẩm có status = 1
    const activeProducts = products.filter(p => p.status === 1);
    setFilteredProducts(activeProducts);
  }, [products]);

  const loadCategories = async () => {
    try {
      const response = await categoryService.getAllCategories();
      if (response && response.content) {
        // Map the API response to match our Category type
        const mappedCategories: Category[] = response.content.map(cat => ({
          categoryId: cat.categoryId,
          categoryName: cat.categoryName,
          status: cat.status,
          description: ''
        }));
        setCategories(mappedCategories);
      }
    } catch (error) {
      toast.error('Không thể tải danh mục sản phẩm');
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      
      let response;
      
      // Get price range values if selected
      const priceRange = selectedPriceRange 
        ? priceRanges.find(range => range.value === selectedPriceRange)
        : null;
      
      if (searchQuery) {
        // Search mode - gọi search API với minPrice, maxPrice nếu có
        if (selectedPriceRange && priceRange) {
          // Gọi search API với minPrice và maxPrice
          response = await productService.searchProducts(
            searchQuery, 
            currentPage, 
            pageSize,
            priceRange.minPrice,
            priceRange.maxPrice,
            1
          );
        } else {
          // Không có price filter: gọi search API với status = 1
          response = await productService.searchProducts(searchQuery, currentPage, pageSize, undefined, undefined, 1);
        }
      } else if (categoryName) {
        // Find categoryId from categories list
        const decodedCategoryName = decodeURIComponent(categoryName);
        const selectedCategory = categories.find(cat => cat.categoryName === decodedCategoryName);
        
        if (selectedCategory) {
          // Use API to filter products by category (với price filter nếu có)
          response = await productService.filterProducts({
            categoryId: selectedCategory.categoryId,
            status: 1,
            minPrice: priceRange?.minPrice || 0,
            maxPrice: priceRange?.maxPrice || 999999999,
            page: currentPage,
            size: pageSize
          });
        } else {
          // Fallback to mock data if category not found
          response = { 
            content: mockProducts.filter(product => 
              product.categories.some(cat => cat.categoryName === decodedCategoryName)
            ),
            totalPages: 1,
            totalElements: mockProducts.length
          };
        }
      } else {
        // Load all products if no category selected (luôn filter status = 1 để chỉ hiển thị sản phẩm đang hoạt động)
        response = await productService.filterProducts({
          status: 1,
          minPrice: priceRange?.minPrice,
          maxPrice: priceRange?.maxPrice,
          page: currentPage,
          size: pageSize
        });
      }
      
      setProducts(response.content || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
    } catch (error) {
      toast.error('Không thể tải danh sách sản phẩm');
      
      // Fallback to mock data
      let filteredData = [...mockProducts];
      if (categoryName) {
        const decodedCategoryName = decodeURIComponent(categoryName);
        filteredData = mockProducts.filter(product => 
          product.categories.some(cat => cat.categoryName === decodedCategoryName)
        );
      }
      setProducts(filteredData);
      setTotalPages(1);
      setTotalElements(filteredData.length);
    } finally {
      setLoading(false);
    }
  };

  const handlePriceRangeChange = (rangeValue: string) => {
    if (selectedPriceRange === rangeValue) {
      // Nếu click vào range đang chọn → bỏ chọn
      setSelectedPriceRange(null);
    } else {
      setSelectedPriceRange(rangeValue);
    }
    setCurrentPage(0); // Reset về trang đầu khi đổi filter
  };


  const handleCategoryClick = (categoryName: string) => {
    // Find category by name to get the ID
    const selectedCategory = categories.find(cat => cat.categoryName === categoryName);
    if (selectedCategory) {
    }
    setCurrentPage(0); // Reset to first page when changing category
    window.scrollTo({ top: 0, behavior: 'smooth' });
    navigate(`/product/collection/${encodeURIComponent(categoryName)}`);
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages: (number | string)[] = [];
    const maxPagesToShow = 5;
    
    if (totalPages <= maxPagesToShow) {
      // Show all pages if total pages is less than max
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show first page
      pages.push(0);
      
      // Calculate range around current page
      let startPage = Math.max(1, currentPage - 1);
      let endPage = Math.min(totalPages - 2, currentPage + 1);
      
      // Adjust if we're near the start
      if (currentPage <= 2) {
        endPage = 3;
      }
      
      // Adjust if we're near the end
      if (currentPage >= totalPages - 3) {
        startPage = totalPages - 4;
      }
      
      // Add ellipsis if needed
      if (startPage > 1) {
        pages.push('...');
      }
      
      // Add middle pages
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
      
      // Add ellipsis if needed
      if (endPage < totalPages - 2) {
        pages.push('...');
      }
      
      // Show last page
      pages.push(totalPages - 1);
    }
    
    return pages;
  };

  const handleAddToCart = (product: Product) => {
    // TODO: Implement add to cart functionality
    toast.success(`Đã thêm "${product.productName}" vào giỏ hàng`);
  };

  const handleAddToWishlist = (product: Product) => {
    // TODO: Implement add to wishlist functionality
    toast.success(`Đã thêm "${product.productName}" vào danh sách yêu thích`);
  };


  if (loading) {
    return (
      <div className="min-h-screen bg-[#f2f3f5]">
        <TopNavigation />
        <main className="max-w-8xl mx-auto px-16 pt-0 pb-8">
          <div className="grid grid-cols-10 gap-6">
            {/* Left sidebar - 2.5 columns */}
            <div className="col-span-2">
              <div className="bg-white shadow-sm border border-gray-200 p-4">
                <div className="animate-pulse">
                  <div className="h-4 bg-gray-200 rounded w-28 mb-4"></div>
                  <div className="space-y-2">
                    {Array.from({ length: 8 }).map((_, index) => (
                      <div key={index} className="h-4 bg-gray-200 rounded w-24"></div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
            
            {/* Main content - 7.5 columns */}
            <div className="col-span-8">
              <div className="animate-pulse">
                <div className="h-8 bg-gray-200 rounded w-1/3 mb-6"></div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {Array.from({ length: 8 }).map((_, index) => (
                    <div key={index} className="bg-white rounded-lg shadow-sm p-4">
                      <div className="h-48 bg-gray-200 rounded mb-4"></div>
                      <div className="h-4 bg-gray-200 rounded mb-2"></div>
                      <div className="h-4 bg-gray-200 rounded w-2/3 mb-2"></div>
                      <div className="h-6 bg-gray-200 rounded w-1/2"></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#f2f3f5]">
      <TopNavigation />
      
      <main className="max-w-8xl mx-auto px-16 pt-0 pb-8">
        {/* Main content with 2.5:7.5 layout */}
        <div className="grid grid-cols-10 gap-6">
          {/* Left sidebar - 2.5 columns */}
          <div className="col-span-2">
            <div className="bg-white shadow-sm border border-gray-200 p-4">
              {/* Header */}
              <div className="flex items-center gap-2 mb-4">
                <Menu className="w-4 h-4 text-gray-600" />
                <h2 className="text-sm font-extrabold text-gray-900">DANH MỤC SẢN PHẨM</h2>
              </div>

              {/* Category List */}
              <div className="space-y-2">
                {categories.filter(category => category.status === 1).map((category) => (
                  <div
                    key={category.categoryId}
                    onClick={() => handleCategoryClick(category.categoryName)}
                    className={`flex items-center justify-between py-2 px-2 hover:bg-blue-50 hover:text-blue-600 rounded cursor-pointer transition-all duration-200 group relative ${
                      categoryName && decodeURIComponent(categoryName) === category.categoryName 
                        ? 'bg-blue-50 text-blue-600' 
                        : ''
                    }`}
                  >
                    {/* Blue bar on hover or active */}
                    <div className={`absolute left-0 top-0 bottom-0 w-1 bg-blue-600 transition-opacity duration-200 ${
                      categoryName && decodeURIComponent(categoryName) === category.categoryName 
                        ? 'opacity-100' 
                        : 'opacity-0 group-hover:opacity-100'
                    }`}></div>
                    <span className={`text-sm font-semibold transition-colors ${
                      categoryName && decodeURIComponent(categoryName) === category.categoryName 
                        ? 'text-blue-600' 
                        : 'text-gray-700 group-hover:text-blue-600'
                    }`}>
                      {category.categoryName}
                    </span>
                    <ChevronRight className={`w-4 h-4 transition-all duration-200 ${
                      categoryName && decodeURIComponent(categoryName) === category.categoryName 
                        ? 'text-blue-500' 
                        : 'text-gray-400 group-hover:text-blue-500 group-hover:translate-x-0.5'
                    }`} />
                  </div>
                ))}
              </div>
            </div>
            
            {/* Price Filter - Show when searching or viewing category */}
            {(searchQuery || categoryName) && (
              <div className="bg-white shadow-sm border border-gray-200 p-4 mt-4">
                <div className="flex items-center gap-2 mb-4">
                  <h2 className="text-sm font-extrabold text-gray-900">LỌC THEO GIÁ</h2>
                </div>
                
                <div className="space-y-2">
                  {priceRanges.map((range) => (
                    <div
                      key={range.value}
                      onClick={() => handlePriceRangeChange(range.value)}
                      className={`py-2 px-3 rounded cursor-pointer transition-all duration-200 ${
                        selectedPriceRange === range.value
                          ? 'bg-blue-50 border border-blue-500 text-blue-600'
                          : 'bg-gray-50 hover:bg-gray-100 border border-gray-200'
                      }`}
                    >
                      <div className="flex items-center gap-2">
                        <div className={`w-4 h-4 rounded border-2 flex items-center justify-center ${
                          selectedPriceRange === range.value
                            ? 'border-blue-500 bg-blue-500'
                            : 'border-gray-300'
                        }`}>
                          {selectedPriceRange === range.value && (
                            <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 12 12">
                              <path d="M10 3L4.5 8.5L2 6" stroke="currentColor" strokeWidth="2" fill="none" />
                            </svg>
                          )}
                        </div>
                        <span className={`text-sm font-medium ${
                          selectedPriceRange === range.value ? 'text-blue-600' : 'text-gray-700'
                        }`}>
                          {range.label}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
                
                {/* Clear filter button */}
                {selectedPriceRange && (
                  <button
                    onClick={() => {
                      setSelectedPriceRange(null);
                      setCurrentPage(0);
                    }}
                    className="mt-3 w-full py-2 px-3 text-sm text-gray-600 hover:text-gray-800 bg-gray-100 hover:bg-gray-200 rounded transition-colors"
                  >
                    Xóa bộ lọc
                  </button>
                )}
              </div>
            )}
          </div>
          
          {/* Main content - 7.5 columns */}
          <div className="col-span-8">
            {/* Header */}
            <div className="mb-6 mt-6">
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                {searchQuery 
                  ? `Kết quả tìm kiếm: "${searchQuery}"`
                  : categoryName 
                    ? decodeURIComponent(categoryName) 
                    : 'Tất Cả Sản Phẩm'
                }
              </h1>
              <p className="text-gray-600">
                {totalElements} sản phẩm được tìm thấy
              </p>
            </div>


            {/* Products Grid/List */}
            {filteredProducts.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-gray-400 mb-4">
                  <Search className="w-16 h-16 mx-auto" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Không tìm thấy sản phẩm
                </h3>
                <p className="text-gray-600">
                  Không có sản phẩm nào trong danh mục này
                </p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {filteredProducts.map((product) => (
                    <ProductItem
                      key={product.productId}
                      product={{
                        productId: product.productId,
                        productName: product.productName,
                        price: product.price,
                        images: product.images,
                        category: product.categories[0]?.categoryName,
                        categoryCount: product.categories.length,
                        stock: product.stock, // Thêm stock để kiểm tra tồn kho
                        rating: 4.5
                      }}
                      onAddToCart={() => handleAddToCart(product)}
                      onAddToWishlist={() => handleAddToWishlist(product)}
                    />
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="flex justify-center items-center gap-2 mt-8">
                    {/* Previous Button */}
                    <button
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={currentPage === 0}
                      className={`flex items-center gap-1 px-4 py-2 rounded-lg font-medium transition-colors ${
                        currentPage === 0
                          ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                          : 'bg-white text-gray-700 hover:bg-blue-50 hover:text-blue-600 border border-gray-200'
                      }`}
                    >
                      <ChevronLeft className="w-4 h-4" />
                      <span>Trước</span>
                    </button>

                    {/* Page Numbers */}
                    {getPageNumbers().map((page, index) => (
                      <React.Fragment key={index}>
                        {page === '...' ? (
                          <span className="px-3 py-2 text-gray-400">...</span>
                        ) : (
                          <button
                            onClick={() => handlePageChange(page as number)}
                            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                              currentPage === page
                                ? 'bg-blue-600 text-white shadow-md'
                                : 'bg-white text-gray-700 hover:bg-blue-50 hover:text-blue-600 border border-gray-200'
                            }`}
                          >
                            {(page as number) + 1}
                          </button>
                        )}
                      </React.Fragment>
                    ))}

                    {/* Next Button */}
                    <button
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={currentPage === totalPages - 1}
                      className={`flex items-center gap-1 px-4 py-2 rounded-lg font-medium transition-colors ${
                        currentPage === totalPages - 1
                          ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                          : 'bg-white text-gray-700 hover:bg-blue-50 hover:text-blue-600 border border-gray-200'
                      }`}
                    >
                      <span>Sau</span>
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </main>
      
      <Footer />
    </div>
  );
};

export default ProductPage;