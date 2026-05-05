package com.casebridge.backend.team.service;

import com.casebridge.backend.team.dto.TeamActivityResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TeamActivityService {
    private static final int MAX_EVENTS = 300;
    private final ConcurrentLinkedDeque<TeamActivityResponse> events = new ConcurrentLinkedDeque<>();

    @Async("teamActivityExecutor")
    public void record(String action, String teamName, String actorName, String details) {
        events.addFirst(new TeamActivityResponse(OffsetDateTime.now(), action, teamName, actorName, details));
        while (events.size() > MAX_EVENTS) {
            events.pollLast();
        }
    }

    public List<TeamActivityResponse> getRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<TeamActivityResponse> result = new ArrayList<>(safeLimit);
        int i = 0;
        for (TeamActivityResponse event : events) {
            if (i++ >= safeLimit) {
                break;
            }
            result.add(event);
        }
        return result;
    }
}
