package com.grupo_cordillera.microservicio_kpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web para KpiController.
 *
 * Estrategia:
 *  - @WebMvcTest carga solo el slice web (sin BD ni contexto completo).
 *  - KpiService se mockea con @MockBean → tests rápidos y aislados.
 *  - SecurityConfig se importa para respetar las reglas reales de Spring Security.
 *  - Se usan @Nested para agrupar los tests por endpoint y facilitar la lectura.
 */
@WebMvcTest(KpiController.class)
@Import(com.grupo_cordillera.microservicio_kpi.config.SecurityConfig.class)
@DisplayName("KpiController – Tests de capa web")
class KpiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KpiService kpiService;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private KpiDefinicion definicion1;
    private KpiDefinicion definicion2;
    private KpiMetrica metrica1;

    @BeforeEach
    void setUp() {
        definicion1 = new KpiDefinicion();
        definicion1.setId(1L);
        definicion1.setNombre("Ventas Totales");
        definicion1.setDescripcion("Suma total del monto de ventas del período");
        definicion1.setValorObjetivo(5_000_000.0);
        definicion1.setUnidad("CLP");
        definicion1.setTipoCalculo("SUMAR_MONTO");
        definicion1.setCategoriaId(1L);

        definicion2 = new KpiDefinicion();
        definicion2.setId(2L);
        definicion2.setNombre("Cantidad de Transacciones");
        definicion2.setDescripcion("Número de tickets de venta procesados");
        definicion2.setValorObjetivo(300.0);
        definicion2.setUnidad("tickets");
        definicion2.setTipoCalculo("CONTAR_TRANSACCIONES");
        definicion2.setCategoriaId(1L);

        metrica1 = new KpiMetrica();
        metrica1.setId(10L);
        metrica1.setDefinicion(definicion1);
        metrica1.setSucursalId(1L);
        metrica1.setValorActual(1_250_000.0);
        metrica1.setFechaRegistro(LocalDateTime.of(2026, 6, 14, 10, 30));
    }

    // ─── GET /api/kpi/definiciones ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/kpi/definiciones")
    class ListarDefiniciones {

        @Test
        @DisplayName("Retorna 200 con lista de definiciones")
        void listarDefiniciones_retornaLista() throws Exception {
            when(kpiService.listarTodasLasDefiniciones())
                    .thenReturn(List.of(definicion1, definicion2));

            mockMvc.perform(get("/api/kpi/definiciones"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("Ventas Totales"))
                    .andExpect(jsonPath("$[0].tipoCalculo").value("SUMAR_MONTO"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].tipoCalculo").value("CONTAR_TRANSACCIONES"));

            verify(kpiService, times(1)).listarTodasLasDefiniciones();
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía cuando no hay definiciones")
        void listarDefiniciones_listaVacia() throws Exception {
            when(kpiService.listarTodasLasDefiniciones()).thenReturn(List.of());

            mockMvc.perform(get("/api/kpi/definiciones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ─── GET /api/kpi/definiciones/{id} ──────────────────────────────────────

    @Nested
    @DisplayName("GET /api/kpi/definiciones/{id}")
    class ObtenerDefinicion {

        @Test
        @DisplayName("Retorna 200 cuando la definición existe")
        void obtenerDefinicion_existente_retorna200() throws Exception {
            when(kpiService.obtenerDefinicionPorId(1L))
                    .thenReturn(Optional.of(definicion1));

            mockMvc.perform(get("/api/kpi/definiciones/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Ventas Totales"))
                    .andExpect(jsonPath("$.valorObjetivo").value(5_000_000.0))
                    .andExpect(jsonPath("$.unidad").value("CLP"));
        }

        @Test
        @DisplayName("Retorna 404 cuando la definición no existe")
        void obtenerDefinicion_inexistente_retorna404() throws Exception {
            when(kpiService.obtenerDefinicionPorId(99L))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/kpi/definiciones/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /api/kpi/definiciones ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/kpi/definiciones")
    class CrearDefinicion {

        @Test
        @DisplayName("Crea una definición y retorna 200 con el objeto creado")
        void crearDefinicion_retorna200ConDefinicion() throws Exception {
            KpiDefinicion nuevaDefinicion = new KpiDefinicion();
            nuevaDefinicion.setNombre("Ingresos por Categoría");
            nuevaDefinicion.setDescripcion("Total de ingresos por categoría");
            nuevaDefinicion.setValorObjetivo(2_000_000.0);
            nuevaDefinicion.setUnidad("CLP");
            nuevaDefinicion.setTipoCalculo("SUMAR_INGRESOS");
            nuevaDefinicion.setCategoriaId(3L);

            KpiDefinicion definicionGuardada = new KpiDefinicion();
            definicionGuardada.setId(4L);
            definicionGuardada.setNombre("Ingresos por Categoría");
            definicionGuardada.setDescripcion("Total de ingresos por categoría");
            definicionGuardada.setValorObjetivo(2_000_000.0);
            definicionGuardada.setUnidad("CLP");
            definicionGuardada.setTipoCalculo("SUMAR_INGRESOS");
            definicionGuardada.setCategoriaId(3L);

            when(kpiService.guardarDefinicion(any(KpiDefinicion.class)))
                    .thenReturn(definicionGuardada);

            mockMvc.perform(post("/api/kpi/definiciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nuevaDefinicion)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(4))
                    .andExpect(jsonPath("$.nombre").value("Ingresos por Categoría"))
                    .andExpect(jsonPath("$.tipoCalculo").value("SUMAR_INGRESOS"));

            verify(kpiService, times(1)).guardarDefinicion(any(KpiDefinicion.class));
        }
    }

    // ─── PUT /api/kpi/definiciones/{id} ──────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/kpi/definiciones/{id}")
    class ActualizarDefinicion {

        @Test
        @DisplayName("Actualiza una definición existente y retorna 200")
        void actualizarDefinicion_existente_retorna200() throws Exception {
            KpiDefinicion datosActualizados = new KpiDefinicion();
            datosActualizados.setNombre("Ventas Totales (Actualizado)");
            datosActualizados.setDescripcion("Suma mensual del monto de ventas");
            datosActualizados.setValorObjetivo(6_000_000.0);
            datosActualizados.setUnidad("CLP");
            datosActualizados.setTipoCalculo("SUMAR_MONTO");
            datosActualizados.setCategoriaId(1L);

            KpiDefinicion definicionActualizada = new KpiDefinicion();
            definicionActualizada.setId(1L);
            definicionActualizada.setNombre("Ventas Totales (Actualizado)");
            definicionActualizada.setDescripcion("Suma mensual del monto de ventas");
            definicionActualizada.setValorObjetivo(6_000_000.0);
            definicionActualizada.setUnidad("CLP");
            definicionActualizada.setTipoCalculo("SUMAR_MONTO");
            definicionActualizada.setCategoriaId(1L);

            when(kpiService.actualizarDefinicion(eq(1L), any(KpiDefinicion.class)))
                    .thenReturn(Optional.of(definicionActualizada));

            mockMvc.perform(put("/api/kpi/definiciones/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(datosActualizados)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Ventas Totales (Actualizado)"))
                    .andExpect(jsonPath("$.valorObjetivo").value(6_000_000.0));
        }

        @Test
        @DisplayName("Retorna 404 al actualizar una definición inexistente")
        void actualizarDefinicion_inexistente_retorna404() throws Exception {
            when(kpiService.actualizarDefinicion(eq(99L), any(KpiDefinicion.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(put("/api/kpi/definiciones/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(definicion1)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── DELETE /api/kpi/definiciones/{id} ───────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/kpi/definiciones/{id}")
    class EliminarDefinicion {

        @Test
        @DisplayName("Elimina una definición y retorna 204 sin contenido")
        void eliminarDefinicion_retorna204() throws Exception {
            doNothing().when(kpiService).eliminarDefinicion(1L);

            mockMvc.perform(delete("/api/kpi/definiciones/1"))
                    .andExpect(status().isNoContent());

            verify(kpiService, times(1)).eliminarDefinicion(1L);
        }
    }

    // ─── GET /api/kpi/metricas/{id} ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/kpi/metricas/{id}")
    class ListarMetricas {

        @Test
        @DisplayName("ADMIN: retorna 200 con métricas de todas las sucursales")
        void listarMetricas_rolAdmin_retornaTodasLasSucursales() throws Exception {
            KpiMetrica metrica2 = new KpiMetrica();
            metrica2.setId(11L);
            metrica2.setDefinicion(definicion1);
            metrica2.setSucursalId(2L);
            metrica2.setValorActual(870_000.0);
            metrica2.setFechaRegistro(LocalDateTime.of(2026, 6, 14, 11, 0));

            when(kpiService.obtenerMetricasPorDefinicionSegura(1L, "ADMIN", null))
                    .thenReturn(List.of(metrica1, metrica2));

            mockMvc.perform(get("/api/kpi/metricas/1")
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].sucursalId").value(1))
                    .andExpect(jsonPath("$[1].sucursalId").value(2));
        }

        @Test
        @DisplayName("USUARIO: retorna 200 con métrica solo de su sucursal")
        void listarMetricas_rolUsuario_retornaSoloSuSucursal() throws Exception {
            when(kpiService.obtenerMetricasPorDefinicionSegura(1L, "USUARIO", 1L))
                    .thenReturn(List.of(metrica1));

            mockMvc.perform(get("/api/kpi/metricas/1")
                            .header("X-User-Role", "USUARIO")
                            .header("X-Sucursal-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].sucursalId").value(1))
                    .andExpect(jsonPath("$[0].valorActual").value(1_250_000.0));
        }

        @Test
        @DisplayName("Sin datos: retorna 200 con métrica vacía (valorActual=0)")
        void listarMetricas_sinDatos_retornaMetricaVacia() throws Exception {
            KpiMetrica metricaVacia = new KpiMetrica();
            metricaVacia.setId(0L);
            metricaVacia.setDefinicion(definicion1);
            metricaVacia.setSucursalId(1L);
            metricaVacia.setValorActual(0.0);
            metricaVacia.setFechaRegistro(LocalDateTime.now());

            when(kpiService.obtenerMetricasPorDefinicionSegura(1L, "USUARIO", 1L))
                    .thenReturn(List.of(metricaVacia));

            mockMvc.perform(get("/api/kpi/metricas/1")
                            .header("X-User-Role", "USUARIO")
                            .header("X-Sucursal-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(0))
                    .andExpect(jsonPath("$[0].valorActual").value(0.0));
        }

        @Test
        @DisplayName("Sin headers: retorna 200 llamando al servicio con nulls")
        void listarMetricas_sinHeaders_llamaServicioConNulls() throws Exception {
            when(kpiService.obtenerMetricasPorDefinicionSegura(1L, null, null))
                    .thenReturn(List.of(metrica1));

            mockMvc.perform(get("/api/kpi/metricas/1"))
                    .andExpect(status().isOk());

            verify(kpiService).obtenerMetricasPorDefinicionSegura(1L, null, null);
        }
    }

    // ─── POST /api/kpi/metricas ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/kpi/metricas")
    class RegistrarMetrica {

        @Test
        @DisplayName("Registra una métrica y retorna 200 con el objeto guardado")
        void registrarMetrica_retorna200() throws Exception {
            KpiMetrica metricaInput = new KpiMetrica();
            metricaInput.setDefinicion(definicion1);
            metricaInput.setSucursalId(3L);
            metricaInput.setValorActual(450_000.0);

            KpiMetrica metricaGuardada = new KpiMetrica();
            metricaGuardada.setId(15L);
            metricaGuardada.setDefinicion(definicion1);
            metricaGuardada.setSucursalId(3L);
            metricaGuardada.setValorActual(450_000.0);
            metricaGuardada.setFechaRegistro(LocalDateTime.of(2026, 6, 14, 13, 45));

            when(kpiService.guardarMetrica(any(KpiMetrica.class)))
                    .thenReturn(metricaGuardada);

            mockMvc.perform(post("/api/kpi/metricas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(metricaInput)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(15))
                    .andExpect(jsonPath("$.sucursalId").value(3))
                    .andExpect(jsonPath("$.valorActual").value(450_000.0));

            verify(kpiService, times(1)).guardarMetrica(any(KpiMetrica.class));
        }
    }

    // ─── DELETE /api/kpi/metricas/{id} ───────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/kpi/metricas/{id}")
    class EliminarMetrica {

        @Test
        @DisplayName("Elimina una métrica y retorna 204 sin contenido")
        void eliminarMetrica_retorna204() throws Exception {
            doNothing().when(kpiService).eliminarMetrica(15L);

            mockMvc.perform(delete("/api/kpi/metricas/15"))
                    .andExpect(status().isNoContent());

            verify(kpiService, times(1)).eliminarMetrica(15L);
        }
    }

    // ─── PUT /api/kpi/acumular ───────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/kpi/acumular")
    class AcumularProgreso {

        @Test
        @DisplayName("Acumula progreso con venta mixta y retorna 200")
        void acumularProgreso_ventaMixta_retorna200() throws Exception {
            List<Map<String, Object>> productosVendidos = List.of(
                    Map.of("productoId", 101, "nombre", "Café Americano",
                            "cantidad", 3, "montoTotal", 9000.0),
                    Map.of("productoId", 205, "nombre", "Sándwich de Pollo",
                            "cantidad", 2, "montoTotal", 14000.0)
            );

            doNothing().when(kpiService).acumularProgresoVenta(eq(1L), anyList());

            mockMvc.perform(put("/api/kpi/acumular")
                            .param("sucursalId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productosVendidos)))
                    .andExpect(status().isOk());

            verify(kpiService, times(1))
                    .acumularProgresoVenta(eq(1L), anyList());
        }

        @Test
        @DisplayName("Acumula progreso con un solo ítem (CONTAR_TRANSACCIONES) y retorna 200")
        void acumularProgreso_unSoloItem_retorna200() throws Exception {
            List<Map<String, Object>> productoUnico = List.of(
                    Map.of("productoId", 300, "nombre", "Agua Mineral",
                            "cantidad", 1, "montoTotal", 1500.0)
            );

            doNothing().when(kpiService).acumularProgresoVenta(eq(2L), anyList());

            mockMvc.perform(put("/api/kpi/acumular")
                            .param("sucursalId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoUnico)))
                    .andExpect(status().isOk());

            verify(kpiService, times(1))
                    .acumularProgresoVenta(eq(2L), anyList());
        }

        @Test
        @DisplayName("Retorna 400 si falta el parámetro sucursalId")
        void acumularProgreso_sinSucursalId_retorna400() throws Exception {
            mockMvc.perform(put("/api/kpi/acumular")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isBadRequest());
        }
    }
}
