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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.container.Ole2ContainerContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.ZipContainerContentIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * File identification results printer.
 *
 * NB: This class is called recursively when archive files are opened 
 *
 * @author rbrennan
 */
public class ResultPrinter {

    private static final String R_SLASH = "/";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";
    private static final String SPACE = " ";

    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private List<TriggerPuid> triggerPuids;
    private IdentificationRequestFactory requestFactory;
    private String path;
    private String slash;
    private String slash1;
    private String wrongSlash;
    private boolean archives;
    private boolean webArchives;
    private final String OLE2_CONTAINER = "OLE2";
    private final String ZIP_CONTAINER = "ZIP";
    private final String ZIP_ARCHIVE = "x-fmt/263";
    private final String JIP_ARCHIVE = "x-fmt/412";
    private final String TAR_ARCHIVE = "x-fmt/265";
    private final String GZIP_ARCHIVE = "x-fmt/266";
    private final String ARC_ARCHIVE = "x-fmt/219";
    private final String OTHERARC_ARCHIVE = "fmt/410";
    private final String WARC_ARCHIVE = "fmt/289";

    private ContainerAggregator aggregator;
    private Map<String, Format> puidFormatMap;

    private static final Logger logger = LoggerFactory.getLogger(ResultPrinter.class);

    /**
     * Store signature files.
     *
     * @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current file/container path 
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     * @param archives                      Should archives be examined?
     * @param webArchives                   Should web archives be examined?
     * @param aggregator
     * @param puidFormatMap                 map of puids to formats
     */
    public ResultPrinter(final BinarySignatureIdentifier binarySignatureIdentifier,
                         final ContainerSignatureDefinitions containerSignatureDefinitions,
                         final String path, final String slash, final String slash1, boolean archives, boolean webArchives,
                         final ContainerAggregator aggregator, final Map<String, Format> puidFormatMap) {

        this.binarySignatureIdentifier = binarySignatureIdentifier;
        this.containerSignatureDefinitions = containerSignatureDefinitions;
        this.path = path;
        this.slash = slash;
        this.slash1 = slash1;
        this.wrongSlash = this.slash.equals(R_SLASH) ? "\\" : R_SLASH;
        this.archives = archives;
        this.webArchives = webArchives;
        if (containerSignatureDefinitions != null) {
            triggerPuids = containerSignatureDefinitions.getTiggerPuids();
        }
        this.aggregator = aggregator;
        this.puidFormatMap = puidFormatMap;
    }

    /**
     * Output identification for this file.
     *
     * @param results                       identification Results
     * @param request                       identification Request
     *
     * @throws CommandExecutionException    if unexpected container type encountered
     */
    public void print(final IdentificationResultCollection results,
                      final IdentificationRequest request) throws CommandExecutionException {

        final String fileName = (path + request.getFileName()).replace(wrongSlash, slash);
        final IdentificationResultCollection containerResults =
                getContainerResults(results, request, fileName);

        IdentificationResultCollection finalResults = new IdentificationResultCollection(request);
        boolean container = false;
        if (containerResults.getResults().size() > 0) {
            container = true;
            finalResults = containerResults;
        } else if (results.getResults().size() > 0) {
            finalResults = results;
        }
        if (finalResults.getResults().size() > 0) {
            binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
        }
        if (finalResults.getResults().size() > 0) {
            int cnt = 0;
            for (IdentificationResult identResult : finalResults.getResults()) {
                if (+cnt > 1) {
                    logger.warn("Count: " + cnt);
                }
                String formatName = identResult.getName();
                String puid = identResult.getPuid();
                if (!container && JIP_ARCHIVE.equals(puid)) {
                    puid = ZIP_ARCHIVE;
                }

                String normalizedFormat = DroidToolOutputter.mapFormatName(formatName);
                String output = String.format("fileName: %s,\n mimeType: %s,\n formatName: %s,\n normalizedFormat: %s,\n puid: %s",
                        fileName, identResult.getMimeType(), formatName, normalizedFormat, puid);
                logger.debug(output);
                // add a single format type
                aggregator.addFormat(normalizedFormat);
            }
        } else {
            aggregator.incrementUnknownFormat();
            logger.debug(fileName + " -- Unknown filetype");
        }
    }

    private IdentificationResultCollection getContainerResults(
            final IdentificationResultCollection results,
            final IdentificationRequest request, final String fileName) throws CommandExecutionException {

        IdentificationResultCollection containerResults =
                new IdentificationResultCollection(request);

        if (results.getResults().size() > 0 && containerSignatureDefinitions != null) {
            int cnt = 0;
            for (IdentificationResult identResult : results.getResults()) {
                if (+cnt > 1) {
                    logger.info("IdentificationResult count: " + cnt);
                }
                String filePuid = identResult.getPuid();
                if (filePuid != null) {
                    TriggerPuid containerPuid = getTriggerPuidByPuid(filePuid);
                    if (containerPuid != null) {

                        requestFactory = new ContainerFileIdentificationRequestFactory();
                        String containerType = containerPuid.getContainerType();

                        if (OLE2_CONTAINER.equals(containerType)) {
                            try {
                                Ole2ContainerContentIdentifier ole2Identifier =
                                        new Ole2ContainerContentIdentifier();
                                ole2Identifier.init(containerSignatureDefinitions, containerType);
                                Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
                                ole2IdentifierEngine.setRequestFactory(requestFactory);
                                ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
                                containerResults = ole2Identifier.process(
                                        request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {   // carry on after container i/o problems
                                logger.warn(e + SPACE + L_BRACKET + fileName + R_BRACKET);
                            }
                        } else if (ZIP_CONTAINER.equals(containerType)) {
                            try {
                                ZipContainerContentIdentifier zipIdentifier =
                                        new ZipContainerContentIdentifier();
                                zipIdentifier.init(containerSignatureDefinitions, containerType);
                                ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
                                zipIdentifierEngine.setRequestFactory(requestFactory);
                                zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
                                containerResults = zipIdentifier.process(
                                        request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {   // carry on after container i/o problems
                                logger.warn(e + SPACE + L_BRACKET + fileName + R_BRACKET);
                            }
                        } else {
                            throw new CommandExecutionException("Unknown container type: " + containerPuid);
                        }
                    }
                }
            }
        }

        // container results only have the PUID filled in
        for (IdentificationResult result : containerResults.getResults()) {
            IdentificationResultImpl impl = (IdentificationResultImpl) result;
            Format format = puidFormatMap.get(result.getPuid());
            if (format != null) {
                impl.setName(format.getName());
                impl.setMimeType(format.getMimeType());
                impl.setVersion(format.getVersion());
            }
        }

        return containerResults;
    }

    private TriggerPuid getTriggerPuidByPuid(final String puid) {
        for (final TriggerPuid tp : triggerPuids) {
            if (tp.getPuid().equals(puid)) {
                return tp;
            }
        }
        return null;
    }
}
