package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Report;
import com.devteria.identityservice.entity.User;
import com.devteria.identityservice.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report,String> {
    List<Report> findByUserOrderByCreatedAtDesc(User user);
    List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);
}
