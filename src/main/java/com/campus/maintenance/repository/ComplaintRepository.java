package com.campus.maintenance.repository;

import com.campus.maintenance.entity.Complaint;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByReportedBy_UserIdOrderByReportedAtDesc(Long userId);

    List<Complaint> findByAssignedTo_UserIdOrderByReportedAtDesc(Long staffId);

    List<Complaint> findByStatusInOrderByReportedAtDesc(Collection<ComplaintStatus> statuses);

    List<Complaint> findByStatusOrderByReportedAtDesc(ComplaintStatus status);

    List<Complaint> findByPriorityOrderByReportedAtDesc(ComplaintPriority priority);

    @Query("SELECT c FROM Complaint c WHERE c.status NOT IN (:terminalStatuses) AND c.dueAt IS NOT NULL AND c.dueAt < :now ORDER BY c.dueAt ASC")
    List<Complaint> findOverdueComplaints(
            @Param("terminalStatuses") Collection<ComplaintStatus> terminalStatuses,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countComplaintsGroupByStatus();

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countComplaintsGroupByPriority();

    @Query("SELECT c.asset.assetId, c.asset.assetName, c.location.building, COUNT(c) " +
           "FROM Complaint c WHERE c.asset IS NOT NULL " +
           "GROUP BY c.asset.assetId, c.asset.assetName, c.location.building " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> findFrequentlyFailingAssets();
}
