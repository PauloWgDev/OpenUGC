package com.diversestudio.openugc.controller;


import com.diversestudio.openugc.entity.Download;
import com.diversestudio.openugc.service.DownloadService;
import com.diversestudio.openugc.util.NativeQueryHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/download")
public class DownloadController {
    private final NativeQueryHelper nativeQueryHelper;
    private final DownloadService downloadService;

    public DownloadController(NativeQueryHelper nativeQueryHelper, DownloadService downloadService)
    {
        this.nativeQueryHelper = nativeQueryHelper;
        this.downloadService = downloadService;
    }

    @PostMapping
    ResponseEntity<Download> registerDownload(@RequestBody Download download)
    {
        Download savedDownload = downloadService.registerDownload(download);
        return new ResponseEntity<>(savedDownload, HttpStatus.CREATED);
    }
}
