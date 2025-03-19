package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Optional<UserDTO> getUserById(Long id){
        return userRepository.findById(id).map(this::convertToDTO);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDTO createUser(User user) {
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    private UserDTO convertToDTO(User user){
     return new UserDTO(
             user.getUserId(),
             user.getUsername(),
             user.getJoinedAt(),
             user.getRole() != null ? user.getRole().getRoleName() : null,
             user.getProfilePicture(),
             user.getAboutMe(),
             0, // i should get the statists values
             0,
             0f
     );
    }
}
