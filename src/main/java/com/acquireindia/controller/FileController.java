package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.model.User;
import com.acquireindia.service.FileStorageService;
import com.acquireindia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private UserService userService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @PostMapping("/upload/kyc/{documentType}")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadKycDocument(
            @RequestParam("file") MultipartFile file,
            @PathVariable String documentType,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate file type
            if (!isValidDocumentType(file)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Only PDF, JPG, JPEG, PNG files are allowed"));
            }
            
            // Validate file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size exceeds 5MB limit"));
            }
            
            String filePath = fileStorageService.storeFile(file, "kyc/" + user.getId());
            
            // Update user with document path
            updateUserDocument(user, documentType, filePath);
            userService.updateUser(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("documentType", documentType);
            
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
    
    @PostMapping("/upload/listing")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadListingFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate file type for listings (images and videos)
            if (!isValidMediaType(file)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid file type. Only JPG, JPEG, PNG, MP4, AVI files are allowed"));
            }
            
            // Validate file size (50MB limit for videos)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size exceeds 50MB limit"));
            }
            
            String filePath = fileStorageService.storeFile(file, "listings/" + user.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileUrl", "/api/files/download/" + filePath);
            
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
    
    @GetMapping("/download/{folder}/{userId}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String folder,
                                                @PathVariable String userId,
                                                @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(folder).resolve(userId).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                String contentType = "application/octet-stream";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private boolean isValidDocumentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png")
        );
    }
    
    private boolean isValidMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("video/mp4") ||
                contentType.equals("video/avi")
        );
    }
    
    private void updateUserDocument(User user, String documentType, String filePath) {
        switch (documentType.toLowerCase()) {
            case "pan":
                user.setPanCard(filePath);
                break;
            case "gst":
                user.setGstNumber(filePath);
                break;
            case "corporate":
                user.setCorporateRegistration(filePath);
                break;
            default:
                throw new RuntimeException("Invalid document type");
        }
        
        // Update KYC status to pending if it was rejected before
        if (user.getKycStatus() == User.KycStatus.REJECTED) {
            user.setKycStatus(User.KycStatus.PENDING);
        }
    }
}