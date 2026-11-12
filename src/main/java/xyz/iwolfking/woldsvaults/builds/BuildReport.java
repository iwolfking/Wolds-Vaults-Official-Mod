package xyz.iwolfking.woldsvaults.builds;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Everything one build command found: errors that stop it, game rules a load bypassed, fallbacks it used and plain
 * notes. Every fallback is also logged as a warning so it shows in the server log.
 */
public final class BuildReport {
    private final String action;
    private final String buildName;
    private final String playerName;
    private final List<String> errors = new ArrayList<>();
    private final List<String> bypassed = new ArrayList<>();
    private final List<String> fallbacks = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private final List<String> summary = new ArrayList<>();
    private final List<String> source = new ArrayList<>();
    private Throwable failure;

    public BuildReport(String action, String buildName, String playerName) {
        this.action = action;
        this.buildName = buildName;
        this.playerName = playerName;
    }

    public void error(String message) {
        this.errors.add(message);
        WoldLogger.error("[wvbuilds] {} {} for {}: {}", this.action, this.buildName, this.playerName, message);
    }

    public void bypassed(String message) {
        this.bypassed.add(message);
    }

    public void fallback(String message) {
        this.fallbacks.add(message);
        WoldLogger.error("[wvbuilds] {} {} for {} used a fallback: {}", this.action, this.buildName,
                this.playerName, message);
    }

    public void note(String message) {
        this.notes.add(message);
    }

    public void summary(String message) {
        this.summary.add(message);
    }

    public void source(String message) {
        this.source.add(message);
    }

    /** Records an unexpected exception; it counts as an error and its stack trace goes into the report file. */
    public void failure(String context, Throwable throwable) {
        this.failure = throwable;
        this.error(context + ": " + throwable.getClass().getSimpleName()
                + (throwable.getMessage() == null ? "" : " - " + throwable.getMessage()));
        WoldLogger.error("[wvbuilds] Unexpected failure during {} {} for {}", this.action, this.buildName,
                this.playerName, throwable);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public List<String> getBypassed() {
        return this.bypassed;
    }

    public List<String> getFallbacks() {
        return this.fallbacks;
    }

    public String getAction() {
        return this.action;
    }

    public String getBuildName() {
        return this.buildName;
    }

    /** The full report text, section by section. */
    public String render() {
        StringBuilder out = new StringBuilder();
        out.append("wvbuilds ").append(this.action).append(" '").append(this.buildName).append("' for ")
                .append(this.playerName).append('\n');
        section(out, "Errors", this.errors);
        section(out, "Bypassed game rules", this.bypassed);
        section(out, "Fallbacks", this.fallbacks);
        section(out, "Notes", this.notes);
        section(out, "Summary", this.summary);
        section(out, "Source", this.source);
        if (this.failure != null) {
            StringWriter trace = new StringWriter();
            this.failure.printStackTrace(new PrintWriter(trace));
            out.append("\n== Stack trace\n").append(trace);
        }
        return out.toString();
    }

    private static void section(StringBuilder out, String title, List<String> lines) {
        out.append("\n== ").append(title).append(" (").append(lines.size()).append(")\n");
        for (String line : lines) {
            out.append("- ").append(line).append('\n');
        }
    }

    /** Writes the report; a write failure is logged and returns null instead of failing the command. */
    public Path writeTo(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, this.render(), StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            WoldLogger.error("[wvbuilds] Could not write the report file {}", file, e);
            return null;
        }
    }
}
