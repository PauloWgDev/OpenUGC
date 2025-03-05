package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.StatsResponse;
import com.diversestudio.unityapi.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService)
    {
        this.statsService = statsService;
    }


    // GET /api/stats/{id}
    @GetMapping("/{id}")
    public ResponseEntity<StatsResponse> getStats(@PathVariable("id") Long contentId)
    {
        StatsResponse stats = statsService.getStats(contentId);
        return ResponseEntity.ok(stats);
    }
}
