package com.cyphershare.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transfer_audits")
public class TransferAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String sessionCode;

    @Column(nullable = false)
    private String senderIp;

    private String receiverIp;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private SubscriptionTier tierUsed;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    @PrePersist
    protected void onStart() {
        startedAt = new Date();
    }

    public void complete() {
        completedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public String getSenderIp() { return senderIp; }
    public void setSenderIp(String senderIp) { this.senderIp = senderIp; }

    public String getReceiverIp() { return receiverIp; }
    public void setReceiverIp(String receiverIp) { this.receiverIp = receiverIp; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public SubscriptionTier getTierUsed() { return tierUsed; }
    public void setTierUsed(SubscriptionTier tierUsed) { this.tierUsed = tierUsed; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}
