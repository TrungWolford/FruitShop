package fruitshop.catalog_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload file to Cloudinary
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            String publicId = UUID.randomUUID().toString();

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "quality", "auto:good",
                    "fetch_format", "auto"
            );

            if (folder != null && !folder.trim().isEmpty()) {
                uploadOptions.put("folder", folder);
                publicId = folder + "/" + publicId;
                uploadOptions.put("public_id", publicId);
            }

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            log.info("File uploaded successfully to Cloudinary. Public ID: {}", result.get("public_id"));
            return result;
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }

    /**
     * Upload image with specific transformations
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder, Integer width, Integer height) throws IOException {
        try {
            String publicId = UUID.randomUUID().toString();

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "public_id", folder != null ? folder + "/" + publicId : publicId,
                    "resource_type", "image",
                    "quality", "auto:good",
                    "fetch_format", "auto"
            );

            if (width != null && height != null) {
                uploadOptions.put("transformation", ObjectUtils.asMap(
                        "width", width,
                        "height", height,
                        "crop", "fill",
                        "gravity", "center"
                ));
            }

            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            log.info("Image uploaded successfully to Cloudinary. Public ID: {}", result.get("public_id"));
            return result;
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload image to Cloudinary", e);
        }
    }

    /**
     * Delete file from Cloudinary
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteFile(String publicId) throws IOException {
        try {
            String resourceType = "image";
            try {
                Map<String, Object> fileInfo = getFileInfo(publicId);
                resourceType = (String) fileInfo.get("resource_type");
            } catch (Exception e) {
                log.warn("Could not determine resource type for {}, using default 'image'", publicId);
            }

            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            log.info("File deleted successfully from Cloudinary. Public ID: {}", publicId);
            return result;
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }

    /**
     * Generate optimized URL
     */
    @SuppressWarnings("rawtypes")
    public String generateOptimizedUrl(String publicId, Integer width, Integer height, String quality) {
        try {
            Transformation transformation = new Transformation()
                    .width(width)
                    .height(height)
                    .crop("fill")
                    .gravity("center")
                    .quality(quality != null ? quality : "auto:good")
                    .fetchFormat("auto");

            return cloudinary.url()
                    .transformation(transformation)
                    .generate(publicId);
        } catch (Exception e) {
            log.error("Error generating optimized URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get file info
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getFileInfo(String publicId) throws IOException {
        try {
            try {
                return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "image"));
            } catch (Exception imageException) {
                try {
                    return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "video"));
                } catch (Exception videoException) {
                    return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "raw"));
                }
            }
        } catch (Exception e) {
            log.error("Error getting file info from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to get file info from Cloudinary", e);
        }
    }
}
