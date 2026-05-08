package com.grupo_cordillera.microservicio_kpi.controller;

import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
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
}