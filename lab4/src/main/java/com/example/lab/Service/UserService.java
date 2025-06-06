package com.example.lab.Service;

import com.example.lab.DTO.UserDTO;
import com.example.lab.Model.User;
import com.example.lab.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.lab.Cache.InMemoryCache;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final InMemoryCache cache;

    @Autowired
    public UserService(UserRepository userRepository, InMemoryCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        User savedUser = userRepository.save(user);
        cache.putUser(savedUser.getId(), savedUser);
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        if (cache.containsUserKey(id)) {
            return Optional.of(cache.getUser(id));
        }

        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> cache.putUser(u.getId(), u));
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .peek(user -> cache.putUser(user.getId(), user))
                .collect(Collectors.toList());
    }

    public User updateUser(Long id, UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(userDTO.getUsername());
            User updatedUser = userRepository.save(user);
            cache.putUser(updatedUser.getId(), updatedUser);
            cache.removeUserConversions(id);
            return updatedUser;
        }
        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        cache.removeUser(id);
        cache.removeUserConversions(id);
    }
}
