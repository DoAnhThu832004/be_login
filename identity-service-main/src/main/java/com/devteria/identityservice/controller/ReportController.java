package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.ReportCreationRequest;
import com.devteria.identityservice.dto.request.ReportStatusUpdateRequest;
import com.devteria.identityservice.dto.response.ReportResponse;
import com.devteria.identityservice.enums.ReportStatus;
import com.devteria.identityservice.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ApiResponse<ReportResponse> createReport(@RequestBody @Valid ReportCreationRequest request) {
        return ApiResponse.<ReportResponse>builder()
                .result(reportService.createReport(request))
                .build();
    }

    @GetMapping("/my-reports")
    public ApiResponse<List<ReportResponse>> getMyReports() {
        return ApiResponse.<List<ReportResponse>>builder()
                .result(reportService.getMyReports())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<ReportResponse>> getAllReports(
            @RequestParam(required = false) ReportStatus status) {
        List<ReportResponse> reports = (status != null)
                ? reportService.getReportsByStatus(status)
                : reportService.getAllReports();

        return ApiResponse.<List<ReportResponse>>builder()
                .result(reports)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reportId}/status")
    public ApiResponse<ReportResponse> updateReportStatus(
            @PathVariable String reportId,
            @RequestBody @Valid ReportStatusUpdateRequest request) {
        return ApiResponse.<ReportResponse>builder()
                .result(reportService.updateReportStatus(reportId, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{reportId}")
    public ApiResponse<String> deleteReport(@PathVariable String reportId) {
        reportService.deleteReport(reportId);
        return ApiResponse.<String>builder()
                .result("Report has been deleted successfully")
                .build();
    }
}