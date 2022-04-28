package fitBut.utils.logging;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import static fitBut.utils.logging.HorseRiderFilter.*;

/**
 * @author : Vaclav Uhlir
 * @since : 6.9.2019
 **/
public final class HorseRider {

    private static Logger logger = Logger.getLogger("HorseRider");
    private static boolean NOT_NEUTERED = false;

    /**
     * Detailed print meant for debugging
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     */
    static public void challenge(String tag, String message) {
        logger.finest(tag + ": " + message);
        if (WTF && DEBUG_level > 3) {
            if (SUB_RULES.containsKey(tag) && SUB_RULES.get(tag) <= 3) {
                return;
            }
            System.out.println("[WTF] " + tag + ": " + message);
        }
    }

    /**
     * Basic print meant for debugging
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     */
    static public void inquire(String tag, String message) {
        logger.fine(tag + ": " + message);
        if (DEBUG && DEBUG_level > 2) {
            if (SUB_RULES.containsKey(tag) && SUB_RULES.get(tag) <= 2) {
                return;
            }
            System.out.println("[D] " + tag + ": " + message);
        }
    }

    /**
     * Verbose info
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     */
    static public void inform(String tag, String message) {
        logger.info(tag + ": " + message);
        if (INFORM && DEBUG_level > 1) {
            if (SUB_RULES.containsKey(tag) && SUB_RULES.get(tag) <= 1) {
                return;
            }
            System.out.println("[I] " + tag + ": " + message);
        }
    }

    /**
     * Important message
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     */
    static public void warn(String tag, String message) {
        warn(tag, message, null);
    }

    /**
     * Important message
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     * @param e       error
     */
    static public void warn(String tag, String message, Throwable e) {
        if (e != null) {
            logger.log(Level.WARNING, tag + ": " + message, e);
            e.printStackTrace();
        } else {
            logger.log(Level.WARNING, tag + ": " + message);
        }
        if (WARNING && DEBUG_level > 0) {
            if (SUB_RULES.containsKey(tag) && SUB_RULES.get(tag) <= 0) {
                return;
            }
            System.out.println("[W] " + tag + ": " + message);
        }
    }

    /**
     * Error message
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     */
    static public void yell(String tag, String message) {
        yell(tag, message, null);
    }

    /**
     * Error message
     *
     * @param tag     tag for filtering
     * @param message message to be printed
     * @param e       error
     */
    static public void yell(String tag, String message, Throwable e) {
        if (e != null) {
            logger.log(Level.SEVERE, tag + ": " + message, e);
            e.printStackTrace();
        } else {
            logger.log(Level.SEVERE, tag + ": " + message);
        }
        if (ERROR && DEBUG_level >= 0) {
            System.err.println("[E] " + tag + ": " + message);
        }
    }

    private static final Formatter format = new Formatter() {
        @Override
        public String format(LogRecord record) {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
            return "[" + formatter.format(new Date(record.getMillis())) + "] " + record.getLevel() + ": " + record.getMessage() + "\n";
        }
    };

    static {
        try {
            logger.setUseParentHandlers(false);

            // file output
            try {
                Path path = Paths.get("logs");
                Files.createDirectory(path);
            } catch (FileAlreadyExistsException ignored) {
            }

            if (NOT_NEUTERED) {
                FileHandler fh = new FileHandler("logs/fitBut.log");

                fh.setFormatter(format);

                logger.addHandler(fh);
                logger.setLevel(Level.ALL);
                fh.setLevel(Level.ALL);

                FileHandler inform_fh = new FileHandler("logs/fitBut_inform.log");
                inform_fh.setFormatter(format);
                logger.addHandler(inform_fh);
                inform_fh.setLevel(Level.INFO);

                FileHandler warning_fh = new FileHandler("logs/fitBut_warnings.log");
                warning_fh.setFormatter(format);
                logger.addHandler(warning_fh);
                warning_fh.setLevel(Level.WARNING);
            }

            FileHandler error_fh = new FileHandler("logs/fitBut_errors.log");
            error_fh.setFormatter(format);
            logger.addHandler(error_fh);
            error_fh.setLevel(Level.SEVERE);

            if (NOT_NEUTERED) {

                makeLogFor("agentA10_info", "agentA10", Level.INFO);
                makeLogFor("Unknown", "Unknown", Level.FINEST);
                makeLogFor("agent_actions", "returning action", Level.FINEST);
                makeLogFor("server_actions", " submitting action", Level.FINEST);
                makeLogFor("percepts", "Percept debug ", Level.FINEST);
            }
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * creates separate log file based on message content
     *
     * @param name     name of the log file
     * @param filterTo key word
     * @throws IOException error on file creation
     */
    @SuppressWarnings("SameParameterValue")

    private static void makeLogFor(String name, String filterTo, Level level) throws IOException {

        if (NOT_NEUTERED) {
            FileHandler fileHandler = new FileHandler("logs/" + name + ".log");
            fileHandler.setFilter(record -> record.getMessage().contains(filterTo));
            fileHandler.setFormatter(format);
            logger.addHandler(fileHandler);
            fileHandler.setLevel(level);
        }
    }

    @SuppressWarnings("unused")
    private static void makeLogFor(String name, Level level) throws IOException {
        makeLogFor(name, name, level);
    }

    private static void makeLogFor(String name, String filterTo) throws IOException {
        makeLogFor(name, filterTo, Level.FINEST);
    }

    public static void makeLogFor(String name) throws IOException {
        makeLogFor(name, name);
    }

    public static void makeLogFor(int match, String name) throws IOException {
        try {
            Path path = Paths.get("logs/" + match);
            Files.createDirectory(path);
        } catch (FileAlreadyExistsException ignored) {
        }
        makeLogFor(match + "/" + name, name);
    }
}
