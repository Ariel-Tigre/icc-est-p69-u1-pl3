package evaluacion;

import java.time.LocalDateTime;
import java.util.*;
import models.*;
import static org.junit.jupiter.api.Assertions.*;

final class Fixtures {
    static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 1, 8, 0);

    static Incidente i(String code, int s, int m, int... users) {
        List<ServicioAfectado> servicios = new ArrayList<>();
        for (int j = 0; j < users.length; j++)
            servicios.add(new ServicioAfectado("S" + j, users[j]));
        return new Incidente(code, s, m, servicios, FECHA);
    }

    static Incidente fecha(Incidente i, LocalDateTime f) {
        return new Incidente(i.getCodigo(), i.getSeveridad(), i.getMinutosRestantes(), i.getServicios(), f);
    }

    static void antes(Incidente a, Incidente b) {
        assertTrue(a.compareTo(b) < 0, "El primer incidente debe atenderse antes");
        assertTrue(b.compareTo(a) > 0, "La comparación inversa debe cambiar de signo");
    }

    // Doble de prueba: contrato artificial para aislar el controller del compareTo
    // pendiente.
    // Este rango NO representa los criterios de negocio del README.
    static class PorRango extends Incidente {
        final int rango;

        PorRango(int r) {
            super("X-" + Math.abs((long) r), 2, 0, List.of(), FECHA);
            rango = r;
        }

        @Override
        public int compareTo(Incidente o) {
            return Integer.compare(rango, ((PorRango) o).rango);
        }
    }

    static List<Incidente> rangos(int... r) {
        List<Incidente> result = new ArrayList<>();
        for (int n : r)
            result.add(new PorRango(n));
        return result;
    }

    static List<Integer> valores(List<Incidente> lista) {
        return lista.stream().map(x -> ((PorRango) x).rango).toList();
    }

    static List<String> ordenEsperado() throws Exception {
        try (var in = Fixtures.class.getResourceAsStream("/orden-1000.txt")) {
            assertNotNull(in);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).lines().toList();
        }
    }

    static List<Incidente> datosEnOrden() throws Exception {
        Map<String, Incidente> porCodigo = new HashMap<>();
        for (Incidente i : data.Datos.cargar())
            porCodigo.put(i.getCodigo(), i);
        List<Incidente> resultado = new ArrayList<>();
        for (String codigo : ordenEsperado())
            resultado.add(porCodigo.get(codigo));
        return resultado; // Colocación por posiciones predefinidas; no ordena ni compara.
    }

    static class ListaVigilada extends AbstractList<Incidente> implements RandomAccess {
        final List<Incidente> datos;
        int accesos;

        ListaVigilada(List<Incidente> datos) {
            this.datos = datos;
        }

        @Override
        public Incidente get(int index) {
            assertTrue(++accesos <= 64, "Se excedió el presupuesto de accesos; revise la reducción del intervalo");
            return datos.get(index);
        }

        @Override
        public int size() {
            return datos.size();
        }

        @Override
        public Iterator<Incidente> iterator() {
            throw new AssertionError("La búsqueda no debe recorrer la lista");
        }

        @Override
        public Object[] toArray() {
            throw new AssertionError("La búsqueda no debe copiar la lista");
        }
    }
}
