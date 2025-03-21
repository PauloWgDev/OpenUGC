package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.DownloadRepository;
import com.diversestudio.unityapi.repository.RatingRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;
    private final RatingRepository ratingRepository;

    public UserService(UserRepository userRepository, DownloadRepository downloadRepository, RatingRepository ratingRepository){
        this.userRepository = userRepository;
        this.downloadRepository = downloadRepository;
        this.ratingRepository = ratingRepository;
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
        int downloadsPerformed = downloadRepository.countDownloadsPerformed(user.getUserId());
        int downloadsReceived = downloadRepository.countDownloadsReceived(user.getUserId());
        float averageRating = ratingRepository.averageRatingOfUserContent(user.getUserId());

     return new UserDTO(
             user.getUserId(),
             user.getUsername(),
             user.getJoinedAt(),
             user.getRole() != null ? user.getRole().getRoleName() : null,
             user.getProfilePicture(),
             user.getAboutMe(),
             downloadsPerformed, // i should get the statists values
             downloadsReceived,
             averageRating
     );
    }
}
