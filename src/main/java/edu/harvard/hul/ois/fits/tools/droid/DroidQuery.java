//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//



/* Droid 6.1 has no nicely packaged way to make simple queries. This
 * class attempts to fill that gap for FITS, in a way that will let it
 * be lifted for other uses and perhaps incorporated into Droid itself.
 */
package edu.harvard.hul.ois.fits.tools.droid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

public class DroidQuery {

    private BinarySignatureIdentifier sigIdentifier;
    private ContainerIdentifierFactory containerIdentifierFactory;
    private ArchiveFormatResolver containerFormatResolver;
    private Map<String, Format> puidFormatMap;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    // Certain file types (possibly really large file), we only want to examine the beginning of the file.
    private long bytesToRead = -1;
    private List<String> fileExtensions; // file extensions for files on which to apply file read limit
    private File file; // input file that is being processed


    /**
     * Create a DroidQuery object. This can be retained for any number of
     * different queries.
     *
     * @param sigIdentifier                 BinarySignatureIdentifier  for a Droid signature file
     * @param containerIdentifierFactory    container identifier
     * @param containerFormatResolver       container format resolver
     * @param puidFormatMap                 map of puids to formats
     * @param containerSignatureDefinitions container sig definitions
     * @param includeExts                   File extensions to include for possibly limiting number of bytes to read of file to process.
     * @param kbReadLimit                   Number of bytes to process in KB from the beginning of the file. -1 indicates read entire file.
     * @param file                          The file to be processed by DROID.
     * @throws SignatureParseException If there is a problem processing the DROID signature file.
     */
    public DroidQuery(BinarySignatureIdentifier sigIdentifier,
                      ContainerIdentifierFactory containerIdentifierFactory,
                      ArchiveFormatResolver containerFormatResolver,
                      Map<String, Format> puidFormatMap,
                      ContainerSignatureDefinitions containerSignatureDefinitions,
                      List<String> includeExts,
                      long kbReadLimit, File file) throws SignatureParseException, FileNotFoundException {
        this.sigIdentifier = sigIdentifier;
        this.containerIdentifierFactory = containerIdentifierFactory;
        this.containerFormatResolver = containerFormatResolver;
        this.puidFormatMap = puidFormatMap;
        this.containerSignatureDefinitions = containerSignatureDefinitions;
        this.fileExtensions = includeExts;
        if (kbReadLimit > 0) {
            this.bytesToRead = (kbReadLimit * 1024) - 1;
        }
        this.file = file;
    }

    /**
     * Process the file by DROID.
     *
     * @return A collection of results from DROID. Usually a single result.
     * @throws IOException
     */
    IdentificationResultCollection queryFile()
            throws IOException {

        // For certain file types, set max. number of bytes at beginning of file to process.
        // See https://groups.google.com/forum/#!msg/droid-list/HqN6lKOATJk/i-qTEI-XEwAJ;context-place=forum/droid-list
        // which indicates minimum number of bytes required to identify certain input file types.    	
        long bytesToExamine = file.length();
        String filename = file.getName();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && filename.length() > lastDot) {
            String fileExtension = filename.substring(++lastDot).toLowerCase(); // examine extension past the last dot
            if (fileExtensions != null && fileExtensions.contains(fileExtension) && bytesToRead > 0) {
                bytesToExamine = Math.min(file.length(), bytesToRead);
            }
        }
        RequestMetaData metadata = new RequestMetaData(bytesToExamine, file.lastModified(), file.getName());
        RequestIdentifier identifier = new RequestIdentifier(file.toURI());
        FileSystemIdentificationRequest req = null;
        try {
            req = new FileSystemIdentificationRequest(metadata, identifier);
            req.open(file.toPath());

            // This logic is based on https://github.com/digital-preservation/droid/blob/master/droid-results/src/main/java/uk/gov/nationalarchives/droid/submitter/SubmissionGateway.java

            IdentificationResultCollection results = sigIdentifier.matchBinarySignatures(req);
            IdentificationResultCollection containerResults = handleContainer(req, results);

            if (containerResults != null) {
                results = containerResults;
            }

            sigIdentifier.removeLowerPriorityHits(results);
            if (results.getResults().isEmpty()) {
                results = sigIdentifier.matchExtensions(req, false);
            }

            return results;
        } finally {
            if (req != null) {
                req.close();
            }
        }
    }

    private IdentificationResultCollection handleContainer(IdentificationRequest request,
                                                           IdentificationResultCollection results) throws IOException {
        String containerFormat = getContainerFormat(results);

        if (containerFormat != null) {
            ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(containerFormat);
            IdentificationResultCollection containerResults = containerIdentifier.submit(request);
            sigIdentifier.removeLowerPriorityHits(containerResults);

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

            return containerResults.getResults().isEmpty() ? null : containerResults;
        }

        return null;
    }

    private String getContainerFormat(IdentificationResultCollection results) {
        for (IdentificationResult result : results.getResults()) {
            final String format = containerFormatResolver.forPuid(result.getPuid());
            if (format != null) {
                return format;
            }
        }

        return null;
    }

    /**
     * Provides additional results from DROID for processing ZIP files.
     *
     * @param results This is the same value returned from the call to queryFile().
     * @return Aggregated data of all files contained within the ZIP file.
     * @throws IOException       If the file cannot be read.
     * @throws FitsToolException If the file is not a ZIP file.
     */
    ContainerAggregator queryContainerData(IdentificationResultCollection results) throws IOException, FitsToolException {

        RequestMetaData metadata = new RequestMetaData(bytesToRead, file.lastModified(), file.getName());
        RequestIdentifier identifier = new RequestIdentifier(file.toURI());
        FileSystemIdentificationRequest request = null;
        request = new FileSystemIdentificationRequest(metadata, identifier);
        request.open(file.toPath());

        ZipArchiveContentIdentifier zipArchiveIdentifier =
                new ZipArchiveContentIdentifier(this.sigIdentifier,
                        containerSignatureDefinitions, "", File.separator, File.separator, puidFormatMap);
        try {
            ContainerAggregator aggregator = zipArchiveIdentifier.identify(results.getUri(), request);
            return aggregator;
        } catch (CommandExecutionException e) {
            throw new FitsToolException("DROID can't execute zipArchiveIdentifier", e);
        } finally {
            if (request != null) {
                request.close();
            }
        }
    }
}
