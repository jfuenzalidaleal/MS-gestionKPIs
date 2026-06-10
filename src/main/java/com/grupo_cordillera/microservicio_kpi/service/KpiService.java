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
            def.setTipoCalculo(datos.getTipoCalculo()); // 🌟 No olvides mapear el nuevo campo aquí también
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

    public void acumularProgresoVenta(Long sucursalId, List<java.util.Map<String, Object>> itemsVendidos) {
        log.info("Procesando métricas para la sucursal ID: {}. Líneas totales en venta: {}", sucursalId, itemsVendidos.size());

        // 1. Buscamos todas las definiciones de KPIs guardadas (Ventas Totales, Unidades Totales, etc.)
        List<KpiDefinicion> definiciones = definicionRepository.findAll();

        if (definiciones.isEmpty() || itemsVendidos == null || itemsVendidos.isEmpty()) {
            return;
        }

        // 2. Evaluamos cada indicador de forma independiente
        for (KpiDefinicion definicion : definiciones) {
            double valorAIncrementar = 0.0;

            // ─── OPCIÓN 1: Contar la venta como una sola transacción única ───
            if ("CONTAR_TRANSACCIONES".equalsIgnoreCase(definicion.getTipoCalculo())) {
                valorAIncrementar = 1.0; // Suma 1 independiente de los artículos de la venta

                // ─── OPCIÓN 2: Sumar absolutamente todos los productos tecnológicos vendidos ───
            } else if ("SUMAR_PRODUCTOS".equalsIgnoreCase(definicion.getTipoCalculo())) {
                for (java.util.Map<String, Object> item : itemsVendidos) {
                    Number cantidadNum = (Number) item.get("cantidad");
                    if (cantidadNum != null) {
                        // Suma directo las cantidades físicas sin importar el tipo de artículo
                        valorAIncrementar += cantidadNum.intValue();
                    }
                }
            }

            if (valorAIncrementar == 0.0) continue;

            // 3. Guardar o actualizar el progreso acumulado en tu tabla de métricas
            Optional<KpiMetrica> metricaOpt = metricaRepository.findBySucursalIdAndDefinicionId(sucursalId, definicion.getId());

            if (metricaOpt.isPresent()) {
                KpiMetrica metricaExistente = metricaOpt.get();
                double valorActual = metricaExistente.getValorActual() != null ? metricaExistente.getValorActual() : 0.0;
                metricaExistente.setValorActual(valorActual + valorAIncrementar);
                metricaRepository.save(metricaExistente);
            } else {
                KpiMetrica nuevaMetrica = new KpiMetrica();
                nuevaMetrica.setSucursalId(sucursalId);
                nuevaMetrica.setDefinicion(definicion);
                nuevaMetrica.setValorActual(valorAIncrementar);
                metricaRepository.save(nuevaMetrica);
            }
        }
    }

    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activo debido a: {}", t.getMessage());
        return new ArrayList<>();
    }
}