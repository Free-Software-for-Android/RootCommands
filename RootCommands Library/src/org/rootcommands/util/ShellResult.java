/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks (RootTools)
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

package org.rootcommands.util;

import java.io.Serializable;

public abstract class ShellResult implements IShellResult {
    private Serializable data = null;
    private int error = 0;

    public abstract void process(String line);

    public abstract void processError(String line);

    public abstract void onFailure(Exception ex);

    public void waitThread() {
        synchronized (this) {
            try {
                // maximum time to wait: 20s
                this.wait(20000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void notifyThread() {
        synchronized (this) {
            this.notify();
        }
    }

    public ShellResult setData(Serializable data) {
        this.data = data;
        return this;
    }

    public Serializable getData() {
        return data;
    }

    public ShellResult setError(int error) {
        this.error = error;
        return this;
    }

    public int getError() {
        return error;
    }
}