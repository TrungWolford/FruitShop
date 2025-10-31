import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Badge } from '../../components/ui/badge';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '../../components/ui/alert-dialog';
import {
    ShoppingBag,
    Star,
    Minus,
    Plus,
    Share2,
    Truck,
    Shield,
    Phone,
    RotateCcw,
    ThumbsUp,
    MessageCircle,
    User,
} from 'lucide-react';
import { productService } from '../../services/productService';
import { cartService } from '../../services/cartService';
import { ratingService } from '../../services/ratingService';
import { toast } from 'sonner';
import type { Product } from '../../types/product';
import type { Rating } from '../../types/rating';

const ProductDetail: React.FC = () => {
    const { productName } = useParams<{ productName: string }>();
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAppSelector((state) => state.auth);

    const [product, setProduct] = useState<Product | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);
    const [isAddingToCart, setIsAddingToCart] = useState(false);

    // Rating states
    const [ratings, setRatings] = useState<Rating[]>([]);
    const [ratingsLoading, setRatingsLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalRatings, setTotalRatings] = useState(0);
    const [userRating, setUserRating] = useState<Rating | null>(null);
    const [isEditingRating, setIsEditingRating] = useState(false);
    const [isUpdatingRating, setIsUpdatingRating] = useState(false);
    const [averageRating, setAverageRating] = useState<number>(0);
    const [showDeleteDialog, setShowDeleteDialog] = useState(false);
    const [editRating, setEditRating] = useState({
        ratingStar: 5,
        comment: ''
    });

    // Fetch product data
    useEffect(() => {
        const fetchProduct = async () => {
            if (!productName) {
                console.warn('⚠️ No productName in URL params');
                return;
            }

            try {
                setIsLoading(true);
                
                // productName từ URL params chính là productId (không cần parse gì cả)
                const productId = productName;
                
                console.log('🔍 URL param productName:', productName);
                console.log('🔑 ProductId:', productId);
                console.log('📡 Will call API: /api/product/' + productId);
                
                const response = await productService.getProductById(productId);

                if (response.success && response.data) {
                    console.log('✅ Product data from API:', response.data);
                    setProduct(response.data);
                } else {
                    console.error('❌ Failed to fetch product:', response);
                    toast.error(response.message || 'Không tìm thấy sản phẩm');
                    navigate('/');
                }
            } catch (error) {
                console.error('💥 Error fetching product:', error);
                toast.error('Đã xảy ra lỗi khi tải sản phẩm');
                navigate('/');
            } finally {
                setIsLoading(false);
            }
        };

        fetchProduct();
    }, [productName, navigate]);

    // Fetch product ratings
    useEffect(() => {
        const fetchRatings = async () => {
            if (!product?.productId) return;

            setRatingsLoading(true);
            try {
                // Fetch ratings list - Backend trả về Spring Boot Page format trực tiếp
                const response: any = await ratingService.getRatingsByProduct(product.productId, currentPage, 5);
                console.log('📊 Ratings API Response:', response);
                
                // Backend trả về trực tiếp Spring Boot Page format (không có wrapper success/data)
                if (response && response.content) {
                    console.log('✅ Ratings content:', response.content);
                    console.log('📏 Total ratings:', response.totalElements);
                    
                    setRatings(response.content);
                    setTotalPages(response.totalPages);
                    setTotalRatings(response.totalElements);
                } else if (response.success && response.data) {
                    // Fallback: Nếu có wrapper
                    console.log('✅ Ratings data (with wrapper):', response.data);
                    setRatings(response.data.content);
                    setTotalPages(response.data.totalPages);
                    setTotalRatings(response.data.totalElements);
                }

                // Fetch average rating
                const avgResponse: any = await ratingService.getAverageRatingByProduct(product.productId);
                console.log('⭐ Average rating response:', avgResponse);
                
                if (typeof avgResponse === 'number') {
                    setAverageRating(avgResponse);
                } else if (avgResponse.success && typeof avgResponse.data === 'number') {
                    setAverageRating(avgResponse.data);
                }
            } catch (error) {
                console.error('❌ Error fetching ratings:', error);
            } finally {
                setRatingsLoading(false);
            }
        };

        fetchRatings();
    }, [product, currentPage]);

    // Debug: Log ratings state whenever it changes
    useEffect(() => {
        console.log('🔍 DEBUG Ratings State:');
        console.log('- ratings:', ratings);
        console.log('- ratings.length:', ratings.length);
        console.log('- userRating:', userRating);
        console.log('- ratingsLoading:', ratingsLoading);
        
        // Check if any rating belongs to current user
        if (user && ratings.length > 0) {
            console.log('👤 Current user accountId:', user.accountId);
            ratings.forEach((rating, index) => {
                console.log(`Rating ${index}:`, {
                    ratingId: rating.ratingId,
                    accountId: rating.account?.accountId,
                    accountName: rating.account?.accountName,
                    isCurrentUser: rating.account?.accountId === user.accountId ? '✅ YES' : '❌ NO'
                });
            });
            
            // 🔧 FIX: If userRating is null but we found user's rating in the list, set it
            if (!userRating) {
                const foundUserRating = ratings.find(rating => rating.account?.accountId === user.accountId);
                if (foundUserRating) {
                    console.log('🔧 Found user rating in list, setting userRating:', foundUserRating);
                    setUserRating(foundUserRating);
                }
            }
        }
        
        const filteredRatings = ratings.filter(rating => rating.ratingId !== userRating?.ratingId);
        console.log('- Filtered ratings (excluding user):', filteredRatings);
        console.log('- Filtered length:', filteredRatings.length);
    }, [ratings, userRating, ratingsLoading]);

    // Fetch user's existing rating for this product
    useEffect(() => {
        const fetchUserRating = async () => {
            console.log('🔍 Fetching user rating...');
            console.log('- isAuthenticated:', isAuthenticated);
            console.log('- user:', user);
            console.log('- product?.productId:', product?.productId);
            
            if (!isAuthenticated || !user || !product?.productId) {
                console.log('❌ Cannot fetch user rating - missing data');
                return;
            }

            try {
                console.log('📡 Calling getRatingByAccountAndProduct:', {
                    accountId: user.accountId,
                    productId: product.productId
                });
                
                const ratings = await ratingService.getRatingByAccountAndProduct(user.accountId, product.productId);
                
                console.log('📦 Response from getRatingByAccountAndProduct:', ratings);
                
                if (ratings && ratings.length > 0) {
                    // Get the most recent rating (last one in array)
                    const mostRecentRating = ratings[ratings.length - 1];
                    console.log('✅ User has rated this product. Most recent rating:', mostRecentRating);
                    setUserRating(mostRecentRating);
                } else {
                    console.log('⚠️ User has not rated this product yet');
                    setUserRating(null);
                }
            } catch (error: any) {
                console.log('❌ Error fetching user rating:', error);
                console.log('Error response:', error.response?.data);
                // If error, user hasn't rated yet
                setUserRating(null);
            }
        };

        fetchUserRating();
    }, [isAuthenticated, user, product]);

    // Debug: Log when userRating changes
    useEffect(() => {
        console.log('🔄 userRating changed:', userRating);
        if (userRating) {
            console.log('✅ User HAS rated this product');
            console.log('- Rating ID:', userRating.ratingId);
            console.log('- Account:', userRating.account);
            console.log('- Stars:', userRating.ratingStar);
            console.log('- Comment:', userRating.comment);
        } else {
            console.log('⚠️ User has NOT rated this product (userRating is null)');
        }
    }, [userRating]);

    // Handle rating update
    const handleUpdateRating = async () => {
        if (!isAuthenticated || !user || !product || !userRating) {
            toast.error('Không thể cập nhật đánh giá');
            return;
        }

        if (!editRating.comment.trim()) {
            toast.error('Vui lòng nhập nội dung đánh giá');
            return;
        }

        setIsUpdatingRating(true);
        
        try {
            const updateData = {
                comment: editRating.comment,
                ratingStar: editRating.ratingStar,
                status: 1 // Keep status active
            };

            const response = await ratingService.updateRating(userRating.ratingId, updateData);
            
            console.log('📤 Update rating response:', response);
            console.log('- response.success:', response.success);
            console.log('- response.data:', response.data);
            
            // Backend trả về trực tiếp RatingResponse, không có wrapper
            // Check nếu có ratingId là thành công
            const isSuccess = response.success === true || (response as any).ratingId;
            
            if (isSuccess) {
                console.log('✅ Rating updated successfully, showing toast...');
                
                toast.success('Đánh giá của bạn đã được cập nhật thành công!', {
                    duration: 2000,
                });
                
                console.log('✅ Toast called');
                
                // Reload page sau khi cập nhật thành công
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                toast.error(response.message || 'Không thể cập nhật đánh giá. Vui lòng thử lại!');
                setIsUpdatingRating(false);
            }
        } catch (error: any) {
            console.error('Error updating rating:', error);
            const errorMessage = error.response?.data?.message || 'Đã xảy ra lỗi khi cập nhật đánh giá';
            toast.error(errorMessage);
            setIsUpdatingRating(false);
        }
    };

    // Handle start editing rating
    const handleStartEdit = () => {
        if (userRating) {
            setEditRating({
                ratingStar: userRating.ratingStar,
                comment: userRating.comment
            });
            setIsEditingRating(true);
        }
    };

    // Handle cancel editing
    const handleCancelEdit = () => {
        setIsEditingRating(false);
        setEditRating({
            ratingStar: 5,
            comment: ''
        });
    };

    // Handle delete rating (soft delete by changing status)
    const handleDeleteRating = async () => {
        if (!isAuthenticated || !user || !product || !userRating) {
            toast.error('Không thể xóa đánh giá');
            return;
        }

        try {
            const response = await ratingService.changeRatingStatus(userRating.ratingId);
            
            console.log('📤 Delete rating response:', response);
            
            // Backend returns RatingResponse with updated status
            const isSuccess = response.success === true || (response as any).ratingId;
            
            if (isSuccess) {
                console.log('✅ Rating deleted successfully');
                
                toast.success('Đánh giá của bạn đã được xóa thành công!', {
                    duration: 2000,
                });
                
                // Close dialog and reload page
                setShowDeleteDialog(false);
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                toast.error(response.message || 'Không thể xóa đánh giá. Vui lòng thử lại!');
            }
        } catch (error: any) {
            console.error('Error deleting rating:', error);
            const errorMessage = error.response?.data?.message || 'Đã xảy ra lỗi khi xóa đánh giá';
            toast.error(errorMessage);
        }
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    };

    const formatDiscountPrice = (price: number, discount: number) => {
        const discountedPrice = price * (1 - discount / 100);
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(discountedPrice);
    };

    // Function to convert number to Vietnamese text
    const numberToVietnameseText = (num: number): string => {
        const ones = ['', 'một', 'hai', 'ba', 'bốn', 'năm', 'sáu', 'bảy', 'tám', 'chín'];
        const tens = [
            '',
            '',
            'hai mươi',
            'ba mươi',
            'bốn mươi',
            'năm mươi',
            'sáu mươi',
            'bảy mươi',
            'tám mươi',
            'chín mươi',
        ];
        const hundreds = [
            '',
            'một trăm',
            'hai trăm',
            'ba trăm',
            'bốn trăm',
            'năm trăm',
            'sáu trăm',
            'bảy trăm',
            'tám trăm',
            'chín trăm',
        ];

        if (num === 0) return 'không đồng';
        if (num < 0) return 'âm ' + numberToVietnameseText(-num);

        let result = '';

        // Handle millions
        if (num >= 1000000) {
            const millions = Math.floor(num / 1000000);
            if (millions === 1) {
                result += 'một triệu ';
            } else {
                result += ones[millions] + ' triệu ';
            }
            num %= 1000000;
        }

        // Handle thousands
        if (num >= 1000) {
            const thousands = Math.floor(num / 1000);
            if (thousands === 1) {
                result += 'một nghìn ';
            } else if (thousands < 10) {
                result += ones[thousands] + ' nghìn ';
            } else if (thousands < 100) {
                if (thousands >= 20) {
                    const ten = Math.floor(thousands / 10);
                    const one = thousands % 10;
                    result += tens[ten];
                    if (one > 0) {
                        result += ' ' + ones[one];
                    }
                } else if (thousands >= 10) {
                    if (thousands === 10) {
                        result += 'mười';
                    } else {
                        result += 'mười ' + ones[thousands % 10];
                    }
                }
                result += ' nghìn ';
            } else {
                const hundred = Math.floor(thousands / 100);
                const remainder = thousands % 100;
                result += hundreds[hundred] + ' ';
                if (remainder > 0) {
                    if (remainder >= 20) {
                        const ten = Math.floor(remainder / 10);
                        const one = remainder % 10;
                        result += tens[ten];
                        if (one > 0) {
                            result += ' ' + ones[one];
                        }
                    } else if (remainder >= 10) {
                        if (remainder === 10) {
                            result += 'mười';
                        } else {
                            result += 'mười ' + ones[remainder % 10];
                        }
                    } else {
                        result += ones[remainder];
                    }
                }
                result += ' nghìn ';
            }
            num %= 1000;
        }

        // Handle hundreds
        if (num >= 100) {
            const hundred = Math.floor(num / 100);
            result += hundreds[hundred] + ' ';
            num %= 100;
        }

        // Handle tens and ones
        if (num >= 20) {
            const ten = Math.floor(num / 10);
            const one = num % 10;
            result += tens[ten];
            if (one > 0) {
                result += ' ' + ones[one];
            }
        } else if (num >= 10) {
            if (num === 10) {
                result += 'mười';
            } else {
                result += 'mười ' + ones[num % 10];
            }
        } else if (num > 0) {
            result += ones[num];
        }

        return result.trim() + ' đồng';
    };

    // Helper function to get image URL
    const getImageUrl = (imageUrl: string | undefined | null) => {
        if (!imageUrl || typeof imageUrl !== 'string') {
            return '/placeholder-image.jpg';
        }

        if (imageUrl.startsWith('http')) {
            return imageUrl;
        }

        if (imageUrl.startsWith('/products/')) {
            return imageUrl;
        }

        const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
        return `/products/${cleanUrl}`;
    };

    const handleAddToCart = async () => {
        if (!isAuthenticated || !user || !product) {
            toast.error('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng');
            return;
        }

        setIsAddingToCart(true);
        try {
            const response = await cartService.addToCart({
                productId: product.productId,
                quantity: quantity,
                accountId: user.accountId,
            });

            if (response.success) {
                toast.success('Đã thêm sản phẩm vào giỏ hàng');
                window.dispatchEvent(new CustomEvent('cartUpdated'));
            } else {
                toast.error(response.message || 'Không thể thêm sản phẩm vào giỏ hàng');
            }
        } catch (error: any) {
            console.error('Error adding to cart:', error);
            toast.error(error.response?.data?.message || error.message || 'Đã xảy ra lỗi khi thêm vào giỏ hàng');
        } finally {
            setIsAddingToCart(false);
        }
    };

    const handleBuyNow = async () => {
        if (!isAuthenticated || !user) {
            toast.error('Vui lòng đăng nhập để mua hàng');
            return;
        }

        if (quantity <= 0) {
            toast.error('Số lượng phải lớn hơn 0');
            return;
        }

        try {
            // Clear current cart first
            await cartService.clearCart(user.accountId);

            // Add only this product to cart
            const response = await cartService.addToCart({
                productId: product.productId,
                quantity: quantity,
                accountId: user.accountId,
            });

            if (response.success) {
                toast.success('Đã thêm sản phẩm vào giỏ hàng');
                window.dispatchEvent(new CustomEvent('cartUpdated'));

                // Navigate to checkout
                navigate('/checkout');
            } else {
                toast.error(response.message || 'Không thể thêm sản phẩm vào giỏ hàng');
            }
        } catch (error: any) {
            console.error('Error in buy now:', error);
            toast.error(error.response?.data?.message || error.message || 'Đã xảy ra lỗi khi mua hàng');
        }
    };

    // Render star rating
    const renderStars = (rating: number, interactive: boolean = false, onStarClick?: (star: number) => void) => {
        return (
            <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                        key={star}
                        className={`w-5 h-5 ${
                            star <= rating
                                ? 'fill-yellow-400 text-yellow-400'
                                : 'fill-gray-200 text-gray-200'
                        } ${interactive ? 'cursor-pointer hover:scale-110 transition-transform' : ''}`}
                        onClick={() => interactive && onStarClick?.(star)}
                    />
                ))}
            </div>
        );
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <TopNavigation />
                <div className="container mx-auto px-4 py-8">
                    <div className="max-w-6xl mx-auto">
                        <div className="bg-white p-8">
                            <div className="animate-pulse">
                                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                                    <div className="space-y-4">
                                        <div className="aspect-[3/4] bg-gray-200"></div>
                                    </div>
                                    <div className="space-y-4">
                                        <div className="h-8 bg-gray-200"></div>
                                        <div className="h-4 bg-gray-200 w-3/4"></div>
                                        <div className="h-6 bg-gray-200 w-1/2"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    if (!product) {
        return (
            <div className="min-h-screen bg-gray-50">
                <TopNavigation />
                <div className="container mx-auto px-4 py-8">
                    <div className="max-w-6xl mx-auto">
                        <div className="bg-white p-8 text-center">
                            <h1 className="text-2xl font-bold text-gray-900 mb-4">Không tìm thấy sản phẩm</h1>
                            <Button onClick={() => navigate('/')} className="bg-blue-600 hover:bg-blue-700">
                                Quay về trang chủ
                            </Button>
                        </div>
                    </div>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <TopNavigation />

            <main className="container mx-auto px-4 py-8">
                <div className="max-w-6xl mx-auto">
                    <div className="bg-white p-8">
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                            {/* Left Column - Product Images */}
                            <div className="space-y-4">
                                {/* Main Image */}
                                <div className="relative aspect-[3/4] bg-gray-100 overflow-hidden">
                                    {product.images &&
                                    product.images.length > 0 &&
                                    product.images[currentImageIndex] ? (
                                        <img
                                            src={getImageUrl(product.images[currentImageIndex].imageUrl)}
                                            alt={product.productName}
                                            className="w-full h-full object-cover"
                                            onError={(e) => {
                                                e.currentTarget.style.display = 'none';
                                            }}
                                        />
                                    ) : product.imageUrl ? (
                                        <img
                                            src={getImageUrl(product.imageUrl)}
                                            alt={product.productName}
                                            className="w-full h-full object-cover"
                                            onError={(e) => {
                                                e.currentTarget.style.display = 'none';
                                            }}
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center">
                                            <span className="text-gray-400 text-lg">No Image</span>
                                        </div>
                                    )}

                                    {/* Navigation Arrow */}
                                    {product.images && product.images.length > 1 && (
                                        <button
                                            onClick={() =>
                                                setCurrentImageIndex((prev) => (prev + 1) % product.images!.length)
                                            }
                                            className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-white bg-opacity-80 hover:bg-opacity-100 p-2 transition-all duration-200"
                                        >
                                            <span className="text-gray-600 font-bold text-lg">&gt;</span>
                                        </button>
                                    )}
                                </div>

                                {/* Thumbnail Images */}
                                {product.images && product.images.length > 1 && (
                                    <div className="flex space-x-2 overflow-x-auto">
                                        {product.images
                                            .filter((image) => image && image.imageUrl)
                                            .map((image, index) => (
                                                <button
                                                    key={index}
                                                    onClick={() => setCurrentImageIndex(index)}
                                                    className={`flex-shrink-0 w-20 h-20 border-2 overflow-hidden ${
                                                        index === currentImageIndex
                                                            ? 'border-blue-500'
                                                            : 'border-gray-200'
                                                    }`}
                                                >
                                                    <img
                                                        src={getImageUrl(image.imageUrl)}
                                                        alt={`${product.productName} ${index + 1}`}
                                                        className="w-full h-full object-cover"
                                                        onError={(e) => {
                                                            e.currentTarget.style.display = 'none';
                                                        }}
                                                    />
                                                </button>
                                            ))}
                                    </div>
                                )}

                                {/* Social Sharing */}
                                <div className="flex items-center space-x-4 pt-4 border-t border-gray-200">
                                    <span className="text-sm text-gray-600">Chia sẻ:</span>
                                    <div className="flex space-x-2">
                                        <button className="w-8 h-8 bg-blue-600 text-white flex items-center justify-center text-sm font-bold">
                                            f
                                        </button>
                                        <button className="w-8 h-8 bg-blue-500 text-white flex items-center justify-center">
                                            <Share2 className="w-4 h-4" />
                                        </button>
                                        <button className="w-8 h-8 bg-blue-400 text-white flex items-center justify-center">
                                            <Share2 className="w-4 h-4" />
                                        </button>
                                        <button className="w-8 h-8 bg-red-600 text-white flex items-center justify-center text-sm font-bold">
                                            P
                                        </button>
                                        <button className="w-8 h-8 bg-gray-500 text-white flex items-center justify-center">
                                            <Share2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {/* Right Column - Product Info */}
                            <div className="space-y-6">
                                {/* Product Title */}
                                <div>
                                    <h1 className="text-2xl font-bold text-gray-900 mb-4">
                                        <span className="text-red-700 font-black">[{product.productId}]</span> -{' '}
                                        {product.productName}
                                    </h1>
                                </div>

                                {/* Product Metadata */}
                                <div className="space-y-2">
                                    <div className="flex flex-wrap items-center gap-4">
                                        <div className="flex items-center space-x-2">
                                            <span className="text-sm text-gray-600">Tình trạng:</span>
                                            <Badge
                                                className={
                                                    product.status === 1
                                                        ? 'bg-green-100 text-green-800 border-green-300'
                                                        : 'bg-red-100 text-red-800 border-red-300'
                                                }
                                            >
                                                {product.status === 1 ? 'Còn hàng' : 'Hết hàng'}
                                            </Badge>
                                        </div>
                                        {/* author and cover removed */}
                                        <div className="flex items-center space-x-2">
                                            <span className="text-sm text-gray-600">Kho:</span>
                                            <span className="text-sm font-bold text-orange-700">
                                                {product.stock} sản phẩm
                                            </span>
                                        </div>
                                    </div>
                                    {product.categories && product.categories.length > 0 && (
                                        <div className="flex items-center space-x-2">
                                            <span className="text-sm text-gray-600">Danh mục:</span>
                                            <div className="flex flex-wrap gap-1">
                                                {product.categories.map((category, index) => (
                                                    <Badge
                                                        key={index}
                                                        className="bg-blue-600 text-white border-blue-600 text-xs font-bold"
                                                    >
                                                        {category.categoryName}
                                                    </Badge>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>

                                {/* Price */}
                                <div>
                                    <span className="text-sm text-gray-600">Giá:</span>
                                    <div className="flex items-center gap-3 mt-1">
                                        <div className="text-3xl font-bold text-red-600">
                                            {product.discount
                                                ? formatDiscountPrice(product.price, product.discount)
                                                : formatPrice(product.price)}
                                        </div>
                                        <div className="text-sm text-gray-400">
                                            (
                                            {product.discount
                                                ? numberToVietnameseText(
                                                      Math.floor(product.price * (1 - product.discount / 100)),
                                                  )
                                                : numberToVietnameseText(product.price)}
                                            )
                                        </div>
                                    </div>
                                    {product.discount && (
                                        <div className="text-lg text-gray-500 line-through mt-1">
                                            {formatPrice(product.price)}
                                        </div>
                                    )}
                                </div>

                                {/* Quantity Selector */}
                                <div className="flex items-center space-x-4">
                                    <span className="text-sm text-gray-600">Số lượng:</span>
                                    <div className="flex items-center border border-gray-300 overflow-hidden">
                                        <button
                                            onClick={() => setQuantity(Math.max(1, quantity - 1))}
                                            className="w-8 h-8 bg-gray-50 hover:bg-gray-100 flex items-center justify-center transition-all duration-200"
                                        >
                                            <Minus className="w-4 h-4 text-gray-600" />
                                        </button>
                                        <input
                                            type="number"
                                            value={quantity}
                                            onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                                            className="w-12 h-8 text-center border-0 focus:ring-0 focus:outline-none bg-white font-medium text-sm"
                                            min="1"
                                        />
                                        <button
                                            onClick={() => setQuantity(quantity + 1)}
                                            className="w-8 h-8 bg-gray-50 hover:bg-gray-100 flex items-center justify-center transition-all duration-200"
                                        >
                                            <Plus className="w-4 h-4 text-gray-600" />
                                        </button>
                                    </div>
                                </div>

                                {/* Action Buttons */}
                                <div className="flex space-x-4">
                                    <Button
                                        onClick={handleAddToCart}
                                        disabled={isAddingToCart}
                                        className="bg-white border-2 border-red-600 text-red-600 hover:bg-red-50 px-6 py-3 flex items-center space-x-2"
                                    >
                                        <ShoppingBag className="w-5 h-5" />
                                        <span>THÊM VÀO GIỎ</span>
                                    </Button>
                                    <Button
                                        onClick={handleBuyNow}
                                        className="bg-red-600 hover:bg-red-700 text-white px-8 py-3 flex items-center space-x-2"
                                    >
                                        <span>MUA NGAY</span>
                                    </Button>
                                </div>

                                {/* Service Information */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-6 border-t border-gray-200">
                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <MessageCircle className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Tư vấn chọn sách
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <Truck className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Miễn phí vận chuyển từ 350.000₫ ở HCM, từ 500.000₫ các tỉnh khác
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <Shield className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Cam kết 100% sách thật
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <Phone className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Hỗ trợ 24/7 - Hotline: 0903400028
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <ThumbsUp className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Mở hộp kiểm tra nhận hàng
                                            </p>
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4">
                                        <div className="flex-shrink-0">
                                            <RotateCcw className="w-8 h-8 text-gray-700" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-800 leading-relaxed">
                                                Đổi trả nếu hư hỏng
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Product Description */}
                        <div className="mt-12 pt-8 border-t border-gray-200">
                            <h2 className="text-xl font-bold text-gray-900 mb-4">MÔ TẢ SẢN PHẨM</h2>
                            <div className="prose max-w-none">
                                <p className="text-gray-700 leading-relaxed mb-4">
                                    {product.description || 'Chưa có mô tả sản phẩm.'}
                                </p>
                                <div className="mt-4">
                                    <h3 className="text-lg font-semibold text-gray-900 mb-2">Thông tin sách:</h3>
                                    <ul className="list-disc list-inside space-y-1 text-gray-700">
                                        {/* author and cover removed from details */}
                                        <li>Mã sản phẩm: {product.productId}</li>
                                        <li>Số lượng còn lại: {product.stock} sản phẩm</li>
                                        <li>Giá: {formatPrice(product.price)}</li>
                                        <li>Ngày tạo: {new Date(product.createdAt).toLocaleDateString('vi-VN')}</li>
                                        {product.categories && product.categories.length > 0 && (
                                            <li>
                                                <span className="block mb-2">Danh mục:</span>
                                                <div className="flex flex-wrap gap-2">
                                                    {product.categories.map((cat) => (
                                                        <span
                                                            key={cat.categoryId}
                                                            className="px-3 py-1 bg-blue-100 text-blue-800 text-sm rounded-full border border-blue-200"
                                                        >
                                                            {cat.categoryName}
                                                        </span>
                                                    ))}
                                                </div>
                                            </li>
                                        )}
                                    </ul>
                                </div>
                            </div>
                        </div>

                        {/* Customer Reviews Section */}
                        <div className="mt-12 pt-8 border-t border-gray-200">
                            <div className="flex items-center justify-between mb-6">
                                <div>
                                    <h2 className="text-xl font-bold text-gray-900 mb-2">ĐÁNH GIÁ KHÁCH HÀNG</h2>
                                    <div className="flex items-center gap-4">
                                        <div className="flex items-center gap-2">
                                            <span className="text-3xl font-bold text-yellow-500">{averageRating.toFixed(1)}</span>
                                            {renderStars(averageRating)}
                                        </div>
                                        <span className="text-gray-600">({totalRatings} đánh giá)</span>
                                    </div>
                                </div>
                            </div>

                            {/* User's Existing Review */}
                            {(() => {
                                console.log('🎨 Render check:', { 
                                    hasUserRating: !!userRating, 
                                    isEditingRating,
                                    shouldShowEditButton: userRating && !isEditingRating,
                                    userRating 
                                });
                                return null;
                            })()}
                            {userRating && !isEditingRating && (
                                <div className="bg-blue-50 p-6 rounded-lg mb-6 border-2 border-blue-200">
                                    <div className="flex items-start justify-between mb-3">
                                        <div className="flex items-start gap-4 flex-1">
                                            <div className="flex-shrink-0">
                                                <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
                                                    <User className="w-6 h-6 text-white" />
                                                </div>
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-2 mb-1 flex-wrap">
                                                    <p className="font-semibold text-gray-900">{userRating.account?.accountName || 'Bạn'}</p>
                                                    <Badge className="bg-blue-600 text-white">Đánh giá của bạn</Badge>
                                                    {userRating.status === 0 && (
                                                        <Badge className="bg-red-600 text-white">⚠️ Đã bị ẩn bởi quản trị viên</Badge>
                                                    )}
                                                </div>
                                                <p className="text-sm text-gray-500">
                                                    {new Date(userRating.createdAt || '').toLocaleDateString('vi-VN')}
                                                    {userRating.updatedAt && userRating.updatedAt !== userRating.createdAt && (
                                                        <span className="ml-2">(Đã chỉnh sửa)</span>
                                                    )}
                                                </p>
                                            </div>
                                        </div>
                                        <div className="flex gap-2 flex-shrink-0 ml-4">
                                            <Button
                                                onClick={handleStartEdit}
                                                className="bg-white hover:bg-gray-100 text-gray-700 border border-gray-300 px-4 py-2 text-sm font-medium rounded-md shadow-sm"
                                            >
                                                ✏️ Sửa
                                            </Button>
                                            <Button
                                                onClick={() => setShowDeleteDialog(true)}
                                                className="bg-red-50 hover:bg-red-100 text-red-600 border border-red-300 px-4 py-2 text-sm font-medium rounded-md shadow-sm"
                                            >
                                                🗑️ Xóa
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="ml-16">
                                        {renderStars(userRating.ratingStar)}
                                        <p className="text-gray-700 mt-2 leading-relaxed">{userRating.comment}</p>
                                    </div>
                                </div>
                            )}

                            {/* Edit Review Form */}
                            {userRating && isEditingRating && (
                                <div className="bg-blue-50 p-6 rounded-lg mb-6 border-2 border-blue-200">
                                    <h3 className="text-lg font-semibold text-gray-900 mb-4">Chỉnh sửa đánh giá của bạn</h3>
                                    <div className="space-y-4">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                                Đánh giá của bạn
                                            </label>
                                            {renderStars(editRating.ratingStar, true, (star) => setEditRating({ ...editRating, ratingStar: star }))}
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                                Nhận xét
                                            </label>
                                            <textarea
                                                value={editRating.comment}
                                                onChange={(e) => setEditRating({ ...editRating, comment: e.target.value })}
                                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                                rows={4}
                                                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
                                            />
                                        </div>
                                        <div className="flex gap-2">
                                            <Button
                                                onClick={handleUpdateRating}
                                                disabled={isUpdatingRating}
                                                className="bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                {isUpdatingRating ? 'Đang cập nhật...' : 'Cập nhật đánh giá'}
                                            </Button>
                                            <Button
                                                onClick={handleCancelEdit}
                                                disabled={isUpdatingRating}
                                                className="bg-gray-200 hover:bg-gray-300 text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                                            >
                                                Hủy
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Reviews List */}
                            {ratingsLoading ? (
                                <div className="space-y-4">
                                    {[1, 2, 3].map((i) => (
                                        <div key={i} className="animate-pulse bg-gray-100 p-6 rounded-lg">
                                            <div className="flex items-start gap-4">
                                                <div className="w-12 h-12 bg-gray-300 rounded-full"></div>
                                                <div className="flex-1 space-y-2">
                                                    <div className="h-4 bg-gray-300 rounded w-1/4"></div>
                                                    <div className="h-4 bg-gray-300 rounded w-1/2"></div>
                                                    <div className="h-4 bg-gray-300 rounded w-full"></div>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            ) : ratings.length === 0 ? (
                                <div className="text-center py-12">
                                    <Star className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                                    <p className="text-gray-500 text-lg">Chưa có đánh giá nào cho sản phẩm này</p>
                                    <p className="text-gray-400 text-sm mt-2">Hãy là người đầu tiên đánh giá sản phẩm!</p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {ratings
                                        .filter(rating => rating.ratingId !== userRating?.ratingId)
                                        .map((rating) => {
                                            const isOwnRating = user && rating.account?.accountId === user.accountId;
                                            
                                            return (
                                                <div key={rating.ratingId} className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-md transition-shadow">
                                                    <div className="flex items-start gap-4">
                                                        <div className="flex-shrink-0">
                                                            <div className={`w-12 h-12 ${isOwnRating ? 'bg-blue-600' : 'bg-gradient-to-br from-blue-500 to-purple-600'} rounded-full flex items-center justify-center`}>
                                                                <User className="w-6 h-6 text-white" />
                                                            </div>
                                                        </div>
                                                        <div className="flex-1 min-w-0">
                                                            <div className="flex items-center justify-between mb-2">
                                                                <div className="flex items-center gap-2 flex-wrap">
                                                                    <p className="font-semibold text-gray-900">{rating.account?.accountName || 'Khách hàng'}</p>
                                                                    {isOwnRating && (
                                                                        <Badge className="bg-blue-600 text-white">Đánh giá của bạn</Badge>
                                                                    )}
                                                                </div>
                                                                {isOwnRating && (
                                                                    <div className="flex gap-2 flex-shrink-0 ml-2">
                                                                        <Button
                                                                            onClick={() => {
                                                                                setUserRating(rating);
                                                                                handleStartEdit();
                                                                            }}
                                                                            className="bg-white hover:bg-gray-100 text-gray-700 border border-gray-300 px-3 py-1.5 text-xs font-medium rounded-md shadow-sm"
                                                                        >
                                                                            ✏️ Sửa
                                                                        </Button>
                                                                        <Button
                                                                            onClick={() => {
                                                                                setUserRating(rating);
                                                                                setShowDeleteDialog(true);
                                                                            }}
                                                                            className="bg-red-50 hover:bg-red-100 text-red-600 border border-red-300 px-3 py-1.5 text-xs font-medium rounded-md shadow-sm"
                                                                        >
                                                                            🗑️ Xóa
                                                                        </Button>
                                                                    </div>
                                                                )}
                                                            </div>
                                                            <p className="text-sm text-gray-500">
                                                                {new Date(rating.createdAt || '').toLocaleDateString('vi-VN', {
                                                                    year: 'numeric',
                                                                    month: 'long',
                                                                    day: 'numeric'
                                                                })}
                                                                {rating.updatedAt && rating.updatedAt !== rating.createdAt && (
                                                                    <span className="ml-2">(Đã chỉnh sửa)</span>
                                                                )}
                                                            </p>
                                                            {renderStars(rating.ratingStar)}
                                                            <p className="text-gray-700 mt-3 leading-relaxed">{rating.comment}</p>
                                                        </div>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                </div>
                            )}

                            {/* Pagination */}
                            {totalPages > 1 && (
                                <div className="mt-8 flex justify-center gap-2">
                                    <Button
                                        onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                                        disabled={currentPage === 0}
                                        className="bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Trang trước
                                    </Button>
                                    <div className="flex items-center gap-2">
                                        {Array.from({ length: totalPages }, (_, i) => (
                                            <button
                                                key={i}
                                                onClick={() => setCurrentPage(i)}
                                                className={`w-10 h-10 rounded-lg font-medium transition-colors ${
                                                    currentPage === i
                                                        ? 'bg-blue-600 text-white'
                                                        : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                                }`}
                                            >
                                                {i + 1}
                                            </button>
                                        ))}
                                    </div>
                                    <Button
                                        onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                                        disabled={currentPage === totalPages - 1}
                                        className="bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        Trang sau
                                    </Button>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </main>

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle className="text-xl font-bold text-gray-900">
                            Xác nhận xóa đánh giá
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-gray-600 text-base">
                            Bạn có chắc chắn muốn xóa đánh giá này? Hành động này không thể hoàn tác.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel className="bg-gray-100 hover:bg-gray-200 text-gray-700">
                            Hủy
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={handleDeleteRating}
                            className="bg-red-600 hover:bg-red-700 text-white"
                        >
                            Xóa đánh giá
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            <Footer />
        </div>
    );
};

export default ProductDetail;
