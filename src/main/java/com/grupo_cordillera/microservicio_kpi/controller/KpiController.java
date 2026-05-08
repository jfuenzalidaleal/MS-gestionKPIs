package com.grupo_cordillera.microservicio_kpi.controller;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    // Endpoint para consultar los datos de un KPI
    @GetMapping("/metricas/{id}")
    public List<KpiMetrica> listarMetricas(@PathVariable Long id) {
        return kpiService.obtenerMetricasPorDefinicion(id);
    }
    @PostMapping("/crear")
    public KpiDefinicion crearKpi(@RequestBody KpiDefinicion kpi){
        return kpiService.guardarKpi(kpi);
    }
    @PutMapping("/acualizar/{id}")
    public ResponseEntity<KpiDefinicion> actualizarKpi(@PathVariable Long id, @RequestBody KpiDefinicion kpiDetalles){
       return kpiService.obtenerId(id)
               .map(kpi -> {
                   kpi.setNombre(kpiDetalles.getNombre());
                   kpi.setDescripcion(kpiDetalles.getDescripcion());
                   return ResponseEntity.ok(kpiService.guardarKpi(kpi));
               })
               .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarKpi(@PathVariable Long id){
        kpiService.eliminarKpi(id);
        return ResponseEntity.noContent().build();
    }
}