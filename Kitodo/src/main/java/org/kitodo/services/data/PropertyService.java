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

package org.kitodo.services.data;

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.List;

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.PropertyDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.PropertyType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.TitleSearchService;

public class PropertyService extends TitleSearchService {

    private PropertyDAO propertyDao = new PropertyDAO();
    private PropertyType propertyType = new PropertyType();
    private Indexer<Property, PropertyType> indexer = new Indexer<>(Property.class);

    /**
     * Constructor with searcher's assigning.
     */
    public PropertyService() {
        super(new Searcher(Property.class));
    }

    /**
     * Save to database and index.
     * 
     * @param property
     *            object
     */
    public void save(Property property) throws CustomResponseException, DAOException, IOException {
        propertyDao.save(property);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(property, propertyType);
    }

    /**
     * Find in database.
     * 
     * @param id
     *            as Integer
     * @return Property
     */
    public Property find(Integer id) throws DAOException {
        return propertyDao.find(id);
    }

    /**
     * Find all properties in database.
     * 
     * @return list of all properties
     */
    public List<Property> findAll() throws DAOException {
        return propertyDao.findAll();
    }

    /**
     * Search by query in database.
     * 
     * @param query
     *            as String
     * @return list of properties
     */
    public List<Property> search(String query) throws DAOException {
        return propertyDao.search(query);
    }

    /**
     * Remove property from database.
     * 
     * @param property
     *            to be removed
     */
    public void remove(Property property) throws DAOException {
        propertyDao.remove(property);
    }

    /**
     * Get normalized title.
     * 
     * @param property
     *            object
     * @return normalized title
     */
    public String getNormalizedTitle(Property property) {
        return property.getTitle().replace(" ", "_").trim();
    }

    /**
     * Get normalized value.
     * 
     * @param property
     *            object
     * @return normalized value
     */
    public String getNormalizedValue(Property property) {
        return property.getValue().replace(" ", "_").trim();
    }
}