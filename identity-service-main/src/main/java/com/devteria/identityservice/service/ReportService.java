package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.ReportCreationRequest;
import com.devteria.identityservice.dto.request.ReportStatusUpdateRequest;
import com.devteria.identityservice.dto.response.ReportResponse;
import com.devteria.identityservice.entity.Report;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.enums.ReportStatus;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.ReportRepository;
import com.devteria.identityservice.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReportResponse createReport(ReportCreationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Report report = new Report();
        report.setUser(user);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setIssueType(request.getIssueType());
        report.setDescription(request.getDescription());

        return toReportResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    public List<ReportResponse> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    public List<ReportResponse> getMyReports() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return reportRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse updateReportStatus(String reportId, ReportStatusUpdateRequest request) {
        Report report = reportRepository.findById(reportId)
                // Cần khai báo REPORT_NOT_EXISTED trong ErrorCode
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        report.setStatus(request.getStatus());
        return toReportResponse(reportRepository.save(report));
    }

    @Transactional
    public void deleteReport(String reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        reportRepository.deleteById(reportId);
    }

    private ReportResponse toReportResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setUsername(report.getUser().getUsername());
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setIssueType(report.getIssueType());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }
}