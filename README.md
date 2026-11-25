# icc-est-u1-pl3

## Práctica 3 · Priorización y localización de incidentes

**Asignatura:** Estructura de Datos · Unidad 1  
**Modalidad:** individual · **Calificación:** 10 puntos

## 1. Solicitud del centro de operaciones

Una empresa mantiene la infraestructura de varias instituciones. Sus herramientas de monitoreo producen incidentes en distintos momentos y los envían en lotes. El orden de llegada de un lote no representa el orden de atención. El equipo necesita convertir una instantánea de 1000 incidentes en una agenda de intervención y localizar posteriormente un incidente dentro de esa misma agenda.

La jefatura establece que los incidentes de mayor gravedad deben atenderse antes. La escala corporativa identifica como 1 a los críticos, 2 a los altos, 3 a los medios y 4 a los bajos. Entre incidentes de igual gravedad, el equipo debe intervenir primero en aquel que tenga menos minutos disponibles para cumplir el acuerdo de servicio. Un valor negativo significa que el plazo venció: un incidente con −30 minutos está más atrasado que uno con −5 minutos. Los minutos pertenecen a la instantánea recibida; no se calculan con la hora actual.

Cuando la gravedad y el plazo no permiten decidir, la jefatura prefiere resolver el incidente que interrumpe más servicios. Si ambos interrumpen la misma cantidad, se atiende antes el que acumula más usuarios afectados entre sus servicios. Las cantidades corresponden a poblaciones independientes: deben sumarse, no deduplicarse ni promediarse. Los nombres de los servicios y su orden interno no influyen en esta decisión. Un incidente sin servicios tiene alcance cero y cero usuarios afectados.

Si persiste la igualdad en estas condiciones, se atiende primero el incidente registrado más temprano. Finalmente, el código permite desempatar en orden lexicográfico, sin distinguir mayúsculas de minúsculas. El código siempre es el último recurso de desempate. No se debe agregar un criterio adicional cuando estas condiciones coinciden.

Otra aplicación consulta la agenda enviando una copia de los datos relevantes de un incidente. Puede usar otra combinación de mayúsculas en el código, otros nombres para los servicios o una distribución diferente de usuarios entre ellos. Si la copia conserva la misma clave de atención completa, debe localizarse como equivalente. Si no existe una clave equivalente, la aplicación espera −1. No se solicita buscar únicamente por código.

## 2. Modelo recibido

`Incidente` ya implementa `Comparable<Incidente>`. Sus datos se mantienen inmutables para que una modificación no invalide el orden de una agenda ya preparada.

| Atributo | Tipo | Significado |
|---|---|---|
| `codigo` | `String` | Código alfanumérico que puede incluir guiones, sin espacios |
| `severidad` | `int` | Nivel corporativo de 1 a 4 |
| `minutosRestantes` | `int` | Minutos de la instantánea, incluidos negativos y extremos de `int` |
| `servicios` | `List<ServicioAfectado>` | Servicios interrumpidos; puede estar vacía |
| `registradoEn` | `LocalDateTime` | Momento del registro |

Cada `ServicioAfectado` contiene `nombre` y `usuariosAfectados`. La cantidad de usuarios es un entero no negativo. Su suma puede superar el máximo de `int`: seleccione un tipo que conserve el resultado.

Los constructores y las validaciones están implementados. Las listas internas no contienen `null` y no se pueden modificar. Los elementos de la agenda también son no nulos. No es necesario implementar nuevas validaciones de construcción.

`equals` conserva la identidad de objeto. La equivalencia de búsqueda se define **exclusivamente por `compareTo(...) == 0`**, que puede ser verdadera entre instancias distintas. Esta distinción es intencional en la práctica.

## 3. Trabajo solicitado

### C · Orden natural de Incidente

Complete `Incidente.compareTo(Incidente otro)`.

- Declare y componga un `Comparator<Incidente>` **dentro de `compareTo`** y utilícelo para producir el resultado.
- Interprete la política de atención descrita en la solicitud. Utilice los mecanismos de composición de `Comparator` y los accesores o cálculos necesarios.
- No cree una clase de comparadores ni un método externo que sustituya el trabajo solicitado.
- Devuelva un valor negativo, cero o positivo según el orden natural. No se exige exactamente −1 y 1.
- La comparación debe respetar reflexividad, inversión del signo, transitividad y equivalencia coherente frente a terceros.
- No modifique los objetos ni ordene sus listas internas.
- Comparar con `null` debe producir `NullPointerException`.

### S · Preparación de la agenda

Complete `IncidenteController.sort(List<Incidente> incidentes)`.

- Reciba una lista mutable, ordénela mediante **una llamada a `incidentes.sort(null)`** y devuelva la misma referencia.
- El parámetro `null` indica que `List.sort` debe utilizar el orden natural del objeto.
- Conserve todos los elementos, incluidas las repeticiones. Los equivalentes conservan su orden relativo.
- Las listas vacías y unitarias son válidas. Una lista nula produce `NullPointerException`.
- No reconstruya criterios en el controller, no copie la lista y no implemente otro algoritmo de ordenamiento.

### B · Consulta sobre la agenda

Complete `IncidenteController.busquedaBinaria(List<Incidente> incidentes, Incidente objetivo)`.

- La lista de entrada **ya está ordenada por su orden natural**. No tiene que ordenar ni comprobar que está ordenada dentro de este método.
- Implemente la búsqueda binaria y tome sus decisiones utilizando `compareTo`.
- Devuelva el índice de una entrada equivalente al objetivo, o exactamente `-1` si no existe.
- Si varias entradas son equivalentes, cualquier índice de ese grupo es válido.
- Una lista vacía devuelve `-1` para un objetivo válido.
- Una lista nula o un objetivo nulo producen `NullPointerException`, incluso si la lista está vacía.
- No modifique, copie ni recorra secuencialmente la lista. No use `indexOf`, `contains`, streams, `Collections.binarySearch` ni `Arrays.binarySearch`.
- Puede implementar una versión iterativa o recursiva.

La garantía de eficiencia se evalúa sobre listas con acceso aleatorio: debe hacer una cantidad logarítmica de consultas a posiciones y comparaciones. La cantidad de servicios también afecta al costo de cada comparación. Si `m` es el máximo de servicios por incidente, el trabajo de comparar alcances puede ser O(m); no afirme que comparar objetos siempre cuesta O(1). Sobre `LinkedList` no se exige tiempo O(log n).

### Conexión de los componentes

`App` carga los datos, prepara una consulta con una instancia distinta, llama a `sort`, muestra los primeros diez incidentes y realiza la búsqueda. Complete los tres métodos para que el flujo funcione. No agregue menú, lectura de archivos ni entrada por teclado.

## 4. Ejemplo de aceptación del cliente

Todos los registros de esta tabla tienen fecha 1 de octubre de 2026. La columna de usuarios muestra una cantidad por cada servicio.

| Código | Severidad | Minutos | Usuarios por servicio | Hora |
|---|---:|---:|---|---|
| I-B | 1 | 0 | 20 | 08:00 |
| I-G | 2 | −100 | 1000, 1000, 1000 | 06:00 |
| I-D | 1 | 0 | 30 | 08:00 |
| I-A | 1 | 0 | 20 | 08:00 |
| I-F | 1 | −10 | 10 | 09:00 |
| I-C | 1 | 0 | 20 | 07:00 |
| I-E | 1 | 0 | 10, 10 | 10:00 |

Agenda esperada: `I-F, I-E, I-D, I-C, I-A, I-B, I-G`.

Una nueva instancia equivalente a `I-A`, con código `i-a`, se encuentra en el índice **4**. Una consulta con los mismos datos pero código `I-X` devuelve **−1**. Los índices comienzan en cero.

Buscar solo `I-A` sin disponer de los demás datos de su clave no es una operación de esta API. El orden por atención no equivale a un orden por código.

## 5. Datos y estructura

`Datos.cargar()` crea una lista nueva con **1000 registros explícitos y desordenados**. No utiliza aleatoriedad en ejecución. El conjunto contiene empates que llegan al código, plazos vencidos, cero minutos y listas internas vacías. Cada llamada es independiente.

```text
src/main/java/App.java
src/main/java/models/Incidente.java              ← completar compareTo
src/main/java/models/ServicioAfectado.java
src/main/java/controllers/IncidenteController.java ← completar sort y busquedaBinaria
src/main/java/data/Datos.java
src/test/java/evaluacion/
src/test/resources/orden-1000.txt
tools/evaluar.py
tools/calificar.py
tools/rubrica.json
.github/workflows/calificacion.yml
```

Modifique únicamente los cuerpos de los tres métodos pendientes y las importaciones necesarias en sus dos archivos. Conserve firmas, paquetes, constructores y accesores. No reemplace los datos ni las pruebas. No agregue archivos de solución fuera de estos dos archivos.

## 6. Entorno y ejecución

- JDK 21.
- Gradle 8.14.3 mediante Wrapper incluido.
- JUnit Jupiter 5.12.2.
- Python 3.12 para el calificador, sin paquetes externos.
- Acceso a Internet la primera vez para descargar Gradle y las dependencias.

Desde la raíz del proyecto, en la terminal integrada de VS Code:

```bash
java -version
python3 --version
./gradlew classes
./gradlew run
./gradlew test
```

En Windows use `gradlew.bat` y `python` en lugar de `./gradlew` y `python3`. Si su descompresor no conserva el permiso de ejecución, ejecute `chmod +x gradlew` o use `sh gradlew`.

Pruebas por componente:

```bash
./gradlew test --tests 'evaluacion.ComparableTest'
./gradlew test --tests 'evaluacion.SortTest'
./gradlew test --tests 'evaluacion.BusquedaBinariaTest'
./gradlew test --tests 'evaluacion.IntegracionTest'
```

Una prueba concreta:

```bash
./gradlew test --tests 'evaluacion.ComparableTest.C03'
```

**Evaluación completa con nota:**

```bash
python3 tools/evaluar.py
```

Este comando descarta reportes anteriores, compila, ejecuta todas las pruebas y genera la calificación incluso cuando existen casos fallidos. Sale con código 0 si todas las verificaciones aprueban, 1 si hay incumplimientos y 2 si no fue posible completar la ejecución. No interprete un fallo del entorno como una nota definitiva.

Reportes:

- `build/evaluacion/reporte-calificacion.md`: calificación y estado de cada caso.
- `build/evaluacion/ejecucion.log`: salida del proceso de construcción y pruebas.
- `build/reports/tests/test/index.html`: detalle de JUnit.
- `build/test-results/test/`: resultados XML.

También puede reprocesar XML existentes mediante `python3 tools/calificar.py`. Para una nota actual y completa use siempre `tools/evaluar.py`: ejecutar solo un componente no acredita los demás.

La plantilla inicial compila y `App` informa que hay métodos pendientes. Sus casos evaluables deben fallar hasta que se implementen; su nota inicial es **0/10**.

## 7. Evaluación

| Componente | Identificadores | Casos | Puntaje máximo |
|---|---|---:|---:|
| Orden natural | C01–C18 | 18 | 4 |
| Ordenamiento delegado | S01–S08 | 8 | 2 |
| Búsqueda binaria | B01–B14 | 14 | 4 |
| Integración y datos | I01–I02 | 2 | Sin puntaje adicional |
| **Total evaluable** | | **40** | **10** |

Los pesos reflejan la complejidad del trabajo: `sort` es una delegación breve. Para cada componente:

`puntaje = aprobadas / cantidad original de casos × puntaje máximo`

La nota suma los valores sin redondear y redondea solo el total a dos decimales. Casos omitidos, ausentes, duplicados, fallidos o con error no suman puntos. Eliminar una prueba no reduce el denominador.

Las pruebas del controller utilizan objetos de prueba con un orden artificial para que puedan aprobar aunque el `compareTo` de negocio todavía esté pendiente. I01 verifica el flujo real completo y debe aprobar para considerar terminada la práctica. I02 comprueba los datos proporcionados y no asigna puntos por código ya resuelto.

Las pruebas comprueban resultados, propiedades del orden, estabilidad y presupuestos de acceso. No contienen una solución de referencia del comparador ni de la búsqueda. El docente revisará además que el `Comparator` esté compuesto dentro de `compareTo` y que la búsqueda implemente el algoritmo solicitado. Los resultados funcionales por sí solos no demuestran todas las restricciones de código.

## 8. GitHub Actions y entrega

Al realizar `push`, el workflow ejecuta el mismo `tools/evaluar.py`. También puede iniciarlo manualmente con `workflow_dispatch`.

Consulte **Repositorio → Actions → Calificación PL3 → ejecución → Summary**. Los XML, HTML y el reporte se adjuntan como artefactos. Una ejecución con casos fallidos conserva y publica la nota parcial.

1. Abra el proyecto y configure JDK 21.
2. Implemente los tres métodos.
3. Ejecute la evaluación completa.
4. Revise los resultados y realice commits descriptivos.
5. Realice `push`, revise Actions y entregue el enlace del repositorio.

```bash
git add src/main/java/models/Incidente.java src/main/java/controllers/IncidenteController.java
git commit -m "feat: implementar priorización y búsqueda de incidentes"
git push
```

No modifique pruebas, scripts, rúbrica, configuración o workflow de evaluación. La nota de su repositorio es retroalimentación; la evaluación oficial utiliza la copia original controlada por el docente.
