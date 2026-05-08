package com.grupo_cordillera.microservicio_kpi.repository;

import com.grupo_cordillera.microservicio_kpi.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    // Hereda todos los métodos básicos de CRUD
}