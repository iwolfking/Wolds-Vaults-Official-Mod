package xyz.iwolfking.woldsvaults.builds;

/** A build file the running game cannot read or apply; the message names the JSON path and what is wrong. */
public class BuildException extends RuntimeException {
    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
