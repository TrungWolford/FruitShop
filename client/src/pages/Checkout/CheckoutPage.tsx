import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingCart, MapPin, User, Phone, CreditCard } from 'lucide-react';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { useAppSelector } from '../../hooks/redux';
import { cartService } from '../../services/cartService';
import { shippingService } from '../../services/shippingService';
import { orderService } from '../../services/orderService';
import { accountService } from '../../services/adminAccountService';
import { productService } from '../../services/productService';
import { momoService } from '../../services/momoService';
import type { CreateShippingRequest, ShippingResponse } from '../../services/shippingService';
import type { CreateOrderRequest } from '../../services/orderService';
import type { CartItem as CartItemType } from '../../types/cart';
import { toast } from 'sonner';

// Shipping method options
const SHIPPING_METHODS = [
    { id: 'super_fast', name: 'HCM - Siêu tốc', fee: 50000, description: 'Giao hàng trong 2 giờ' },
    { id: 'express_4h', name: 'HCM - 4H', fee: 30000, description: 'Giao hàng trong 4 giờ' },
    { id: 'super_cheap', name: 'HCM Siêu rẻ 2H', fee: 40000, description: 'Giao hàng trong 2 ngày' },
];

// Initial shipping data structure based on backend API
const initialShippingData = {
    receiverName: '',
    receiverPhone: '',
    address: '', // Địa chỉ chi tiết
    ward: '', // Phường/Xã
    district: '', // Quận/Huyện
    city: '',
    paymentMethod: 0, // 0: Tiền mặt (COD), 1: Chuyển khoản
};

const CheckoutPage: React.FC = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);
    const [cartItems, setCartItems] = useState<CartItemType[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [shippingData, setShippingData] = useState(initialShippingData);
    const [savedShippingAddresses, setSavedShippingAddresses] = useState<ShippingResponse[]>([]);
    const [selectedShippingId, setSelectedShippingId] = useState<string>('');
    const [isProcessing, setIsProcessing] = useState(false);
    const [isLoadingShipping, setIsLoadingShipping] = useState(false);
    const [selectedShippingMethod, setSelectedShippingMethod] = useState<string>(SHIPPING_METHODS[0].id);

    // Fetch cart items and shipping addresses
    useEffect(() => {
        if (isAuthenticated && user) {
            fetchCartItems();
            fetchShippingAddresses();
        } else {
            // Redirect to home if not authenticated
            navigate('/');
        }
    }, [isAuthenticated, user, navigate]);

    const fetchCartItems = async () => {
        if (!user) return;

        setIsLoading(true);
        try {
            const response = await cartService.getCartItems(user.accountId);

            if (response.success && response.data) {
                let items: CartItemType[] = [];

                if (Array.isArray(response.data)) {
                    items = response.data;
                } else if (typeof response.data === 'object' && 'items' in response.data) {
                    items = (response.data as any).items || [];
                } else if (typeof response.data === 'object' && 'cartItems' in response.data) {
                    items = (response.data as any).cartItems || [];
                } else {
                    items = [response.data as any];
                }

                setCartItems(items);
            } else {
                setCartItems([]);
                toast.error('Không thể tải giỏ hàng');
            }
        } catch (error) {
            console.error('Error fetching cart items:', error);
            setCartItems([]);
            toast.error('Đã xảy ra lỗi khi tải giỏ hàng');
        } finally {
            setIsLoading(false);
        }
    };

    const fetchShippingAddresses = async () => {
        if (!user) return;

        setIsLoadingShipping(true);
        try {
            const response = await shippingService.getShippingByAccount(user.accountId);

            if (response.success && response.data) {
                setSavedShippingAddresses(response.data);

                // If user has saved addresses, use the first one as default
                if (response.data.length > 0) {
                    const firstAddress = response.data[0];
                    setSelectedShippingId(firstAddress.shippingId);

                    // Parse the receiver_address back to components
                    const parsedAddress = parseAddressComponents(firstAddress.receiverAddress);

                    setShippingData({
                        receiverName: firstAddress.receiverName,
                        receiverPhone: firstAddress.receiverPhone,
                        address: parsedAddress.address,
                        ward: parsedAddress.ward,
                        district: parsedAddress.district,
                        city: firstAddress.city,
                        paymentMethod: 0, // Default to COD
                    });
                }
            } else {
                setSavedShippingAddresses([]);
            }
        } catch (error) {
            console.error('Error fetching shipping addresses:', error);
            setSavedShippingAddresses([]);
        } finally {
            setIsLoadingShipping(false);
        }
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    // Parse address components from combined address string
    const parseAddressComponents = (fullAddress: string) => {
        // Expected format: "address, ward, district"
        // Example: "1022/123 Pham The Hien, Phường Đa Kao, Quận 8"

        const parts = fullAddress.split(',').map((part) => part.trim());

        if (parts.length >= 3) {
            return {
                address: parts[0], // "1022/123 Pham The Hien"
                ward: parts[1], // "Phường Đa Kao"
                district: parts[2], // "Quận 8"
            };
        } else if (parts.length === 2) {
            return {
                address: parts[0],
                ward: parts[1],
                district: '',
            };
        } else {
            return {
                address: fullAddress,
                ward: '',
                district: '',
            };
        }
    };

    const getImageUrl = (imageUrl?: string) => {
        if (!imageUrl) return '/placeholder-image.jpg';

        if (imageUrl.startsWith('http')) {
            return imageUrl;
        }

        if (imageUrl.startsWith('/products/')) {
            return imageUrl;
        }

        const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
        return `/products/${cleanUrl}`;
    };

    const calculateShippingFee = () => {
        const method = SHIPPING_METHODS.find(m => m.id === selectedShippingMethod);
        return method ? method.fee : SHIPPING_METHODS[0].fee;
    };

    const calculateTotal = () => {
        const subtotal = cartItems.reduce((total, item) => total + item.productPrice * item.quantity, 0);
        const shippingFee = calculateShippingFee();
        return subtotal + shippingFee;
    };

    const handleInputChange = (field: string, value: string | number) => {
        setShippingData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleShippingAddressSelect = (shippingId: string) => {
        if (shippingId === 'new') {
            // Reset to new address
            setSelectedShippingId('');
            setShippingData({
                receiverName: '',
                receiverPhone: '',
                address: '',
                ward: '',
                district: '',
                city: '',
                paymentMethod: 0,
            });
            return;
        }

        const selectedAddress = savedShippingAddresses.find((addr) => addr.shippingId === shippingId);
        if (selectedAddress) {
            setSelectedShippingId(shippingId);

            // Parse the receiver_address back to components
            const parsedAddress = parseAddressComponents(selectedAddress.receiverAddress);

            setShippingData((prev) => ({
                ...prev,
                receiverName: selectedAddress.receiverName,
                receiverPhone: selectedAddress.receiverPhone,
                address: parsedAddress.address,
                ward: parsedAddress.ward,
                district: parsedAddress.district,
                city: selectedAddress.city,
            }));
        }
    };

    const handlePlaceOrder = async () => {
        if (!user) return;

        // Validation
        if (
            !shippingData.receiverName ||
            !shippingData.receiverPhone ||
            !shippingData.address ||
            !shippingData.ward ||
            !shippingData.district ||
            !shippingData.city
        ) {
            toast.error('Vui lòng điền đầy đủ thông tin giao hàng');
            return;
        }

        if (cartItems.length === 0) {
            toast.error('Giỏ hàng trống');
            return;
        }

        setIsProcessing(true);
        try {
            // Step 1: Create or update shipping address if needed
            let shippingResponse = null;

            if (!selectedShippingId || selectedShippingId === 'new') {
                // Combine address components into receiver_address for database
                const fullAddress = `${shippingData.address}, ${shippingData.ward}, ${shippingData.district}`;

                // Create new shipping address
                const createShippingRequest: CreateShippingRequest = {
                    accountId: user.accountId,
                    receiverName: shippingData.receiverName,
                    receiverPhone: shippingData.receiverPhone,
                    receiverAddress: fullAddress, // Combined: address + ward + district
                    city: shippingData.city,
                    shippingFee: calculateShippingFee(), // Add shipping fee based on selected method
                };

                console.log('Creating shipping address with data:', createShippingRequest);

                shippingResponse = await shippingService.createShipping(createShippingRequest);
                if (!shippingResponse.success) {
                    toast.error(shippingResponse.message || 'Không thể tạo địa chỉ giao hàng');
                    return;
                }

                console.log('Shipping address created successfully:', shippingResponse.data);
            }

            // Step 2: Create order
            let finalShippingId = selectedShippingId;

            // If creating new shipping, use the newly created shipping ID
            if (selectedShippingId === 'new' || !selectedShippingId) {
                if (shippingResponse && shippingResponse.data) {
                    finalShippingId = shippingResponse.data.shippingId;
                } else {
                    toast.error('Không thể lấy thông tin địa chỉ giao hàng');
                    return;
                }
            }

            const createOrderRequest: CreateOrderRequest = {
                accountId: user.accountId,
                shippingId: finalShippingId,
                paymentMethod: shippingData.paymentMethod,
                items: cartItems.map((item) => ({
                    productId: item.productId,
                    quantity: item.quantity,
                })),
            };

            // Validate cart items before sending
            if (createOrderRequest.items.length === 0) {
                toast.error('Giỏ hàng trống');
                return;
            }

            // Check if all items have valid productId and quantity
            const invalidItems = createOrderRequest.items.filter((item) => !item.productId || item.quantity <= 0);

            if (invalidItems.length > 0) {
                toast.error('Có sản phẩm không hợp lệ trong giỏ hàng');
                console.error('Invalid items:', invalidItems);
                return;
            }

            console.log('Creating order with data:', createOrderRequest);
            console.log('Cart items for order:', cartItems);
            console.log('User account ID:', user.accountId);
            console.log('Payment method:', shippingData.paymentMethod);

            // Detailed validation logging
            console.log('=== ORDER REQUEST VALIDATION ===');
            console.log('AccountId type:', typeof createOrderRequest.accountId);
            console.log('AccountId value:', createOrderRequest.accountId);
            console.log('PaymentMethod type:', typeof createOrderRequest.paymentMethod);
            console.log('PaymentMethod value:', createOrderRequest.paymentMethod);
            console.log('Items count:', createOrderRequest.items.length);

            createOrderRequest.items.forEach((item, index) => {
                console.log(`Item ${index}:`, {
                    productId: item.productId,
                    productIdType: typeof item.productId,
                    quantity: item.quantity,
                    quantityType: typeof item.quantity,
                });
            });
            console.log('================================');

            // Validate account exists before creating order
            try {
                console.log('Validating account existence...');
                const accountCheck = await accountService.getAccountById(user.accountId);
                console.log('Account validation successful:', accountCheck);
            } catch (accountError) {
                console.error('Account validation failed:', accountError);
                toast.error('Tài khoản không tồn tại hoặc đã bị khóa');
                return;
            }

            // Validate all products exist and have sufficient stock
            console.log('Validating products...');
            for (const item of createOrderRequest.items) {
                try {
                    const productResponse = await productService.getProductById(item.productId);

                    if (!productResponse.success || !productResponse.data) {
                        toast.error(`Không tìm thấy sản phẩm ${item.productId}`);
                        return;
                    }

                    const product = productResponse.data;
                    console.log(`Product ${item.productId} validation:`, {
                        productName: product.productName,
                        stock: product.stock,
                        requestedQuantity: item.quantity,
                        status: product.status,
                    });

                    if (product.status !== 1) {
                        toast.error(`Sản phẩm "${product.productName}" đã ngừng kinh doanh`);
                        return;
                    }

                    if (product.stock < item.quantity) {
                        toast.error(
                            `Sản phẩm "${product.productName}" không đủ hàng. Còn lại: ${product.stock}, yêu cầu: ${item.quantity}`,
                        );
                        return;
                    }
                } catch (productError) {
                    console.error(`Product validation failed for ${item.productId}:`, productError);
                    toast.error(`Sản phẩm với ID ${item.productId} không tồn tại`);
                    return;
                }
            }
            console.log('All products validated successfully');

            const orderResponse = await orderService.createOrder(createOrderRequest);

            console.log('Order creation response:', orderResponse);

            if (orderResponse.success && orderResponse.data) {
                const orderId = orderResponse.data.orderId;
                
                // Check payment method
                if (shippingData.paymentMethod === 1) {
                    // Chuyển khoản - Create MoMo payment
                    console.log('Creating MoMo payment for order:', orderId);
                    
                    try {
                        const momoResponse = await momoService.createPayment(orderId);
                        
                        console.log('📥 Full MoMo Response:', momoResponse);
                        console.log('📥 MoMo Data:', momoResponse.data);
                        console.log('📥 QR Code URL:', momoResponse.data?.qrCodeUrl);
                        
                        if (momoResponse.success && momoResponse.data) {
                            console.log('✅ MoMo payment created successfully:', momoResponse.data);
                            
                            // Clear cart sau khi tạo MoMo payment thành công
                            console.log('🧹 Clearing cart for MoMo payment, accountId:', user.accountId);
                            const clearCartResponse = await cartService.clearCart(user.accountId);
                            console.log('🧹 Clear cart response:', clearCartResponse);

                            if (clearCartResponse.success) {
                                console.log('Cart cleared successfully for MoMo payment');
                                
                                // Dispatch events để refresh cart UI
                                console.log('🔔 Dispatching cart update events');
                                window.dispatchEvent(new CustomEvent('cartUpdated'));
                                window.dispatchEvent(
                                    new CustomEvent('cartItemsUpdated', {
                                        detail: { items: [], count: 0 },
                                    }),
                                );
                                window.dispatchEvent(new CustomEvent('closeCartModal'));
                            } else {
                                console.error('Failed to clear cart for MoMo:', clearCartResponse.message);
                            }
                            
                            // Navigate to payment page with payment data
                            navigate('/payment', {
                                state: {
                                    orderId: orderId,
                                    qrCodeUrl: momoResponse.data.qrCodeUrl,
                                    deeplink: momoResponse.data.deeplink,
                                    payUrl: momoResponse.data.payUrl,
                                    amount: calculateTotal(),
                                }
                            });
                        } else {
                            // MoMo payment creation failed - order should be auto-cancelled by backend
                            toast.error(momoResponse.message || 'Không thể tạo thanh toán MoMo');
                            console.error('MoMo payment creation failed:', momoResponse.message);
                            
                            // Reload to refresh cart
                            setTimeout(() => {
                                window.location.reload();
                            }, 2000);
                        }
                    } catch (momoError) {
                        console.error('Error creating MoMo payment:', momoError);
                        toast.error('Đã xảy ra lỗi khi tạo thanh toán MoMo. Đơn hàng đã bị hủy.');
                        
                        // Reload to refresh cart
                        setTimeout(() => {
                            window.location.reload();
                        }, 2000);
                    }
                } else {
                    // COD - Clear cart and navigate to orders
                    console.log('Order created successfully with COD, now clearing cart...');
                    console.log('🧹 Calling clearCart for accountId:', user.accountId);

                    const clearCartResponse = await cartService.clearCart(user.accountId);
                    console.log('🧹 Clear cart response:', clearCartResponse);

                    if (clearCartResponse.success) {
                        console.log('Cart cleared successfully');
                        toast.success('Đặt hàng thành công! Giỏ hàng đã được xóa.');

                        // Dispatch event để Cart component và TopNavigation refresh
                        console.log('🔔 Dispatching cartUpdated event');
                        window.dispatchEvent(new CustomEvent('cartUpdated'));

                        console.log('🔔 Dispatching cartItemsUpdated event with empty items');
                        window.dispatchEvent(
                            new CustomEvent('cartItemsUpdated', {
                                detail: { items: [], count: 0 },
                            }),
                        );

                        // Dispatch event để đóng Cart modal nếu đang mở
                        console.log('🔔 Dispatching closeCartModal event');
                        window.dispatchEvent(new CustomEvent('closeCartModal'));

                        // Dispatch event để refresh order history
                        console.log('🔔 Dispatching orderCreated event');
                        window.dispatchEvent(new CustomEvent('orderCreated'));

                        // Force refresh tất cả cart-related components với delay nhỏ
                        setTimeout(() => {
                            console.log('🔄 Force refreshing all cart components...');
                            window.dispatchEvent(new CustomEvent('cartUpdated'));
                            window.dispatchEvent(
                                new CustomEvent('cartItemsUpdated', {
                                    detail: { items: [], count: 0 },
                                }),
                            );
                        }, 100);
                    } else {
                        console.error('Failed to clear cart:', clearCartResponse.message);
                        toast.success('Đặt hàng thành công!');
                        toast.warning('Không thể xóa giỏ hàng, vui lòng xóa thủ công.');
                    }

                    navigate('/customer/orders');
                }
            } else {
                toast.error(orderResponse.message || 'Không thể tạo đơn hàng');
            }
        } catch (error) {
            console.error('Error placing order:', error);
            toast.error('Đã xảy ra lỗi khi đặt hàng');
        } finally {
            setIsProcessing(false);
        }
    };

    if (isLoading) {
        return (
            <div>
                <TopNavigation />
                <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                    <div className="text-center">
                        <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent  animate-spin mx-auto mb-4"></div>
                        <p className="text-gray-600">Đang tải...</p>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    if (cartItems.length === 0) {
        return (
            <div>
                <TopNavigation />
                <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                    <div className="text-center">
                        <ShoppingCart className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                        <h2 className="text-2xl font-semibold text-gray-700 mb-2">Giỏ hàng trống</h2>
                        <p className="text-gray-500 mb-6">Bạn cần có sản phẩm trong giỏ hàng để thanh toán</p>
                        <Button onClick={() => navigate('/')} className="bg-blue-600 hover:bg-blue-700 rounded-none">
                            Tiếp tục mua sắm
                        </Button>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div>
            <TopNavigation />
            <div className="min-h-screen bg-gray-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Left Column - Shipping Information */}
                        <div className="space-y-6">
                            <div className="bg-white  shadow-sm border p-6">
                                <div className="flex items-center gap-3 mb-6">
                                    <MapPin className="w-6 h-6 text-green-600" />
                                    <h2 className="text-lg font-semibold text-gray-900">Thông tin giao hàng</h2>
                                </div>

                                <div className="space-y-4">
                                {/* Loading indicator for shipping addresses */}
                                {isLoadingShipping && (
                                    <div className="text-center py-4">
                                        <div className="text-sm text-gray-500">Đang tải địa chỉ đã lưu...</div>
                                    </div>
                                )}

                                {/* Saved Shipping Addresses */}
                                {!isLoadingShipping && savedShippingAddresses.length > 0 && (
                                    <div>
                                        <Label htmlFor="savedAddress">Địa chỉ đã lưu</Label>
                                        <Select value={selectedShippingId} onValueChange={handleShippingAddressSelect}>
                                            <SelectTrigger className="rounded-none">
                                                <SelectValue placeholder="Chọn địa chỉ đã lưu hoặc nhập mới" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="new">Nhập địa chỉ mới</SelectItem>
                                                {savedShippingAddresses.map((address) => (
                                                    <SelectItem key={address.shippingId} value={address.shippingId}>
                                                        {address.receiverName} - {address.receiverPhone}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                )}

                                {/* Receiver Name */}
                                <div>
                                    <Label htmlFor="receiverName">Tên người nhận *</Label>
                                    <div className="relative">
                                        <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                        <Input
                                            id="receiverName"
                                            value={shippingData.receiverName}
                                            onChange={(e) => handleInputChange('receiverName', e.target.value)}
                                            className="pl-10 rounded-none"
                                            placeholder="Nhập tên người nhận"
                                        />
                                    </div>
                                </div>

                                {/* Receiver Phone */}
                                <div>
                                    <Label htmlFor="receiverPhone">Số điện thoại người nhận *</Label>
                                    <div className="relative">
                                        <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                                        <Input
                                            id="receiverPhone"
                                            value={shippingData.receiverPhone}
                                            onChange={(e) => handleInputChange('receiverPhone', e.target.value)}
                                            className="pl-10 rounded-none"
                                            placeholder="Nhập số điện thoại người nhận"
                                        />
                                    </div>
                                </div>

                                {/* Address */}
                                <div>
                                    <Label htmlFor="address">Địa chỉ chi tiết *</Label>
                                    <Input
                                        id="address"
                                        value={shippingData.address}
                                        onChange={(e) => handleInputChange('address', e.target.value)}
                                        className="rounded-none"
                                        placeholder="Nhập số nhà, tên đường..."
                                    />
                                </div>

                                {/* Ward */}
                                <div>
                                    <Label htmlFor="ward">Phường/Xã *</Label>
                                    <Select
                                        value={shippingData.ward}
                                        onValueChange={(value) => handleInputChange('ward', value)}
                                    >
                                        <SelectTrigger className="rounded-none">
                                            <SelectValue placeholder="Chọn phường/xã" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="Phường Bến Nghé">Phường Bến Nghé</SelectItem>
                                            <SelectItem value="Phường Đa Kao">Phường Đa Kao</SelectItem>
                                            <SelectItem value="Phường Bến Thành">Phường Bến Thành</SelectItem>
                                            <SelectItem value="Phường Nguyễn Thái Bình">
                                                Phường Nguyễn Thái Bình
                                            </SelectItem>
                                            <SelectItem value="Phường Cô Giang">Phường Cô Giang</SelectItem>
                                            <SelectItem value="Phường Nguyễn Cư Trinh">
                                                Phường Nguyễn Cư Trinh
                                            </SelectItem>
                                            <SelectItem value="Phường Tân Định">Phường Tân Định</SelectItem>
                                            <SelectItem value="Phường Đa Kao">Phường Đa Kao</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {/* District */}
                                <div>
                                    <Label htmlFor="district">Quận/Huyện *</Label>
                                    <Select
                                        value={shippingData.district}
                                        onValueChange={(value) => handleInputChange('district', value)}
                                    >
                                        <SelectTrigger className="rounded-none">
                                            <SelectValue placeholder="Chọn quận/huyện" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="Quận 1">Quận 1</SelectItem>
                                            <SelectItem value="Quận 2">Quận 2</SelectItem>
                                            <SelectItem value="Quận 3">Quận 3</SelectItem>
                                            <SelectItem value="Quận 4">Quận 4</SelectItem>
                                            <SelectItem value="Quận 5">Quận 5</SelectItem>
                                            <SelectItem value="Quận 6">Quận 6</SelectItem>
                                            <SelectItem value="Quận 7">Quận 7</SelectItem>
                                            <SelectItem value="Quận 8">Quận 8</SelectItem>
                                            <SelectItem value="Quận 9">Quận 9</SelectItem>
                                            <SelectItem value="Quận 10">Quận 10</SelectItem>
                                            <SelectItem value="Quận 11">Quận 11</SelectItem>
                                            <SelectItem value="Quận 12">Quận 12</SelectItem>
                                            <SelectItem value="Quận Bình Thạnh">Quận Bình Thạnh</SelectItem>
                                            <SelectItem value="Quận Gò Vấp">Quận Gò Vấp</SelectItem>
                                            <SelectItem value="Quận Phú Nhuận">Quận Phú Nhuận</SelectItem>
                                            <SelectItem value="Quận Tân Bình">Quận Tân Bình</SelectItem>
                                            <SelectItem value="Quận Tân Phú">Quận Tân Phú</SelectItem>
                                            <SelectItem value="Quận Thủ Đức">Quận Thủ Đức</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {/* City */}
                                <div>
                                    <Label htmlFor="city">Thành phố *</Label>
                                    <Select
                                        value={shippingData.city}
                                        onValueChange={(value) => handleInputChange('city', value)}
                                    >
                                        <SelectTrigger className="rounded-none">
                                            <SelectValue placeholder="Chọn thành phố" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="Hồ Chí Minh">Hồ Chí Minh</SelectItem>
                                            <SelectItem value="Hà Nội">Hà Nội</SelectItem>
                                            <SelectItem value="Đà Nẵng">Đà Nẵng</SelectItem>
                                            <SelectItem value="Cần Thơ">Cần Thơ</SelectItem>
                                            <SelectItem value="Hải Phòng">Hải Phòng</SelectItem>
                                            <SelectItem value="Biên Hòa">Biên Hòa</SelectItem>
                                            <SelectItem value="Nha Trang">Nha Trang</SelectItem>
                                            <SelectItem value="Buôn Ma Thuột">Buôn Ma Thuột</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {/* Shipping Method */}
                                <div>
                                    <Label htmlFor="shippingMethod">Phương thức vận chuyển *</Label>
                                    <Select
                                        value={selectedShippingMethod}
                                        onValueChange={(value) => setSelectedShippingMethod(value)}
                                    >
                                        <SelectTrigger className="rounded-none">
                                            <SelectValue placeholder="Chọn phương thức vận chuyển" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {SHIPPING_METHODS.map((method) => (
                                                <SelectItem key={method.id} value={method.id}>
                                                    <div className="flex items-center justify-between w-full">
                                                        <span>{method.name}</span>
                                                        <span className="ml-4 text-gray-600">
                                                            {formatPrice(method.fee)}
                                                        </span>
                                                    </div>
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    <p className="text-sm text-gray-500 mt-1">
                                        {SHIPPING_METHODS.find(m => m.id === selectedShippingMethod)?.description}
                                    </p>
                                </div>

                                {/* Payment Method */}
                                <div>
                                    <Label htmlFor="paymentMethod">Phương thức thanh toán *</Label>
                                    <Select
                                        value={shippingData.paymentMethod.toString()}
                                        onValueChange={(value) => handleInputChange('paymentMethod', parseInt(value))}
                                    >
                                        <SelectTrigger className="rounded-none">
                                            <SelectValue placeholder="Chọn phương thức thanh toán" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="0">Thanh toán khi nhận hàng (COD)</SelectItem>
                                            <SelectItem value="1">Chuyển khoản ngân hàng</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right Column - Cart Items and Order Summary */}
                    <div className="space-y-6">
                        {/* Cart Items */}
                        <div className="bg-white  shadow-sm border p-6">
                            <div className="flex items-center gap-3 mb-6">
                                <ShoppingCart className="w-6 h-6 text-blue-600" />
                                <h2 className="text-lg font-semibold text-gray-900">Sản phẩm đã chọn</h2>
                                <span className="bg-blue-100 text-blue-800 text-sm px-2 py-1 ">
                                    {cartItems.length} sản phẩm
                                </span>
                            </div>

                            <div className="space-y-4">
                                {cartItems.map((item) => (
                                    <div
                                        key={item.cartItemId}
                                        className="flex items-center gap-4 p-4 border border-gray-200 "
                                    >
                                        <div className="w-16 h-16 bg-gray-100  overflow-hidden flex-shrink-0">
                                            {item.images && item.images.length > 0 ? (
                                                <img
                                                    src={getImageUrl(item.images[0])}
                                                    alt={item.productName}
                                                    className="w-full h-full object-cover"
                                                    onError={(e) => {
                                                        e.currentTarget.src = '/placeholder-image.jpg';
                                                    }}
                                                />
                                            ) : (
                                                <div className="w-full h-full bg-gray-200 flex items-center justify-center">
                                                    <span className="text-xs text-gray-400">No image</span>
                                                </div>
                                            )}
                                        </div>

                                        <div className="flex-1 min-w-0">
                                            <h3 className="text-sm font-medium text-gray-900 line-clamp-2">
                                                {item.productName}
                                            </h3>
                                            <p className="text-sm text-gray-500 mt-1">Số lượng: {item.quantity}</p>
                                        </div>

                                        <div className="text-right">
                                            <p className="text-sm font-semibold text-gray-900">
                                                {formatPrice(item.productPrice * item.quantity)}
                                            </p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Order Summary */}
                        <div className="bg-white  shadow-sm border p-6">
                            <h3 className="text-lg font-semibold text-gray-900 mb-4">Tóm tắt đơn hàng</h3>

                            <div className="space-y-3">
                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-600">Tạm tính:</span>
                                    <span className="font-medium">
                                        {formatPrice(
                                            cartItems.reduce(
                                                (total, item) => total + item.productPrice * item.quantity,
                                                0,
                                            ),
                                        )}
                                    </span>
                                </div>

                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-600">Phí vận chuyển:</span>
                                    <div className="text-right">
                                        <div className="font-medium">{formatPrice(calculateShippingFee())}</div>
                                        <div className="text-xs text-gray-500">
                                            {SHIPPING_METHODS.find(m => m.id === selectedShippingMethod)?.name}
                                        </div>
                                    </div>
                                </div>

                                <div className="border-t pt-3">
                                    <div className="flex justify-between text-lg font-semibold">
                                        <span>Tổng cộng:</span>
                                        <span className="text-red-600">{formatPrice(calculateTotal())}</span>
                                    </div>
                                </div>
                            </div>

                            <Button
                                onClick={handlePlaceOrder}
                                disabled={isProcessing}
                                className="w-full mt-6 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-none"
                            >
                                {isProcessing ? (
                                    <div className="flex items-center gap-2">
                                        <div className="w-4 h-4 border-2 border-white border-t-transparent  animate-spin"></div>
                                        Đang xử lý...
                                    </div>
                                ) : (
                                    <div className="flex items-center gap-2">
                                        <CreditCard className="w-4 h-4" />
                                        Đặt hàng
                                    </div>
                                )}
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
            </div>
            <Footer />
        </div>
    );
};

export default CheckoutPage;
