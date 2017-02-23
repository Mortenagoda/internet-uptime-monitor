package works.softwarethat.internet.monitor;

/**
 * @author Morten Andersen (mortena@gmail.com)
 */
public enum Program {
    CLIENT((args) -> new Client(args)),
    SERVER(args -> new Server(args));

    private RunWithArgs runnable;
    private Program(RunWithArgs runnable) {
        this.runnable = runnable;
    }

    public void start(String[] args) {
        runnable.run(args);
    }

    private interface RunWithArgs {
        void run(String[] args);
    }
}
