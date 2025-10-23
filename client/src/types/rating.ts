export interface Rating {
  ratingId: string;
  account: {
    accountId: string;
    accountName: string;
    accountEmail?: string;
    accountPhone?: string;
  };
  product: {
    productId: string;
    productName: string;
  };
  comment: string;
  ratingStar: number;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateRatingRequest {
  accountId: string;
  productId: string;
  comment: string;
  ratingStar: number;
}

export interface UpdateRatingRequest {
  comment: string;
  status: number;
  ratingStar: number;
}
