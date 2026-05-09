package com.grupo_cordillera.microservicio_kpi.service;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.repository.KpiDefinicionRepository;
import com.grupo_cordillera.microservicio_kpi.repository.KpiMetricaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiService {

    // 1. Necesitas inyectar AMBOS repositorios para usarlos
    @Autowired
    private final KpiMetricaRepository metricaRepository;

    @Autowired
    private final KpiDefinicionRepository kpiDefinicionRepository; // <-- Faltaba este

    // 2. CORRECCIÓN: Usar la variable 'kpiDefinicionRepository' (minúscula)
    // y NO la clase 'KpiDefinicionRepository' (mayúscula)

    // Crear o Actualizar KPI
    public KpiDefinicion guardarKpi(KpiDefinicion kpi){
        return kpiDefinicionRepository.save(kpi); // ✅ Correcto: usa la instancia
    }

    // Buscar KPI
    public Optional<KpiDefinicion> obtenerId(Long id){
        return kpiDefinicionRepository.findById(id); // ✅ Correcto
    }

    // Eliminar KPI
    public void eliminarKpi(Long id){
        kpiDefinicionRepository.deleteById(id); // ✅ Correcto
    }

    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldo")
    public List<KpiMetrica> obtenerMetricasPorDefinicion(Long definicionId) {
        log.info("Consultando métricas para el KPI ID: {}", definicionId);
        return metricaRepository.findByDefinicionId(definicionId);
    }

    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activó debido a: {}", t.getMessage());
        return new ArrayList<>();
    }
}