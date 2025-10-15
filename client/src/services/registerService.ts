import axiosInstance from '../libs/axios';
import type { AxiosResponse } from 'axios';
import { API } from '../config/constants';
import type { Role } from '../types/account';

export interface RegisterRequest {
  accountName: string;
  accountPhone: string;
  password: string;
}

export interface RegisterResponse {
  accountId: string;
  accountName: string;
  accountPhone: string;
  status: number;
  roles: Role[];
  message: string;
}

// Register Service
export const registerService = {
  // Get CUSTOMER role
  getCustomerRole: async (): Promise<Role> => {
    try {
      const response: AxiosResponse<Role[]> = await axiosInstance.get(API.GET_ALL_ROLES);
      const customerRole = response.data.find(role => role.roleName === 'CUSTOMER');
      if (!customerRole) {
        throw new Error('Không tìm thấy role CUSTOMER');
      }
      return customerRole;
    } catch (error) {
      throw error;
    }
  },

  // Register new account with CUSTOMER role
  registerAccount: async (registerData: RegisterRequest): Promise<RegisterResponse> => {
    try {
      // First, get the CUSTOMER role
      const customerRole = await registerService.getCustomerRole();
      
      // Prepare account data with CUSTOMER role
      const accountData = {
        accountName: registerData.accountName,
        accountPhone: registerData.accountPhone,
        password: registerData.password,
        status: 1, // Active by default
        roleIds: [customerRole.roleId] // Assign CUSTOMER role
      };

      // Create account
      const response: AxiosResponse<RegisterResponse> = await axiosInstance.post(API.CREATE_ACCOUNT, accountData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },


};
