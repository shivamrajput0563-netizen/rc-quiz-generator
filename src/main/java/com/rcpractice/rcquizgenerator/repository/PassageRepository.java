package com.rcpractice.rcquizgenerator.repository;

import com.rcpractice.rcquizgenerator.entity.Passage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PassageRepository extends JpaRepository<Passage, Long> {

    @Query("SELECT COUNT(p) FROM Passage p WHERE p.createdAt >= :startOfDay")
    long countGeneratedSince(LocalDateTime startOfDay);

    @Query("SELECT p FROM Passage p WHERE p.isDailyRc = true AND p.createdAt >= :startOfDay ORDER BY p.createdAt DESC")
    Optional<Passage> findTodaysDailyRc(LocalDateTime startOfDay);

    @Query("SELECT p FROM Passage p WHERE p.isDailyRc = true ORDER BY p.createdAt DESC")
    List<Passage> findAllDailyRcHistory();
}