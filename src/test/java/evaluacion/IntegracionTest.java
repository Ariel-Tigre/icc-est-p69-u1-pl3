package evaluacion;

import java.util.*;
import models.*;
import controllers.IncidenteController;
import data.Datos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import static evaluacion.Fixtures.*;

@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class IntegracionTest {
    // Sin puntos adicionales: verifica la conexión de los tres componentes.
    @Test
    void I01() throws Exception {
        var original = Datos.cargar();
        var controller = new IncidenteController();
        var lista = controller.sort(original);
        assertSame(original, lista);
        assertEquals(ordenEsperado(), lista.stream().map(Incidente::getCodigo).toList());
        for (int j = 0; j < lista.size(); j++) {
            Incidente a = lista.get(j);
            Incidente clave = new Incidente(a.getCodigo().toLowerCase(Locale.ROOT), a.getSeveridad(),
                    a.getMinutosRestantes(), a.getServicios(), a.getRegistradoEn());
            assertEquals(j, controller.busquedaBinaria(lista, clave),
                    "Consulta de objeto distinto con clave equivalente");
        }
        Incidente a = lista.get(333);
        Incidente ausente = new Incidente("ZZZ-NO-EXISTE", a.getSeveridad(), a.getMinutosRestantes(), a.getServicios(),
                a.getRegistradoEn());
        assertEquals(-1, controller.busquedaBinaria(lista, ausente));
    }

    @Test
    void I02() throws Exception {
        var a = Datos.cargar();
        var b = Datos.cargar();
        assertEquals(1000, a.size());
        assertNotSame(a, b);
        assertEquals(1000, a.stream().map(Incidente::getCodigo).distinct().count());
        assertEquals(a.stream().map(Incidente::getCodigo).toList(), b.stream().map(Incidente::getCodigo).toList());
        assertNotEquals(ordenEsperado(), a.stream().map(Incidente::getCodigo).toList());
        a.clear();
        assertEquals(1000, b.size());
    }
}
