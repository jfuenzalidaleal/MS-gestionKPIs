package com.grupo_cordillera.microservicio_kpi.controller;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    // ─── KpiDefinicion ───────────────────────────────────────────────

    // 🛡️ CORREGIDO POR SEGURIDAD: Ahora intercepta el Rol y la Sucursal del JWT para aislar el Dashboard
    @GetMapping("/definiciones")
    public List<KpiDefinicion> listarDefiniciones(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalAutenticada) {

        return kpiService.listarDefinicionesSeguras(userRole, sucursalAutenticada);
    }

    @GetMapping("/definiciones/{id}")
    public ResponseEntity<KpiDefinicion> obtenerDefinicion(@PathVariable Long id) {
        return kpiService.obtenerDefinicionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/definiciones")
    public KpiDefinicion crearDefinicion(@RequestBody KpiDefinicion definicion) {
        return kpiService.guardarDefinicion(definicion);
    }

    @PutMapping("/definiciones/{id}")
    public ResponseEntity<KpiDefinicion> actualizarDefinicion(
            @PathVariable Long id,
            @RequestBody KpiDefinicion definicion) {
        return kpiService.actualizarDefinicion(id, definicion)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/definiciones/{id}")
    public ResponseEntity<Void> eliminarDefinicion(@PathVariable Long id) {
        kpiService.eliminarDefinicion(id);
        return ResponseEntity.noContent().build();
    }

    // ─── KpiMetrica ──────────────────────────────────────────────────

    @GetMapping("/metricas/{id}")
    public List<KpiMetrica> listarMetricas(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalAutenticada) {

        return kpiService.obtenerMetricasPorDefinicionSegura(id, userRole, sucursalAutenticada);
    }

    @PutMapping("/acumular")
    public ResponseEntity<Void> acumularProgreso(
            @RequestParam("sucursalId") Long sucursalId,
            @RequestBody List<java.util.Map<String, Object>> productosVendidos) {

        kpiService.acumularProgresoVenta(sucursalId, productosVendidos);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metricas")
    public KpiMetrica registrarMetrica(@RequestBody KpiMetrica metrica) {
        return kpiService.guardarMetrica(metrica);
    }

    @DeleteMapping("/metricas/{id}")
    public ResponseEntity<Void> eliminarMetrica(@PathVariable Long id) {
        kpiService.eliminarMetrica(id);
        return ResponseEntity.noContent().build();
    }
}