package cookie;

import java.time.LocalDateTime;

public class Cookie {

    private final String key;
    private final String value;
    private int maxAge;
    private String domain;
    private boolean httpOnly;
    private boolean secure;
    private LocalDateTime expires;

    public Cookie(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public void setMaxAge(final int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setHttpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean getHttpOnly() {
        return this.httpOnly;
    }

    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    public boolean getSecure() {
        return this.secure;
    }

    public void setExpires(final LocalDateTime expiredDate) {
        this.expires = expiredDate;
    }

    public LocalDateTime getExpires() {
        return this.expires;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.key + "=" + this.value + "; ");
        if (maxAge != 0) {
            stringBuilder.append("Max-Age=").append(maxAge).append("; ");
        }
        if (domain != null) {
            stringBuilder.append("Domain=").append(domain).append("; ");
        }
        if (httpOnly) {
            stringBuilder.append("HttpOnly; ");
        }
        if (secure) {
            stringBuilder.append("Secure; ");
        }
        if (expires != null) {
            String dayOfWeek = expires.getDayOfWeek().name().charAt(0) + expires.getDayOfWeek().name().substring(1, 3).toLowerCase();
            int day = expires.getDayOfMonth();
            String month = expires.getMonth().toString().charAt(0) + expires.getMonth().toString().substring(1, 3).toLowerCase();
            int year = expires.getYear();
            int hour = expires.getHour();
            int minute = expires.getMinute();
            int second = expires.getSecond();
            stringBuilder.append("Expires=")
                    .append(dayOfWeek).append(", ")
                    .append(day < 10 ? "0" + day : day).append(" ")
                    .append(month).append(" ")
                    .append(year).append(" ")
                    .append(hour < 10 ? "0" + hour : hour).append(":")
                    .append(minute < 10 ? "0" + minute : minute).append(":")
                    .append(second < 10 ? "0" + second : second)
                    .append(" GMT; ");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
