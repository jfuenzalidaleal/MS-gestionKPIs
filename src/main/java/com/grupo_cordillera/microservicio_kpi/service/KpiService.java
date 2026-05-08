package com.grupo_cordillera.microservicio_kpi.service;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.repository.KpiDefinicionRepository;
import com.grupo_cordillera.microservicio_kpi.repository.KpiMetricaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiService {

    private final KpiMetricaRepository metricaRepository;
    //Crear o Actualizar KPI
    public KpiDefinicion guardarKpi(KpiDefinicion kpi){
        return KpiDefinicionRepository.save(kpi);
    }
    //buscar KPI
    public Optional<KpiDefinicion> obtenerId(Long id){
        return KpiDefinicionRepository.findById(id);
    }

    public  void eliminarKpi(Long id){
        KpiDefinicionRepository.deleteByI(id);
    }

    // Nombre del circuito definido en la configuración
    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldo")
    public List<KpiMetrica> obtenerMetricasPorDefinicion(Long definicionId) {
        log.info("Consultando métricas para el KPI ID: {}", definicionId);

        // Aquí es donde el Circuit Breaker vigila:
        // Si la base de datos falla o hay mucha latencia, saltará al fallback.
        return metricaRepository.findByDefinicionId(definicionId);
    }

    /**
     * MÉTODO DE RESPALDO (Fallback)
     * Se ejecuta automáticamente si el método principal falla.
     * Esto evita que el microservicio devuelva un error 500 a la alta gerencia.
     */
    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activó debido a: {}", t.getMessage());

        //Retornamos una lista vacía o datos simulados
        // para que la interfaz de usuario no se rompa (Graceful Degradation).
        return new ArrayList<>();
    }
}