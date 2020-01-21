/*
 * Copyright 2020 StarChart-Labs Contributors (https://github.com/StarChart-Labs)
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
package org.starchartlabs.lure.command;

import picocli.CommandLine.Command;

/**
 * Represents the lure command-set as a whole. This structure is used by the picocli library, as it assumes output may
 * be things structured like "git", where there is a first command indicating the program, and second indicating the
 * operation
 *
 * @author romeara
 * @since 0.3.0
 */
@Command(mixinStandardHelpOptions = true, subcommands = { PostbinCommand.class, PushCommand.class })
public class LureCommand implements Runnable {

    @Override
    public void run() {
        // This command has no behavior of its own, and serves to structure sub-commands
    }

}
