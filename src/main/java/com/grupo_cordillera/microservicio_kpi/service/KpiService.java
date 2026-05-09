package com.grupo_cordillera.microservicio_kpi.service;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.model.Sucursal;
import com.grupo_cordillera.microservicio_kpi.repository.KpiDefinicionRepository;
import com.grupo_cordillera.microservicio_kpi.repository.KpiMetricaRepository;
import com.grupo_cordillera.microservicio_kpi.repository.SucursalRepository;
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
    private final SucursalRepository sucursalRepository;

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

    public List<KpiMetrica> metodoRespaldo(Long definicionId, Throwable t) {
        log.error("El circuito se activo debido a: {}", t.getMessage());
        return new ArrayList<>();
    }

    // ─── Sucursal ────────────────────────────────────────────────────

    public List<Sucursal> listarSucursales() {
        return sucursalRepository.findAll();
    }

    public Optional<Sucursal> obtenerSucursalPorId(Long id) {
        return sucursalRepository.findById(id);
    }

    public Sucursal guardarSucursal(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    public Optional<Sucursal> actualizarSucursal(Long id, Sucursal datos) {
        return sucursalRepository.findById(id).map(suc -> {
            suc.setNombre(datos.getNombre());
            suc.setCiudad(datos.getCiudad());
            return sucursalRepository.save(suc);
        });
    }

    public void eliminarSucursal(Long id) {
        sucursalRepository.deleteById(id);
    }
}
