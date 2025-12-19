package com.devteria.identityservice.service;

import com.devteria.identityservice.entity.Year;
import com.devteria.identityservice.repository.YearRepository;
import org.springframework.stereotype.Service;

@Service
public class YearService {
    private final YearRepository yearRepository;

    public YearService(YearRepository yearRepository) {
        this.yearRepository = yearRepository;
    }
    public Year createYear(Integer year) {
        Year existing = getByYear(year);
        if(existing != null) {
            return existing;
        }
        Year newYear = new Year();
        newYear.setYear(year);
        return yearRepository.save(newYear);
    }
    public Year getByYear(Integer yearValue) {return yearRepository.findByYear(yearValue).orElse(null);}
}
