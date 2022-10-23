/*
 * Copyright 2009 Harvard University Library
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
package edu.harvard.hul.ois.fits.junit.service;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.tests.AbstractXmlUnitTest;
import java.io.File;
import java.util.Scanner;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * These tests compare actual FITS output with expected output on text files.
 * NOTE: This is an integration test that requires a running web application with the
 * FITS Service running.
 *
 * @author dan179
 */
@Ignore
public class TextMDXmlUnitServiceTest extends AbstractXmlUnitTest {

    @BeforeClass
    public static void initializeHttpClient() throws Exception {
        AbstractXmlUnitTest.beforeServiceTest();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        AbstractXmlUnitTest.afterServiceTest();
    }

    @Test
    public void testUTF16TextMD() throws Exception {

        String inputFilename = "utf16.txt";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = super.examine(input);
        fitsOut.addStandardCombinedFormat();
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File("testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testPlainText() throws Exception {

        String inputFilename = "plain-text.txt";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = super.examine(input);
        fitsOut.addStandardCombinedFormat();
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File("testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testCsv() throws Exception {

        String inputFilename = "random_data.csv";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = super.examine(input);
        fitsOut.addStandardCombinedFormat();
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File("testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }
}
