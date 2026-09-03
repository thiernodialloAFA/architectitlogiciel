package com.pvg.governance.web;

/**
 * v1 auth boundary is explicitly demo-scoped (proposal §4): the acting user is taken
 * from an optional {@code X-Actor} header, defaulting to "demo-user". This is not a
 * production security control and is documented as such.
 */
public final class ActorHeader {

    public static final String NAME = "X-Actor";
    public static final String DEFAULT = "demo-user";

    private ActorHeader() {
    }

    public static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT;
        }
        String trimmed = raw.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }
}
