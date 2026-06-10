package com.grupo_cordillera.microservicio_kpi.repository;

import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KpiMetricaRepository extends JpaRepository<KpiMetrica, Long> {
    // Buscamos métricas por el ID de la definición (útil para gráficos)
    List<KpiMetrica> findByDefinicionId(Long definicionId);
    List<KpiMetrica> findBySucursalId(Long sucursalId);
    Optional<KpiMetrica> findBySucursalIdAndDefinicionId(Long sucursalId, Long definicionId);

}