package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.DownloadRepository;
import com.diversestudio.unityapi.repository.RatingRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;
    private final RatingRepository ratingRepository;
    private final NativeQueryHelper nativeQueryHelper;

    public UserService(UserRepository userRepository, DownloadRepository downloadRepository,
                       RatingRepository ratingRepository, NativeQueryHelper nativeQueryHelper){
        this.userRepository = userRepository;
        this.downloadRepository = downloadRepository;
        this.ratingRepository = ratingRepository;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    public Page<UserDTO> getAllUsers(String prompt, Pageable pageable)
    {
        // Build the base SQL for users.
        StringBuilder sqlBuilder = new StringBuilder(nativeQueryHelper.getFindAllUsers());


        // If a prompt is provided, add filtering.
        if (prompt != null && !prompt.isEmpty()) {
            sqlBuilder.append(nativeQueryHelper.getWhereFilter("u.username"));
        }

        // Build dynamic ORDER BY.
        if (pageable.getSort().isSorted()) {
            sqlBuilder.append(" ORDER BY ");
            List<String> orderList = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                String property = order.getProperty();
                String direction = order.getDirection().toString();
                // For fields like averageRatingsReceive, add NULLS LAST if needed.
                if ("averageRatingsReceive".equals(property)) {
                    orderList.add(property + " " + direction + " NULLS LAST");
                } else {
                    orderList.add(property + " " + direction);
                }
            });
            sqlBuilder.append(String.join(", ", orderList));
        }
        // If no explicit sort but a prompt exists, sort by similarity.
        else if (prompt != null && !prompt.isEmpty()) {
            sqlBuilder.append(nativeQueryHelper.getOrderBySimilarity("u.username"));
        }
        // Otherwise, use a default sort.
        else {
            sqlBuilder.append(" ORDER BY joinedAt DESC");
        }

        // Append pagination.
        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), "UserDTOMapping");
        if (prompt != null && !prompt.isEmpty()) {
            query.setParameter("prompt", prompt);
        }
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        List<UserDTO> results = query.getResultList();
        long total = results.size();  // For production, you might want a COUNT(*) query.

        return new PageImpl<>(results, pageable, total);
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
             downloadsPerformed,
             downloadsReceived,
             averageRating
     );
    }
}
