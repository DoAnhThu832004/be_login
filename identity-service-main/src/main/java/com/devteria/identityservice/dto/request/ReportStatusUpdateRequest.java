package com.devteria.identityservice.dto.request;

import com.devteria.identityservice.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class ReportStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
}
