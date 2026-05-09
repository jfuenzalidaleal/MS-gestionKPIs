package com.grupo_cordillera.microservicio_kpi.repository;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KpiDefinicionRepository extends JpaRepository<KpiDefinicion, Long> {
}