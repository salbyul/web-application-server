package exception;

public class HttpRequestException extends RuntimeException {

    public HttpRequestException() {
    }

    public HttpRequestException(final String message) {
        super(message);
    }
}
