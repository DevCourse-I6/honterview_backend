package com.i6.honterview.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.i6.honterview.domain.Record;

public interface RecordRepository extends JpaRepository<Record, Long> {
}
