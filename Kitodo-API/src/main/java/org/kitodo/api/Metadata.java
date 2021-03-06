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

package org.kitodo.api;

public class Metadata {
    /**
     * In which conceptual area in the METS file this meta-data entry is stored.
     */
    private MdSec domain;

    // The key of the metadata.
    private String key;

    /**
     * Returns the domain of the meta-data.
     *
     * @return the location of the meta-data entry
     */
    public MdSec getDomain() {
        return domain;
    }

    /**
     * Get the key o the metadata.
     *
     * @return The key of the metadata.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the domain of the meta-data.
     *
     * @param domain
     *            location to set for the meta-data entry
     */
    public void setDomain(MdSec domain) {
        this.domain = domain;
    }

    /**
     * Set the key of the metadata.
     *
     * @param key
     *            The key value of the metadata.
     */
    public void setKey(String key) {
        this.key = key;
    }
}
