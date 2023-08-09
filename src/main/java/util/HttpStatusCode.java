package util;

public enum HttpStatusCode {
    OK(200), SEE_OTHER(303), BAD_REQUEST(400), FORBIDDEN(403);


    private final int code;

    HttpStatusCode(final int code) {
        this.code = code;
    }
}
