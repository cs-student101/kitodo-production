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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;

/**
 * Connects a legacy doc struct type from the logical map to a division view.
 * This is a soldering class to keep legacy code operational which is about to
 * be removed. Do not use this class.
 */
public class LegacyLogicalDocStructTypeHelper {
    private static final Logger logger = LogManager.getLogger(LegacyLogicalDocStructTypeHelper.class);

    /**
     * The division view accessed via this soldering class.
     */
    private StructuralElementViewInterface divisionView;

    @Deprecated
    public LegacyLogicalDocStructTypeHelper(StructuralElementViewInterface divisionView) {
        this.divisionView = divisionView;
    }

    @Deprecated
    public List<String> getAllAllowedDocStructTypes() {
        return new ArrayList<>(divisionView.getAllowedSubstructuralElements().keySet());
    }

    @Deprecated
    public List<LegacyMetadataTypeHelper> getAllMetadataTypes() {
        //TODO remove
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Deprecated
    public String getAnchorClass() {
        return null;
    }

    @Deprecated
    public String getName() {
        return divisionView.getId();
    }

    @Deprecated
    public String getNameByLanguage(String language) {
        return divisionView.getLabel();
    }

    @Deprecated
    public String getNumberOfMetadataType(LegacyMetadataTypeHelper metadataType) {
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
