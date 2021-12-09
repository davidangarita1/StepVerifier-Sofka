# Taller StepVerifier Y TestPublisher


## TestPublishe

En ocasiones y bajo ciertos casos de prueba es posible que necesitemos algunos datos especiales para activar ciertos comportamientos concretos que queramos probar de un publicador especifico. TestPublisher<T>, nos permitirá activar señales de prueba como si de un publicador real se tratara, los métodos mas comunes son:

* next(T value) o next(T value, T rest) - envía una o más señales a los suscriptores
* emit(T Value) - igual que next (T) pero invoca complete() al terminar
* complete() - termina la fuente con la señal completar
* error(Throwable tr) - termina una fuente con un error
* flux() - método para envolver un TestPublisher en Flux
* mono() - lo mismo que flux() pero envolviéndolo en Mono


### Usando TestPublisher
Podemos crear un TestPublisher simple que emita un par de datos y termine con una excepción.

```java
TestPublisher
  .<String>create()
  .next("Primero", "Segundo", "Tercero")
  .error(new RuntimeException("Message"));
```

El ejemplo anterior es muy trivial, y no podemos evidenciar los beneficios de usar un TestPublisher sobre un publicador particular. Como mencionamos anteriormente, a veces es posible que deseemos activar una señal concreta que coincida estrechamente con una situación particular en nuestro caso de prueba. Evidenciemos esto en el siguiente ejemplo:

Crearemos una clase que use Flux<String> como parámetro constructor el cual mediante el método getUpperCase() convertira este flujo de Strings a mayúsculas. 

```java
class UppercaseConverter {
    private final Flux<String> source;
    UppercaseConverter(Flux<String> source) {
        this.source = source;
    }
    Flux<String> getUpperCase() {
        return source
          .map(String::toUpperCase);
    }   
}
```

Supongamos que esta clase es muy compleja, y necesitamos proporcionar datos muy particulares para un caso de prueba. Podemos usar TestPublisher para generar estos datos sin la complejidad del publicador original. 

```java
final TestPublisher<String> testPublisher = TestPublisher.create();
    @Test
    void testUpperCase() {
        UppercaseConverter uppercaseConverter = new UppercaseConverter(testPublisher.flux());
        StepVerifier.create(uppercaseConverter.getUpperCase())
                .then(() -> testPublisher.emit("datos", "GeNeRaDoS", "Sofka"))
                .expectNext("DATOS", "GENERADOS", "SOFKA")
                .verifyComplete();
    }
```

En este ejemplo, creamos un TestPublisher de Flux de prueba en el parámetro del constructor UppercaseConverter. Luego, nuestro TestPublisher emite tres elementos y completa de esta manera la prueba de la clase sin hacer uso del publicador original de los datos.

En el caso anterior creamos un TestPublisher que emite datos para poder probar un componente especifico, también podemos simular casos de comportamientos inesperados (misbehaving) de un posible publicador. En el siguiente ejemplo podemos simular un publicador que emite una serie de números, uno de los cuales va nulo, en circunstancias normales este comportamiento arrojaria un NullPointException que es precisamente lo que queremos probar. 

```java
TestPublisher
  .createNoncompliant(TestPublisher.Violation.ALLOW_NULL)
  .emit("1", "2", null, "3");
```

De esta manera nuestro TestPublisher puede enviar datos concretos para probar errores entre la comunicación publicador-suscriptor. Además de ALLOW_NULL podemos configurar algunos otros comportamientos típicos que ocasionarían errores. 
* REQUEST_OVERFLOW – permite llamar a next() sin lanzar una IllegalStateException cuando hay un número insuficiente de solicitudes.
* CLEANUP_ON_TERMINATE – permite enviar varias señales de terminación consecutivamente.
* DEFER_CANCELLATION – nos permite ignorar las señales de cancelación y continuar con la emisión de elementos


Fuente: https://www.baeldung.com/reactive-streams-step-verifier-test-publisher