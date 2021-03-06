/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Connects a legacy doc struct from the physical map to a media unit. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyInnerPhysicalDocStructHelper implements LegacyDocStructHelperInterface {
    private static final Logger logger = LogManager.getLogger(LegacyInnerPhysicalDocStructHelper.class);

    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * A constant for local use. We cannot make this constant constant because
     * the service loader is an instance variable.
     */
    @Deprecated
    public final UseXmlAttributeAccessInterface local = metsService.createUseXmlAttributeAccess();

    {
        local.setUse("LOCAL");
        local.setMimeType("image/tiff");
    }

    /**
     * The media unit accessed via this soldering class.
     */
    private FileXmlElementAccessInterface mediaUnit;

    @Deprecated
    public LegacyInnerPhysicalDocStructHelper() {
        this.mediaUnit = metsService.createFileXmlElementAccess();
    }

    @Deprecated
    public LegacyInnerPhysicalDocStructHelper(FileXmlElementAccessInterface mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    @Deprecated
    public void addChild(LegacyDocStructHelperInterface child) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void addChild(Integer index, LegacyDocStructHelperInterface child) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void addContentFile(LegacyContentFileHelper contentFile) {
        mediaUnit.putFLocatForUse(local, ((LegacyContentFileHelper) contentFile).getMediaFile());
    }

    @Override
    @Deprecated
    public void addMetadata(LegacyMetadataHelper metadata) {
        if (LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER.equals(metadata.getMetadataType())) {
            mediaUnit.setOrder(Integer.parseInt(metadata.getValue()));
        } else if (LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL.equals(metadata.getMetadataType())) {
            mediaUnit.setOrderlabel(metadata.getValue());
        } else {
            logger.log(Level.TRACE, "addMetadata(metadata: {})", metadata);
        }
    }

    @Deprecated
    public LegacyDocStructHelperInterface addMetadata(String metadataType, String value) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyReferenceHelper addReferenceTo(LegacyDocStructHelperInterface docStruct, String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface copy(boolean copyMetaData, Boolean recursive) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface createChild(String docStructType,
            LegacyMetsModsDigitalDocumentHelper digitalDocument,
            LegacyPrefsHelper prefs) {

        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void deleteUnusedPersonsAndMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getAddableMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildrenByTypeAndMetadataType(String docStructType,
            String metaDataType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public List<LegacyContentFileHelper> getAllContentFiles() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public List<LegacyReferenceHelper> getAllFromReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public List<LegacyMetadataHelper> getAllIdentifierMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadata() {
        return Arrays.asList(
            new LegacyMetadataHelper(this, LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER,
                    Integer.toString(mediaUnit.getOrder())),
            new LegacyMetadataHelper(this, LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL,
                    mediaUnit.getOrderlabel()));
    }

    @Override
    @Deprecated
    public List<? extends LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        if (metadataType == LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER) {
            return Objects.nonNull(mediaUnit)
                    ? Arrays.asList(
                        new LegacyMetadataHelper(this, metadataType, Integer.toString(mediaUnit.getOrder())))
                    : Collections.emptyList();
        } else if (metadataType == LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL) {
            return Objects.nonNull(mediaUnit) && Objects.nonNull(mediaUnit.getOrderlabel())
                    ? Arrays.asList(new LegacyMetadataHelper(this, metadataType, mediaUnit.getOrderlabel()))
                    : Collections.emptyList();
        } else {
            // TODO remove
            throw andLog(new UnsupportedOperationException("Not yet implemented"));
        }
    }

    @Override
    @Deprecated
    public List<LegacyReferenceHelper> getAllReferences(String direction) {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public Collection<LegacyReferenceHelper> getAllToReferences(String type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public Object getAllVisibleMetadata() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public String getAnchorClass() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public LegacyDocStructHelperInterface getChild(String type, String identifierField, String identifier) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public String getImageName() {
        FLocatXmlElementAccessInterface uri = this.mediaUnit.getFLocatForUse(local);
        return new File(uri.getUri().getPath()).getName();
    }

    FileXmlElementAccessInterface getMediaUnit() {
        return mediaUnit;
    }

    @Deprecated
    public LegacyDocStructHelperInterface getNextChild(LegacyDocStructHelperInterface predecessor) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyDocStructHelperInterface getParent() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getPossibleMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getDocStructType() {
        return LegacyInnerPhysicalDocStructTypePageHelper.INSTANCE;
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getType() {
        if (!logger.isTraceEnabled()) {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!",
                stackTrace[1].getClassName(), stackTrace[1].getMethodName(), stackTrace[0].getClassName(),
                stackTrace[0].getMethodName());
        }
        return getDocStructType();
    }

    @Deprecated
    public boolean isDocStructTypeAllowedAsChild(LegacyLogicalDocStructTypeHelper type) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void removeChild(LegacyDocStructHelperInterface docStruct) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void removeContentFile(LegacyContentFileHelper contentFile) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void removeMetadata(LegacyMetadataHelper metaDatum) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    @Deprecated
    public void removeReferenceTo(LegacyDocStructHelperInterface target) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));

    }

    @Deprecated
    public void setImageName(String imageName) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public void setType(LegacyLogicalDocStructTypeHelper docStructType) {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        buffer.append("()");
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }
}
