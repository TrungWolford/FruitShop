import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ErrorBoundary from '@/components/ui/ErrorBoundary/ErrorBoundary'
import Home from '../pages/Home/Home';
import Register from '../pages/Mainpage/Register';
import ProfileCustomer from '../pages/Customer/ProfileCustomer';
import HistoryReceipt from '../pages/Customer/HistoryOrder';
import ProductPage from '../pages/Customer/Product';
import AdminLogin from '../pages/Admin/AdminLogin';
import AdminDashboard from '../pages/Admin/AdminDashboard';
import AdminProduct from '../pages/Admin/AdminProduct';
import AdminAccounts from '../pages/Admin/AdminAccounts';
import AdminRoles from '../pages/Admin/AdminRoles';
import AdminCategory from '../pages/Admin/AdminCategory';
import AdminCart from '../pages/Admin/AdminCart';
import AdminOrder from '../pages/Admin/AdminOrder';
import AdminPayment from '../pages/Admin/AdminPayment';
import AdminRefund from '../pages/Admin/AdminRefund';
import AdminShipping from '../pages/Admin/AdminShipping';
import AdminRating from '../pages/Admin/AdminRating';
import CheckoutPage from '../pages/Checkout/CheckoutPage';
import ProductDetail from '../pages/ProductDetail/ProductDetail';
import PaymentPage from '../pages/Payment/PaymentPage';
import OrderSuccessPage from '../pages/OrderSuccess/OrderSuccessPage';

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Customer routes - có Header + Footer qua DefaultLayout */}
      <Route>
        <Route path="/" element={<Home />} />
        <Route path="/account/register" element={<Register />} />
        <Route path="/account/profile" element={<ProfileCustomer />} />
        <Route path="/customer/orders" element={<HistoryReceipt />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/payment" element={<PaymentPage />} />
        <Route path="/order-success" element={<OrderSuccessPage />} />
        <Route
          path="/product"
          element={
            <ErrorBoundary>
              <ProductPage />
            </ErrorBoundary>
          }
        />
        <Route
          path="/product/search"
          element={
            <ErrorBoundary>
              <ProductPage />
            </ErrorBoundary>
          }
        />
        <Route
          path="/product/collection/:categoryName"
          element={
            <ErrorBoundary>
              <ProductPage />
            </ErrorBoundary>
          }
        />
        <Route
          path="/collection/:categoryName"
          element={
            <ErrorBoundary>
              <ProductPage />
            </ErrorBoundary>
          }
        />
        <Route
          path="/product/:productName"
          element={
            <ErrorBoundary>
              <ProductDetail />
            </ErrorBoundary>
          }
        />
      </Route>

      {/* Admin routes - không có DefaultLayout */}
      <Route path="/admin" element={<AdminLogin />} />
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route path="/admin/dashboard" element={<AdminDashboard />} />
      <Route path="/admin/products" element={<AdminProduct />} />
      <Route path="/admin/carts" element={<AdminCart />} />
      <Route path="/admin/orders" element={<AdminOrder />} />
      <Route path="/admin/payments" element={<AdminPayment />} />
      <Route path="/admin/refunds" element={<AdminRefund />} />
      <Route path="/admin/shippings" element={<AdminShipping />} />
      <Route path="/admin/ratings" element={<AdminRating />} />
      <Route path="/admin/accounts" element={<AdminAccounts />} />
      <Route path="/admin/roles" element={<AdminRoles />} />
      <Route path="/admin/categories" element={<AdminCategory />} />

      {/* Redirect old auth routes to home */}
      <Route path="/auth/login" element={<Navigate to="/" replace />} />
      <Route path="/auth/register" element={<Navigate to="/account/register" replace />} />
      <Route path="/auth/logout" element={<Navigate to="/" replace />} />

      {/* Catch all unmatched routes */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
