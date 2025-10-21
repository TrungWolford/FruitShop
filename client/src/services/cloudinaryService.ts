import axiosInstance from '../libs/axios';
import { API } from '@/config/constants'
import { FILE_VALIDATION } from '@/config/cloudinary'

export interface UploadResponse {
  success: boolean
  message: string
  data?: {
    url: string
    publicId: string
    format: string
    width?: number
    height?: number
    bytes: number
    resourceType?: string
    createdAt: string
  }
  error?: string
}

export interface FileUploadOptions {
  folder?: string
  width?: number
  height?: number
  onProgress?: (progress: number) => void
}

export interface OptimizeUrlOptions {
  width?: number
  height?: number
  quality?: string
}

class CloudinaryService {
  /**
   * Validate file before upload
   */
  private validateFile(file: File, isImage: boolean = false): string | null {
    // Check file size
    const maxSize = isImage ? FILE_VALIDATION.maxImageSize : FILE_VALIDATION.maxFileSize
    if (file.size > maxSize) {
      const maxSizeMB = Math.round(maxSize / (1024 * 1024))
      return `Kích thước file không được vượt quá ${maxSizeMB}MB`
    }

    // Check file type
    const allowedTypes = isImage ? FILE_VALIDATION.allowedImageTypes : FILE_VALIDATION.allowedFileTypes
    if (!allowedTypes.includes(file.type)) {
      return isImage ? 'Chỉ chấp nhận file hình ảnh (JPEG, PNG, GIF, WebP)' : 'Định dạng file không được hỗ trợ'
    }

    return null
  }

  /**
   * Upload image to Cloudinary
   */
  async uploadImage(file: File, options: FileUploadOptions = {}): Promise<UploadResponse> {
    try {
      // Validate file
      const validationError = this.validateFile(file, true)
      if (validationError) {
        return {
          success: false,
          message: validationError,
          error: validationError
        }
      }

      // Prepare form data
      const formData = new FormData()
      formData.append('file', file)
      if (options.folder) formData.append('folder', options.folder)
      if (options.width) formData.append('width', options.width.toString())
      if (options.height) formData.append('height', options.height.toString())

      // Upload with progress tracking
      const response = await axiosInstance.post<UploadResponse>(API.UPLOAD_IMAGE, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent: any) => {
          if (options.onProgress && progressEvent.total) {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            options.onProgress(progress)
          }
        }
      })

      return response.data
    } catch (error: any) {
      console.error('Upload image error:', error)
      return {
        success: false,
        message: error.response?.data?.error || 'Lỗi khi upload hình ảnh',
        error: error.response?.data?.error || error.message
      }
    }
  }

  /**
   * Upload any file to Cloudinary
   */
  async uploadFile(file: File, options: FileUploadOptions = {}): Promise<UploadResponse> {
    try {
      // Validate file
      const validationError = this.validateFile(file, false)
      if (validationError) {
        return {
          success: false,
          message: validationError,
          error: validationError
        }
      }

      // Prepare form data
      const formData = new FormData()
      formData.append('file', file)
      if (options.folder) formData.append('folder', options.folder)

      // Upload with progress tracking
      const response = await axiosInstance.post<UploadResponse>(API.UPLOAD_FILE, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent: any) => {
          if (options.onProgress && progressEvent.total) {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            options.onProgress(progress)
          }
        }
      })

      return response.data
    } catch (error: any) {
      console.error('Upload file error:', error)
      return {
        success: false,
        message: error.response?.data?.error || 'Lỗi khi upload file',
        error: error.response?.data?.error || error.message
      }
    }
  }

  /**
   * Delete file from Cloudinary
   */
  async deleteFile(publicId: string): Promise<UploadResponse> {
    try {
      // Encode slashes in publicId for URL
      const encodedPublicId = publicId.replace(/\//g, '%2F')
      
      const response = await axiosInstance.delete<UploadResponse>(`${API.DELETE_FILE}/${encodedPublicId}`)
      return response.data
    } catch (error: any) {
      console.error('Delete file error:', error)
      return {
        success: false,
        message: error.response?.data?.error || 'Lỗi khi xóa file',
        error: error.response?.data?.error || error.message
      }
    }
  }

  /**
   * Get file information from Cloudinary
   */
  async getFileInfo(publicId: string): Promise<UploadResponse> {
    try {
      // Encode slashes in publicId for URL
      const encodedPublicId = publicId.replace(/\//g, '%2F')
      
      const response = await axiosInstance.get<UploadResponse>(`${API.GET_FILE_INFO}?publicId=${encodedPublicId}`)
      return response.data
    } catch (error: any) {
      console.error('Get file info error:', error)
      return {
        success: false,
        message: error.response?.data?.error || 'Lỗi khi lấy thông tin file',
        error: error.response?.data?.error || error.message
      }
    }
  }

  /**
   * Generate optimized URL for image
   */
  async generateOptimizedUrl(publicId: string, options: OptimizeUrlOptions = {}): Promise<UploadResponse> {
    try {
      const params = new URLSearchParams({ publicId })
      if (options.width) params.append('width', options.width.toString())
      if (options.height) params.append('height', options.height.toString())
      if (options.quality) params.append('quality', options.quality)

      const response = await axiosInstance.post<UploadResponse>(`${API.OPTIMIZE_URL}?${params}`)
      return response.data
    } catch (error: any) {
      console.error('Generate optimized URL error:', error)
      return {
        success: false,
        message: error.response?.data?.error || 'Lỗi khi tạo URL tối ưu',
        error: error.response?.data?.error || error.message
      }
    }
  }

  /**
   * Upload multiple files
   */
  async uploadMultipleFiles(
    files: File[], 
    options: FileUploadOptions = {},
    onFileProgress?: (fileIndex: number, progress: number) => void
  ): Promise<UploadResponse[]> {
    const results: UploadResponse[] = []
    
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      const fileOptions = {
        ...options,
        onProgress: (progress: number) => {
          if (onFileProgress) {
            onFileProgress(i, progress)
          }
        }
      }
      
      // Determine if file is image
      const isImage = file.type.startsWith('image/')
      const result = isImage 
        ? await this.uploadImage(file, fileOptions)
        : await this.uploadFile(file, fileOptions)
      
      results.push(result)
    }
    
    return results
  }

  /**
   * Get file size in human readable format
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes'
    
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * Get file extension from filename
   */
  getFileExtension(filename: string): string {
    return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2)
  }

  /**
   * Check if file is image
   */
  isImageFile(file: File): boolean {
    return file.type.startsWith('image/')
  }

  /**
   * Check if file is video
   */
  isVideoFile(file: File): boolean {
    return file.type.startsWith('video/')
  }
}

export const cloudinaryService = new CloudinaryService()
