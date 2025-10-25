import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../hooks/redux';
import TopNavigation from '../../components/ui/Header/Header';
import Footer from '../../components/ui/Footer/Footer';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { User, Phone, Save, Edit, X, Eye, EyeOff, Lock, Shield, MapPin, Home } from 'lucide-react';
import { accountService } from '../../services/adminAccountService';
import { shippingService } from '../../services/shippingService';
import { toast } from 'sonner';

const ProfileCustomer: React.FC = () => {
    const navigate = useNavigate();
    const { user } = useAppSelector((state) => state.auth);

    const [isEditing, setIsEditing] = useState(false);
    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [accountData, setAccountData] = useState<any>(null);
    const [shippingData, setShippingData] = useState<any>(null);

    // Form data
    const [formData, setFormData] = useState({
        accountName: '',
        accountPhone: '',
        address: '',
        city: '',
    });

    // Password change form
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    useEffect(() => {
        if (!user) {
            navigate('/');
            return;
        }

        // Load account data from API
        loadAccountData();
    }, [user, navigate]);

    // Debug: Log formData changes
    useEffect(() => {
        console.log('FormData changed:', formData);
    }, [formData]);

    const loadAccountData = async () => {
        if (!user?.accountId) return;

        try {
            setLoading(true);

            // Load account data
            const accountResponse = await accountService.getAccountById(user.accountId);
            console.log('Account response:', accountResponse); // Debug log
            setAccountData(accountResponse);

            // Load shipping data
            try {
                const shippingResponse = await shippingService.getShippingByAccount(user.accountId);
                console.log('Shipping response:', shippingResponse); // Debug log

                if (shippingResponse.success && shippingResponse.data && shippingResponse.data.length > 0) {
                    const firstShipping = shippingResponse.data[0];
                    console.log('First shipping data:', firstShipping); // Debug log
                    setShippingData(firstShipping);
                    setFormData({
                        accountName: accountResponse.accountName || '',
                        accountPhone: accountResponse.accountPhone || '',
                        address: firstShipping.receiverAddress || '',
                        city: firstShipping.city || '',
                    });
                    console.log('Form data set with shipping:', {
                        accountName: accountResponse.accountName || '',
                        accountPhone: accountResponse.accountPhone || '',
                        address: firstShipping.receiverAddress || '',
                        city: firstShipping.city || '',
                    }); // Debug log
                } else {
                    console.log('No shipping data found'); // Debug log
                    // No shipping data found, set default values
                    setFormData({
                        accountName: accountResponse.accountName || '',
                        accountPhone: accountResponse.accountPhone || '',
                        address: '',
                        city: '',
                    });
                }
            } catch (shippingError) {
                console.error('Error loading shipping data:', shippingError);
                // Set default values if shipping data fails to load
                setFormData({
                    accountName: accountResponse.accountName || '',
                    accountPhone: accountResponse.accountPhone || '',
                    address: '',
                    city: '',
                });
            }
        } catch (error) {
            toast.error('Không thể tải thông tin tài khoản');
            console.error('Error loading account data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setPasswordData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSaveProfile = async () => {
        if (!user?.accountId) return;

        try {
            setLoading(true);

            // Update account information
            await accountService.updateAccount(user.accountId, {
                accountName: formData.accountName,
                accountPhone: formData.accountPhone,
                status: accountData?.status || 1,
            });

            // Update or create shipping address if personal info is provided
            if (formData.address && formData.city) {
                if (shippingData?.shippingId) {
                    // Update existing shipping
                    await shippingService.updateShipping(shippingData.shippingId, {
                        receiverName: formData.accountName,
                        receiverPhone: formData.accountPhone,
                        receiverAddress: formData.address,
                        city: formData.city,
                    });
                } else {
                    // Create new shipping
                    await shippingService.createShipping({
                        accountId: user.accountId,
                        receiverName: formData.accountName,
                        receiverPhone: formData.accountPhone,
                        receiverAddress: formData.address,
                        city: formData.city,
                    });
                }
            }

            toast.success('Cập nhật thông tin thành công!');
            setIsEditing(false);
            loadAccountData(); // Reload data
        } catch (error) {
            toast.error('Không thể cập nhật thông tin');
            console.error('Error updating profile:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleChangePassword = () => {
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            toast.error('Mật khẩu mới không khớp!');
            return;
        }

        // TODO: Implement API call to change password
        console.log('Changing password:', passwordData);
        setIsChangingPassword(false);
        setPasswordData({
            currentPassword: '',
            newPassword: '',
            confirmPassword: '',
        });
        toast.success('Đổi mật khẩu thành công!');
    };

    const handleCancelEdit = () => {
        setFormData({
            accountName: accountData?.accountName || '',
            accountPhone: accountData?.accountPhone || '',
            address: shippingData?.address || '',
            city: shippingData?.city || '',
        });
        setIsEditing(false);
    };

    if (!user) {
        return null;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <TopNavigation />

            <main className="container mx-auto px-4 py-8">
                <div className="max-w-4xl mx-auto">
                    {/* Header */}
                    <div className="mb-8">
                        <h1 className="text-3xl font-bold text-gray-900 mb-2">Thông tin tài khoản</h1>
                        <p className="text-gray-600">Quản lý thông tin cá nhân và bảo mật tài khoản</p>
                    </div>

                    <Tabs defaultValue="profile" className="space-y-6">
                        <TabsList className="grid w-full grid-cols-2 rounded-none">
                            <TabsTrigger value="profile" className="flex items-center gap-2 rounded-none">
                                <User className="w-4 h-4" />
                                Thông tin cá nhân
                            </TabsTrigger>
                            <TabsTrigger value="security" className="flex items-center gap-2 rounded-none">
                                <Shield className="w-4 h-4" />
                                Bảo mật
                            </TabsTrigger>
                        </TabsList>

                        <TabsContent value="profile">
                            <Card className="rounded-none border-2 border-gray-200">
                                <CardHeader className="border-b border-gray-200">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <CardTitle className="flex items-center gap-2">
                                                <User className="w-5 h-5 text-blue-600" />
                                                Thông tin cá nhân
                                            </CardTitle>
                                            <CardDescription>Cập nhật thông tin cá nhân của bạn</CardDescription>
                                        </div>
                                        {!isEditing ? (
                                            <Button
                                                onClick={() => setIsEditing(true)}
                                                className="bg-blue-600 hover:bg-blue-700 rounded-none"
                                            >
                                                <Edit className="w-4 h-4 mr-2" />
                                                Chỉnh sửa
                                            </Button>
                                        ) : (
                                            <div className="flex gap-2">
                                                <Button
                                                    onClick={handleCancelEdit}
                                                    variant="outline"
                                                    className="rounded-none"
                                                >
                                                    <X className="w-4 h-4 mr-2" />
                                                    Hủy
                                                </Button>
                                                <Button
                                                    onClick={handleSaveProfile}
                                                    className="bg-green-600 hover:bg-green-700 rounded-none"
                                                >
                                                    <Save className="w-4 h-4 mr-2" />
                                                    Lưu
                                                </Button>
                                            </div>
                                        )}
                                    </div>
                                </CardHeader>
                                <CardContent className="space-y-6 p-6">
                                    {loading ? (
                                        <div className="text-center py-8">
                                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                                            <p className="mt-2 text-gray-600">Đang tải thông tin...</p>
                                        </div>
                                    ) : (
                                        <>
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                                <div className="space-y-2">
                                                    <Label htmlFor="accountName" className="flex items-center gap-2">
                                                        <User className="w-4 h-4" />
                                                        Tên tài khoản
                                                    </Label>
                                                    <Input
                                                        id="accountName"
                                                        name="accountName"
                                                        value={formData.accountName}
                                                        onChange={handleInputChange}
                                                        disabled={!isEditing}
                                                        className="bg-gray-50 rounded-none border-2 border-gray-200"
                                                    />
                                                </div>

                                                <div className="space-y-2">
                                                    <Label htmlFor="accountPhone" className="flex items-center gap-2">
                                                        <Phone className="w-4 h-4" />
                                                        Số điện thoại
                                                    </Label>
                                                    <Input
                                                        id="accountPhone"
                                                        name="accountPhone"
                                                        value={formData.accountPhone}
                                                        onChange={handleInputChange}
                                                        disabled={!isEditing}
                                                        className="bg-gray-50 rounded-none border-2 border-gray-200"
                                                    />
                                                </div>

                                                <div className="space-y-2">
                                                    <Label htmlFor="address" className="flex items-center gap-2">
                                                        <Home className="w-4 h-4" />
                                                        Địa chỉ
                                                    </Label>
                                                    <Input
                                                        id="address"
                                                        name="address"
                                                        value={formData.address}
                                                        onChange={handleInputChange}
                                                        disabled={!isEditing}
                                                        className="bg-gray-50 rounded-none border-2 border-gray-200"
                                                    />
                                                </div>

                                                <div className="space-y-2">
                                                    <Label htmlFor="city" className="flex items-center gap-2">
                                                        <MapPin className="w-4 h-4" />
                                                        Tỉnh/Thành phố
                                                    </Label>
                                                    <Input
                                                        id="city"
                                                        name="city"
                                                        value={formData.city}
                                                        onChange={handleInputChange}
                                                        disabled={!isEditing}
                                                        className="bg-gray-50 rounded-none border-2 border-gray-200"
                                                    />
                                                </div>
                                            </div>

                                            {!isEditing && (
                                                <div className="bg-blue-50 border-2 border-blue-200 p-4">
                                                    <div className="flex items-center gap-2 text-blue-800">
                                                        <Shield className="w-4 h-4" />
                                                        <span className="font-medium">Vai trò:</span>
                                                        <span className="bg-blue-100 px-2 py-1 text-sm">
                                                            {user.roles?.some((role) => role.roleName === 'ADMIN')
                                                                ? 'Quản trị viên'
                                                                : 'Khách hàng'}
                                                        </span>
                                                    </div>
                                                </div>
                                            )}
                                        </>
                                    )}
                                </CardContent>
                            </Card>
                        </TabsContent>

                        <TabsContent value="security">
                            <Card className="rounded-none border-2 border-gray-200">
                                <CardHeader className="border-b border-gray-200">
                                    <CardTitle className="flex items-center gap-2">
                                        <Lock className="w-5 h-5 text-red-600" />
                                        Bảo mật tài khoản
                                    </CardTitle>
                                    <CardDescription>Thay đổi mật khẩu để bảo vệ tài khoản</CardDescription>
                                </CardHeader>
                                <CardContent className="p-6">
                                    {!isChangingPassword ? (
                                        <div className="space-y-4">
                                            <div className="bg-yellow-50 border-2 border-yellow-200 p-4">
                                                <div className="flex items-center gap-2 text-yellow-800">
                                                    <Shield className="w-4 h-4" />
                                                    <span className="font-medium">Bảo mật tài khoản</span>
                                                </div>
                                                <p className="text-yellow-700 text-sm mt-2">
                                                    Thay đổi mật khẩu định kỳ để đảm bảo an toàn cho tài khoản của bạn.
                                                </p>
                                            </div>
                                            <Button
                                                onClick={() => setIsChangingPassword(true)}
                                                className="bg-red-600 hover:bg-red-700 rounded-none"
                                            >
                                                <Lock className="w-4 h-4 mr-2" />
                                                Thay đổi mật khẩu
                                            </Button>
                                        </div>
                                    ) : (
                                        <div className="space-y-6">
                                            <div className="space-y-2">
                                                <Label htmlFor="currentPassword">Mật khẩu hiện tại</Label>
                                                <div className="relative">
                                                    <Input
                                                        id="currentPassword"
                                                        name="currentPassword"
                                                        type={showPassword ? 'text' : 'password'}
                                                        value={passwordData.currentPassword}
                                                        onChange={handlePasswordChange}
                                                        className="pr-10 rounded-none border-2 border-gray-200"
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => setShowPassword(!showPassword)}
                                                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                                    >
                                                        {showPassword ? (
                                                            <EyeOff className="w-4 h-4" />
                                                        ) : (
                                                            <Eye className="w-4 h-4" />
                                                        )}
                                                    </button>
                                                </div>
                                            </div>

                                            <div className="space-y-2">
                                                <Label htmlFor="newPassword">Mật khẩu mới</Label>
                                                <div className="relative">
                                                    <Input
                                                        id="newPassword"
                                                        name="newPassword"
                                                        type={showNewPassword ? 'text' : 'password'}
                                                        value={passwordData.newPassword}
                                                        onChange={handlePasswordChange}
                                                        className="pr-10"
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => setShowNewPassword(!showNewPassword)}
                                                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                                    >
                                                        {showNewPassword ? (
                                                            <EyeOff className="w-4 h-4" />
                                                        ) : (
                                                            <Eye className="w-4 h-4" />
                                                        )}
                                                    </button>
                                                </div>
                                            </div>

                                            <div className="space-y-2">
                                                <Label htmlFor="confirmPassword">Xác nhận mật khẩu mới</Label>
                                                <div className="relative">
                                                    <Input
                                                        id="confirmPassword"
                                                        name="confirmPassword"
                                                        type={showConfirmPassword ? 'text' : 'password'}
                                                        value={passwordData.confirmPassword}
                                                        onChange={handlePasswordChange}
                                                        className="pr-10"
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                                    >
                                                        {showConfirmPassword ? (
                                                            <EyeOff className="w-4 h-4" />
                                                        ) : (
                                                            <Eye className="w-4 h-4" />
                                                        )}
                                                    </button>
                                                </div>
                                            </div>

                                            <div className="flex gap-2">
                                                <Button
                                                    onClick={() => {
                                                        setIsChangingPassword(false);
                                                        setPasswordData({
                                                            currentPassword: '',
                                                            newPassword: '',
                                                            confirmPassword: '',
                                                        });
                                                    }}
                                                    variant="outline"
                                                >
                                                    <X className="w-4 h-4 mr-2" />
                                                    Hủy
                                                </Button>
                                                <Button
                                                    onClick={handleChangePassword}
                                                    className="bg-green-600 hover:bg-green-700"
                                                >
                                                    <Save className="w-4 h-4 mr-2" />
                                                    Cập nhật mật khẩu
                                                </Button>
                                            </div>
                                        </div>
                                    )}
                                </CardContent>
                            </Card>
                        </TabsContent>
                    </Tabs>
                </div>
            </main>

            <Footer />
        </div>
    );
};

export default ProfileCustomer;
