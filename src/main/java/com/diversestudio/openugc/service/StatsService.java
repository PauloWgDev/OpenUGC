package com.diversestudio.openugc.service;

import com.diversestudio.openugc.dto.StatsDTO;
import com.diversestudio.openugc.repository.StatsRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository){
        this.statsRepository = statsRepository;
    }

    public StatsDTO getStats(Long contentId)
    {
        return statsRepository.findStatsByContentId(contentId);
    }
}
