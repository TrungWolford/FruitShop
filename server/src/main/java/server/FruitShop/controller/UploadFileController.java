package server.FruitShop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.FruitShop.service.CloudinaryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadFileController {

    private final CloudinaryService cloudinaryService;

    @RequestMapping(method = RequestMethod.OPTIONS, value = "/**")
    public ResponseEntity<?> handlePreflight() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cloudinary service is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "images") String folder,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ chấp nhận file hình ảnh"));
            }

            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kích thước file không được vượt quá 10MB"));
            }

            Map<String, Object> result;
            if (width != null && height != null) {
                result = cloudinaryService.uploadImage(file, folder, width, height);
            } else {
                result = cloudinaryService.uploadFile(file, folder);
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload thành công");
            response.put("data", Map.of(
                    "url", result.get("secure_url"),
                    "publicId", result.get("public_id"),
                    "format", result.get("format"),
                    "width", result.get("width"),
                    "height", result.get("height"),
                    "bytes", result.get("bytes"),
                    "createdAt", result.get("created_at")
            ));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Lỗi khi upload file: " + e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Đã xảy ra lỗi không mong muốn"
                    ));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "files") String folder) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File không được để trống"));
            }

            // Validate file size (max 20MB)
            if (file.getSize() > 20 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Kích thước file không được vượt quá 20MB"));
            }

            Map<String, Object> result = cloudinaryService.uploadFile(file, folder);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload thành công");
            response.put("data", Map.of(
                    "url", result.get("secure_url"),
                    "publicId", result.get("public_id"),
                    "format", result.get("format"),
                    "bytes", result.get("bytes"),
                    "resourceType", result.get("resource_type"),
                    "createdAt", result.get("created_at")
            ));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Lỗi khi upload file: " + e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Đã xảy ra lỗi không mong muốn"
                    ));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFile(@RequestParam("publicId") String publicId) {
        try {
            // No need to decode since it comes from query parameter

            Map<String, Object> result = cloudinaryService.deleteFile(publicId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa file thành công");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Lỗi khi xóa file: " + e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Đã xảy ra lỗi không mong muốn"
                    ));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getFileInfo(@RequestParam("publicId") String publicId) {
        try {
            // No need to decode since it comes from query parameter

            Map<String, Object> result = cloudinaryService.getFileInfo(publicId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy thông tin file thành công");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error getting file info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", "Không tìm thấy file: " + e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Đã xảy ra lỗi không mong muốn"
                    ));
        }
    }

    @PostMapping("/optimize-url")
    public ResponseEntity<?> generateOptimizedUrl(
            @RequestParam("publicId") String publicId,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "quality", required = false, defaultValue = "auto:good") String quality) {

        try {
            String optimizedUrl = cloudinaryService.generateOptimizedUrl(publicId, width, height, quality);

            if (optimizedUrl == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không thể tạo URL tối ưu"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo URL tối ưu thành công");
            response.put("data", Map.of("optimizedUrl", optimizedUrl));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating optimized URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Lỗi khi tạo URL tối ưu: " + e.getMessage()
                    ));
        }
    }
}

