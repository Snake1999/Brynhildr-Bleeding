package io.nukkit;

import io.nukkit.command.CommandException;
import io.nukkit.command.CommandSender;
import io.nukkit.entity.Player;
import io.nukkit.enumerations.ChatColor;
import io.nukkit.item.ItemFactory;
import io.nukkit.item.meta.MetaItem;
import io.nukkit.plugin.PluginManager;
import io.nukkit.scheduler.Scheduler;
import io.nukkit.util.Versioning;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class Nukkit {

    public static boolean useJline = true;
    public static boolean useConsole = true;
    public static boolean ANSI = true;

    public final static String VERSION = Versioning.getNukkitVersion();
    public final static String VERSION_UNKNOWN = "2.0dev";
    public final static String CODENAME = "Brynhildr";
    public final static String MINECRAFT_VERSION = "v0.15.0 alpha";
    public final static String MINECRAFT_VERSION_NETWORK = "0.15.0";
    public final static String API_VERSION = "2.0.0";

    private static Server server;

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        if (Nukkit.server != null) {
            throw new UnsupportedOperationException("Cannot redefine singleton Server");
        }

        Nukkit.server = server;
        server.getLogger().info("This server is running " + getName() + " version " + getNukkitVersion() + " (Implementing API \"" + ChatColor.AQUA + CODENAME + ChatColor.WHITE + "\" version " + API_VERSION + ")");
    }

    /**
     * @see Server#getName()
     */
    public static String getName() {
        return server.getName();
    }

    /**
     * @see Server#getVersion()
     */
    public static String getVersion() {
        return server.getVersion();
    }

    /**
     * @see Server#getNukkitVersion()
     */
    public static String getNukkitVersion() {
        return server.getNukkitVersion();
    }

    /**
     * @see Server#getOnlinePlayers()
     */
    public static Collection<? extends Player> getOnlinePlayers() {
        return server.getOnlinePlayers();
    }

    /**
     * @see Server#getPluginManager()
     */
    public static PluginManager getPluginManager() {
        return server.getPluginManager();
    }

    /**
     * @see Server#getScheduler()
     */
    public static Scheduler getScheduler() {
        return server.getScheduler();
    }

    /**
     * Gets the instance of the item factory (for {@link MetaItem}).
     *
     * @return the item factory
     * @see ItemFactory
     */
    public static ItemFactory getItemFactory() {
        return server.getItemFactory();
    }

    public static Logger getLogger() {
        return server.getLogger();
    }

    /**
     * @see Server#dispatchCommand(CommandSender sender, String commandLine)
     */
    public static boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
        return server.dispatchCommand(sender, commandLine);
    }

    /**
     * @see Server#getPlayerExact(String name)
     */
    @Deprecated
    public static Player getPlayerExact(String name) {
        return server.getPlayerExact(name);
    }


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
            System.out.println(Server.class.getPackage().getImplementationVersion());
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

                if (!useJline) {
                    // This ensures the terminal literal will always match the jline implementation
                    System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());
                }

                if (ANSI) {
                    AnsiConsole.systemInstall();
                }

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

                Nukkit.server = new Server(optionSet);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}
