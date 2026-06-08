package com.grupo_cordillera.microservicio_kpi.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table(name = "kpi_metricas")
@Data
public class KpiMetrica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "definicion_id")
    private KpiDefinicion definicion;

    @Column(name = "sucursal_id")
    private Long sucursalId;

    @Column(name = "valor_actual")
    private Double valorActual;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}