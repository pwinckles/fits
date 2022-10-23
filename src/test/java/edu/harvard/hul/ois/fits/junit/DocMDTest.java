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
package edu.harvard.hul.ois.fits.junit;

import edu.harvard.hul.ois.fits.tests.AbstractOutputTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class generates FITS and Standard output for inspection during development.
 * It does not actually perform any JUnit test assertions.
 *
 * @author dan179
 */
@Ignore("These tests have no asserts and just generate output files")
public class DocMDTest extends AbstractOutputTest {

    @Test
    public void testWordDocUrlEmbeddedResources() throws Exception {
        writeOutput("Word2003_has_URLs_has_embedded_resources.doc");
    }

    @Test
    public void testWordDocGraphics() throws Exception {
        writeOutput("Word2003_many_graphics.doc");
    }

    @Test
    public void testWordDocPasswordProtected() throws Exception {
        writeOutput("Word2003PasswordProtected.doc");
    }

    @Test
    public void testWordDoc2011() throws Exception {
        writeOutput("Word2011_Has_Outline.doc");
    }

    @Test
    public void testWordDocLibreOffice() throws Exception {
        writeOutput("LibreOffice.doc");
    }

    @Test
    public void testWordDocHyperlinks() throws Exception {
        writeOutput("Word2003_has_table_of_contents.doc");
    }

    @Test
    public void testWordDocPasswordAndEncrypted() throws Exception {
        writeOutput("Word_protected_encrypted.doc");
    }

    @Test
    public void testWordDocV2() throws Exception {
        writeOutput("NEWSSLID_Word2_0.DOC");
    }

    @Test
    public void testOpenOfficeDoc() throws Exception {
        writeOutput("LibreODT-Ur-doc.odt");
    }

    @Test
    public void testOpenOfficeDocEmbeddedResources() throws Exception {
        writeOutput("LibreODTwFormulas.odt");
    }

    @Test
    public void testOpenOfficeDocHasTables() throws Exception {
        writeOutput("LibreODT-hasTables.odt");
    }

    @Test
    public void testOpenOfficeDocUnparseableDate() throws Exception {
        writeOutput("UnparseableDate.odt");
    }

    @Test
    public void testWordDocxOutput() throws Exception {
        writeOutput("Word_has_index.docx");
    }

    @Test
    public void testWordDocxPasswordProtected() throws Exception {
        writeOutput("WordPasswordProtected.docx");
    }

    @Test
    public void testWordDocmOutput() throws Exception {
        writeOutput("Document_Has_Form_Controls.docm");
    }

    @Test
    public void testEpubOutput() throws Exception {

        // process multiple files to examine different types of output
        String[] inputFilenames = {
            "Winnie-the-Pooh-protected.epub",
            "GeographyofBliss_oneChapter.epub",
            "aliceDynamic_images_metadata_tableOfContents.epub",
            "epub30-test-font-embedding-obfuscation.epub",
            "Calibre_hasTable_of_Contents.epub"
        };

        for (String inputFilename : inputFilenames) {
            writeOutput(inputFilename);
        }
    }

    @Test
    public void testWPDOutput() throws Exception {

        // process multiple files to examine different types of output
        String[] inputFilenames = {"WordPerfect6-7.wpd", "WordPerfect4_2.wp", "WordPerfect5_0.wp", "WordPerfect5_2.wp"};
        //    			"WordPerfectCompoundFile.wpd"};  // (not identified as a WordPerfect document)

        for (String inputFilename : inputFilenames) {
            writeOutput(inputFilename);
        }
    }

    @Test
    public void testRtfOutput() throws Exception {
        writeOutput("TestDoc.rtf");
    }

    @Test
    public void testRtfWithCompanyOutput() throws Exception {
        writeOutput("Doc2.rtf");
    }

    @Test
    public void testPdf() throws Exception {
        String[] inputFilenames = {
            "PDF_embedded_resources.pdf", "HasChangeHistory.pdf", "PDF_eng.pdf", "HasAnnotations.pdf"
        };

        for (String inputFilename : inputFilenames) {
            writeOutput(inputFilename);
        }
    }

    @Test
    public void testPdfA() throws Exception {

        String[] inputFilenames = {
            "PDFa_equations.pdf",
            "PDFa_multiplefonts.pdf",
            "PDFa_has_form.pdf",
            "PDFa_has_table_of_contents.pdf",
            "PDFa_has_tables.pdf",
            "PDFA_Document with tables.pdf",
            "PDFa_embedded_resources.pdf"
        };

        for (String inputFilename : inputFilenames) {
            writeOutput(inputFilename);
        }
    }

    @Test
    public void testPdfX() throws Exception {

        String[] inputFilenames = {
            "altona_technical_1v2_x3_has_annotations.pdf",
            "Book_pdfx1a.pdf", // converts to PDF/A
            "PDFx3.pdf"
        }; // converts to PDF/A

        for (String inputFilename : inputFilenames) {
            writeOutput(inputFilename);
        }
    }
}
