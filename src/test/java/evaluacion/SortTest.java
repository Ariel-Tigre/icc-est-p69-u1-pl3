package evaluacion;

import java.util.*;
import models.Incidente;
import controllers.IncidenteController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;
import static evaluacion.Fixtures.*;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SortTest {
    private final IncidenteController c = new IncidenteController();

    @Test
    void S01() {
        var l = rangos(7, 1, 9, 2, 0);
        assertSame(l, c.sort(l));
        assertEquals(List.of(0, 1, 2, 7, 9), valores(l));
    }

    @Test
    void S02() {
        var l = rangos();
        assertSame(l, c.sort(l));
        assertTrue(l.isEmpty());
    }

    @Test
    void S03() {
        var l = rangos(5);
        var x = l.get(0);
        assertSame(l, c.sort(l));
        assertSame(x, l.get(0));
    }

    @Test
    void S04() {
        var l = rangos(0, 1, 2, 3);
        c.sort(l);
        assertEquals(List.of(0, 1, 2, 3), valores(l));
    }

    @Test
    void S05() {
        var l = rangos(3, 2, 1, 0);
        c.sort(l);
        assertEquals(List.of(0, 1, 2, 3), valores(l));
    }

    @Test
    void S06() {
        Incidente a = new PorRango(2), b = new PorRango(2), z = new PorRango(0);
        List<Incidente> l = new ArrayList<>(List.of(a, z, b, a));
        c.sort(l);
        assertEquals(4, l.size());
        assertSame(z, l.get(0));
        assertSame(a, l.get(1));
        assertSame(b, l.get(2));
        assertSame(a, l.get(3));
    }

    @Test
    void S07() {
        assertThrows(NullPointerException.class, () -> c.sort(null));
    }

    @Test
    void S08() {
        class Lista extends ArrayList<Incidente> {
            int llamadas;

            @Override
            public void sort(Comparator<? super Incidente> comparator) {
                llamadas++;
                assertNull(comparator, "Debe delegar al orden natural con List.sort(null)");
                super.sort(comparator);
            }
        }
        Lista l = new Lista();
        l.addAll(rangos(4, 1, 3));
        c.sort(l);
        assertEquals(1, l.llamadas);
        assertEquals(List.of(1, 3, 4), valores(l));
    }
}
