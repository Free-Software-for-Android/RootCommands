/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sufficientlysecure.rootcommands;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RootCommands {
    public static boolean DEBUG = false;
    public static int DEFAULT_TIMEOUT = 10000;

    private static final Logger LOGGER = Logger.getLogger(RootCommands.class.getName());

    /**
     * General method to check if user has su binary and accepts root access for this program!
     * 
     * @return true if everything worked
     */
    public static boolean rootAccessGiven() {
        boolean rootAccess = false;

        try {
            Shell rootShell = Shell.startRootShell();

            Toolbox tb = new Toolbox(rootShell);
            if (tb.isRootAccessGiven()) {
                rootAccess = true;
            }

            rootShell.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Problem while checking for root access!", e);
        }

        return rootAccess;
    }
}
