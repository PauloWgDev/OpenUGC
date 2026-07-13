package com.diversestudio.unityapi.dto.projections;

import jakarta.persistence.*;

import java.sql.SQLTimeoutException;
import java.sql.Timestamp;


@SqlResultSetMapping(
        name="ContentDTOMapping",
        classes = @ConstructorResult(
                targetClass = com.diversestudio.unityapi.dto.ContentDTO.class,
                columns = {
                        @ColumnResult(name = "contentId", type = Long.class),
                        @ColumnResult(name = "creatorId", type = Long.class),
                        @ColumnResult(name = "creatorName", type = String.class),
                        @ColumnResult(name = "data", type = String.class),
                        @ColumnResult(name = "thumbnail", type = String.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "tags", type = String[].class),
                        @ColumnResult(name = "version", type = Integer.class),
                        @ColumnResult(name = "downloadsCount", type = Long.class),
                        @ColumnResult(name = "latestDownloadsCount", type = Long.class),
                        @ColumnResult(name = "avgRating", type = Double.class),
                        @ColumnResult(name = "createdAt", type = Timestamp.class),
                        @ColumnResult(name = "updatedAt", type = Timestamp.class)
                }
        )
)
@Entity
public class ContentDTOProjection {
    @Id
    private Long id;
}
