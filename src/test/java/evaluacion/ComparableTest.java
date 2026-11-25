package evaluacion;

import java.util.*;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import static evaluacion.Fixtures.*;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ComparableTest {
    @Test
    void C01() {
        antes(i("Z", 1, 500), i("A", 2, -500, 1000));
    }

    @Test
    void C02() {
        antes(i("Z", 2, -5), i("A", 2, 5, 1000));
    }

    @Test
    void C03() {
        antes(i("Z", 2, 0, 0, 0), i("A", 2, 0, 1000));
    }

    @Test
    void C04() {
        antes(i("Z", 2, 0, 20, 90), i("A", 2, 0, 50, 50));
    }

    @Test
    void C05() {
        antes(fecha(i("Z", 2, 0, 10), FECHA.minusMinutes(1)), i("A", 2, 0, 10));
    }

    @Test
    void C06() {
        antes(i("a-01", 2, 0, 10), i("B-01", 2, 0, 10));
    }

    @Test
    void C07() {
        assertEquals(0, i("abc", 2, 0, 10, 20).compareTo(i("ABC", 2, 0, 5, 25)), "Misma clave de atención");
    }

    @Test
    void C08() {
        antes(i("Z", 2, Integer.MIN_VALUE), i("A", 2, Integer.MAX_VALUE));
    }

    @Test
    void C09() {
        antes(i("Z", 2, 0, Integer.MAX_VALUE, Integer.MAX_VALUE), i("A", 2, 0, 1, 1));
    }

    @Test
    void C10() {
        Incidente x = i("A", 1, 0);
        assertEquals(0, x.compareTo(x));
        assertEquals(0, x.compareTo(i("a", 1, 0)));
    }

    @Test
    void C11() {
        assertThrows(NullPointerException.class, () -> i("A", 1, 0).compareTo(null));
    }

    @Test
    void C12() {
        List<Incidente> lista = data.Datos.cargar();
        for (int j = 0; j < 80; j++) {
            Incidente a = lista.get(j), b = lista.get(999 - j);
            assertEquals(-Integer.signum(a.compareTo(b)), Integer.signum(b.compareTo(a)));
        }
    }

    @Test
    void C13() throws Exception {
        List<Incidente> l = datosEnOrden();
        for (int j = 0; j < 998; j += 7) {
            Incidente a = l.get(j), b = l.get(j + 1), c = l.get(j + 2);
            assertTrue(a.compareTo(b) < 0 && b.compareTo(c) < 0 && a.compareTo(c) < 0,
                    "Transitividad y orden esperado");
        }
    }

    @Test
    void C14() {
        Incidente a = i("A", 2, 0, 10, 20), b = i("a", 2, 0, 5, 25);
        for (Incidente c : List.of(i("C", 2, 0, 10, 19), i("D", 1, 0), i("E", 2, 0, 10, 21))) {
            assertEquals(0, a.compareTo(b));
            assertEquals(Integer.signum(a.compareTo(c)), Integer.signum(b.compareTo(c)));
        }
    }

    @Test
    void C15() {
        Incidente a = i("A", 2, 0, 40, 10), b = i("A", 2, 0, 10, 40);
        var antes = List.copyOf(a.getServicios());
        assertEquals(0, a.compareTo(b));
        assertEquals(antes, a.getServicios(), "No modificar servicios");
    }

    @Test
    void C16() {
        antes(i("Z", 2, 0, 0), i("A", 2, 0));
    }

    @Test
    void C17() throws Exception {
        List<Incidente> l = data.Datos.cargar();
        l.sort(null);
        assertEquals(ordenEsperado(), l.stream().map(Incidente::getCodigo).toList(), "Instantánea completa");
    }

    @Test
    void C18() {
        Incidente a = new Incidente("A", 2, 0, List.of(new ServicioAfectado("Z", 30)), FECHA);
        Incidente b = new Incidente("a", 2, 0, List.of(new ServicioAfectado("Otro", 30)), FECHA);
        assertEquals(0, a.compareTo(b), "El nombre del servicio no agrega un criterio");
    }
}
