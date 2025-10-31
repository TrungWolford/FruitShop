export interface Rating {
  ratingId: string;
  account: {
    accountId: string;
    accountName: string;
    accountPhone?: string;
  };
  product: {
    productId: string;
    productName: string;
  };
  orderItemId?: string; // Link to specific order item
  comment: string;
  ratingStar: number;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateRatingRequest {
  accountId: string;
  productId: string;
  orderItemId?: string; // Link rating to specific order item
  comment: string;
  ratingStar: number;
}

export interface UpdateRatingRequest {
  comment: string;
  status: number;
  ratingStar: number;
}
