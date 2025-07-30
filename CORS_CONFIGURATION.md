# CORS Configuration Documentation

## Overview

This application has been configured with a comprehensive CORS (Cross-Origin Resource Sharing) setup that provides security, flexibility, and ease of management.

## Configuration Files

### 1. `CorsConfig.java`
- **Location**: `src/main/java/com/acquireindia/config/CorsConfig.java`
- **Purpose**: Dedicated CORS configuration bean for Spring Security
- **Features**:
  - Configurable through application properties
  - Supports multiple origins
  - Configurable methods, headers, and credentials
  - Preflight request caching

### 2. `WebConfig.java`
- **Location**: `src/main/java/com/acquireindia/config/WebConfig.java`
- **Purpose**: WebMvcConfigurer for CORS on all endpoints
- **Features**:
  - Handles CORS for non-Security endpoints
  - Consistent configuration with Security CORS
  - Same property-based configuration

### 3. `WebSocketConfig.java`
- **Location**: `src/main/java/com/acquireindia/config/WebSocketConfig.java`
- **Purpose**: WebSocket CORS configuration
- **Features**:
  - Specific CORS settings for WebSocket endpoints
  - Uses same origin configuration as other CORS settings

### 4. `SecurityConfig.java`
- **Location**: `src/main/java/com/acquireindia/security/SecurityConfig.java`
- **Purpose**: Spring Security configuration
- **Features**:
  - Uses the dedicated CORS configuration
  - Removed inline CORS configuration for better maintainability

## Configuration Properties

All CORS settings are configurable through `application.yml`:

```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000,https://your-production-domain.com
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
  allowed-headers: "*"
  exposed-headers: Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers
  allow-credentials: true
  max-age: 3600
```

### Property Descriptions

- **allowed-origins**: Comma-separated list of allowed origins (supports patterns)
- **allowed-methods**: HTTP methods allowed for CORS requests
- **allowed-headers**: Headers allowed in CORS requests (use "*" for all)
- **exposed-headers**: Headers exposed to the browser
- **allow-credentials**: Whether to allow credentials (cookies, authorization headers)
- **max-age**: Cache duration for preflight requests in seconds

## Security Features

### 1. Origin Validation
- Uses `setAllowedOriginPatterns()` for flexible origin matching
- Supports both exact matches and patterns
- Configurable through properties

### 2. Method Restrictions
- Only allows specified HTTP methods
- Includes OPTIONS for preflight requests
- Configurable per environment

### 3. Header Management
- Configurable allowed headers
- Exposed headers for client access
- Supports wildcard for development

### 4. Credentials Policy
- Configurable credentials allowance
- Secure by default
- Required for authentication headers

## Environment-Specific Configuration

### Development
```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
  allowed-headers: "*"
  allow-credentials: true
```

### Production
```yaml
cors:
  allowed-origins: https://your-production-domain.com
  allowed-headers: Authorization,Content-Type,X-Requested-With
  allow-credentials: true
```

### Staging
```yaml
cors:
  allowed-origins: https://staging.your-domain.com
  allowed-headers: Authorization,Content-Type,X-Requested-With
  allow-credentials: true
```

## Best Practices

### 1. Security
- Never use `*` for origins in production
- Specify exact origins when possible
- Use HTTPS in production
- Limit exposed headers to necessary ones

### 2. Performance
- Set appropriate `max-age` for preflight caching
- Use specific headers instead of `*` when possible
- Consider CDN caching for static resources

### 3. Maintenance
- Use environment-specific configurations
- Document origin changes
- Test CORS settings in all environments

## Troubleshooting

### Common Issues

1. **CORS errors in browser console**
   - Check if origin is in allowed-origins list
   - Verify credentials setting matches frontend
   - Ensure methods are included in allowed-methods

2. **WebSocket connection issues**
   - Verify WebSocket endpoint CORS settings
   - Check if origin patterns match WebSocket origins

3. **Authentication headers not sent**
   - Ensure `allow-credentials: true`
   - Check if Authorization is in allowed-headers
   - Verify frontend sends credentials

### Debugging

Enable debug logging for CORS:
```yaml
logging:
  level:
    org.springframework.web.cors: DEBUG
```

## Migration Notes

### From Previous Configuration
- Removed inline CORS configuration from SecurityConfig
- Created dedicated CORS configuration classes
- Added property-based configuration
- Improved WebSocket CORS security

### Breaking Changes
- WebSocket now uses specific origins instead of wildcard
- CORS configuration is now centralized
- Properties must be set for custom configurations

## Testing

### Manual Testing
1. Test with different origins
2. Verify preflight requests work
3. Check credentials handling
4. Test WebSocket connections

### Automated Testing
```java
@Test
public void testCorsConfiguration() {
    // Test CORS headers in responses
    // Verify allowed origins
    // Check preflight handling
}
```

## Future Enhancements

1. **Dynamic CORS Configuration**
   - Runtime origin validation
   - Database-stored origins
   - Admin interface for CORS management

2. **Advanced Security**
   - Origin validation middleware
   - Rate limiting for CORS requests
   - Audit logging for CORS violations

3. **Performance Optimization**
   - CORS response caching
   - CDN integration
   - Preflight request optimization 