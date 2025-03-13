package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.StatsDTO;
import com.diversestudio.unityapi.repository.StatsRepository;
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
