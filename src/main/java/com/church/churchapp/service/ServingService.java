package com.church.churchapp.service;

import com.church.churchapp.entity.ServingOpportunity;
import com.church.churchapp.repository.ServingOpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServingService {

    private final ServingOpportunityRepository servingOpportunityRepository;

    public List<ServingOpportunity> getOpenOpportunities() {
        return servingOpportunityRepository.findByStatusOrderByDateAsc("OPEN");
    }
    
    public ServingOpportunity createOpportunity(ServingOpportunity opportunity) {
        if(opportunity.getStatus() == null) {
            opportunity.setStatus("OPEN");
        }
        return servingOpportunityRepository.save(opportunity);
    }
}
