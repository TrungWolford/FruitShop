// src/components/CategoryMainPage.tsx
import React, { useState, useEffect } from 'react';
import { ChevronRight, Plus, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
// import { categoryService } from '../services/categoryService';
import { toast } from 'sonner';
import { categoryService } from '../services/categoryService';

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
      const response = await categoryService.getAllCategories(0, 100);
      console.log('Categories response:', response);
      if (response && response.content) {
        // filter active categories (status = 1)
        const activeCategories = response.content.filter(c => c.status === 1);
        setCategories(activeCategories);
      } else {
        setCategories([]);
      }
    } catch (error) {
      console.error('Error loading categories:', error);
      toast.error('Không thể tải danh mục sản phẩm');
      // fallback to empty list
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
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden h-fit sticky top-4">
        <div className="bg-primary text-white px-4 py-3">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-white/20 animate-pulse rounded"></div>
            <div className="h-4 bg-white/20 animate-pulse rounded w-32"></div>
          </div>
        </div>
        <div className="p-4 space-y-3">
          {Array.from({ length: 8 }).map((_, index) => (
            <div key={index} className="flex items-center justify-between">
              <div className="h-4 bg-gray-200 animate-pulse rounded w-28"></div>
              <div className="w-3 h-3 bg-gray-200 animate-pulse rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden h-fit sticky top-4">
      {/* Header */}
      <div className="bg-primary text-white px-4 py-3">
        <div className="flex items-center gap-2">
          <Menu className="w-4 h-4 text-white" />
          <h2 className="text-sm font-extrabold text-white">DANH MỤC SẢN PHẨM</h2>
        </div>
      </div>

      {/* Category List */}
      <div className="divide-y divide-gray-100">
        {categories.slice(0, 8).map((category) => (
          <div
            key={category.categoryId}
            onClick={() => handleCategoryClick(category.categoryName)}
            className="relative px-4 py-3 hover:bg-blue-50 cursor-pointer transition-all duration-200 group"
          >
            {/* Blue accent bar on hover */}
            <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary opacity-0 group-hover:opacity-100 transition-opacity duration-200"></div>
            
            <div className="flex items-center justify-between">
              <span className="text-gray-700 text-sm font-medium group-hover:text-primary transition-colors duration-200 line-clamp-1">
                {category.categoryName}
              </span>
              <ChevronRight className="w-4 h-4 text-gray-400 group-hover:text-primary group-hover:translate-x-0.5 transition-all duration-200 flex-shrink-0 ml-2" />
            </div>
          </div>
        ))}
      </div>

      {/* See More Footer */}
      {categories.length > 12 && (
        <div className="bg-gray-50 border-t border-gray-100">
          <div 
            className="px-4 py-3 flex items-center gap-2 text-primary hover:text-primary-dark cursor-pointer transition-colors duration-200"
            onClick={() => navigate('/categories')}
          >
            <Plus className="w-4 h-4" />
            <span className="text-sm font-semibold">Xem tất cả danh mục</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default CategoryMainPage;