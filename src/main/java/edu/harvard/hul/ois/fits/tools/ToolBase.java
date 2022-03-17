//
// Copyright (c) 2016 by The President and Fellows of Harvard College
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the License at:
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permission and limitations under the License.
//

package edu.harvard.hul.ois.fits.tools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

//import org.apache.xalan.processor.TransformerFactoryImpl;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3c.dom.Node;

import edu.harvard.hul.ois.fits.exceptions.FitsToolException;
import edu.harvard.hul.ois.fits.identity.ToolIdentity;

/**
 * An abstract class implementing the Tool interface, the base
 * for all FITS tools.
 */
public abstract class ToolBase implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(ToolBase.class);

    protected ToolInfo info = null;
    protected ToolOutput output = null;
    protected SAXBuilder saxBuilder;
    protected long duration;
    protected RunStatus runStatus;
    protected Hashtable<String, String> transformMap;
    private TransformerFactory tFactory;
    private File inputFile;
    private String name;

    private List<String> excludedExtensions;
    private List<String> includedExtensions;

    private Throwable caughtThrowable;

    public ToolBase() throws FitsToolException {
        info = new ToolInfo();
        tFactory = TransformerFactory.newInstance();
        String factoryImpl = "net.sf.saxon.TransformerFactoryImpl";
        try {
            Class<?> clazz = Class.forName(factoryImpl, true, ToolBase.class.getClassLoader());
            tFactory = (TransformerFactory) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new FitsToolException("Could not access or instantiate class: " + factoryImpl, e);
        }

        saxBuilder = new SAXBuilder();
        excludedExtensions = new ArrayList<String>();
        includedExtensions = new ArrayList<String>();
    }

    /**
     * Returns the name. This should be the name of the tool class,
     * without the package prefix.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name. This should be the name of the tool class, without
     * the package prefix (e.g., "Droid" or "Jhove").
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Returns identifying information about the tool.
     */
    public ToolInfo getToolInfo() {
        return info;
    }

    /**
     * Returns true if the tool provides identification information.
     * Override if a value other than true should ever be returned.
     */
    public Boolean canIdentify() {
        return true;
    }

    /**
     * Specifies the file to be processed.
     */
    public void setInputFile(File file) {
        inputFile = file;
    }

    public boolean isIdentityKnown(ToolIdentity identity) {
        if (!canIdentify()) {
            return false;
        }
        //identity and mimetype must not be null or empty strings for an identity to be "known"
        if (identity == null
                || identity.getMime() == null
                || identity.getMime().length() == 0
                || identity.getFormat() == null
                || identity.getFormat().length() == 0) {
            return false;
        }
        String format = identity.getFormat();
        String mime = identity.getMime();
        if (format.equals("Unknown Binary") || mime.equals("application/octet-stream")) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean validate(File files_or_dir, ToolIdentity identity) {
        return null;
    }

    public Document transform(String xslt, Document input) throws FitsToolException {
        org.jdom.Document doc = null;
        try {
//			Configuration config = ((TransformerFactoryImpl)tFactory).getConfiguration();
//			DocumentWrapper docw = new DocumentWrapper(input,null,config);
            Templates templates = tFactory.newTemplates(new StreamSource(xslt));
            Transformer transformer = templates.newTransformer();
//			transformer.transform(new DOMSource((Node)input), out);
//			transformer.transform(docw, out);

            JDOMSource in = new JDOMSource(input);
            JDOMResult out = new JDOMResult();
//		       JDOMResult result = new JDOMResult();
            transformer.transform(in, out);
            doc = out.getDocument();


//			doc = out.getDocument();
        } catch (Exception e) {
            throw new FitsToolException(info.getName() + ": Error converting output using " + xslt, e);
        }
        return doc;
    }

    public void addExcludedExtension(String ext) {
        excludedExtensions.add(ext);
    }

    public boolean hasExcludedExtension(String ext) {
        for (String extension : excludedExtensions) {
            if (extension.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    public void addIncludedExtension(String ext) {
        includedExtensions.add(ext);
    }

    public boolean hasIncludedExtensions() {
        if (includedExtensions.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasExcludedExtensions() {
        if (excludedExtensions.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasIncludedExtension(String ext) {
        for (String extension : includedExtensions) {
            if (extension.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Take the list of tools-used items and modify the included and
     * excluded extensions if applicable. included-exts and
     * excluded-exts attributes take priority.
     */
    public void applyToolsUsed(List<ToolBelt.ToolsUsedItem> toolsUsedItems) {
        for (ToolBelt.ToolsUsedItem tui : toolsUsedItems) {
            if (tui.toolNames.contains(name)) {
                // If a tool is
                // listed by tui, we add its extensions to
                // included-extensions unless it's in excluded-extensions.
                // If a tool is not listed by tui, we add its extensions
                // to excluded-extensions unless it's in included-extensions.
                // If there are no included extensions, that means all extensions
                // are allowed unless excluded, so we don't add anything.
                for (String ext : tui.extensions) {
                    if (hasIncludedExtensions()) {
                        if (!containsIgnoreCase(includedExtensions, ext) &&
                                !containsIgnoreCase(excludedExtensions, ext)) {
                            includedExtensions.add(ext);
                        }
                    }
                }
            } else {
                for (String ext : tui.extensions) {
                    if (!containsIgnoreCase(excludedExtensions, ext) &&
                            !containsIgnoreCase(includedExtensions, ext)) {
                        excludedExtensions.add(ext);
                    }
                }
            }
        }
    }

    /* A case-independent surrogate for List.contains */
    private boolean containsIgnoreCase(List<String> lst, String s) {
        for (String s1 : lst) {
            if (s1.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public ToolOutput getOutput() {
        return output;
    }

    public void resetOutput() {
        output = null;
        caughtThrowable = null;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public RunStatus getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(RunStatus runStatus) {
        this.runStatus = runStatus;
    }

    /*
     * Save an exception for reporting.
     */
    private void setCaughtThrowable(Throwable e) {
        caughtThrowable = e;
    }

    public Throwable getCaughtThrowable() {
        return caughtThrowable;
    }

    /**
     * Called to extract metadata when this tool's thread is kicked off.
     * We capture any error (Throwable) so that it can be reported later.
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            output = extractInfo(inputFile);
        } catch (Throwable e) {
            setCaughtThrowable(e);
        }
    }

    public SAXBuilder getSaxBuilder() {
        return saxBuilder;
    }

}
