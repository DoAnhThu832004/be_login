package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Year;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface YearRepository extends JpaRepository<Year,String> {
    Optional<Year> findByYear(Integer year);
}
