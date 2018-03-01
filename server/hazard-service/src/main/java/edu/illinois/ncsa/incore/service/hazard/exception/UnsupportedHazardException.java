package edu.illinois.ncsa.incore.service.hazard.exception;

public class UnsupportedHazardException extends Exception {
    public UnsupportedHazardException() {

    }

    public UnsupportedHazardException(String message)
    {
        super(message);
    }

    public UnsupportedHazardException(Throwable cause) {
        super(cause);
    }

    public UnsupportedHazardException(String message, Throwable cause) {
        super(message, cause);
    }
}
