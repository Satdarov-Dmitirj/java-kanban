package exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagerSaveExceptionTest {
    @Test
    void shouldCreateWithMessageAndCause() {
        Exception cause = new Exception("Test cause");
        ManagerSaveException exception = new ManagerSaveException("Test", cause);

        assertEquals("Test", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}