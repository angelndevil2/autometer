package kr.blogspot.andmemories.autometer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.cli.*;

/**
 * @author k, Created on 16. 1. 27.
 */
@Data
public class CmdOptions {

    private final Options options = new Options();
    private final CommandLineParser parser = new DefaultParser();
    @Setter(AccessLevel.NONE)
    private CommandLine cmd;
    private String[] args;

    public CmdOptions() {

        options.addOption("h", "help", false, "print this message");
        options.addOption(
                Option.builder("d").
                        argName("path").
                        longOpt("dir").
                        numberOfArgs(1).
                        desc("autometer base directory").build());
        options.addOption(
                Option.builder("p").
                        argName("port").
                        longOpt("port").
                        numberOfArgs(1).
                        desc("rmi server port").build());
/*        options.addOption(
                Option.builder("X").
                        argName("ms=value").
                        numberOfArgs(2).
                        valueSeparator().
                        desc("value must be ##m").build());*/
    }

    /**
     * set commaond line arguments and parse
     *
     * @param args
     * @throws ParseException
     */
    public void setArgs(String[] args) throws ParseException {
        this.args = args;
        cmd = parser.parse(options, args);
    }

    /**
     * print usage and help information
     */
    public void printUsage() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("autometer", options, true);
    }
}
