package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.application.port.out.NotificationRepository;
import dev.civicpulse.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class NotificationRepositoryAdapter implements NotificationRepository {

  private final NotificationJpaRepository jpaRepository;
  private final NotificationMapper mapper;

  NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Notification save(Notification notification) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
  }

  @Override
  public Optional<Notification> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Notification> findByRecipient(UUID recipientAccountId, int page, int pageSize) {
    return jpaRepository.findByRecipientAccountId(recipientAccountId, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public long countUnread(UUID recipientAccountId) {
    return jpaRepository.countByRecipientAccountIdAndReadFalse(recipientAccountId);
  }

  @Override
  public boolean existsByRecipientAndSourceEventId(UUID recipientAccountId, String sourceEventId) {
    return jpaRepository.existsByRecipientAccountIdAndSourceEventId(recipientAccountId, sourceEventId);
  }
}
