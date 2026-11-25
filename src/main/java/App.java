import controllers.IncidenteController;
import data.Datos;
import java.util.List;
import models.Incidente;

public class App {

    public static void main(String[] args) {
        // Cargar los incidentes desordenados.
        List<Incidente> incidentes = Datos.cargar();
        System.out.println("Registros cargados: " + incidentes.size());

        // Mostrar los primeros cinco registros.
        System.out.println("Primeros cinco registros recibidos:");
        incidentes.stream().limit(5).forEach(System.out::println);

        // Crear otra instancia con los mismos datos para buscarla.
        Incidente original = incidentes.get(437);
        Incidente consulta = new Incidente(original.getCodigo(), original.getSeveridad(),
                original.getMinutosRestantes(), original.getServicios(), original.getRegistradoEn());

        try {
            IncidenteController controller = new IncidenteController();

            // Ordenar según el compareTo de Incidente.
            List<Incidente> ordenados = controller.sort(incidentes);

            // Mostrar los primeros diez incidentes priorizados.
            System.out.println("Primeros diez registros priorizados:");
            ordenados.stream().limit(10).forEach(System.out::println);

            // Buscar la consulta en la lista ordenada.
            int posicion = controller.busquedaBinaria(ordenados, consulta);
            System.out.println("Consulta " + consulta.getCodigo() + ": posición " + posicion);

            // Mostrar el incidente si fue encontrado.
            if (posicion >= 0)
                System.out.println(ordenados.get(posicion));

        } catch (UnsupportedOperationException e) {
            // Informar si algún método todavía está pendiente.
            System.out.println("Implementación pendiente: " + e.getMessage());
        }
    }
}
