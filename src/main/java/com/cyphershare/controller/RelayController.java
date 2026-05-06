package com.cyphershare.controller;

import com.cyphershare.service.SessionManager;
import com.cyphershare.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.cyphershare.security.JwtUtils;
import com.cyphershare.model.User;
import com.cyphershare.model.TransferAudit;
import com.cyphershare.model.SubscriptionTier;
import com.cyphershare.repository.UserRepository;
import com.cyphershare.repository.TransferAuditRepository;

import java.util.Map;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/relay")
@CrossOrigin(origins = "*") // In production, restrict to frontend origin
public class RelayController {

    private final SessionManager sessionManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final TransferAuditRepository transferAuditRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    public RelayController(SessionManager sessionManager, JwtUtils jwtUtils, UserRepository userRepository, TransferAuditRepository transferAuditRepository, EmailService emailService) {
        this.sessionManager = sessionManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.transferAuditRepository = transferAuditRepository;
        this.emailService = emailService;
    }

    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody(required = false) Map<String, String> requestBody,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader,
                                           HttpServletRequest httpRequest) {
        String tier = "BASIC";
        String customCode = requestBody != null ? requestBody.get("customCode") : null;
        User user = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                String email = jwtUtils.getEmailFromJwtToken(token);
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    tier = user.getTier().name();
                }
            }
        }

        String code;
        if ("BUSINESS".equals(tier) && customCode != null && !customCode.isBlank()) {
            if (sessionManager.validateSession(customCode)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Code already in use"));
            }
            code = customCode;
        } else {
            code = String.format("%06d", random.nextInt(1000000));
        }

        sessionManager.createSession(code, tier);

        TransferAudit audit = new TransferAudit();
        audit.setSessionCode(code);
        audit.setTierUsed(SubscriptionTier.valueOf(tier));
        audit.setSenderIp(httpRequest.getRemoteAddr() != null ? httpRequest.getRemoteAddr() : "UNKNOWN");
        if (user != null) {
            audit.setUser(user);
        }
        transferAuditRepository.save(audit);
        
        return ResponseEntity.ok(Map.of(
                "code", code,
                "tier", tier,
                "message", "Session created successfully"
        ));
    }

    @GetMapping("/session/{code}/validate")
    public ResponseEntity<?> validateSession(@PathVariable String code) {
        boolean isValid = sessionManager.validateSession(code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.status(404).body(Map.of("valid", false, "message", "Invalid or expired code"));
        }
    }

    @GetMapping("/audits")
    public ResponseEntity<?> getUserAudits(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                String email = jwtUtils.getEmailFromJwtToken(token);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    List<TransferAudit> audits = transferAuditRepository.findByUserOrderByStartedAtDesc(user);
                    return ResponseEntity.ok(audits.stream().map(a -> Map.of(
                        "sessionCode", a.getSessionCode(),
                        "startedAt", a.getStartedAt() != null ? a.getStartedAt().getTime() : 0,
                        "completedAt", a.getCompletedAt() != null ? a.getCompletedAt().getTime() : 0,
                        "fileSize", a.getFileSize() != null ? a.getFileSize() : 0,
                        "receiverIp", a.getReceiverIp() != null ? a.getReceiverIp() : "N/A"
                    )).toList());
                }
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> payload, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String toEmail = payload.get("toEmail");
        String sessionCode = payload.get("sessionCode");
        
        if (toEmail == null || sessionCode == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing toEmail or sessionCode"));
        }
        
        String senderEmail = "Anonymous User";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                 senderEmail = jwtUtils.getEmailFromJwtToken(token);
            }
        }
        
        emailService.sendSessionCodeEmail(toEmail, sessionCode, senderEmail);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
    }
}
