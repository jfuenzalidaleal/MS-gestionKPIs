package com.grupo_cordillera.microservicio_kpi.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Table(name = "kpi_definiciones")
@Data
public class KpiDefinicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;

    @Column(name = "valor_objetivo")
    private Double valorObjetivo;

    private String unidad;
    @Column(name= "tipo_calculo")
    private String tipoCalculo;
    @Column(name = "categoria_id")
    private Long categoriaId;
}