import axios from 'axios';
import { CONFIG, API } from '../config/constants';

// Types
export interface UploadImageResponse {
    url: string;
    publicId: string;
    format: string;
    width: number;
    height: number;
    bytes: number;
}

export interface UploadImageRequest {
    file: File;
    folder?: string;
}

// Service
class UploadService {
    private baseURL = CONFIG.API_GATEWAY;

    /**
     * Upload image to Cloudinary
     * @param file - Image file to upload
     * @param folder - Optional folder name in Cloudinary (default: "general")
     * @returns Promise with upload response
     */
    async uploadImage(file: File, folder: string = 'general'): Promise<{
        success: boolean;
        data?: UploadImageResponse;
        message?: string;
    }> {
        try {
            // Validate file
            if (!file) {
                return {
                    success: false,
                    message: 'Không có file được chọn'
                };
            }

            // Validate file type
            const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
            if (!allowedTypes.includes(file.type)) {
                return {
                    success: false,
                    message: 'Định dạng file không được hỗ trợ. Vui lòng chọn file JPG, PNG, WEBP hoặc GIF'
                };
            }

            // Validate file size (max 5MB)
            const maxSize = 5 * 1024 * 1024; // 5MB in bytes
            if (file.size > maxSize) {
                return {
                    success: false,
                    message: 'File quá lớn. Kích thước tối đa là 5MB'
                };
            }

            // Create FormData
            const formData = new FormData();
            formData.append('file', file);
            formData.append('folder', folder);

            // Upload to server
            const response = await axios.post<any>(
                `${this.baseURL}${API.UPLOAD_IMAGE}`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            );

            // Debug logging
            // Backend returns: { success: true, data: { url, publicId, ... } }
            // We need to extract the data field
            const uploadData = response.data.data;
            
            if (!uploadData || !uploadData.url) {
                return {
                    success: false,
                    message: 'Backend trả về dữ liệu không hợp lệ'
                };
            }
            return {
                success: true,
                data: uploadData as UploadImageResponse
            };
        } catch (error: any) {
            let errorMessage = 'Không thể tải lên hình ảnh';
            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            return {
                success: false,
                message: errorMessage
            };
        }
    }

    /**
     * Upload multiple images to Cloudinary
     * @param files - Array of image files to upload
     * @param folder - Optional folder name in Cloudinary (default: "general")
     * @returns Promise with array of upload responses
     */
    async uploadMultipleImages(files: File[], folder: string = 'general'): Promise<{
        success: boolean;
        data?: UploadImageResponse[];
        message?: string;
        failedCount?: number;
    }> {
        try {
            if (!files || files.length === 0) {
                return {
                    success: false,
                    message: 'Không có file được chọn'
                };
            }

            const uploadPromises = files.map(file => this.uploadImage(file, folder));
            const results = await Promise.all(uploadPromises);

            const successfulUploads = results.filter(r => r.success && r.data);
            const failedUploads = results.filter(r => !r.success);

            if (successfulUploads.length === 0) {
                return {
                    success: false,
                    message: 'Không thể tải lên bất kỳ hình ảnh nào',
                    failedCount: failedUploads.length
                };
            }

            return {
                success: true,
                data: successfulUploads.map(r => r.data!),
                message: failedUploads.length > 0 
                    ? `Đã tải lên ${successfulUploads.length}/${files.length} hình ảnh`
                    : 'Tải lên thành công tất cả hình ảnh',
                failedCount: failedUploads.length
            };
        } catch (error) {
            return {
                success: false,
                message: 'Có lỗi xảy ra khi tải lên hình ảnh'
            };
        }
    }

    /**
     * Get file info from Cloudinary by publicId
     * @param publicId - The public ID of the file
     * @returns Promise with file info
     */
    async getFileInfo(publicId: string): Promise<{
        success: boolean;
        data?: any;
        message?: string;
    }> {
        try {
            const response = await axios.get(`${this.baseURL}${API.GET_FILE_INFO}`, {
                params: { publicId }
            });

            return {
                success: true,
                data: response.data
            };
        } catch (error: any) {
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể lấy thông tin file'
            };
        }
    }
}

export const uploadService = new UploadService();
export default uploadService;
