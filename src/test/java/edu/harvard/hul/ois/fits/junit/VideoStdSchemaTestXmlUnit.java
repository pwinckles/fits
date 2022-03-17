/*
 * Copyright 2015 Harvard University Library
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
package edu.harvard.hul.ois.fits.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.tests.AbstractXmlUnitTest;

public class VideoStdSchemaTestXmlUnit extends AbstractXmlUnitTest {

    // These override the values in the parent class.
    private static final String[] OVERRIDING_IGNORED_XML_ELEMENTS = {
            "version",
            "toolversion",
            "dateModified",
            "fslastmodified",
            "startDate",
            "startTime",
            "timestamp",
            "fitsExecutionTime",
            "executionTime",
            "filepath",
            "location",
            "lastmodified",
            "ebucore:locator"};

    /*
     *  Only one Fits instance is needed to run all tests.
     *  This also speeds up the tests.
     */
    private static Fits fits;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Set up FITS for entire class.
        fits = new Fits();
    }

    @AfterClass
    public static void afterClass() {
        fits = null;
    }

    @Test
    public void testVideoXmlUnitFitsOutput_AVC() throws Exception {

        String inputFilename = "FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1.mp4";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitFitsOutput_DV() throws Exception {

        String inputFilename = "FITS-SAMPLE-26.mov";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitStandardOutput_AVC() throws Exception {

        String inputFilename = "FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1.mp4";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);

        // Output stream for FITS to write to
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Create standard output in the stream passed in
        Fits.outputStandardSchemaXml(fitsOut, out);

        File file = new File("test-generated-output/" + inputFilename + "-standard-only" + ACTUAL_OUTPUT_FILE_SUFFIX);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(out.toByteArray());
        fos.flush();
        fos.close();

        // Turn output stream into a String HtmlUnit can use
        String actualXmlStr = new String(out.toByteArray(), "UTF-8");

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + "-standard-only" + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitStandardOutput_DV() throws Exception {

        String inputFilename = "FITS-SAMPLE-26.mov";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);

        // Output stream for FITS to write to
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Create standard output in the stream passed in
        Fits.outputStandardSchemaXml(fitsOut, out);
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + "-standard-only" + ACTUAL_OUTPUT_FILE_SUFFIX);

        // Turn output stream into a String HtmlUnit can use
        String actualXmlStr = new String(out.toByteArray(), "UTF-8");

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output//" + inputFilename + "-standard-only" + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitCombinedOutput_DV() throws Exception {

        String inputFilename = "FITS-SAMPLE-26.mov";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        // Include standard schema output
        fitsOut.addStandardCombinedFormat();

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        // Turn output stream into a String HtmlUnit can use
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // also save file to disk for visual examination
        File file = new File("test-generated-output/" + inputFilename + "-combined" + ACTUAL_OUTPUT_FILE_SUFFIX);
        FileOutputStream fos = new FileOutputStream(file);
        fitsOut.output(fos);
        fos.close();

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + "-combined" + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    /**
     * Tests that the output from FITS matches the expected output.
     */
    @Test
    public void testVideoXmlUnitOutput_MXF() throws Exception {

        String inputFilename = "freeMXF-mxf1a.mxf";

        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        // Turn output stream into a String HtmlUnit can use
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitCombinedOutput() throws Exception {

        String inputFilename = "FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_4_8_1_2_1_1.mov";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        // Create combined output in the stream passed in
        fitsOut.addStandardCombinedFormat();

        // Turn output stream into a String HtmlUnit can use
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());
        fitsOut.saveToDisk("test-generated-output/" + inputFilename + ACTUAL_OUTPUT_FILE_SUFFIX);

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/" + inputFilename + EXPECTED_OUTPUT_FILE_SUFFIX));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    @Test
    public void testVideoXmlUnitFitsOutput_AVC_NO_MD5() throws Exception {

        File fitsConfigFile = new File("testfiles/properties/fits_no_md5_video.xml");
        Fits fits = new Fits(null, fitsConfigFile);

        // First generate the FITS output
        String inputFilename = "FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1.mp4";
        File input = new File("testfiles/" + inputFilename);
        FitsOutput fitsOut = fits.examine(input);
        fitsOut.saveToDisk("test-generated-output/" + "FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1_mp4_FITS_NO_MD5_XmlUnitActualOutput.xml");

        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String actualXmlStr = serializer.outputString(fitsOut.getFitsXml());

        // Read in the expected XML file
        Scanner scan = new Scanner(new File(
                "testfiles/output/FITS-SAMPLE-44_1_1_4_4_4_6_1_1_2_3_1_mp4_FITS_NO_MD5.xml"));
        String expectedXmlStr = scan.
                useDelimiter("\\Z").next();
        scan.close();

        testActualAgainstExpected(actualXmlStr, expectedXmlStr, inputFilename);
    }

    /**
     * Send in set of XML elements to ignore that overrides the default.
     */
    @Override
    protected String[] getIgnoredXmlElements() {
        return OVERRIDING_IGNORED_XML_ELEMENTS;
    }
}
