//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.tools.utils;

import edu.harvard.hul.ois.fits.exceptions.FitsToolCLIException;
import java.io.*;
import java.util.List;

/**
 *  A static class for command line invocation.
 */
public abstract class CommandLine {

    public static String exec(List<String> cmd, String directory) throws FitsToolCLIException {
        return exec(cmd, directory, true);
    }

    public static String exec(List<String> cmd, String directory, boolean includeStdErr) throws FitsToolCLIException {
        String output = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Runtime rt = Runtime.getRuntime();
            // Process proc = rt.exec(cmd.toString());

            ProcessBuilder builder = new ProcessBuilder(cmd);
            if (directory != null) {
                builder.directory(new File(directory));
            }
            Process proc = builder.start();

            StreamGobbler errorGobbler = null;
            if (includeStdErr) {
                errorGobbler = new StreamGobbler(proc.getErrorStream(), bos);
                errorGobbler.start();
            }

            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), bos);
            outputGobbler.start();
            proc.waitFor();
            if (errorGobbler != null) {
                errorGobbler.join();
            }
            outputGobbler.join();
            // output = sb.toString();
            bos.flush();
            output = bos.toString();
        } catch (Exception e) {
            throw new FitsToolCLIException("Error calling external command line routine", e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                throw new FitsToolCLIException("Error closing external command line output stream", e);
            }
        }
        return output;
    }
}
