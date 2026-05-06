package com.cyphershare.repository;

import com.cyphershare.model.TransferAudit;
import com.cyphershare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransferAuditRepository extends JpaRepository<TransferAudit, Long> {
    Optional<TransferAudit> findBySessionCode(String sessionCode);
    List<TransferAudit> findByUserOrderByStartedAtDesc(User user);
}
