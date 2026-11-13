package com.diversestudio.openugc.service;

import com.diversestudio.openugc.dto.StatsDTO;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.repository.StatsRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository){
        this.statsRepository = statsRepository;
    }

    /**
     * @throws ResourceNotFoundException if no content has this id (404)
     */
    public StatsDTO getStats(Long contentId)
    {
        StatsDTO stats = statsRepository.findStatsByContentId(contentId);
        if (stats == null) {
            throw new ResourceNotFoundException("Content not found for id: " + contentId);
        }
        return stats;
    }
}
