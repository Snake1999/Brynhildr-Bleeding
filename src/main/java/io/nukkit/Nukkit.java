package io.nukkit;

import io.nukkit.util.Versioning;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class Nukkit {
    public final static String BUKKIT_VERSION = Versioning.getBukkitVersion();
    public final static String VERSION = NukkitServer.class.getPackage().getImplementationVersion();
    public final static String CODENAME = "Brynhildr";
    public final static String MINECRAFT_VERSION = "v1.2.0.22";
    public final static String MINECRAFT_VERSION_NETWORK = "1.2.0.22";
    public static boolean useJline = true;
    public static boolean useConsole = true;
    public static boolean ANSI = true;
    public static boolean enableStatusBar = false;
    private static PrintStream SYSTEM_OUT;

    public static void main(String[] args) {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "help"), "Show the help");

                //TODO: SERVER CONFIGURATION ARGUMENTS

                acceptsAll(asList("n", "nukkit-settings"), "File for nukkit settings").withRequiredArg().ofType(File.class).defaultsTo(new File("nukkit.yml"), new File[0]).describedAs("Yml file");

                acceptsAll(asList("nojline", "disable-jline"), "Disables jline and emulates the vanilla console");

                acceptsAll(asList("noansi", "disable-ansi"), "Disables jline and emulates the vanilla console");

                acceptsAll(asList("noconsole"), "Disables the console");

                acceptsAll(asList("s", "statusbar"), "Enables the status bar");

                acceptsAll(asList("v", "version"), "Show the version of Nukkit");

                acceptsAll(asList("debug"), "Show the debug logs");
            }
        };

        OptionSet optionSet = null;

        try {
            optionSet = parser.parse(args);
        } catch (OptionException e) {
            LogManager.getLogger(Nukkit.class.getName()).fatal(e.getLocalizedMessage());
        }

        if (optionSet == null || optionSet.has("?")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                LogManager.getLogger(Nukkit.class.getName()).fatal((String) null, e);
            }
        } else if (optionSet.has("v")) {
            System.out.println(NukkitServer.class.getPackage().getImplementationVersion());
        } else {
            String path = new File(".").getAbsolutePath();
            if (path.contains("!") || path.contains("+")) {
                System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
                return;
            }

            try {

                // This trick bypasses Maven Shade's clever rewriting of our getProperty call when using String literals
                String jline_UnsupportedTerminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 'U', 'n', 's', 'u', 'p', 'p', 'o', 'r', 't', 'e', 'd', 'T', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});
                String jline_terminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 't', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});

                useJline = !(jline_UnsupportedTerminal).equals(System.getProperty(jline_terminal));

                if (optionSet.has("nojline")) {
                    useJline = false;
                }

                if (optionSet.has("noansi")) {
                    ANSI = false;
                }

                if (optionSet.has("statusbar")) {
                    enableStatusBar = true;
                }

                if (!useJline) {
                    // This ensures the terminal literal will always match the jline implementation
                    System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());
                }

                if (ANSI) {
                    AnsiConsole.systemInstall();
                }

                SYSTEM_OUT = System.out;

                if (optionSet.has("noconsole")) {
                    useConsole = false;
                }

                if (optionSet.has("debug")) {
                    LoggerContext context = (LoggerContext) LogManager.getContext(false);
                    Configuration config = context.getConfiguration();
                    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                    loggerConfig.setLevel(Level.DEBUG);
                    context.updateLoggers();
                }

                new NukkitServer(optionSet);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static PrintStream getSystemOut() {
        return SYSTEM_OUT == null ? System.out : SYSTEM_OUT;
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}
