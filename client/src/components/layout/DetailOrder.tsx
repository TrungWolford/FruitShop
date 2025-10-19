import React from 'react';
import { Button } from '../ui/Button/Button';
import { Card, CardContent } from '../ui/card';
import { XCircle, User, Phone, MapPin, Package } from 'lucide-react';
import type { Order } from '../../hooks/data';
import { getPaymentMethodText } from '../../hooks/data';

interface DetailOrderProps {
    order: Order | null;
    isOpen: boolean;
    onClose: () => void;
}

const DetailOrder: React.FC<DetailOrderProps> = ({ order, isOpen, onClose }) => {
    if (!isOpen || !order) return null;

    const formatDate = (date: Date) => {
        return new Date(date).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    return (
        <div
            className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-lg border-2 border-gray-200 max-w-4xl w-full max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
            >
                {/* Header */}
                <div className="sticky top-0 bg-white border-b border-gray-200 p-6 rounded-t-lg">
                    <div className="flex items-center justify-between">
                        <div>
                            <h2 className="text-2xl font-bold text-gray-900">Chi tiết đơn hàng</h2>
                            <p className="text-sm text-gray-600 mt-1">Mã đơn hàng: {order.orderNumber}</p>
                        </div>
                        <Button
                            onClick={onClose}
                            variant="outline"
                            size="sm"
                            className="rounded-full w-10 h-10 p-0 border-2 border-gray-200 hover:bg-gray-50 hover:border-gray-300 transition-colors"
                        >
                            <XCircle className="w-5 h-5" />
                        </Button>
                    </div>
                </div>

                <div className="p-6 space-y-6">
                    {/* Order Info */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <Card className="border-2 border-gray-200">
                            <CardContent className="p-4">
                                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                                    <Package className="w-5 h-5 text-blue-600" />
                                    Thông tin đơn hàng
                                </h3>
                                <div className="space-y-3 text-sm">
                                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                        <span className="text-gray-600 font-medium">Mã đơn hàng:</span>
                                        <span className="font-semibold text-gray-900">{order.orderNumber}</span>
                                    </div>
                                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                        <span className="text-gray-600 font-medium">Ngày đặt:</span>
                                        <span className="font-semibold text-gray-900">
                                            {formatDate(order.orderDate)}
                                        </span>
                                    </div>
                                    {order.deliveryDate && (
                                        <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                            <span className="text-gray-600 font-medium">Ngày giao:</span>
                                            <span className="font-semibold text-gray-900">
                                                {formatDate(order.deliveryDate)}
                                            </span>
                                        </div>
                                    )}
                                    <div className="flex justify-between items-center py-2">
                                        <span className="text-gray-600 font-medium">Phương thức thanh toán:</span>
                                        <span className="font-semibold text-gray-900">
                                            {getPaymentMethodText(order.paymentMethod)}
                                        </span>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>

                        <Card className="border-2 border-gray-200">
                            <CardContent className="p-4">
                                <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                                    <User className="w-5 h-5 text-green-600" />
                                    Thông tin giao hàng
                                </h3>
                                <div className="space-y-3 text-sm">
                                    <div className="flex items-start gap-3 py-2 border-b border-gray-100">
                                        <User className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                                        <div className="flex-1">
                                            <span className="text-gray-600 font-medium block">Tên khách hàng:</span>
                                            <span className="font-semibold text-gray-900">{order.customerName}</span>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3 py-2 border-b border-gray-100">
                                        <Phone className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                                        <div className="flex-1">
                                            <span className="text-gray-600 font-medium block">Số điện thoại:</span>
                                            <span className="font-semibold text-gray-900">{order.customerPhone}</span>
                                        </div>
                                    </div>
                                    <div className="flex items-start gap-3 py-2">
                                        <MapPin className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                                        <div className="flex-1">
                                            <span className="text-gray-600 font-medium block">Địa chỉ:</span>
                                            <span className="font-semibold text-gray-900">{order.customerAddress}</span>
                                        </div>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    {/* Order Items */}
                    <Card className="border-2 border-gray-200">
                        <CardContent className="p-4">
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                                <Package className="w-5 h-5 text-purple-600" />
                                Sản phẩm đã đặt ({order.items.length} sản phẩm)
                            </h3>
                            <div className="space-y-4">
                                {order.items.map((item) => (
                                    <div
                                        key={item.id}
                                        className="flex items-center gap-4 p-4 border-2 border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                                    >
                                        <div className="w-20 h-20 bg-gray-200 rounded-lg border-2 border-gray-300 flex items-center justify-center overflow-hidden flex-shrink-0">
                                            {item.product.images && item.product.images.length > 0 ? (
                                                <img
                                                    src={item.product.images[0]}
                                                    alt={item.product.productName}
                                                    className="w-full h-full object-cover"
                                                    onError={(e) => {
                                                        const target = e.target as HTMLImageElement;
                                                        target.style.display = 'none';
                                                        target.nextElementSibling?.classList.remove('hidden');
                                                    }}
                                                />
                                            ) : null}
                                            <Package className="w-8 h-8 text-gray-400 hidden" />
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <h4 className="font-semibold text-gray-900 text-lg mb-1">
                                                {item.product.productName}
                                            </h4>
                                            <div className="text-sm text-gray-600 space-y-1">
                                                <p>Tác giả: {item.product.author}</p>
                                                <p>Bìa: {item.product.cover}</p>
                                            </div>
                                        </div>
                                        <div className="text-right flex-shrink-0">
                                            <p className="text-sm text-gray-600 mb-1">
                                                Số lượng: <span className="font-semibold">{item.quantity}</span>
                                            </p>
                                            <p className="text-sm text-gray-600 mb-1">
                                                Đơn giá:{' '}
                                                <span className="font-semibold">{formatPrice(item.price)}</span>
                                            </p>
                                            <p className="text-lg font-bold text-gray-900">{formatPrice(item.total)}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Order Summary */}
                    <Card className="border-2 border-gray-200 bg-gradient-to-r from-blue-50 to-purple-50">
                        <CardContent className="p-6">
                            <h3 className="text-xl font-bold text-gray-900 mb-4 text-center">Tổng kết đơn hàng</h3>
                            <div className="space-y-3 text-sm">
                                <div className="flex justify-between items-center py-2 border-b border-gray-200">
                                    <span className="text-gray-600 font-medium">Tổng tiền hàng:</span>
                                    <span className="font-semibold text-gray-900">{formatPrice(order.subtotal)}</span>
                                </div>
                                <div className="flex justify-between items-center py-2 border-b border-gray-200">
                                    <span className="text-gray-600 font-medium">Phí vận chuyển:</span>
                                    <span className="font-semibold text-gray-900">
                                        {formatPrice(order.shippingFee)}
                                    </span>
                                </div>
                                <div className="flex justify-between items-center text-xl font-bold text-red-600 pt-3 border-t-2 border-gray-300">
                                    <span>Tổng thanh toán:</span>
                                    <span>{formatPrice(order.total)}</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    {/* Notes */}
                    {order.notes && (
                        <Card className="border-2 border-gray-200">
                            <CardContent className="p-4">
                                <h3 className="text-lg font-semibold text-gray-900 mb-3 flex items-center gap-2">
                                    <span className="w-2 h-2 bg-yellow-500 rounded-full"></span>
                                    Ghi chú đơn hàng
                                </h3>
                                <p className="text-sm text-gray-700 bg-yellow-50 p-4 border-2 border-yellow-200 rounded-lg">
                                    {order.notes}
                                </p>
                            </CardContent>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DetailOrder;
