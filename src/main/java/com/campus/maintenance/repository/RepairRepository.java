package com.campus.maintenance.repository;

import com.campus.maintenance.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {

    Optional<Repair> findByComplaint_ComplaintId(Long complaintId);

    boolean existsByComplaint_ComplaintId(Long complaintId);

    @Query("SELECT COALESCE(SUM(r.cost), 0), COALESCE(AVG(r.cost), 0), COUNT(r) FROM Repair r")
    Object[] getRepairCostSummary();
}
