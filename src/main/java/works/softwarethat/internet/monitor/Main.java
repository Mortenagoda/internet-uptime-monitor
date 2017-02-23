package works.softwarethat.internet.monitor;

import java.util.logging.Logger;

/**
 * @author Morten Andersen (mortena@gmail.com)
 */
public class Main {
    private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Program program = Program.valueOf(args[0].toUpperCase());
        program.start(args);
    }
}
