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

/**
 * Implement this interface and inject the resulting object when invoking <code>sendShell</code>.
 * <code>RootTools</code> comes with a reference implementation: <code>RootTools.Result</code>
 */
public interface IShellResult {
    public abstract void process(String line);

    public abstract void processError(String line);

    public abstract void onFailure(Exception ex);

    public void waitThread();

    public void notifyThread();

    public IShellResult setData(Serializable data);

    public Serializable getData();

    public IShellResult setError(int error);

    public int getError();

}
