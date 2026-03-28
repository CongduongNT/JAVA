# Bug Report: UserService & UserController

## CRITICAL BUGS FOUND

### 1. **UserController - GET /me ROUTE ORDER BUG** ⚠️
**Location**: UserController lines 96-103
**Problem**: `@GetMapping("/me")` MUST come BEFORE `@GetMapping("/{id}")` because Spring matches `/{id}` first, causing `/me` to be treated as an ID parameter.
**Impact**: GET /api/v1/users/me will be treated as GET /api/v1/users/{id=me}, causing 404 errors.
**Fix**: Move `getMe()` and `updateMe()` methods BEFORE `getById()` method in class definition.

### 2. **UserService.create() - PASSWORD STORED AS PLAIN TEXT** ⚠️ SECURITY ISSUE
**Location**: UserService line 60
**Current Code**: `user.setPasswordHash(req.getPassword());`
**Problem**: Password is stored directly without hashing/encoding. Should use PasswordEncoder.
**Impact**: User passwords are visible in database in plain text - MAJOR SECURITY BREACH.
**Fix**: Inject PasswordEncoder, use: `user.setPasswordHash(passwordEncoder.encode(req.getPassword()));`

### 3. **UserService.update() - PASSWORD STORED AS PLAIN TEXT** ⚠️ SECURITY ISSUE
**Location**: UserService lines 83-85
**Current Code**: 
```java
if (req.getPassword() != null && !req.getPassword().isEmpty()) {
    existing.setPasswordHash(req.getPassword());
}
```
**Problem**: Same as #2 - plain text password storage.
**Fix**: Use PasswordEncoder to encode password before storing.

### 4. **UserService.create() - UNNECESSARY user.setId(null)** ⚠️
**Location**: UserService line 46
**Problem**: JPA auto-generates ID with @GeneratedValue(IDENTITY), explicitly setting null is redundant and indicates incomplete understanding.
**Fix**: Remove `user.setId(null);` line entirely - let JPA handle it.

### 5. **UserService - MISSING INPUT TRIMMING**
**Location**: UserService.create() and update() methods
**Problem**: Email and fullName fields are not trimmed, allowing whitespace issues (e.g., "  user@email.com  " gets treated differently than "user@email.com").
**Fix**: Trim inputs:
```java
user.setFullName(req.getFullName().trim());
user.setEmail(req.getEmail().trim());
```

### 6. **UserController.DELETE - MISSING AUTHORIZATION**
**Location**: UserController line 118
**Problem**: DELETE endpoint has no `@PreAuthorize`, allowing any authenticated user to delete any user. Should be ADMIN only.
**Fix**: Add `@PreAuthorize("hasRole('ADMIN')")`

### 7. **UserService.assignRole() - INCONSISTENT NULL HANDLING**
**Location**: UserService lines 107-113
**Problem**: Comments are in Vietnamese, inconsistent style with rest of code. Minor issue but affects maintainability.
**Fix**: Use English comments, consistent with other methods.

### 8. **UserController - NO RESPONSE STRUCTURE FOR DELETE**
**Location**: UserController line 126 (delete endpoint)
**Problem**: Using `ResponseEntity.noContent().build()` returns HTTP 204 with no body, unclear for client.
**Suggestion**: Consider returning empty ErrorResponse or custom SuccessResponse for consistency.

### 9. **Missing PasswordEncoder Dependency**
**Location**: UserService
**Problem**: No PasswordEncoder injected in UserService for password encoding.
**Fix**: Need to inject PasswordEncoder from Spring Security.

### 10. **UserService - MISSING NULL CHECKS IN CONVERTERS**
**Location**: toResponse() and toProfileResponse() methods
**Problem**: No NullPointerException handling if `u` is null (unlikely but should be defensive).
**Minor Issue**: Not critical but best practice.

---

## MISSING FEATURES

### 1. **No Password Strength Validation in Service**
- DTOs validate min 6 chars, but no complexity checks (uppercase, special chars, numbers)
- Should enforce password policy at service layer

### 2. **No Duplicate Email Check Before Creating User**
- Already implemented (line 40) ✓

### 3. **No Role Default Assignment**
- When creating user without roleId, should assign default ROLE_USER
- Currently leaves role as null

### 4. **No Email Normalization**
- Emails should be converted to lowercase for consistency
- Example: "User@Email.com" vs "user@email.com" are treated as different emails

### 5. **No Audit Trail / Request Logging**
- No logging of who created/modified users
- Should add logging for security/compliance

---

## PRIORITY FIXES

**🔴 CRITICAL (Do immediately)**:
1. Move GET /me BEFORE GET /{id}
2. Add PasswordEncoder for password hashing
3. Add @PreAuthorize to DELETE endpoint

**🟠 HIGH (Do soon)**:
4. Add input trimming for email/fullName
5. Remove unnecessary user.setId(null)
6. Add email normalization (lowercase)

**🟡 MEDIUM (Nice to have)**:
7. Fix Vietnamese comments
8. Add default role assignment
9. Add logging

---
