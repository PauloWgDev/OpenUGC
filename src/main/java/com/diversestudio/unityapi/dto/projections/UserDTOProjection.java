package com.diversestudio.unityapi.dto.projections;

import jakarta.persistence.*;

import java.sql.Timestamp;


@SqlResultSetMapping(
        name = "UserDTOMapping",
        classes = @ConstructorResult(
                targetClass = com.diversestudio.unityapi.dto.UserDTO.class,
                columns = {
                        @ColumnResult(name = "userId", type = Long.class),
                        @ColumnResult(name = "username", type = String.class),
                        @ColumnResult(name = "joinedAt", type = Timestamp.class),
                        @ColumnResult(name = "role", type = String.class),
                        @ColumnResult(name = "profilePicture", type = String.class),
                        @ColumnResult(name = "aboutMe", type = String.class),
                        @ColumnResult(name = "downloadsPerformed", type = Integer.class),
                        @ColumnResult(name = "downloadReceived", type = Integer.class),
                        @ColumnResult(name = "averageRatingsReceive", type = Float.class)
                }
        )
)
@Entity
public class UserDTOProjection {
    @Id
    private Long id;
}
