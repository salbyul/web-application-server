package http;

public enum Charset {
    UTF8("utf-8");

    private final String charset;

    Charset(final String charset) {
        this.charset = charset;
    }

    @Override
    public String toString() {
        return this.charset;
    }
}
