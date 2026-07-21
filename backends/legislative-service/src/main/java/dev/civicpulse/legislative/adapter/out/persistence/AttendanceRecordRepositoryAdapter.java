package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.AttendanceRecordRepository;
import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AttendanceRecordRepositoryAdapter implements AttendanceRecordRepository {

  private final AttendanceRecordJpaRepository jpaRepository;
  private final AttendanceRecordMapper mapper;

  AttendanceRecordRepositoryAdapter(AttendanceRecordJpaRepository jpaRepository, AttendanceRecordMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AttendanceRecord save(AttendanceRecord record) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(record)));
  }

  @Override
  public Optional<AttendanceRecord> findById(UUID politicianAccountId) {
    return jpaRepository.findById(politicianAccountId).map(mapper::toDomain);
  }

  @Override
  public void deleteById(UUID politicianAccountId) {
    jpaRepository.deleteById(politicianAccountId);
  }
}
