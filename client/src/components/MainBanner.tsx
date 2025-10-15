import React from 'react';

const MainBanner: React.FC = () => {
  return (
    <div className="w-full">
      {/* Features Bar */}
      <div className="w-full bg-white py-4 px-6 border-b border-gray-200">
        <div className="flex items-center justify-start space-x-12">
          <div className="flex items-center space-x-2">
            <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center">
              <span className="text-blue-600 text-sm">🏆</span>
            </div>
            <span className="text-gray-700 text-sm font-medium">Đảm bảo sách thật</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <span className="text-green-600 text-sm">✓</span>
            </div>
            <span className="text-gray-700 text-sm font-medium">Vận chuyển nhanh chóng</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-6 h-6 bg-orange-100 rounded-full flex items-center justify-center">
              <span className="text-orange-600 text-sm">🚚</span>
            </div>
            <span className="text-gray-700 text-sm font-medium">Mở hộp kiểm tra nhận hàng</span>
          </div>
        </div>
      </div>

      {/* Banner Section */}
      <div className="w-full h-auto overflow-hidden relative">
        {/* Layout 7:3 */}
        <div className="grid grid-cols-10 h-full">
          {/* Left section - 7 columns - Banner 1 */}
          <div className="col-span-7 relative">
            <img
              src="/banner1.jpg"
              alt="Banner 1"
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.style.display = 'none';
              }}
            />
          </div>
          
          {/* Right section - 3 columns - Banner 2,3,4 */}
          <div className="col-span-3 flex flex-col space-y-1">
            {/* Banner 2 */}
            <div className="flex-1 relative">
              <img
                src="/banner2.jpg"
                alt="Banner 2"
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
            </div>
            
            {/* Banner 3 */}
            <div className="flex-1 relative">
              <img
                src="/banner3.jpg"
                alt="Banner 3"
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
            </div>
            
            {/* Banner 4 */}
            <div className="flex-1 relative">
              <img
                src="/banner4.jpg"
                alt="Banner 4"
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MainBanner;
