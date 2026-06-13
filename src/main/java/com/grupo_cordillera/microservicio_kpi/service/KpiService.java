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
import java.util.stream.Collectors;

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

    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldoDefiniciones")
    public List<KpiDefinicion> listarDefinicionesSeguras(String userRole, Long sucursalAutenticada) {
        if (userRole != null && "ADMIN".equalsIgnoreCase(userRole.trim())) {
            return definicionRepository.findAll();
        }

        log.info("[🔒 AISLAMIENTO DASHBOARD] -> Filtrando estructura de KPIs para Sucursal ID: {}", sucursalAutenticada);

        List<KpiMetrica> metricasSucursal = metricaRepository.findAll().stream()
                .filter(m -> m.getSucursalId() != null && m.getSucursalId().equals(sucursalAutenticada))
                .collect(Collectors.toList());

        return metricasSucursal.stream()
                .map(KpiMetrica::getDefinicion)
                .distinct()
                .collect(Collectors.toList());
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
            def.setTipoCalculo(datos.getTipoCalculo());
            return definicionRepository.save(def);
        });
    }

    public void eliminarDefinicion(Long id) {
        decayDefinicion(id);
    }

    private void decayDefinicion(Long id) {
        definicionRepository.deleteById(id);
    }

    // ─── KpiMetrica ──────────────────────────────────────────────────

    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldo")
    public List<KpiMetrica> obtenerMetricasPorDefinicion(Long definicionId) {
        log.info("Consultando metricas globales para el KPI ID: {}", definicionId);
        return metricaRepository.findByDefinicionId(definicionId);
    }

    @CircuitBreaker(name = "kpiService", fallbackMethod = "metodoRespaldoSeguro")
    public List<KpiMetrica> obtenerMetricasPorDefinicionSegura(Long definicionId, String userRole, Long sucursalAutenticada) {
        log.info("Consulta Segura -> KPI ID: {} | Rol: {} | Sucursal JWT: {}", definicionId, userRole, sucursalAutenticada);

        List<KpiMetrica> metricas = metricaRepository.findByDefinicionId(definicionId);

        if (userRole != null && !"ADMIN".equalsIgnoreCase(userRole.trim())) {
            log.info("[🔒 AISLAMIENTO KPI] -> Filtrando datos únicamente para Sucursal ID: {}", sucursalAutenticada);
            metricas = metricas.stream()
                    .filter(m -> m.getSucursalId() != null && m.getSucursalId().equals(sucursalAutenticada))
                    .collect(Collectors.toList());
        }

        if (metricas == null || metricas.isEmpty()) {
            metricas = new ArrayList<>();
            Optional<KpiDefinicion> defOpt = definicionRepository.findById(definicionId);
            if (defOpt.isPresent()) {
                KpiMetrica metricaVacia = new KpiMetrica();
                metricaVacia.setId(0L);
                metricaVacia.setDefinicion(defOpt.get());
                metricaVacia.setSucursalId(userRole != null && !"ADMIN".equalsIgnoreCase(userRole.trim()) ? sucursalAutenticada : 0L);
                metricaVacia.setValorActual(0.0);
                metricas.add(metricaVacia);
            }
        }
        return metricas;
    }

    public KpiMetrica guardarMetrica(KpiMetrica metrica) {
        return metricaRepository.save(metrica);
    }

    public void eliminarMetrica(Long id) {
        metricaRepository.deleteById(id);
    }

    // 🎯 LOGICA CORREGIDA: Asigna y calcula valores de forma segregada según el tipo de cálculo real
    public void acumularProgresoVenta(Long sucursalId, List<java.util.Map<String, Object>> itemsVendidos) {
        log.info("Procesando métricas para la sucursal ID: {}. Líneas totales en venta: {}", sucursalId, itemsVendidos.size());

        List<KpiDefinicion> definiciones = definicionRepository.findAll();

        if (definitionsOrItemsEmpty(definiciones, itemsVendidos)) {
            return;
        }

        for (KpiDefinicion definicion : definiciones) {
            double valorAIncrementar = 0.0;
            String tipoCalculo = definicion.getTipoCalculo() != null ? definicion.getTipoCalculo().trim().toUpperCase() : "";

            // 1. Condición para conteo de transacciones netas (+1 por cada ticket de venta procesado)
            if ("CONTAR_TRANSACCIONES".equalsIgnoreCase(tipoCalculo)) {
                valorAIncrementar = 1.0;
            }
            // 2. Condición para KPIs basados en volumen físico de productos (+N unidades vendidas)
            else if ("SUMAR_PRODUCTOS".equalsIgnoreCase(tipoCalculo)) {
                for (java.util.Map<String, Object> item : itemsVendidos) {
                    Number cantidadNum = (Number) item.get("cantidad");
                    if (cantidadNum != null) {
                        valorAIncrementar += cantidadNum.intValue();
                    }
                }
            }
            // 3. CORREGIDO: Condición para KPIs financieros basados en montos brutos de dinero ($)
            else if ("SUMAR_MONTO".equalsIgnoreCase(tipoCalculo) || "SUMAR_INGRESOS".equalsIgnoreCase(tipoCalculo)) {
                for (java.util.Map<String, Object> item : itemsVendidos) {
                    Number montoNum = (Number) item.get("montoTotal");
                    if (montoNum != null) {
                        valorAIncrementar += montoNum.doubleValue();
                    }
                }
            }

            // Si el KPI procesado no aplica para ninguna de las reglas anteriores de venta, se ignora de forma segura
            if (valorAIncrementar == 0.0) {
                continue;
            }

            Optional<KpiMetrica> metricaOpt = metricaRepository.findBySucursalIdAndDefinicionId(sucursalId, definicion.getId());

            if (metricaOpt.isPresent()) {
                KpiMetrica metricaExistente = metricaOpt.get();
                double valorActual = metricaExistente.getValorActual() != null ? metricaExistente.getValorActual() : 0.0;
                metricaExistente.setValorActual(valorActual + valorAIncrementar);
                metricaRepository.save(metricaExistente);
                log.info("📊 KPI '{}' actualizado para sucursal {}. Incremento: +{}", definicion.getNombre(), sucursalId, valorAIncrementar);
            } else {
                KpiMetrica nuevaMetrica = new KpiMetrica();
                nuevaMetrica.setSucursalId(sucursalId);
                nuevaMetrica.setDefinicion(definicion);
                nuevaMetrica.setValorActual(valorAIncrementar);
                metricaRepository.save(nuevaMetrica);
                log.info("⭐ Inicializada nueva métrica para KPI '{}' en sucursal {}. Valor inicial: {}", definicion.getNombre(), sucursalId, valorAIncrementar);
            }
        }
    }

    private boolean definitionsOrItemsEmpty(List<KpiDefinicion> definiciones, List<java.util.Map<String, Object>> itemsVendidos) {
        return definiciones.isEmpty() || itemsVendidos == null || itemsVendidos.isEmpty();
    }

    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activó debido a: {}", t.getMessage());
        return new ArrayList<>();
    }

    public List<KpiMetrica> metodoRespaldoSeguro(Long definicionId, String userRole, Long sucursalAutenticada, Throwable t) {
        log.error("El circuito seguro de métricas se activó debido a: {}", t.getMessage());
        return new ArrayList<>();
    }

    public List<KpiDefinicion> metodoRespaldoDefiniciones(String userRole, Long sucursalAutenticada, Throwable t) {
        log.error("El circuito seguro de definiciones se activó debido a: {}", t.getMessage());
        return new ArrayList<>();
    }
    public List<KpiDefinicion> listarTodasLasDefiniciones() {
        return definicionRepository.findAll();
    }
}