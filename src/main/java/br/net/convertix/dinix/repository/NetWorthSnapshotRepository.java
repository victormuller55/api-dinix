package br.net.convertix.dinix.repository;

import br.net.convertix.dinix.entity.NetWorthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NetWorthSnapshotRepository extends JpaRepository<NetWorthSnapshot, UUID> {

    List<NetWorthSnapshot> findByUserIdOrderByYearAscMonthAsc(UUID userId);

    Optional<NetWorthSnapshot> findByUserIdAndMonthAndYear(UUID userId, Integer month, Integer year);
}
