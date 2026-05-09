package com.grupo_cordillera.microservicio_kpi.controller;

import com.grupo_cordillera.microservicio_kpi.model.Sucursal;
import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final KpiService kpiService;

    @GetMapping
    public List<Sucursal> listarSucursales() {
        return kpiService.listarSucursales();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerSucursal(@PathVariable Long id) {
        return kpiService.obtenerSucursalPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sucursal crearSucursal(@RequestBody Sucursal sucursal) {
        return kpiService.guardarSucursal(sucursal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizarSucursal(
            @PathVariable Long id,
            @RequestBody Sucursal sucursal) {
        return kpiService.actualizarSucursal(id, sucursal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSucursal(@PathVariable Long id) {
        kpiService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }
}

