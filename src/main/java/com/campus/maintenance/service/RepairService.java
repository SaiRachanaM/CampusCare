package com.campus.maintenance.service;

import com.campus.maintenance.dto.domain.RepairCreateDto;
import com.campus.maintenance.dto.domain.RepairResponseDto;
import com.campus.maintenance.entity.User;

public interface RepairService {
    RepairResponseDto recordRepair(RepairCreateDto dto, User loggedInStaff);
    RepairResponseDto getRepairByComplaintId(Long complaintId, User currentUser);
}
