package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Download;
import com.diversestudio.unityapi.repository.DownloadRepository;
import com.diversestudio.unityapi.security.AuthHelper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class DownloadService {

    private final DownloadRepository downloadRepository;

    public DownloadService(DownloadRepository downloadRepository)
    {
        this.downloadRepository = downloadRepository;
    }

    public Download registerDownload(Download download)
    {
        download.setDate(new Timestamp(System.currentTimeMillis()));
        return downloadRepository.save(download);
    }

    public Download registerDownload(ContentDTO content)
    {
        // Create the download record
        Download download = new Download();
        download.setContentId(content.contentId());
        // Assuming ContentDTO provides the version as an int. Adapt if necessary.
        download.setContentVersion(content.version());

        // Retrieve the current user id
        Long currentUserId = AuthHelper.getCurrentUserId();
        download.setUserId(currentUserId);

        // Register the download (this method sets the current timestamp)
        registerDownload(download);

        return download;
    }
}
