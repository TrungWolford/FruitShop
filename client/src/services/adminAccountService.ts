import axiosInstance from '../libs/axios';
import type { AxiosResponse } from 'axios';
import { API } from '../config/constants';
import type { 
  Account, 
  CreateAccountRequest, 
  UpdateAccountRequest, 
  LoginRequest, 
  PaginatedResponse,
  Role 
} from '../types/account';

// Account Service với tất cả endpoints từ AccountController
export const accountService = {
  // Get all accounts with pagination
  getAllAccounts: async (page = 0, size = 10): Promise<PaginatedResponse<Account>> => {
    const response: AxiosResponse<PaginatedResponse<Account>> = await axiosInstance.get(
      `${API.GET_ALL_ACCOUNTS}?page=${page}&size=${size}`
    );
    return response.data;
  },

  // Get account by ID
  getAccountById: async (accountId: string): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.get(API.GET_ACCOUNT_BY_ID(accountId));
    return response.data;
  },

  // Create new account
  createAccount: async (accountData: CreateAccountRequest): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.post(API.CREATE_ACCOUNT, accountData);
    return response.data;
  },

  // Update account
  updateAccount: async (accountId: string, accountData: UpdateAccountRequest): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.put(API.UPDATE_ACCOUNT(accountId), accountData);
    return response.data;
  },

  // Delete account
  deleteAccount: async (accountId: string): Promise<void> => {
    await axiosInstance.delete(API.DELETE_ACCOUNT(accountId));
  },

  // Get accounts by status
  getAccountsByStatus: async (status: number, page = 0, size = 10): Promise<PaginatedResponse<Account>> => {
    const response: AxiosResponse<PaginatedResponse<Account>> = await axiosInstance.get(
      `${API.GET_ACCOUNTS_BY_STATUS(status)}?page=${page}&size=${size}`
    );
    return response.data;
  },

  // Get account by phone
  getAccountByPhone: async (accountPhone: string): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.get(API.GET_ACCOUNT_BY_PHONE(accountPhone));
    return response.data;
  },

  // Search accounts by name
  searchAccountsByName: async (accountName: string, page = 0, size = 10): Promise<PaginatedResponse<Account>> => {
    const response: AxiosResponse<PaginatedResponse<Account>> = await axiosInstance.get(
      `${API.SEARCH_ACCOUNTS}?accountName=${encodeURIComponent(accountName)}&page=${page}&size=${size}`
    );
    return response.data;
  },

  // Update account status (khóa/mở khóa tài khoản)
  updateAccountStatus: async (accountId: string, status: number): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.put(API.UPDATE_ACCOUNT(accountId), { status });
    return response.data;
  },

  // Login
  login: async (credentials: LoginRequest): Promise<Account> => {
    const response: AxiosResponse<Account> = await axiosInstance.post(API.ACCOUNT_LOGIN, credentials);
    return response.data;
  },
};

// Role Service
export const roleService = {
  // Get all roles
  getAllRoles: async (): Promise<Role[]> => {
    const response: AxiosResponse<Role[]> = await axiosInstance.get(API.GET_ALL_ROLES);
    return response.data;
  },

  // Get role by ID
  getRoleById: async (roleId: string): Promise<Role> => {
    const response: AxiosResponse<Role> = await axiosInstance.get(API.GET_ROLE_BY_ID(roleId));
    return response.data;
  },

  // Create role
  createRole: async (roleData: Omit<Role, 'roleId'>): Promise<Role> => {
    const response: AxiosResponse<Role> = await axiosInstance.post(API.CREATE_ROLE, roleData);
    return response.data;
  },

  // Update role
  updateRole: async (roleId: string, roleData: Partial<Role>): Promise<Role> => {
    const response: AxiosResponse<Role> = await axiosInstance.put(API.UPDATE_ROLE(roleId), roleData);
    return response.data;
  },

  // Delete role
  deleteRole: async (roleId: string): Promise<void> => {
    await axiosInstance.delete(API.DELETE_ROLE(roleId));
  },
};
