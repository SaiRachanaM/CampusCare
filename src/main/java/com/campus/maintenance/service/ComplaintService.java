package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.*;
import com.campus.maintenance.entity.User;
import com.campus.maintenance.util.ComplaintPriority;
import com.campus.maintenance.util.ComplaintStatus;

import java.util.List;

public interface ComplaintService {
    ComplaintResponseDto createComplaint(ComplaintCreateDto dto, User currentUser);
    ComplaintDetailResponseDto getComplaintById(Long complaintId, User currentUser);
    List<ComplaintResponseDto> getComplaintsForUser(User currentUser);
    List<ComplaintResponseDto> getAllComplaints(ComplaintStatus status, ComplaintPriority priority, User currentUser);
    ComplaintResponseDto assignComplaint(Long complaintId, ComplaintAssignDto dto, User currentUser);
    ComplaintResponseDto updateComplaintStatus(Long complaintId, ComplaintStatusUpdateDto dto, User currentUser);
    List<StatusHistoryResponseDto> getComplaintStatusHistory(Long complaintId, User currentUser);
}
