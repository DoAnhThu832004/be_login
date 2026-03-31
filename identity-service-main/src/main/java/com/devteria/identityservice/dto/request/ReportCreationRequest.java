package com.devteria.identityservice.dto.request;

import com.devteria.identityservice.enums.IssueType;
import com.devteria.identityservice.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportCreationRequest {
    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotNull(message = "Issue type is required")
    private IssueType issueType;

    private String description;

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public IssueType getIssueType() { return issueType; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
