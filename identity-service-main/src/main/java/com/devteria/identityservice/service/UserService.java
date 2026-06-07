package com.devteria.identityservice.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.devteria.identityservice.dto.response.PageResponse;
import com.devteria.identityservice.dto.response.RoleResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.devteria.identityservice.constant.PredefinedRole;
import com.devteria.identityservice.dto.request.ChangePasswordRequest;
import com.devteria.identityservice.dto.request.UserCreationRequest;
import com.devteria.identityservice.dto.request.UserUpdateRequest;
import com.devteria.identityservice.dto.response.UserResponse;
import com.devteria.identityservice.entity.Genre;
import com.devteria.identityservice.entity.Role;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.UserMapper;
import com.devteria.identityservice.repository.GenreRepository;
import com.devteria.identityservice.repository.RoleRepository;
import com.devteria.identityservice.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor // Tự động sinh ra constructor chứa tất cả các field final hoặc có @NonNull trong class.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    GenreRepository genreRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    Cloudinary cloudinary;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        // Xử lý preferred genres — load Genre entities từ DB theo danh sách ID được cung cấp
        if (request.getPreferredGenreIds() != null && !request.getPreferredGenreIds().isEmpty()) {
            var genres = genreRepository.findAllById(request.getPreferredGenreIds());
            user.setPreferredGenres(new HashSet<>(genres));
            log.info("User {} đã chọn {} thể loại nhạc yêu thích khi đăng ký",
                    request.getUsername(), genres.size());
        }

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
    @Transactional
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Update các field cơ bản (trừ password, roles, genres)
        userMapper.updateUser(user, request);

        // Password
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        // Roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>(
                    roleRepository.findAllById(request.getRoles())
            );
            user.setRoles(roles);
        }

        // Preferred Genres
        if (request.getPreferredGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(request.getPreferredGenreIds())
            );
            user.setPreferredGenres(genres);
        }

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Chặn tài khoản người dùng — chỉ ADMIN được phép.
     * Sau khi bị chặn, user sẽ không thể đăng nhập cho đến khi được mở khoá.
     *
     * @param userId ID của user cần chặn
     * @return UserResponse với blocked = true
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse blockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setBlocked(true);
        log.info("Admin đã chặn tài khoản user: {}", user.getUsername());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    /**
     * Huỷ chặn tài khoản người dùng — chỉ ADMIN được phép.
     *
     * @param userId ID của user cần mở khoá
     * @return UserResponse với blocked = false
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse unblockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setBlocked(false);
        log.info("Admin đã mở khoá tài khoản user: {}", user.getUsername());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> pageUser = userRepository.findAll(pageable);

        // MAPPING THỦ CÔNG TỪ ENTITY SANG DTO
        List<UserResponse> userResponses = pageUser.getContent().stream()
                .map(user -> {
                    UserResponse res = new UserResponse();
                    res.setId(user.getId());
                    res.setUsername(user.getUsername());
                    res.setFirstName(user.getFirstName()); // Map tay từng trường
                    res.setLastName(user.getLastName());
                    res.setDob(user.getDob());
                    res.setImageUrl(user.getImageUrl());
                    res.setBlocked(user.isBlocked()); // Trạng thái chặn/mở khoá

                    // Nếu roles là một Set<Role>, bạn cũng phải map nó qua RoleResponse
                    if (user.getRoles() != null) {
                        res.setRoles(user.getRoles().stream()
                                .map(role -> {
                                    RoleResponse roleRes = new RoleResponse();
                                    roleRes.setName(role.getName());
                                    roleRes.setDescription(role.getDescription());
                                    return roleRes;
                                })
                                .collect(Collectors.toSet()));
                    }

                    return res;
                })
                .toList();

        return new PageResponse<>(
                page,
                pageUser.getTotalPages(),
                pageUser.getSize(),
                pageUser.getTotalElements(),
                userResponses
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
    @Transactional
    public UserResponse uploadProfileImage(String userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (image == null || image.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                ObjectUtils.asMap("folder", "user_app/images"));

        String imageUrl = uploadResult.get("secure_url").toString();
        user.setImageUrl(imageUrl);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Lấy username từ token của người đang đăng nhập
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        // Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} đã đổi mật khẩu thành công", username);
    }
}
