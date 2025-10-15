import React, { useState, useEffect } from 'react';
import { ChevronRight, Plus, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { categoryService } from '../services/categoryService';
import { toast } from 'sonner';

interface Category {
  categoryId: string;
  categoryName: string;
  status: number;
}

const CategoryMainPage: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const response = await categoryService.getAllCategories();
      console.log('Categories response:', response);
      
      if (response && response.content) {
        setCategories(response.content);
      } else {
        setCategories([]);
      }
    } catch (error) {
      console.error('Error loading categories:', error);
      toast.error('Không thể tải danh mục sản phẩm');
      setCategories([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryClick = (categoryName: string) => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    navigate(`/product/collection/${encodeURIComponent(categoryName)}`);
  };

  if (loading) {
    return (
      <div className="bg-white shadow-sm border border-gray-200 p-4">
        <div className="flex items-center gap-2 mb-4">
          <div className="w-4 h-4 bg-gray-200 animate-pulse rounded"></div>
          <div className="h-4 bg-gray-200 animate-pulse rounded w-28"></div>
        </div>
        <div className="space-y-2">
          {Array.from({ length: 8 }).map((_, index) => (
            <div key={index} className="flex items-center justify-between">
              <div className="h-4 bg-gray-200 animate-pulse rounded w-24"></div>
              <div className="w-3 h-3 bg-gray-200 animate-pulse rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-sm border border-gray-200 p-4">
                {/* Header */}
          <div className="flex items-center gap-2 mb-4">
            <Menu className="w-4 h-4 text-gray-600" />
            <h2 className="text-sm font-extrabold text-gray-900">DANH MỤC SẢN PHẨM</h2>
          </div>

          {/* Category List */}
          <div className="space-y-2">
            {categories.slice(0, 10).map((category) => (
              <div
                key={category.categoryId}
                onClick={() => handleCategoryClick(category.categoryName)}
                className="flex items-center justify-between py-2 px-2 hover:bg-blue-50 hover:text-blue-600 rounded cursor-pointer transition-all duration-200 group relative"
              >
                {/* Blue bar on hover */}
                <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-600 opacity-0 group-hover:opacity-100 transition-opacity duration-200"></div>
                <span className="text-gray-700 text-sm font-semibold group-hover:text-blue-600 transition-colors">{category.categoryName}</span>
                <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-blue-500 group-hover:translate-x-0.5 transition-all duration-200" />
              </div>
            ))}
          </div>

                {/* See More */}
          {categories.length > 10 && (
            <div className="mt-4 pt-2 border-t border-gray-100">
              <div className="flex items-center gap-2 text-blue-600 hover:text-blue-700 cursor-pointer">
                <Plus className="w-4 h-4" />
                <span className="text-sm font-bold">Xem thêm</span>
              </div>
            </div>
          )}
    </div>
  );
};

export default CategoryMainPage;
