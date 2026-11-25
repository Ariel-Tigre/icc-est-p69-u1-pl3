package evaluacion;

import java.util.*;
import models.Incidente;
import controllers.IncidenteController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import static evaluacion.Fixtures.*;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BusquedaBinariaTest {
    private final IncidenteController c = new IncidenteController();

    @Test
    void B01() {
        assertEquals(-1, c.busquedaBinaria(rangos(), new PorRango(1)));
    }

    @Test
    void B02() {
        assertEquals(0, c.busquedaBinaria(rangos(7), new PorRango(7)));
    }

    @Test
    void B03() {
        assertEquals(-1, c.busquedaBinaria(rangos(7), new PorRango(9)));
    }

    @Test
    void B04() {
        assertEquals(0, c.busquedaBinaria(rangos(1, 3, 5, 7, 9), new PorRango(1)));
    }

    @Test
    void B05() {
        assertEquals(4, c.busquedaBinaria(rangos(1, 3, 5, 7, 9), new PorRango(9)));
    }

    @Test
    void B06() {
        assertEquals(2, c.busquedaBinaria(rangos(1, 3, 5, 7, 9), new PorRango(5)));
    }

    @Test
    void B07() {
        for (int n : new int[] { 0, 2, 6, 10 })
            assertEquals(-1, c.busquedaBinaria(rangos(1, 3, 5, 7, 9), new PorRango(n)));
    }

    @Test
    void B08() {
        var l = rangos(1, 2, 2, 2, 4);
        int k = c.busquedaBinaria(l, new PorRango(2));
        assertTrue(k >= 1 && k <= 3, "Cualquier equivalente es válido");
    }

    @Test
    void B09() {
        assertThrows(NullPointerException.class, () -> c.busquedaBinaria(null, new PorRango(0)));
    }

    @Test
    void B10() {
        assertThrows(NullPointerException.class, () -> c.busquedaBinaria(rangos(), null));
    }

    @Test
    void B11() {
        var l = List.copyOf(rangos(0, 2, 4, 6));
        assertEquals(2, c.busquedaBinaria(l, new PorRango(4)));
        assertEquals(List.of(0, 2, 4, 6), valores(l));
    }

    @Test
    void B12() {
        int[] comparaciones = { 0 };
        class Contado extends PorRango {
            Contado(int r) {
                super(r);
            }

            @Override
            public int compareTo(Incidente o) {
                assertTrue(++comparaciones[0] <= 64, "Demasiadas comparaciones");
                return super.compareTo(o);
            }
        }
        List<Incidente> datos = new ArrayList<>();
        for (int k = 0; k < 4096; k++)
            datos.add(new Contado(2 * k));
        for (int buscado : new int[] { 0, 4096, 8190, 4095, -1, 8192 }) {
            comparaciones[0] = 0;
            ListaVigilada l = new ListaVigilada(datos);
            int posicion = c.busquedaBinaria(l, new Contado(buscado));
            int esperado = (buscado >= 0 && buscado < 8192 && buscado % 2 == 0) ? buscado / 2 : -1;
            assertEquals(esperado, posicion);
            assertTrue(l.accesos <= 28, "Máximo 2 accesos por iteración más margen");
            assertTrue(comparaciones[0] >= 1 && comparaciones[0] <= 28, "Debe usar compareTo con trabajo logarítmico");
        }
    }

    @Test
    void B13() {
        List<Incidente> l = new ArrayList<>();
        for (int k = 0; k < 1000; k++)
            l.add(new PorRango(k * 3));
        for (int k = 0; k < 1000; k++)
            assertEquals(k, c.busquedaBinaria(l, new PorRango(k * 3)));
    }

    @Test
    void B14() {
        for (int n : new int[] { 2, 3, 4, 8, 16, 31, 32, 33 }) {
            List<Incidente> l = new ArrayList<>();
            for (int k = 0; k < n; k++)
                l.add(new PorRango(2 * k));
            for (int k = 0; k < n; k++)
                assertEquals(k, c.busquedaBinaria(l, new PorRango(2 * k)));
            assertEquals(-1, c.busquedaBinaria(l, new PorRango(2 * n)));
        }
    }
}
