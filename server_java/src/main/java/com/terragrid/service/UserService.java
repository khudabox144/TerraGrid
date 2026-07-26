package com.terragrid.service;

import com.terragrid.dto.UserCreateRequest;
import com.terragrid.dto.UserResponse;
import com.terragrid.dto.UserUpdateRequest;
import com.terragrid.exception.ResourceNotFoundException;
import com.terragrid.exception.UserAlreadyExistsException;
import com.terragrid.mapper.UserMapper;
import com.terragrid.model.Role;
import com.terragrid.model.User;
import com.terragrid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Integer id) {
        log.debug("Fetching user with ID: {}", id);
        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        validateUserDoesNotExist(request.getEmail());
        
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }

    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User user = findUserById(id);
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", id);
        
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Integer id) {
        log.info("Deleting user with ID: {}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    public void deactivateUser(Integer id) {
        log.info("Deactivating user with ID: {}", id);
        User user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated successfully with ID: {}", id);
    }

    public void activateUser(Integer id) {
        log.info("Activating user with ID: {}", id);
        User user = findUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
        log.info("User activated successfully with ID: {}", id);
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
    }
}