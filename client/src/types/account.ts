// Account interfaces theo backend AccountController
export interface Account {
  accountId: string;
  accountName: string;
  accountPhone: string;
  password?: string;
  status: number; // 0 = INACTIVE, 1 = ACTIVE
  roles: Role[]; // Backend trả về Set<RoleResponse>
}

export interface CreateAccountRequest {
  accountName: string;
  accountPhone: string;
  password: string;
  status: number;
  roleIds: string[]; // Backend nhận Set<String> roleIds
}

export interface UpdateAccountRequest {
  accountName?: string;
  accountPhone?: string;
  password?: string;
  status?: number;
  roleIds?: string[];
}

export interface LoginRequest {
  accountPhone: string;
  password: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Role interface
export interface Role {
  roleId: string;
  roleName: string;
}

export type AccountStatus = 0 | 1; // 0 = INACTIVE, 1 = ACTIVE
