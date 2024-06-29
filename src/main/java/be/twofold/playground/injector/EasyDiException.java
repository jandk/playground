package be.twofold.playground.injector;

public class EasyDiException extends RuntimeException {
    public EasyDiException() {
    }

    public EasyDiException(String message) {
        super(message);
    }

    public EasyDiException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasyDiException(Throwable cause) {
        super(cause);
    }
}
