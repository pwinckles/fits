/**
 * This file has been modified by Harvard University, June, 2017, for the purposes of incorporating
 * into the FITS application. The original can be found here: https://github.com/digital-preservation/droid
 * <p>
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p>
 * * Neither the name of the The National Archives nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.harvard.hul.ois.fits.tools.droid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Parent class for Containers.
 *
 * @author G.Seaman
 *
 */
public abstract class ArchiveContentIdentifier {

    protected String slash;
    protected String slash1;
    protected BinarySignatureIdentifier binarySignatureIdentifier;
    protected ContainerSignatureDefinitions containerSignatureDefinitions;
    protected File tmpDir;
    protected String path;
    private Boolean expandWebArchives;
    private Map<String, Format> puidFormatMap;

    private static final Logger logger = LoggerFactory.getLogger(ArchiveContentIdentifier.class);

    /**
     * Initialization of instance values must be explicitly called by all children.
     * @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current archive path
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     * @param expandWebArchives             optionally expand (W)ARC files
     * @param puidFormatMap                 map of puids to formats
     */
    public ArchiveContentIdentifier(final BinarySignatureIdentifier binarySignatureIdentifier,
                                    final ContainerSignatureDefinitions containerSignatureDefinitions,
                                    final String path, final String slash, final String slash1,
                                    final Boolean expandWebArchives, final Map<String, Format> puidFormatMap) {

        synchronized (this) {
            setBinarySignatureIdentifier(binarySignatureIdentifier);
            setContainerSignatureDefinitions(containerSignatureDefinitions);
            setPath(path);
            setSlash(slash);
            setSlash1(slash1);
            setExpandWebArchives(expandWebArchives);
            if (getTmpDir() == null) {
                setTmpDir(new File(System.getProperty("java.io.tmpdir")));
            }
            this.puidFormatMap = puidFormatMap;
        }
    }

    /**
     * @return local path element delimiter
     */
    protected String getSlash() {
        return slash;
    }

    /**
     * @param newSlash path element delimiter
     */
    protected void setSlash(String newSlash) {
        this.slash = newSlash;
    }

    /**
     * @return container element delimiter
     */
    protected String getSlash1() {
        return slash1;
    }

    /**
     * @param newSlash1 container element delimiter
     */
    protected void setSlash1(String newSlash1) {
        this.slash1 = newSlash1;
    }

    /**
     * @return binary signature identifier
     */
    protected BinarySignatureIdentifier getBinarySignatureIdentifier() {
        return binarySignatureIdentifier;
    }

    /**
     * @param bis binary signature identifier
     */
    protected void setBinarySignatureIdentifier(BinarySignatureIdentifier bis) {
        this.binarySignatureIdentifier = bis;
    }

    /**
     * @return container signatures
     */
    protected ContainerSignatureDefinitions getContainerSignatureDefinitions() {
        return containerSignatureDefinitions;
    }

    /**
     * @param csd container signatures
     */
    protected void setContainerSignatureDefinitions(ContainerSignatureDefinitions csd) {
        this.containerSignatureDefinitions = csd;
    }

    /**
     * @return temporary file directory
     */
    protected File getTmpDir() {
        return tmpDir;
    }

    /**
     * @param tmpDir temporary file directory
     */
    protected void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * @return archive path
     */
    protected String getPath() {
        return path;
    }

    /**
     * @param path archive path
     */
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * @return whether to expand (W)ARCs
     */
    protected Boolean getExpandWebArchives() {
        return expandWebArchives;
    }

    /**
     * @param ewa whether to expand (W)ARCs
     */
    protected void setExpandWebArchives(Boolean ewa) {
        this.expandWebArchives = ewa;
    }

    /**
     *
     * @param prefix    String describing container-type
     * @param filename  Name of file
     * @return URI for container
     */
    protected String makeContainerURI(String prefix, String filename) {
        return prefix + ":" + getSlash1() + getPath() + filename + "!" + getSlash();
    }

    /**
     * @param request  The request
     * @param in The container input stream
     * @param newPath Path for the Container file
     * @param aggregator Aggregates ZIP file container information
     * @throws CommandExecutionException When an exception happens during execution
     */
    protected void expandContainer(IdentificationRequest request, InputStream in, String newPath, ContainerAggregator aggregator)
            throws CommandExecutionException, UnsupportedZipFeatureException {

        try {
            request.open(in);
            IdentificationResultCollection results =
                    getBinarySignatureIdentifier().matchBinarySignatures(request);

            if (results.getResults().isEmpty()) {
                results = binarySignatureIdentifier.matchExtensions(request, true);
            }

            final ResultPrinter resultPrinter =
                    new ResultPrinter(getBinarySignatureIdentifier(),
                            getContainerSignatureDefinitions(), newPath, getSlash(), getSlash1(), true,
                            getExpandWebArchives(), aggregator, puidFormatMap);

            resultPrinter.print(results, request);
            request.close();
        } catch (UnsupportedZipFeatureException e) {
            // output: org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException: unsupported feature encryption used in entry Book_pdfx1a.pdf
            throw e;
        } catch (IOException ioe) {
            logger.warn(ioe + " " + newPath);
        } finally {
            try {
                // make sure no temp files are left behind
                request.close();
            } catch (IOException ioe) {
                logger.warn("Failed to close temp file for Container request:" + ioe);
                // not a lot we can do here - warning msg already given and deleteOnExit set
            }
        }
    }

}
