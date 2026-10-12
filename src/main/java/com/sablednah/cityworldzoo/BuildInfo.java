package com.sablednah.cityworldzoo;

import java.io.InputStream;
import java.util.Properties;

/**
 * Which build this is -- the family's build stamp. A version answers "which release"; the stamp answers
 * "which bytes"; the startup line answers "which ran". Read from a namespaced properties file, not the
 * manifest, because a dev run has no jar; {@code unknown} when absent, never a failure.
 */
public final class BuildInfo {

    private static final String RESOURCE = "/cityworld_zoo/build.properties";

    private static final String COMMIT;
    private static final String BRANCH;
    private static final String TIME;
    private static final String VERSION;

    public record Stamp(String commit, String branch, String time, String version) {}

    /** All or nothing: a half-stamp with a real commit and an unknown time reads as a fact. */
    public static Stamp parse(InputStream in) {
        Stamp unknown = new Stamp("unknown", "unknown", "unknown", "unknown");
        if (in == null) return unknown;
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (Exception e) {
            return unknown;
        }
        return new Stamp(p.getProperty("commit", "unknown"), p.getProperty("branch", "unknown"),
                p.getProperty("time", "unknown"), p.getProperty("version", "unknown"));
    }

    static {
        Stamp s;
        try (InputStream in = BuildInfo.class.getResourceAsStream(RESOURCE)) {
            s = parse(in);
        } catch (Exception e) {
            s = new Stamp("unknown", "unknown", "unknown", "unknown");
        }
        COMMIT = s.commit(); BRANCH = s.branch(); TIME = s.time(); VERSION = s.version();
    }

    public static String describe(Stamp s) {
        return s.version() + " (build " + s.commit() + " on " + s.branch() + ", " + s.time() + ")";
    }

    public static String commit() { return COMMIT; }
    public static String branch() { return BRANCH; }
    public static String time() { return TIME; }
    public static String version() { return VERSION; }

    /** {@code 0.1.0+mc26.2 (build a1b2c3d4 on mc26.2, 2026-10-12T09:15:00Z)}; {@code -dirty} means uncommitted changes. */
    public static String describe() {
        return describe(new Stamp(COMMIT, BRANCH, TIME, VERSION));
    }

    private BuildInfo() {}
}
