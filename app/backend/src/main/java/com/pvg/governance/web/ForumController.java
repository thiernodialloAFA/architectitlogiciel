package com.pvg.governance.web;

import com.pvg.governance.api.ForumDtos.ArbitrationRequest;
import com.pvg.governance.api.ForumDtos.ArbitrationResponse;
import com.pvg.governance.api.ForumDtos.ProposalCreateRequest;
import com.pvg.governance.api.ForumDtos.ProposalOutcomeRequest;
import com.pvg.governance.api.ForumDtos.ProposalResponse;
import com.pvg.governance.api.ForumDtos.ProposalScheduleRequest;
import com.pvg.governance.api.ForumDtos.SessionCreateRequest;
import com.pvg.governance.api.ForumDtos.SessionDetailResponse;
import com.pvg.governance.api.ForumDtos.SessionHoldRequest;
import com.pvg.governance.api.ForumDtos.SessionResponse;
import com.pvg.governance.domain.ProposalStatus;
import com.pvg.governance.service.ForumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Architecture Advice Forum & governance workflow (proposal §3.3). */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/sessions")
    public List<SessionResponse> sessions() {
        return forumService.findAllSessions();
    }

    @GetMapping("/sessions/{id}")
    public SessionDetailResponse session(@PathVariable UUID id) {
        return forumService.sessionDetail(id);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse createSession(@Valid @RequestBody SessionCreateRequest request,
                                         @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.createSession(request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/sessions/{id}/hold")
    public SessionResponse holdSession(@PathVariable UUID id,
                                       @RequestBody(required = false) SessionHoldRequest request,
                                       @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.holdSession(id, request, ActorHeader.sanitize(actor));
    }

    @GetMapping("/proposals")
    public List<ProposalResponse> proposals(@RequestParam(required = false) ProposalStatus status) {
        return forumService.findAllProposals(status);
    }

    @GetMapping("/proposals/{id}")
    public ProposalResponse proposal(@PathVariable UUID id) {
        return forumService.findProposalById(id);
    }

    @PostMapping("/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse createProposal(@Valid @RequestBody ProposalCreateRequest request,
                                           @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.createProposal(request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/proposals/{id}/schedule")
    public ProposalResponse schedule(@PathVariable UUID id,
                                     @Valid @RequestBody ProposalScheduleRequest request,
                                     @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.schedule(id, request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/proposals/{id}/outcome")
    public ProposalResponse recordOutcome(@PathVariable UUID id,
                                          @Valid @RequestBody ProposalOutcomeRequest request,
                                          @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.recordOutcome(id, request, ActorHeader.sanitize(actor));
    }

    /** Group-scope escalation only, never a default (proposal §3.3). */
    @PostMapping("/proposals/{id}/arbitration")
    @ResponseStatus(HttpStatus.CREATED)
    public ArbitrationResponse arbitrate(@PathVariable UUID id,
                                         @Valid @RequestBody ArbitrationRequest request,
                                         @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return forumService.arbitrate(id, request, ActorHeader.sanitize(actor));
    }

    /** Visibility of how often arbitration is actually invoked — it should be rare. */
    @GetMapping("/arbitrations")
    public List<ArbitrationResponse> arbitrations() {
        return forumService.findAllArbitrations();
    }
}
