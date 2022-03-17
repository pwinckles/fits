/*
 * Copyright 2017 Harvard University Library
 *
 * This file is part of FITS (File Information Tool Set).
 *
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hul.ois.fits.tests;

import java.io.File;

/**
 * This base class is for initializing logging for test classes to set the logging configuration
 * in the Fits constructor by means of an environment variable. This allows for a separate logging configuration for test classes
 * other than the default logging as set up within FITS.java.
 *
 * @author dan179
 * @see edu.harvard.hul.ois.fits.Fits#Fits(String, File)
 */
public abstract class AbstractLoggingTest {

    // Suffix added to input file name for actual FITS output file NOT used for test comparison.
    protected static final String OUTPUT_FILE_SUFFIX = "_Output.xml";

    /**
     * Configure logging with the test logging configuration file by setting a system property.
     * See "Default Initialization Procedure" here: https://logging.apache.org/log4j/2.x/manual/
     */
    static {
        File log4jConfig = new File("src/test/resources/log4j2.xml");
        System.setProperty("log4j2.configurationFile", log4jConfig.toURI().toString());
    }
}
