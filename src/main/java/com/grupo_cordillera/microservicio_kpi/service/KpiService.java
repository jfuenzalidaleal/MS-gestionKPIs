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
    private final KpiDefinicionRepository definicionRepository;

    // ─── KpiDefinicion ───────────────────────────────────────────────

    public List<KpiDefinicion> listarDefiniciones() {
        return definicionRepository.findAll();
    }

    public Optional<KpiDefinicion> obtenerDefinicionPorId(Long id) {
        return definicionRepository.findById(id);
    }

    public KpiDefinicion guardarDefinicion(KpiDefinicion definicion) {
        return definicionRepository.save(definicion);
    }

    public Optional<KpiDefinicion> actualizarDefinicion(Long id, KpiDefinicion datos) {
        return definicionRepository.findById(id).map(def -> {
            def.setNombre(datos.getNombre());
            def.setDescripcion(datos.getDescripcion());
            def.setValorObjetivo(datos.getValorObjetivo());
            def.setUnidad(datos.getUnidad());
            return definicionRepository.save(def);
        });
    }

    public void eliminarDefinicion(Long id) {
        definicionRepository.deleteById(id);
    }

    // ─── KpiMetrica ──────────────────────────────────────────────────

    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldo")
    public List<KpiMetrica> obtenerMetricasPorDefinicion(Long definicionId) {
        log.info("Consultando metricas para el KPI ID: {}", definicionId);
        return metricaRepository.findByDefinicionId(definicionId);
    }

    public KpiMetrica guardarMetrica(KpiMetrica metrica) {
        return metricaRepository.save(metrica);
    }

    public void eliminarMetrica(Long id) {
        metricaRepository.deleteById(id);
    }

    // 🌟 MÉTODO CORREGIDO: Itera sobre todas las definiciones e impacta según la sucursal real
    public void acumularProgresoVenta(Long sucursalId, Integer cantidad) {
        log.info("Impactando métricas de forma selectiva para sucursal ID: {} con cantidad: {}", sucursalId, cantidad);

        // 1. Buscamos todas las definiciones (metas globales) creadas en el sistema
        List<KpiDefinicion> definiciones = definicionRepository.findAll();

        if (definiciones.isEmpty()) {
            log.warn("No se pudo acumular porque no existen Definiciones de KPIs creadas en el sistema.");
            return;
        }

        // 2. Evaluamos la venta para cada definición existente de manera independiente
        for (KpiDefinicion definicion : definiciones) {

            // Buscamos si ya existe el registro específico para esta Sucursal y este KPI
            Optional<KpiMetrica> metricaOpt = metricaRepository.findBySucursalIdAndDefinicionId(sucursalId, definicion.getId());

            if (metricaOpt.isPresent()) {
                // Caso A: Ya existe la combinación -> Sumamos de forma acumulativa real
                KpiMetrica metricaExistente = metricaOpt.get();
                double valorActual = metricaExistente.getValorActual() != null ? metricaExistente.getValorActual() : 0.0;
                metricaExistente.setValorActual(valorActual + cantidad);

                metricaRepository.save(metricaExistente);
                log.info("Métrica ID {} (KPI: {}) actualizada. Nuevo valor: {}",
                        metricaExistente.getId(), definicion.getNombre(), metricaExistente.getValorActual());
            } else {
                // Caso B: No existe registro para esta sucursal en este KPI específico -> Lo creamos limpio
                KpiMetrica nuevaMetrica = new KpiMetrica();
                nuevaMetrica.setSucursalId(sucursalId);
                nuevaMetrica.setDefinicion(definicion); // 👈 Se vincula a su definición correspondiente del bucle
                nuevaMetrica.setValorActual((double) cantidad);

                metricaRepository.save(nuevaMetrica);
                log.info("Se generó nueva fila para sucursal {} bajo el KPI: {}", sucursalId, definicion.getNombre());
            }
        }
    }

    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activo debido a: {}", t.getMessage());
        return new ArrayList<>();
    }
}