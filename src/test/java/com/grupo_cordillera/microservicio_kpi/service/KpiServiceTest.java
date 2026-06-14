package com.grupo_cordillera.microservicio_kpi.service;

import com.grupo_cordillera.microservicio_kpi.model.KpiDefinicion;
import com.grupo_cordillera.microservicio_kpi.model.KpiMetrica;
import com.grupo_cordillera.microservicio_kpi.repository.KpiDefinicionRepository;
import com.grupo_cordillera.microservicio_kpi.repository.KpiMetricaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de KpiService")
class KpiServiceTest {

    @Mock
    private KpiMetricaRepository metricaRepository;

    @Mock
    private KpiDefinicionRepository definicionRepository;

    @InjectMocks
    private KpiService kpiService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private KpiDefinicion crearDefinicion(Long id, String nombre, String tipoCalculo) {
        KpiDefinicion def = new KpiDefinicion();
        def.setId(id);
        def.setNombre(nombre);
        def.setDescripcion("Descripción de " + nombre);
        def.setValorObjetivo(100.0);
        def.setUnidad("unidad");
        def.setTipoCalculo(tipoCalculo);
        return def;
    }

    private KpiMetrica crearMetrica(Long id, Long sucursalId, Double valorActual, KpiDefinicion def) {
        KpiMetrica m = new KpiMetrica();
        m.setId(id);
        m.setSucursalId(sucursalId);
        m.setValorActual(valorActual);
        m.setDefinicion(def);
        return m;
    }

    // =========================================================================
    // KpiDefinicion – CRUD
    // =========================================================================

    @Nested
    @DisplayName("listarDefiniciones()")
    class ListarDefiniciones {

        @Test
        @DisplayName("Retorna todas las definiciones cuando el repositorio tiene datos")
        void retornaTodasLasDefiniciones() {
            List<KpiDefinicion> lista = List.of(
                    crearDefinicion(1L, "Ventas", "SUMAR_MONTO"),
                    crearDefinicion(2L, "Transacciones", "CONTAR_TRANSACCIONES")
            );
            when(definicionRepository.findAll()).thenReturn(lista);

            List<KpiDefinicion> resultado = kpiService.listarDefiniciones();

            assertThat(resultado).hasSize(2);
            verify(definicionRepository).findAll();
        }

        @Test
        @DisplayName("Retorna lista vacía cuando no hay definiciones")
        void retornaListaVacia() {
            when(definicionRepository.findAll()).thenReturn(Collections.emptyList());

            List<KpiDefinicion> resultado = kpiService.listarDefiniciones();

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarDefinicionesSeguras()")
    class ListarDefinicionesSeguras {

        @Test
        @DisplayName("ADMIN recibe todas las definiciones sin filtrar")
        void adminRecibeTodasLasDefiniciones() {
            List<KpiDefinicion> todas = List.of(
                    crearDefinicion(1L, "KPI-A", "SUMAR_MONTO"),
                    crearDefinicion(2L, "KPI-B", "CONTAR_TRANSACCIONES")
            );
            when(definicionRepository.findAll()).thenReturn(todas);

            List<KpiDefinicion> resultado = kpiService.listarDefinicionesSeguras("ADMIN", 99L);

            assertThat(resultado).hasSize(2);
            verify(metricaRepository, never()).findAll();
        }

        @Test
        @DisplayName("Rol no-ADMIN solo recibe definiciones de su sucursal")
        void noAdminRecibeDefinicionesFiltradas() {
            KpiDefinicion def1 = crearDefinicion(1L, "KPI-A", "SUMAR_MONTO");
            KpiDefinicion def2 = crearDefinicion(2L, "KPI-B", "CONTAR_TRANSACCIONES");

            KpiMetrica m1 = crearMetrica(10L, 5L, 50.0, def1); // sucursal 5
            KpiMetrica m2 = crearMetrica(11L, 9L, 30.0, def2); // sucursal 9

            when(metricaRepository.findAll()).thenReturn(List.of(m1, m2));

            List<KpiDefinicion> resultado = kpiService.listarDefinicionesSeguras("VENDEDOR", 5L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("KPI-A");
        }

        @Test
        @DisplayName("No-ADMIN sin métricas asociadas recibe lista vacía")
        void noAdminSinMetricasRecibeListaVacia() {
            when(metricaRepository.findAll()).thenReturn(Collections.emptyList());

            List<KpiDefinicion> resultado = kpiService.listarDefinicionesSeguras("VENDEDOR", 5L);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Rol ADMIN en mayúsculas y minúsculas es reconocido correctamente")
        void adminCaseInsensitive() {
            when(definicionRepository.findAll()).thenReturn(List.of(crearDefinicion(1L, "K", "SUMAR_MONTO")));

            // "admin" en minúsculas también debe tratar como ADMIN
            List<KpiDefinicion> resultado = kpiService.listarDefinicionesSeguras("admin", 1L);

            assertThat(resultado).hasSize(1);
            verify(metricaRepository, never()).findAll();
        }
    }

    @Nested
    @DisplayName("obtenerDefinicionPorId()")
    class ObtenerDefinicionPorId {

        @Test
        @DisplayName("Retorna Optional con la definición cuando existe")
        void retornaDefinicionExistente() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            when(definicionRepository.findById(1L)).thenReturn(Optional.of(def));

            Optional<KpiDefinicion> resultado = kpiService.obtenerDefinicionPorId(1L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Ventas");
        }

        @Test
        @DisplayName("Retorna Optional vacío cuando no existe")
        void retornaEmptyCuandoNoExiste() {
            when(definicionRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<KpiDefinicion> resultado = kpiService.obtenerDefinicionPorId(99L);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("guardarDefinicion()")
    class GuardarDefinicion {

        @Test
        @DisplayName("Persiste y retorna la definición guardada")
        void persisteDefinicion() {
            KpiDefinicion def = crearDefinicion(null, "Nueva", "SUMAR_MONTO");
            KpiDefinicion defGuardada = crearDefinicion(1L, "Nueva", "SUMAR_MONTO");
            when(definicionRepository.save(def)).thenReturn(defGuardada);

            KpiDefinicion resultado = kpiService.guardarDefinicion(def);

            assertThat(resultado.getId()).isEqualTo(1L);
            verify(definicionRepository).save(def);
        }
    }

    @Nested
    @DisplayName("actualizarDefinicion()")
    class ActualizarDefinicion {

        @Test
        @DisplayName("Actualiza campos cuando la definición existe")
        void actualizaCamposExistentes() {
            KpiDefinicion defExistente = crearDefinicion(1L, "Viejo", "SUMAR_MONTO");
            KpiDefinicion datosNuevos = crearDefinicion(null, "Nuevo", "CONTAR_TRANSACCIONES");
            datosNuevos.setDescripcion("Desc nueva");
            datosNuevos.setValorObjetivo(200.0);
            datosNuevos.setUnidad("$");

            when(definicionRepository.findById(1L)).thenReturn(Optional.of(defExistente));
            when(definicionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<KpiDefinicion> resultado = kpiService.actualizarDefinicion(1L, datosNuevos);

            assertThat(resultado).isPresent();
            KpiDefinicion actualizada = resultado.get();
            assertThat(actualizada.getNombre()).isEqualTo("Nuevo");
            assertThat(actualizada.getTipoCalculo()).isEqualTo("CONTAR_TRANSACCIONES");
            assertThat(actualizada.getValorObjetivo()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("Retorna Optional vacío cuando la definición no existe")
        void retornaEmptySiNoExiste() {
            when(definicionRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<KpiDefinicion> resultado = kpiService.actualizarDefinicion(99L, new KpiDefinicion());

            assertThat(resultado).isEmpty();
            verify(definicionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("eliminarDefinicion()")
    class EliminarDefinicion {

        @Test
        @DisplayName("Delega la eliminación al repositorio")
        void delegaEliminarAlRepositorio() {
            kpiService.eliminarDefinicion(1L);

            verify(definicionRepository).deleteById(1L);
        }
    }

    // =========================================================================
    // KpiMetrica
    // =========================================================================

    @Nested
    @DisplayName("obtenerMetricasPorDefinicion()")
    class ObtenerMetricasPorDefinicion {

        @Test
        @DisplayName("Retorna las métricas del repositorio para la definición dada")
        void retornaMetricas() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            List<KpiMetrica> metricas = List.of(crearMetrica(1L, 5L, 100.0, def));
            when(metricaRepository.findByDefinicionId(1L)).thenReturn(metricas);

            List<KpiMetrica> resultado = kpiService.obtenerMetricasPorDefinicion(1L);

            assertThat(resultado).hasSize(1);
            verify(metricaRepository).findByDefinicionId(1L);
        }
    }

    @Nested
    @DisplayName("obtenerMetricasPorDefinicionSegura()")
    class ObtenerMetricasPorDefinicionSegura {

        @Test
        @DisplayName("ADMIN recibe todas las métricas de la definición")
        void adminRecibeTodasLasMetricas() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            List<KpiMetrica> metricas = List.of(
                    crearMetrica(1L, 5L, 100.0, def),
                    crearMetrica(2L, 9L, 200.0, def)
            );
            when(metricaRepository.findByDefinicionId(1L)).thenReturn(metricas);

            List<KpiMetrica> resultado = kpiService.obtenerMetricasPorDefinicionSegura(1L, "ADMIN", 5L);

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("Rol no-ADMIN solo recibe métricas de su sucursal")
        void noAdminRecibeMetricasFiltradas() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            List<KpiMetrica> metricas = List.of(
                    crearMetrica(1L, 5L, 100.0, def),
                    crearMetrica(2L, 9L, 200.0, def)
            );
            when(metricaRepository.findByDefinicionId(1L)).thenReturn(metricas);

            List<KpiMetrica> resultado = kpiService.obtenerMetricasPorDefinicionSegura(1L, "VENDEDOR", 5L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getSucursalId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("No-ADMIN sin métricas recibe métrica vacía con valorActual=0")
        void noAdminSinMetricasRecibeMetricaVacia() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            when(metricaRepository.findByDefinicionId(1L)).thenReturn(Collections.emptyList());
            when(definicionRepository.findById(1L)).thenReturn(Optional.of(def));

            List<KpiMetrica> resultado = kpiService.obtenerMetricasPorDefinicionSegura(1L, "VENDEDOR", 5L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getValorActual()).isEqualTo(0.0);
            assertThat(resultado.get(0).getSucursalId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("ADMIN sin métricas recibe métrica vacía con sucursalId=0")
        void adminSinMetricasRecibeMetricaVaciaConSucursal0() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            when(metricaRepository.findByDefinicionId(1L)).thenReturn(Collections.emptyList());
            when(definicionRepository.findById(1L)).thenReturn(Optional.of(def));

            List<KpiMetrica> resultado = kpiService.obtenerMetricasPorDefinicionSegura(1L, "ADMIN", 5L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getSucursalId()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("guardarMetrica()")
    class GuardarMetrica {

        @Test
        @DisplayName("Persiste y retorna la métrica guardada")
        void persisteMetrica() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            KpiMetrica metrica = crearMetrica(null, 5L, 80.0, def);
            KpiMetrica metricaGuardada = crearMetrica(10L, 5L, 80.0, def);
            when(metricaRepository.save(metrica)).thenReturn(metricaGuardada);

            KpiMetrica resultado = kpiService.guardarMetrica(metrica);

            assertThat(resultado.getId()).isEqualTo(10L);
            verify(metricaRepository).save(metrica);
        }
    }

    @Nested
    @DisplayName("eliminarMetrica()")
    class EliminarMetrica {

        @Test
        @DisplayName("Delega la eliminación al repositorio de métricas")
        void delegaEliminar() {
            kpiService.eliminarMetrica(5L);

            verify(metricaRepository).deleteById(5L);
        }
    }

    // =========================================================================
    // acumularProgresoVenta()
    // =========================================================================

    @Nested
    @DisplayName("acumularProgresoVenta()")
    class AcumularProgresoVenta {

        @Test
        @DisplayName("No guarda métricas si la lista de items es null")
        void itemsNullNoProcessa() {
            // El servicio llama findAll() antes de verificar null, por eso lo stubbeamos
            when(definicionRepository.findAll()).thenReturn(Collections.emptyList());

            kpiService.acumularProgresoVenta(1L, null);

            verify(metricaRepository, never()).save(any());
        }

        @Test
        @DisplayName("No guarda métricas si la lista de items está vacía")
        void itemsVaciosNoProcessa() {
            // El servicio llama findAll() antes de verificar vacío, por eso lo stubbeamos
            when(definicionRepository.findAll()).thenReturn(Collections.emptyList());

            kpiService.acumularProgresoVenta(1L, Collections.emptyList());

            verify(metricaRepository, never()).save(any());
        }

        @Test
        @DisplayName("No procesa nada si no hay definiciones de KPI")
        void sinDefinicionesNoGuarda() {
            when(definicionRepository.findAll()).thenReturn(Collections.emptyList());

            kpiService.acumularProgresoVenta(1L, List.of(Map.of("cantidad", 3)));

            verify(metricaRepository, never()).findBySucursalIdAndDefinicionId(any(), any());
            verify(metricaRepository, never()).save(any());
        }

        @Test
        @DisplayName("CONTAR_TRANSACCIONES incrementa en 1 por venta")
        void contarTransaccionesIncrementa1() {
            KpiDefinicion def = crearDefinicion(1L, "Nro Transacciones", "CONTAR_TRANSACCIONES");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            KpiMetrica existente = crearMetrica(10L, 5L, 4.0, def);
            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.of(existente));
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            kpiService.acumularProgresoVenta(5L, List.of(Map.of("montoTotal", 1000)));

            verify(metricaRepository).save(argThat(m -> m.getValorActual() == 5.0));
        }

        @Test
        @DisplayName("SUMAR_PRODUCTOS acumula la cantidad vendida de todos los items")
        void sumarProductosAcumulaCantidades() {
            KpiDefinicion def = crearDefinicion(1L, "Unidades Vendidas", "SUMAR_PRODUCTOS");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            KpiMetrica existente = crearMetrica(10L, 5L, 10.0, def);
            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.of(existente));
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<Map<String, Object>> items = List.of(
                    Map.of("cantidad", 3),
                    Map.of("cantidad", 7)
            );
            kpiService.acumularProgresoVenta(5L, items);

            // 10 (existente) + 3 + 7 = 20
            verify(metricaRepository).save(argThat(m -> m.getValorActual() == 20.0));
        }

        @Test
        @DisplayName("SUMAR_MONTO acumula el montoTotal de todos los items")
        void sumarMontoAcumulaMontos() {
            KpiDefinicion def = crearDefinicion(1L, "Ingresos", "SUMAR_MONTO");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            KpiMetrica existente = crearMetrica(10L, 5L, 500.0, def);
            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.of(existente));
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<Map<String, Object>> items = List.of(
                    Map.of("montoTotal", 200.0),
                    Map.of("montoTotal", 300.0)
            );
            kpiService.acumularProgresoVenta(5L, items);

            // 500 + 200 + 300 = 1000
            verify(metricaRepository).save(argThat(m -> m.getValorActual() == 1000.0));
        }

        @Test
        @DisplayName("SUMAR_INGRESOS es tratado igual que SUMAR_MONTO")
        void sumarIngresosEquivalenteASumarMonto() {
            KpiDefinicion def = crearDefinicion(1L, "Ingresos Brutos", "SUMAR_INGRESOS");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.empty());
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<Map<String, Object>> items = List.of(Map.of("montoTotal", 400.0));
            kpiService.acumularProgresoVenta(5L, items);

            verify(metricaRepository).save(argThat(m -> m.getValorActual() == 400.0));
        }

        @Test
        @DisplayName("Crea una nueva métrica cuando no existe registro previo para la sucursal")
        void creaMetricaNuevaSiNoExiste() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            when(definicionRepository.findAll()).thenReturn(List.of(def));
            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.empty());
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            kpiService.acumularProgresoVenta(5L, List.of(Map.of("montoTotal", 150.0)));

            verify(metricaRepository).save(argThat(m ->
                    m.getSucursalId().equals(5L) && m.getValorActual() == 150.0));
        }

        @Test
        @DisplayName("Ignora definiciones con tipoCalculo desconocido (valorAIncrementar=0)")
        void ignoraDefinicionesConTipoDesconocido() {
            KpiDefinicion def = crearDefinicion(1L, "KPI Raro", "TIPO_DESCONOCIDO");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            kpiService.acumularProgresoVenta(5L, List.of(Map.of("montoTotal", 100.0)));

            verify(metricaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Items sin campo 'cantidad' son ignorados en SUMAR_PRODUCTOS")
        void itemsSinCantidadSonIgnorados() {
            KpiDefinicion def = crearDefinicion(1L, "Productos", "SUMAR_PRODUCTOS");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            // El item no trae "cantidad", por lo tanto valorAIncrementar queda en 0
            kpiService.acumularProgresoVenta(5L, List.of(Map.of("montoTotal", 100.0)));

            verify(metricaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Métrica existente con valorActual null es tratada como 0")
        void metricaConValorNullTratadaComo0() {
            KpiDefinicion def = crearDefinicion(1L, "Ventas", "SUMAR_MONTO");
            when(definicionRepository.findAll()).thenReturn(List.of(def));

            KpiMetrica existente = crearMetrica(10L, 5L, null, def); // valorActual = null
            when(metricaRepository.findBySucursalIdAndDefinicionId(5L, 1L))
                    .thenReturn(Optional.of(existente));
            when(metricaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            kpiService.acumularProgresoVenta(5L, List.of(Map.of("montoTotal", 250.0)));

            // 0 (null tratado como 0) + 250 = 250
            verify(metricaRepository).save(argThat(m -> m.getValorActual() == 250.0));
        }
    }

    // =========================================================================
    // Fallback methods
    // =========================================================================

    @Nested
    @DisplayName("Métodos de respaldo (fallback)")
    class MetodosRespaldo {

        @Test
        @DisplayName("metodoRespaldo retorna lista vacía")
        void metodoRespaldoRetornaListaVacia() {
            List<KpiMetrica> resultado = kpiService.metodoRespaldo(1L, new RuntimeException("error"));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("metodoRespaldoSeguro retorna lista vacía")
        void metodoRespaldoSeguroRetornaListaVacia() {
            List<KpiMetrica> resultado = kpiService.metodoRespaldoSeguro(1L, "VENDEDOR", 5L, new RuntimeException("error"));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("metodoRespaldoDefiniciones retorna lista vacía")
        void metodoRespaldoDefinicionesRetornaListaVacia() {
            List<KpiDefinicion> resultado = kpiService.metodoRespaldoDefiniciones("ADMIN", 5L, new RuntimeException("error"));

            assertThat(resultado).isEmpty();
        }
    }
}