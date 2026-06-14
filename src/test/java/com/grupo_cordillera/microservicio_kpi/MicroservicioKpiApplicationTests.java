package com.grupo_cordillera.microservicio_kpi;

import com.grupo_cordillera.microservicio_kpi.service.KpiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest
class MicroservicioKpiApplicationTests {

	// WebMvcTest levanta solo la capa web (controllers), sin JPA ni BD.
	// KpiService es la única dependencia del controller, la mockeamos.
	@MockitoBean
	KpiService kpiService;

	@Test
	void contextLoads() {
		// Verifica que el contexto web arranca correctamente sin base de datos
	}
}