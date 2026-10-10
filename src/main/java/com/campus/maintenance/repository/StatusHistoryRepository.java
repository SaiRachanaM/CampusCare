package com.campus.maintenance.repository;

import com.campus.maintenance.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByComplaint_ComplaintIdOrderByChangedAtAsc(Long complaintId);
}
