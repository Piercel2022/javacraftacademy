package com.javacraftacademy.userservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    
    private String message;
    
    private T data;
    
    private String error;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_details")
    private List<ErrorDetail> errorDetails;
    
    private String path;
    
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String field;
        private String message;
        private Object rejectedValue;
        private String code;
    }
    
    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode(errorCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> validationError(String message, List<ErrorDetail> errorDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(message)
                .errorCode("VALIDATION_ERROR")
                .errorDetails(errorDetails)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Builder helper methods
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }
    
    public ApiResponse<T> withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    public ApiResponse<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}