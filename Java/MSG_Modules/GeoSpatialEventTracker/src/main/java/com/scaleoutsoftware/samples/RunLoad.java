/*
 * (C) Copyright 2025 by ScaleOut Software, Inc.
 *
 * LICENSE AND DISCLAIMER
 * ----------------------
 * This material contains sample programming source code ("Sample Code").
 * ScaleOut Software, Inc. (SSI) grants you a nonexclusive license to compile,
 * link, run, display, reproduce, and prepare derivative works of
 * this Sample Code.  The Sample Code has not been thoroughly
 * tested under all conditions.  SSI, therefore, does not guarantee
 * or imply its reliability, serviceability, or function. SSI
 * provides no support services for the Sample Code.
 *
 * All Sample Code contained herein is provided to you "AS IS" without
 * any warranties of any kind. THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGMENT ARE EXPRESSLY
 * DISCLAIMED.  SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED
 * WARRANTIES, SO THE ABOVE EXCLUSIONS MAY NOT APPLY TO YOU.  IN NO
 * EVENT WILL SSI BE LIABLE TO ANY PARTY FOR ANY DIRECT, INDIRECT,
 * SPECIAL OR OTHER CONSEQUENTIAL DAMAGES FOR ANY USE OF THE SAMPLE CODE
 * INCLUDING, WITHOUT LIMITATION, ANY LOST PROFITS, BUSINESS
 * INTERRUPTION, LOSS OF PROGRAMS OR OTHER DATA ON YOUR INFORMATION
 * HANDLING SYSTEM OR OTHERWISE, EVEN IF WE ARE EXPRESSLY ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.scaleoutsoftware.samples;

import org.apache.commons.cli.*;
import org.jline.builtins.Completers;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class RunLoad {
    static String[] defaultNodesToAttack = new String[] {
            "98072",
            "98073",
            "98074",
            "98075",
            "98082",
            "98367",
            "10122",
            "10124",
            "10125",
            "10131",
            "10132",
            "10133",
            "33109",
            "33102",
            "33101",
            "33110",
            "33111",
            "33114"
    };
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        String def_connectionString = "bootstrapGateways=localhost;maxPoolSize=32;";
        String def_filePath = "datasource_initfile_2000_1.csv";
        int def_numTrackers = 2000;
        int def_msgsPerSecond = 1000;

        Options options = getOptions();

        CommandLineParser commandLineParser = new org.apache.commons.cli.DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("LoadGenerator", options);
            System.exit(1);
        }

        String connectionString = commandLine.getOptionValue("c");
        String filePath = commandLine.getOptionValue("f");
        String numTrackersStr = commandLine.getOptionValue("n");
        String msgsPerSecondStr = commandLine.getOptionValue("m");
        int numTrackers, msgsPerSecond;

        if(connectionString == null) {
            connectionString = def_connectionString;
        }
        if(numTrackersStr == null) {
            numTrackers = def_numTrackers;
        } else {
            numTrackers = Integer.parseInt(numTrackersStr);
        }
        if(msgsPerSecondStr == null) {
            msgsPerSecond = def_msgsPerSecond;
        } else {
            msgsPerSecond = Integer.parseInt(msgsPerSecondStr);
        }

        Terminal terminal = TerminalBuilder.builder().system(true).build();

        // Tab-completion structure
        Completers.TreeCompleter completer = new Completers.TreeCompleter(
                node("help"),
                node("add",
                        node("--name")
                ),
                node("quit"),
                node("q")
        );

        DefaultParser parser = new DefaultParser();
        parser.setEofOnUnclosedQuote(true);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .parser(parser)
                .build();

        if(numTrackers < defaultNodesToAttack.length) {
            reader.printAbove("Required at least " + defaultNodesToAttack.length + " trackers; using " + defaultNodesToAttack.length);
            reader.printAbove("Throttling msg/second using " + defaultNodesToAttack.length/2 + "msg/second.");
            numTrackers = defaultNodesToAttack.length;
            msgsPerSecond = defaultNodesToAttack.length/2;
        }

        LoadGenerator generator = new LoadGenerator(connectionString, filePath, defaultNodesToAttack, numTrackers, msgsPerSecond, reader);
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
        executor.submit(generator);

        while (true) {
            String line;
            try {
                line = reader.readLine("LoadGenerator> ");
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }

            if (line == null || line.isEmpty()) continue;

            List<String> words = reader.getParser().parse(line, line.length()).words();
            String cmd = words.get(0);

            switch (cmd) {
                case "exit":
                case "quit":
                case "q":
                    println(terminal, "bye.");
                    return;
                case "help":
                    printHelp(terminal);
                    break;
                case "attack":
                    handleAttack(terminal, words.subList(1, words.size()), generator);
                    break;
                default:
                    println(terminal, "Unknown command.");
                    printHelp(terminal);
                    break;
            }
        }
    }

    private static void handleAttack(Terminal t, List<String> args, LoadGenerator generator) {

        int idx = args.indexOf("--id");
        if (idx != -1 && idx + 1 < args.size()) {
            String zip = args.get(idx + 1);
            println(t, "Asynchronously attacking " + zip + ".");
            generator.attack(zip);
            return;
        }

        println(t, "Asynchronously attacking default nodes.");
        generator.attackAll(defaultNodesToAttack);
    }

    private static void printHelp(Terminal t) {
        String msg = "Commands:\nhelp\nattack\nattack --id <zip_code>\nexit\n";
        println(t, msg);
    }

    private static void println(Terminal t, String s) {
        t.writer().println(s);
        t.writer().flush();
    }

    private static Options getOptions() {
        Options options = new Options();

        Option fileOption = new Option("f", "csv_file", true, "The CSV file needed to load event trackers. Required.");
        fileOption.setRequired(true);

        Option numTrackersOption = new Option("n", "num_trackers", true, "The number of event trackers. Default: 2000 min_value: " + defaultNodesToAttack.length);
        numTrackersOption.setRequired(false);

        Option msgsPerSecondOption = new Option("m", "msg_second", true, "The number of msgs per second to send. Default: 1000");
        msgsPerSecondOption.setRequired(false);

        Option connectionStringOption = new Option("c", "con_string", true, "The SOSS connection string. Default: bootstrapGateways=localhost;maxPoolSize=32;");
        connectionStringOption.setRequired(false);

        options.addOption(fileOption);
        options.addOption(numTrackersOption);
        options.addOption(msgsPerSecondOption);
        options.addOption(connectionStringOption);
        return options;
    }

}
