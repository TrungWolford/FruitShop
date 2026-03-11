import React, { useEffect, useState, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { momoService, type PaymentStatusResponse } from '../../services/momoService';
import { Button } from '../../components/ui/Button/Button';
import { toast } from 'sonner';
import TopNavigation from '../../components/layout/Header/Header';
import Footer from '../../components/layout/Footer/Footer';
import QRCode from 'qrcode';

interface LocationState {
  orderId?: string;
  payUrl?: string;
  qrCodeUrl?: string;
  deeplink?: string;
  deeplinkMiniApp?: string;
  amount?: number;
}

const PaymentPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const state = location.state as LocationState;
  const { orderId, payUrl, qrCodeUrl, deeplink, deeplinkMiniApp, amount } = state || {};
  const [paymentStatus, setPaymentStatus] = useState<string>('PENDING');
  const [isChecking, setIsChecking] = useState(false);
  const [countdown, setCountdown] = useState(300); // 5 minutes countdown
  const [generatedQrDataUrl, setGeneratedQrDataUrl] = useState<string>('');

  // Generate QR Code with MoMo logo
  const generateQRWithLogo = async (url: string): Promise<string> => {
    return new Promise((resolve, reject) => {
      try {
        // Create canvas for QR code
        const canvas = document.createElement('canvas');
        const size = 300;
        canvas.width = size;
        canvas.height = size;

        // Generate QR code to canvas
        QRCode.toCanvas(canvas, url, {
          width: size,
          margin: 2,
          errorCorrectionLevel: 'H', // High error correction for logo overlay
          color: {
            dark: '#000000',
            light: '#FFFFFF'
          }
        }, (error: Error | null | undefined) => {
          if (error) {
            reject(error);
            return;
          }

          const ctx = canvas.getContext('2d');
          if (!ctx) {
            reject(new Error('Cannot get canvas context'));
            return;
          }

          // Create MoMo logo
          const logo = new Image();
          logo.crossOrigin = 'anonymous';

          // MoMo logo SVG (simple version)
          const momoLogoSvg = `
                        <svg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 60 60">
                            <rect width="60" height="60" rx="8" fill="#A50064"/>
                            <text x="30" y="40" font-size="32" font-weight="bold" fill="white" text-anchor="middle" font-family="Arial">M</text>
                        </svg>
                    `;

          const svgBlob = new Blob([momoLogoSvg], { type: 'image/svg+xml' });
          const logoUrl = URL.createObjectURL(svgBlob);

          logo.onload = () => {
            // Calculate logo position (center of QR code)
            const logoSize = 60;
            const x = (size - logoSize) / 2;
            const y = (size - logoSize) / 2;

            // Draw white background for logo
            ctx.fillStyle = 'white';
            ctx.fillRect(x - 5, y - 5, logoSize + 10, logoSize + 10);

            // Draw logo
            ctx.drawImage(logo, x, y, logoSize, logoSize);

            // Convert to data URL
            const dataUrl = canvas.toDataURL('image/png');
            URL.revokeObjectURL(logoUrl);
            resolve(dataUrl);
          };

          logo.onerror = () => {
            // Fallback: return QR without logo
            URL.revokeObjectURL(logoUrl);
            resolve(canvas.toDataURL('image/png'));
          };

          logo.src = logoUrl;
        });
      } catch (error) {
        reject(error);
      }
    });
  };

  // Generate QR Code from deeplink (for MoMo app) or payUrl (fallback)
  useEffect(() => {
    // Priority: deeplink > qrCodeUrl > payUrl
    const qrData = deeplink || qrCodeUrl || payUrl;

    if (qrData) {
      console.log('🎨 Generating QR code from:', {
        type: deeplink ? 'deeplink' : (qrCodeUrl ? 'qrCodeUrl' : 'payUrl'),
        data: qrData
      });

      generateQRWithLogo(qrData)
        .then((url: string) => {
          setGeneratedQrDataUrl(url);
        })
        .catch((_err: Error) => {
          // Fallback: generate simple QR without logo
          QRCode.toDataURL(qrData, {
            width: 300,
            margin: 2,
            color: {
              dark: '#000000',
              light: '#FFFFFF'
            }
          }).then((url: string) => setGeneratedQrDataUrl(url));
        });
    }
  }, [deeplink, qrCodeUrl, payUrl]);

  // Check payment status
  const checkStatus = useCallback(async () => {
    if (!orderId) return;

    try {
      setIsChecking(true);
      const statusResponse: PaymentStatusResponse = await momoService.checkPaymentStatus(orderId);
      setPaymentStatus(statusResponse.paymentStatus);

      if (statusResponse.paymentStatus === 'COMPLETED') {
        toast.success('✅ Thanh toán thành công!');
        setTimeout(() => {
          navigate('/customer/orders', {
            state: { orderId, paymentSuccess: true },
            replace: true
          });
        }, 1000);
      } else if (statusResponse.paymentStatus === 'FAILED') {
        toast.error('❌ Thanh toán thất bại. Đơn hàng đã bị hủy.');
        setTimeout(() => {
          navigate('/checkout', { replace: true });
        }, 2000);
      }
    } catch (error) {
    } finally {
      setIsChecking(false);
    }
  }, [orderId, navigate]);

  // Poll payment status every 3 seconds
  useEffect(() => {
    if (!orderId || paymentStatus !== 'PENDING') return;

    const intervalId = setInterval(checkStatus, 3000);

    return () => clearInterval(intervalId);
  }, [orderId, paymentStatus, checkStatus]);

  // Countdown timer
  useEffect(() => {
    if (paymentStatus !== 'PENDING' || countdown <= 0) return;

    const timerId = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          toast.warning('⏰ Hết thời gian thanh toán. Vui lòng thử lại.');
          setTimeout(() => navigate('/checkout'), 2000);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timerId);
  }, [paymentStatus, countdown, navigate]);

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleOpenMoMoApp = () => {
    if (payUrl) {
      // Mở trang thanh toán MoMo để nhập thẻ ATM
      window.location.href = payUrl; // Chuyển trang hiện tại (không mở tab mới)
    } else if (deeplink) {
      // Fallback: Mở app MoMo (nếu trên mobile)
      window.location.href = deeplink;
    } else if (deeplinkMiniApp) {
      window.location.href = deeplinkMiniApp;
    } else {
      toast.error('Không tìm thấy link thanh toán');
    }
  };

  const handleCancel = () => {
    if (window.confirm('Bạn có chắc muốn hủy thanh toán? Đơn hàng sẽ bị hủy.')) {
      navigate('/checkout');
    }
  };

  if (!orderId || !state) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 text-lg mb-4">Không tìm thấy thông tin thanh toán</p>
          <Button onClick={() => navigate('/')}>Về Trang Chủ</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <TopNavigation />

      <main className="flex-1 py-12">
        <div className="max-w-2xl mx-auto px-4">
          {/* Header */}
          <div className="bg-white rounded-xl shadow-lg p-8 mb-6">
            <div className="text-center mb-6">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-pink-500 to-purple-600 rounded-full mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                </svg>
              </div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">Thanh Toán MoMo</h1>
              <p className="text-gray-600">Quét mã QR để hoàn tất thanh toán</p>
            </div>

            {/* Payment Info */}
            <div className="bg-gradient-to-r from-pink-50 to-purple-50 rounded-lg p-6 mb-6">
              <div className="flex justify-between items-center mb-3">
                <span className="text-gray-700 font-medium">Số tiền thanh toán:</span>
                <span className="text-2xl font-bold text-pink-600">
                  {amount?.toLocaleString('vi-VN')} ₫
                </span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-600">Mã đơn hàng:</span>
                <span className="font-mono text-gray-800">{orderId}</span>
              </div>
            </div>

            {/* QR Code */}
            <div className="bg-white border-4 border-pink-200 rounded-2xl p-8 mb-6">
              {generatedQrDataUrl ? (
                <div className="flex flex-col items-center">
                  <img
                    src={generatedQrDataUrl}
                    alt="MoMo QR Code"
                    className="w-72 h-72 object-contain"
                    onLoad={() => {
                    }}
                  />
                  <p className="text-xs text-gray-500 mt-4 text-center max-w-xs">
                    Quét mã QR này bằng ứng dụng MoMo để thanh toán
                  </p>
                </div>
              ) : (
                <div className="flex justify-center items-center h-72">
                  <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-pink-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Đang tạo mã QR...</p>
                    <p className="text-xs text-gray-400 mt-2">Vui lòng chờ trong giây lát</p>
                  </div>
                </div>
              )}
            </div>

            {/* Instructions */}
            <div className="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
              <h3 className="font-semibold text-blue-900 mb-3 flex items-center">
                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
                Hướng dẫn thanh toán
              </h3>

              {/* Cách 1: QR Code */}
              <div className="mb-4">
                <h4 className="font-semibold text-blue-800 mb-2">📱 Cách 1: Quét mã QR (Ví MoMo)</h4>
                <ol className="list-decimal list-inside space-y-1 text-sm text-blue-900 ml-4">
                  <li>Mở ứng dụng <strong>MoMo</strong> trên điện thoại</li>
                  <li>Chọn <strong>"Quét mã QR"</strong></li>
                  <li>Quét mã QR phía trên</li>
                  <li>Xác nhận thanh toán trong ví MoMo</li>
                </ol>
              </div>

              {/* Cách 2: Thẻ ATM */}
              <div>
                <h4 className="font-semibold text-blue-800 mb-2">💳 Cách 2: Thanh toán bằng thẻ ATM</h4>
                <ol className="list-decimal list-inside space-y-1 text-sm text-blue-900 ml-4">
                  <li>Nhấn nút <strong>"Thanh Toán Bằng Thẻ ATM"</strong> bên dưới</li>
                  <li>Chọn ngân hàng của bạn</li>
                  <li>Nhập số thẻ ATM, tên chủ thẻ, ngày hết hạn</li>
                  <li>Nhập mã OTP từ ngân hàng</li>
                  <li>Hoàn tất thanh toán</li>
                </ol>
              </div>

              <p className="text-xs text-blue-700 mt-3 italic">
                💡 Sau khi thanh toán thành công, hệ thống sẽ tự động chuyển trang
              </p>
            </div>

            {/* Payment Status */}
            <div className="text-center mb-6">
              {paymentStatus === 'PENDING' && (
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                  <div className="flex items-center justify-center gap-3 text-yellow-700">
                    <div className="animate-pulse">⏳</div>
                    <span className="font-medium">Đang chờ thanh toán...</span>
                    {isChecking && (
                      <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-yellow-600"></div>
                    )}
                  </div>
                  <p className="text-sm text-yellow-600 mt-2">
                    Thời gian còn lại: <strong>{formatTime(countdown)}</strong>
                  </p>
                </div>
              )}
              {paymentStatus === 'COMPLETED' && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <p className="text-green-700 font-medium flex items-center justify-center gap-2">
                    <span className="text-2xl">✅</span>
                    Thanh toán thành công!
                  </p>
                </div>
              )}
              {paymentStatus === 'FAILED' && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <p className="text-red-700 font-medium flex items-center justify-center gap-2">
                    <span className="text-2xl">❌</span>
                    Thanh toán thất bại!
                  </p>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3">
              <Button
                onClick={handleOpenMoMoApp}
                className="flex-1 bg-gradient-to-r from-pink-600 to-purple-600 hover:from-pink-700 hover:to-purple-700 text-white font-semibold py-3 rounded-lg shadow-lg transform transition hover:scale-105"
              >
                <span className="flex items-center justify-center gap-2">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clipRule="evenodd" />
                  </svg>
                  Thanh Toán Bằng Thẻ ATM
                </span>
              </Button>
              <Button
                onClick={handleCancel}
                variant="outline"
                className="px-6 py-3 border-2 border-gray-300 hover:bg-gray-100 text-gray-700 font-medium rounded-lg"
              >
                Hủy
              </Button>
            </div>
          </div>

          {/* Security Notice */}
          <div className="text-center text-sm text-gray-500">
            <p className="flex items-center justify-center gap-2">
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
              </svg>
              Giao dịch được bảo mật bởi MoMo
            </p>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default PaymentPage;
