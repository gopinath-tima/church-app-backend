package com.church.churchapp.controller;

import com.church.churchapp.entity.ServingOpportunity;
import com.church.churchapp.service.ServingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serving")
@CrossOrigin(origins = {"http://localhost:3000", "https://divinearch.netlify.app"})
@RequiredArgsConstructor
public class ServingController {

    private final ServingService servingService;

    @GetMapping("/opportunities")
    public ResponseEntity<List<ServingOpportunity>> getOpenOpportunities() {
        return ResponseEntity.ok(servingService.getOpenOpportunities());
    }

    @PostMapping("/opportunities")
    public ResponseEntity<ServingOpportunity> createOpportunity(@RequestBody ServingOpportunity opportunity) {
        return ResponseEntity.ok(servingService.createOpportunity(opportunity));
    }
}
