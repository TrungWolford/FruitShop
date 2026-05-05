package server.FruitShop.service;

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
     *
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary (optional)
     * @return Map containing upload result including URL
     * @throws IOException if upload fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            // Generate unique public_id
            String publicId = UUID.randomUUID().toString();

            // Upload options
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto", // Automatically detect file type
                    "quality", "auto:good", // Automatic quality optimization
                    "fetch_format", "auto" // Automatic format optimization
            );

            // Add folder if specified
            if (folder != null && !folder.trim().isEmpty()) {
                uploadOptions.put("folder", folder);
                publicId = folder + "/" + publicId;
                uploadOptions.put("public_id", publicId);
            }

            // Upload file
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
     *
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary
     * @param width Target width for image
     * @param height Target height for image
     * @return Map containing upload result
     * @throws IOException if upload fails
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

            // Add transformation if width and height specified
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
     *
     * @param publicId Public ID of the file to delete
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteFile(String publicId) throws IOException {
        try {
            // Try to determine resource type first by getting file info
            String resourceType = "image"; // default
            try {
                Map<String, Object> fileInfo = getFileInfo(publicId);
                resourceType = (String) fileInfo.get("resource_type");
            } catch (Exception e) {
                log.warn("Could not determine resource type for {}, using default 'image'", publicId);
            }

            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            log.info("File deleted successfully from Cloudinary. Public ID: {}, Resource Type: {}", publicId, resourceType);
            return result;
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }

    /**
     * Generate optimized URL for image with transformations
     *
     * @param publicId Public ID of the image
     * @param width Target width
     * @param height Target height
     * @param quality Image quality
     * @return Optimized image URL
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
     * Get file info from Cloudinary
     *
     * @param publicId Public ID of the file
     * @return Map containing file information
     * @throws IOException if retrieval fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getFileInfo(String publicId) throws IOException {
        try {
            // Try image resource type first
            try {
                return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "image"));
            } catch (Exception imageException) {
                log.debug("File not found in image resource type, trying video...");

                // If not found in image, try video resource type
                try {
                    return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "video"));
                } catch (Exception videoException) {
                    log.debug("File not found in video resource type, trying raw...");

                    // If not found in video, try raw resource type
                    return (Map<String, Object>) cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "raw"));
                }
            }
        } catch (Exception e) {
            log.error("Error getting file info from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to get file info from Cloudinary", e);
        }
    }
}
