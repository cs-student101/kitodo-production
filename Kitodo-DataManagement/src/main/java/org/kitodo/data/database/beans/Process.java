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

package org.kitodo.data.database.beans;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.kitodo.data.database.persistence.ProcessDAO;

@XmlAccessorType(XmlAccessType.NONE)
// This annotation is to instruct the Jersey API not to generate arbitrary XML
// elements. Further XML elements can be
// added as needed by annotating with @XmlElement, but their respective names
// should be wisely chosen according to
// the Coding Guidelines (e.g. *english* names).
@Entity
@Table(name = "process")
public class Process extends BaseTemplateBean {

    private static final long serialVersionUID = -6503348094655786275L;

    @Column(name = "sortHelperImages")
    private Integer sortHelperImages;

    @Column(name = "sortHelperArticles")
    private Integer sortHelperArticles;

    @Column(name = "sortHelperMetadata")
    private Integer sortHelperMetadata;

    @Column(name = "sortHelperDocstructs")
    private Integer sortHelperDocstructs;

    @Column(name = "wikiField", columnDefinition = "longtext")
    private String wikiField = "";

    @Column(name = "processBaseUri")
    private URI processBaseUri;

    @ManyToOne
    @JoinColumn(name = "docket_id", foreignKey = @ForeignKey(name = "FK_process_docket_id"))
    private Docket docket;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_process_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "ruleset_id", foreignKey = @ForeignKey(name = "FK_process_ruleset_id"))
    private Ruleset ruleset;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_process_template_id"))
    private Template template;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "process_x_property", joinColumns = {
            @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_process_x_property_process_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_process_x_property_property_id")) })
    private List<Property> properties;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "template_x_property", joinColumns = {
            @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_template_x_property_process_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_template_x_property_property_id")) })
    private List<Property> templates;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "workpiece_x_property", joinColumns = {
            @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_workpiece_x_property_process_id")) },
                inverseJoinColumns = {
            @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_workpiece_x_property_property_id")) })
    private List<Property> workpieces;

    @ManyToMany(mappedBy = "processes")
    private List<Batch> batches = new ArrayList<>();

    @Transient
    private User blockedUser;

    @Transient
    private long blockedMinutes;

    @Transient
    private long blockedSeconds;

    /**
     * Constructor.
     */
    public Process() {
        this.title = "";
        this.properties = new ArrayList<>();
        this.workpieces = new ArrayList<>();
        this.templates = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.inChoiceListShown = false;
        this.creationDate = new Date();
    }

    /**
     * Get sorting helper for images.
     *
     * @return sorting helper as Integer, in case of null it returns 0
     */
    public Integer getSortHelperImages() {
        if (this.sortHelperImages == null) {
            this.sortHelperImages = 0;
        }
        return this.sortHelperImages;
    }

    public void setSortHelperImages(Integer sortHelperImages) {
        this.sortHelperImages = sortHelperImages;
    }

    /**
     * Get sorting helper for articles.
     *
     * @return sorting helper as Integer, in case of null it returns 0
     */
    public Integer getSortHelperArticles() {
        if (this.sortHelperArticles == null) {
            this.sortHelperArticles = 0;
        }
        return this.sortHelperArticles;
    }

    public void setSortHelperArticles(Integer sortHelperArticles) {
        this.sortHelperArticles = sortHelperArticles;
    }

    /**
     * Get sorting helper for document structure.
     *
     * @return sorting helper as Integer, in case of null it returns 0
     */
    public Integer getSortHelperDocstructs() {
        if (this.sortHelperDocstructs == null) {
            this.sortHelperDocstructs = 0;
        }
        return this.sortHelperDocstructs;
    }

    public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
        this.sortHelperDocstructs = sortHelperDocstructs;
    }

    /**
     * Get sorting helper for metadata.
     *
     * @return sorting helper as Integer, in case of null it returns 0
     */
    public Integer getSortHelperMetadata() {
        if (this.sortHelperMetadata == null) {
            this.sortHelperMetadata = 0;
        }
        return this.sortHelperMetadata;
    }

    public void setSortHelperMetadata(Integer sortHelperMetadata) {
        this.sortHelperMetadata = sortHelperMetadata;
    }

    /**
     * Get wikiField.
     *
     * @return value of wikiField
     */
    public String getWikiField() {
        return this.wikiField;
    }

    /**
     * Set wikiField.
     *
     * @param wikiField as java.lang.String
     */
    public void setWikiField(String wikiField) {
        this.wikiField = wikiField;
    }

    /**
     * Gets the process base URI.
     */
    public URI getProcessBaseUri() {
        return processBaseUri;
    }

    /**
     * Sets the process base URI.
     *
     * @param processBaseUri
     *            the given process base URI
     */
    public void setProcessBaseUri(URI processBaseUri) {
        this.processBaseUri = processBaseUri;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Ruleset getRuleset() {
        return this.ruleset;
    }

    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    public Docket getDocket() {
        return docket;
    }

    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set template.
     *
     * @param template as Template object
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Get list of task.
     *
     * @return list of Task objects or empty list
     */
    public List<Task> getTasks() {
        initialize(new ProcessDAO(), this.tasks);
        if (Objects.isNull(this.tasks)) {
            this.tasks = new ArrayList<>();
        }
        return this.tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Get list of templates.
     *
     * @return list of Property objects or empty list
     */
    public List<Property> getTemplates() {
        initialize(new ProcessDAO(), this.templates);
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        return this.templates;

    }

    /**
     * Set list of templates.
     *
     * @param templates
     *            as list of Property objects
     */
    public void setTemplates(List<Property> templates) {
        this.templates = templates;
    }

    /**
     * Get list of workpieces.
     *
     * @return list of Property objects or empty list
     */
    public List<Property> getWorkpieces() {
        initialize(new ProcessDAO(), this.workpieces);
        if (Objects.isNull(this.workpieces)) {
            this.workpieces = new ArrayList<>();
        }
        return this.workpieces;
    }

    /**
     * Set list of workpieces.
     *
     * @param workpieces
     *            as list of Property objects
     */
    public void setWorkpieces(List<Property> workpieces) {
        this.workpieces = workpieces;
    }

    /**
     * Get list of batches or empty list.
     *
     * @return list of batches or empty list
     */
    public List<Batch> getBatches() {
        initialize(new ProcessDAO(), this.batches);
        if (Objects.isNull(this.batches)) {
            this.batches = new ArrayList<>();
        }
        return this.batches;
    }

    /**
     * Set batches, if list is empty just set, if not first clear and next set.
     *
     * @param batches
     *            list
     */
    public void setBatches(List<Batch> batches) {
        if (this.batches == null) {
            this.batches = batches;
        } else {
            this.batches.clear();
            this.batches.addAll(batches);
        }
    }

    /**
     * Get list of properties.
     *
     * @return list of Property objects or empty list
     */
    public List<Property> getProperties() {
        initialize(new ProcessDAO(), this.properties);
        if (Objects.isNull(this.properties)) {
            this.properties = new ArrayList<>();
        }
        return this.properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Get blocked user.
     *
     * @return User object if this user is blocked
     */
    public User getBlockedUser() {
        return blockedUser;
    }

    /**
     * Set blocked user.
     *
     * @param blockedUser
     *            User object
     */
    public void setBlockedUser(User blockedUser) {
        this.blockedUser = blockedUser;
    }

    /**
     * Get blocked minutes.
     *
     * @return blocked minutes as long
     */
    public long getBlockedMinutes() {
        return blockedMinutes;
    }

    /**
     * Set blocked minutes.
     *
     * @param blockedMinutes
     *            as long
     */
    public void setBlockedMinutes(long blockedMinutes) {
        this.blockedMinutes = blockedMinutes;
    }

    /**
     * Get blocked seconds.
     *
     * @return blocked seconds as long
     */
    public long getBlockedSeconds() {
        return blockedSeconds;
    }

    /**
     * Set blocked seconds.
     *
     * @param blockedSeconds
     *            as long
     */
    public void setBlockedSeconds(long blockedSeconds) {
        this.blockedSeconds = blockedSeconds;
    }

    /**
     * Determines whether or not two processes are equal. Two instances of
     * {@code Process} are equal if the values of their {@code Id}, {@code Title},
     * {@code OutputName} and {@code CreationDate} member fields are the same.
     *
     * @param object
     *            An object to be compared with this {@code Process}.
     * @return {@code true} if the object to be compared is an instance of
     *         {@code Process} and has the same values; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Process) {
            Process process = (Process) object;
            return Objects.equals(this.getId(), process.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortHelperImages, sortHelperArticles, sortHelperMetadata, sortHelperDocstructs,
            processBaseUri, docket, project, ruleset, template, tasks, properties, templates, workpieces);
    }
}
