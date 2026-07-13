package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.entities.Download;
import com.diversestudio.unityapi.entities.DownloadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, DownloadId> {

    @Query("SELECT COUNT(d) FROM Download d WHERE d.userId = :userId")
    int countDownloadsPerformed(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM Download d JOIN Content c ON d.contentId = c.contentId WHERE c.creator.userId = :userId")
    int countDownloadsReceived(@Param("userId") Long userId);
}
