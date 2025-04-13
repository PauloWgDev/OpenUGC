package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.Download;
import com.diversestudio.unityapi.repository.DownloadRepository;
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
}
