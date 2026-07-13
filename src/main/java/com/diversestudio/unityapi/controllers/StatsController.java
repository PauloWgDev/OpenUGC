package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.StatsDTO;
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
    public ResponseEntity<StatsDTO> getStats(@PathVariable("id") Long contentId)
    {
        StatsDTO stats = statsService.getStats(contentId);
        return ResponseEntity.ok(stats);
    }
}
