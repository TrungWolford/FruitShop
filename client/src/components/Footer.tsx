import React from 'react'
import { Phone, Mail, MapPin, Facebook, Twitter, Instagram, Youtube, CreditCard, Truck, Shield, RotateCcw } from 'lucide-react'

const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-800 text-white">
      {/* Services Section */}
      <div className="bg-slate-700 py-8">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="flex items-center space-x-3">
              <div className="bg-amber-400 p-3 rounded-full">
                <Truck className="h-6 w-6 text-slate-800" />
              </div>
              <div>
                <h4 className="font-semibold">Miễn phí vận chuyển</h4>
                <p className="text-sm text-gray-300">Đơn hàng từ 200.000đ</p>
              </div>
            </div>
            
            <div className="flex items-center space-x-3">
              <div className="bg-amber-400 p-3 rounded-full">
                <Shield className="h-6 w-6 text-slate-800" />
              </div>
              <div>
                <h4 className="font-semibold">Thanh toán an toàn</h4>
                <p className="text-sm text-gray-300">Bảo mật 100%</p>
              </div>
            </div>
            
            <div className="flex items-center space-x-3">
              <div className="bg-amber-400 p-3 rounded-full">
                <RotateCcw className="h-6 w-6 text-slate-800" />
              </div>
              <div>
                <h4 className="font-semibold">Đổi trả dễ dàng</h4>
                <p className="text-sm text-gray-300">Trong vòng 30 ngày</p>
              </div>
            </div>
            
            <div className="flex items-center space-x-3">
              <div className="bg-amber-400 p-3 rounded-full">
                <Phone className="h-6 w-6 text-slate-800" />
              </div>
              <div>
                <h4 className="font-semibold">Hỗ trợ 24/7</h4>
                <p className="text-sm text-gray-300">Luôn sẵn sàng phục vụ</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Content */}
      <div className="py-12">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {/* Company Info */}
            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <div className="bg-amber-400 text-slate-800 font-bold px-3 py-2 rounded">
                  BC
                </div>
                <span className="text-xl font-bold">BOOKCITY.VN</span>
              </div>
              <p className="text-gray-300 text-sm leading-relaxed">
                Hệ thống nhà sách trực tuyến hàng đầu Việt Nam với hàng triệu đầu sách đa dạng, 
                phục vụ nhu cầu đọc sách của mọi lứa tuổi.
              </p>
              <div className="flex space-x-3">
                <a href="#" className="bg-blue-600 p-2 rounded-full hover:bg-blue-700 transition-colors">
                  <Facebook className="h-4 w-4" />
                </a>
                <a href="#" className="bg-blue-400 p-2 rounded-full hover:bg-blue-500 transition-colors">
                  <Twitter className="h-4 w-4" />
                </a>
                <a href="#" className="bg-pink-600 p-2 rounded-full hover:bg-pink-700 transition-colors">
                  <Instagram className="h-4 w-4" />
                </a>
                <a href="#" className="bg-red-600 p-2 rounded-full hover:bg-red-700 transition-colors">
                  <Youtube className="h-4 w-4" />
                </a>
              </div>
            </div>

            {/* Quick Links */}
            <div>
              <h3 className="font-semibold text-lg mb-4">Liên kết nhanh</h3>
              <ul className="space-y-2 text-gray-300">
                <li><a href="#" className="hover:text-amber-400 transition-colors">Trang chủ</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Sách mới</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Sách bán chạy</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Khuyến mãi</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Tin tức</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Liên hệ</a></li>
              </ul>
            </div>

            {/* Categories */}
            <div>
              <h3 className="font-semibold text-lg mb-4">Danh mục sách</h3>
              <ul className="space-y-2 text-gray-300">
                <li><a href="#" className="hover:text-amber-400 transition-colors">Văn học</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Kinh tế</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Kỹ năng sống</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Thiếu nhi</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Giáo dục</a></li>
                <li><a href="#" className="hover:text-amber-400 transition-colors">Công nghệ</a></li>
              </ul>
            </div>

            {/* Contact Info */}
            <div>
              <h3 className="font-semibold text-lg mb-4">Thông tin liên hệ</h3>
              <div className="space-y-3 text-gray-300">
                <div className="flex items-center space-x-3">
                  <MapPin className="h-4 w-4 text-amber-400 flex-shrink-0" />
                  <span className="text-sm">123 Đường ABC, Quận XYZ, TP. Hồ Chí Minh</span>
                </div>
                <div className="flex items-center space-x-3">
                  <Phone className="h-4 w-4 text-amber-400 flex-shrink-0" />
                  <span className="text-sm">0903.400.028</span>
                </div>
                <div className="flex items-center space-x-3">
                  <Mail className="h-4 w-4 text-amber-400 flex-shrink-0" />
                  <span className="text-sm">info@bookcity.vn</span>
                </div>
              </div>
              
              {/* Payment Methods */}
              <div className="mt-6">
                <h4 className="font-semibold mb-3">Phương thức thanh toán</h4>
                <div className="flex space-x-2">
                  <div className="bg-white p-2 rounded">
                    <CreditCard className="h-6 w-6 text-slate-800" />
                  </div>
                  <div className="bg-blue-600 p-2 rounded text-white text-xs font-bold flex items-center justify-center">
                    VISA
                  </div>
                  <div className="bg-red-600 p-2 rounded text-white text-xs font-bold flex items-center justify-center">
                    MC
                  </div>
                  <div className="bg-green-600 p-2 rounded text-white text-xs font-bold flex items-center justify-center">
                    ATM
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Newsletter Section */}
      <div className="bg-slate-900 py-8">
        <div className="container mx-auto px-4">
          <div className="text-center">
            <h3 className="text-xl font-semibold mb-2">Đăng ký nhận tin khuyến mãi</h3>
            <p className="text-gray-400 mb-4">Nhận thông tin về sách mới và các chương trình ưu đãi hấp dẫn</p>
            <div className="flex max-w-md mx-auto">
              <input 
                type="email" 
                placeholder="Nhập email của bạn"
                className="flex-1 px-4 py-2 rounded-l-lg text-slate-800 focus:outline-none"
              />
              <button className="bg-amber-400 text-slate-800 font-semibold px-6 py-2 rounded-r-lg hover:bg-amber-500 transition-colors">
                Đăng ký
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Copyright */}
      <div className="bg-slate-900 border-t border-slate-700 py-4">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center text-sm text-gray-400">
            <p>&copy; 2025 BookCity.VN. Tất cả quyền được bảo lưu.</p>
            <div className="flex space-x-4 mt-2 md:mt-0">
              <a href="#" className="hover:text-amber-400 transition-colors">Chính sách bảo mật</a>
              <a href="#" className="hover:text-amber-400 transition-colors">Điều khoản sử dụng</a>
              <a href="#" className="hover:text-amber-400 transition-colors">Sitemap</a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default Footer