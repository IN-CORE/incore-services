package edu.illinois.ncsa.incore.common.exceptions;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientErrorTest
 */
public class IncoreHTTPExceptionTest {

    @Test
    public void handleForbiddenException() {
        IncoreHTTPException exception = assertThrows(IncoreHTTPException.class, () -> {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, "forbidden error");
        });

        String expectedMessage = "forbidden error";
        String actualMessage = exception.getResponse().getEntity().toString();

        assertTrue(actualMessage.contains(expectedMessage));

        int expectedStatusCode = 403;
        int actualStatusCode = exception.getResponse().getStatus();

        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    public void handleNotFoundException() {
        IncoreHTTPException exception = assertThrows(IncoreHTTPException.class, () -> {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "not found error");
        });

        String expectedMessage = "not found error";
        String actualMessage = exception.getResponse().getEntity().toString();

        assertTrue(actualMessage.contains(expectedMessage));

        int expectedStatusCode = 404;
        int actualStatusCode = exception.getResponse().getStatus();

        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    public void handleBadRequestException() {
        IncoreHTTPException exception = assertThrows(IncoreHTTPException.class, () -> {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "bad request error");
        });

        String expectedMessage = "bad request error";
        String actualMessage = exception.getResponse().getEntity().toString();

        assertTrue(actualMessage.contains(expectedMessage));

        int expectedStatusCode = 400;
        int actualStatusCode = exception.getResponse().getStatus();

        assertEquals(expectedStatusCode, actualStatusCode);
    }
}
