package co.com.sofka.testPublisher;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

@SpringBootTest
public class UppercaseConverterTest {

    final TestPublisher<String> testPublisher = TestPublisher.create();

    @Test
    void testUpperCase() {
        UppercaseConverter uppercaseConverter = new UppercaseConverter(testPublisher.flux());
        StepVerifier.create(uppercaseConverter.getUpperCase())
                .then(() -> testPublisher.emit("datos", "GeNeRaDoS", "Sofka"))
                .expectNext("DATOS", "GENERADOS", "SOFKA")
                .verifyComplete();
        TestPublisher
                .createNoncompliant(TestPublisher.Violation.ALLOW_NULL)
                .emit("1", "2", null, "3");
    }
}