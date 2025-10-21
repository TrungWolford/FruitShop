// Debug: Log Cloudinary environment variables
console.log('☁️ Cloudinary Environment Variables:');
console.log('VITE_CLOUDINARY_CLOUD_NAME:', import.meta.env.VITE_CLOUDINARY_CLOUD_NAME);
console.log('VITE_CLOUDINARY_API_KEY:', import.meta.env.VITE_CLOUDINARY_API_KEY);
console.log('VITE_CLOUDINARY_UPLOAD_PRESET:', import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET);
console.log('---');

// Cloudinary configuration - Frontend safe (no API_SECRET)
export const CLOUDINARY_CONFIG = {
  cloudName: import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'your-cloud-name',
  apiKey: import.meta.env.VITE_CLOUDINARY_API_KEY || 'your-api-key',
  uploadPreset: import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'your-upload-preset'
}

// Debug: Log final Cloudinary config
console.log('☁️ Final Cloudinary Config:', CLOUDINARY_CONFIG);
console.log('---');

// Upload endpoints
export const UPLOAD_ENDPOINTS = {
  image: '/api/upload/image',
  file: '/api/upload/file',
  delete: '/api/upload',
  info: '/api/upload/info',
  optimizeUrl: '/api/upload/optimize-url'
}

// File validation
export const FILE_VALIDATION = {
  maxImageSize: 10 * 1024 * 1024, // 10MB
  maxFileSize: 20 * 1024 * 1024, // 20MB
  allowedImageTypes: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'],
  allowedFileTypes: [
    'image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp',
    'application/pdf', 'text/plain', 'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'video/mp4', 'video/avi', 'video/mov', 'video/wmv'
  ]
}

// Cloudinary transformation presets
export const TRANSFORMATION_PRESETS = {
  thumbnail: { width: 150, height: 150, crop: 'fill' },
  medium: { width: 400, height: 300, crop: 'fill' },
  large: { width: 800, height: 600, crop: 'fit' },
  avatar: { width: 100, height: 100, crop: 'fill', gravity: 'face' },
  banner: { width: 1200, height: 400, crop: 'fill' },
  product: { width: 500, height: 500, crop: 'fit' }
}
