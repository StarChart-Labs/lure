/*
 * Copyright 2019 StarChart-Labs Contributors (https://github.com/StarChart-Labs)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.starchartlabs.lure;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starchartlabs.lure.command.PushCommand;

//TODO romeara test
/**
 * Main entry point for command line calls to the application
 *
 * @author romeara
 * @since 0.1.0
 */
public class CommandLineInterface {

    /** Logger reference to output information to the application log files */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Argument(handler = SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name = PushCommand.COMMAND_NAME, impl = PushCommand.class)
    })
    private Runnable command;

    public static void main(String[] args) {
        new CommandLineInterface().run(args);
    }

    public void run(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            if (command != null) {
                command.run();
            } else {
                logger.error("Invalid command line arguments");
                printUsage(parser);
            }
        } catch (CmdLineException e) {
            logger.error("Invalid command line arguments", e);
            printUsage(parser);
        }
    }

    private void printUsage(CmdLineParser parser) {
        Objects.requireNonNull(parser);

        StringWriter usageWriter = new StringWriter();

        usageWriter
        .append("java " + getClass().getSimpleName() + " (generate | addkey | list) [options...] arguments...");
        usageWriter.append('\n');
        usageWriter.append('\n');

        parser.printUsage(usageWriter, null);

        // Print sub-command usages
        Map<String, Object> subCommands = new HashMap<>();
        subCommands.put(PushCommand.COMMAND_NAME, new PushCommand());

        for (Entry<String, Object> subCommand : subCommands.entrySet()) {
            usageWriter.append(subCommand.getKey());
            usageWriter.append('\n');

            CmdLineParser subParser = new CmdLineParser(subCommand.getValue());
            subParser.printUsage(usageWriter, null);
            usageWriter.append('\n');
        }

        String usage = usageWriter.toString();
        logger.error("\n{}", usage);
    }

}
