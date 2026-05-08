package com.grupo_cordillera.microservicio_kpi.service;

import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.repository.KpiMetricaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiService {

    private final KpiMetricaRepository metricaRepository;

    // Nombre del circuito definido en la configuración
    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldo")
    public List<KpiMetrica> obtenerMetricasPorDefinicion(Long definicionId) {
        log.info("Consultando métricas para el KPI ID: {}", definicionId);

        // Aquí es donde el Circuit Breaker vigila:
        // Si la base de datos NeonDB falla o hay mucha latencia, saltará al fallback.
        return metricaRepository.findByDefinicionId(definicionId);
    }

    /**
     * MÉTODO DE RESPALDO (Fallback)
     * Se ejecuta automáticamente si el método principal falla.
     * Esto evita que el microservicio devuelva un error 500 a la alta gerencia.
     */
    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activó debido a: {}", t.getMessage());

        // Comentario equipo: Retornamos una lista vacía o datos simulados
        // para que la interfaz de usuario no se rompa (Graceful Degradation).
        return new ArrayList<>();
    }
}