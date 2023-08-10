package http;

public enum ContentType {
    HTML("text/html"), CSS("text/css"), JS("text/javascript");

    private final String type;

    ContentType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
