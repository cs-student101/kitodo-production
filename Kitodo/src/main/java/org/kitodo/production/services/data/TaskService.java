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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.kitodo.api.command.CommandResult;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.dto.UserDTO;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.CommandService;
import org.kitodo.production.services.data.base.TitleSearchService;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.primefaces.model.SortOrder;

/**
 * The class provides a service for tasks. The service can be used to perform
 * functions on the task because the task itself is a database bean and
 * therefore may not include functionality.
 */
public class TaskService extends TitleSearchService<Task, TaskDTO, TaskDAO> {

    private static final Logger logger = LogManager.getLogger(TaskService.class);
    private static TaskService instance = null;
    private boolean onlyOpenTasks = false;
    private boolean onlyOwnTasks = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TaskService() {
        super(new TaskDAO(), new TaskType(), new Indexer<>(Task.class), new Searcher(Task.class));
    }

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static TaskService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (TaskService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new TaskService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates and returns a query to retrieve tasks for which the currently
     * logged in user is eligible.
     *
     * @return query to retrieve tasks for which the user eligible.
     */
    private BoolQueryBuilder createUserTaskQuery() throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();

        Set<Integer> processingStatuses = new HashSet<>();
        processingStatuses.add(TaskStatus.OPEN.getValue());
        processingStatuses.add(TaskStatus.INWORK.getValue());

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForProcessingStatus(processingStatuses));
        query.must(createSimpleQuery(TaskTypeField.TEMPLATE_ID.getKey(), 0, true));

        if (onlyOpenTasks) {
            query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), TaskStatus.OPEN.getValue(), true));
        }

        if (onlyOwnTasks) {
            query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), user.getId(), true));
        } else {
            BoolQueryBuilder subQuery = new BoolQueryBuilder();
            subQuery.should(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), user.getId(), true));
            for (Role role : user.getRoles()) {
                subQuery.should(createSimpleQuery(TaskTypeField.ROLES + ".id", role.getId(), true));
            }
            query.must(subQuery);
        }

        if (hideCorrectionTasks) {
            query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), 10, true));
        }

        if (!showAutomaticTasks) {
            query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), "false", true));
        }

        List<Map<String, Object>> processes = ServiceManager.getProcessService().findForCurrentSessionClient();
        query.must(createSetQuery(TaskTypeField.PROCESS_ID.getKey(), processes, true));

        return query;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Task");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Task WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(createUserTaskQuery());
    }

    @Override
    public List<Task> getAllNotIndexed() {
        return getByQuery("FROM Task WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Task> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TaskDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(createUserTaskQuery(), getSortBuilder(sortField, sortOrder), first, pageSize, false);
    }

    /**
     * Method saves or removes dependencies with process, users and user's
     * groups related to modified task.
     *
     * @param task
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Task task) throws CustomResponseException, DataException, IOException {
        manageProcessDependenciesForIndex(task);
        manageTemplateDependenciesForIndex(task);
    }

    private void manageProcessDependenciesForIndex(Task task) throws CustomResponseException, DataException, IOException {
        if (task.getIndexAction() == IndexAction.DELETE) {
            Process process = task.getProcess();
            if (Objects.nonNull(process)) {
                process.getTasks().remove(task);
                ServiceManager.getProcessService().saveToIndex(process, false);
            }
        } else {
            Process process = task.getProcess();
            ServiceManager.getProcessService().saveToIndex(process, false);
        }
    }

    private void manageTemplateDependenciesForIndex(Task task) throws CustomResponseException, DataException, IOException {
        if (task.getIndexAction().equals(IndexAction.DELETE)) {
            Template template = task.getTemplate();
            if (Objects.nonNull(template)) {
                template.getTasks().remove(task);
                ServiceManager.getTemplateService().saveToIndex(template, false);
            }
        } else {
            Template template = task.getTemplate();
            ServiceManager.getTemplateService().saveToIndex(template, false);
        }
    }

    /**
     * Replace processing user for given task. Handles add/remove from list of
     * processing tasks.
     *
     * @param task
     *            for which user will be assigned as processing user
     * @param user
     *            which will process given task
     */
    public void replaceProcessingUser(Task task, User user) {
        User currentProcessingUser = task.getProcessingUser();

        if (Objects.isNull(user) && Objects.isNull(currentProcessingUser)) {
            logger.info("do nothing - there is not new nor old user");
        } else if (Objects.isNull(user)) {
            currentProcessingUser.getProcessingTasks().remove(task);
            task.setProcessingUser(null);
        } else if (Objects.isNull(currentProcessingUser)) {
            user.getProcessingTasks().add(task);
            task.setProcessingUser(user);
        } else if (Objects.equals(currentProcessingUser.getId(), user.getId())) {
            logger.info("do nothing - both are the same");
        } else {
            currentProcessingUser.getProcessingTasks().remove(task);
            user.getProcessingTasks().add(task);
            task.setProcessingUser(user);
        }
    }

    /**
     * Find the distinct task titles.
     *
     * @return a list of titles
     */
    public List<String> findTaskTitlesDistinct() throws DataException {
        return findDistinctValues(QueryBuilders.matchAllQuery(), "title.keyword", true);
    }

    /**
     * Get query for processing statuses.
     *
     * @param processingStatus
     *            set of processing statuses as Integer
     * @return query as QueryBuilder
     */
    private QueryBuilder getQueryForProcessingStatus(Set<Integer> processingStatus) {
        return createSetQuery(TaskTypeField.PROCESSING_STATUS.getKey(), processingStatus, true);
    }

    /**
     * Find tasks by id of process.
     *
     * @param id
     *            of process
     * @return list of JSON objects with tasks for specific process id
     */
    List<Map<String, Object>> findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery(TaskTypeField.PROCESS_ID.getKey(), id, true);
        return findDocuments(query);
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @return list of task as JSONObject objects
     */
    List<Map<String, Object>> findByProcessingStatusAndUser(TaskStatus taskStatus, Integer processingUser, SortBuilder sort)
            throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUser, true));
        return findDocuments(query, sort);
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param priority
     *            as Integer
     * @return list of task as JSONObject objects
     */
    private List<Map<String, Object>> findByProcessingStatusUserAndPriority(TaskStatus taskStatus, Integer processingUser,
            Integer priority, SortBuilder sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), priority, true));
        return findDocuments(query, sort);
    }

    /**
     * Find tasks by three parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param typeAutomatic
     *            as boolean
     * @return list of task as JSONObject objects
     */
    private List<Map<String, Object>> findByProcessingStatusUserAndTypeAutomatic(TaskStatus taskStatus, Integer processingUser,
            boolean typeAutomatic, SortBuilder sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), String.valueOf(typeAutomatic), true));
        return findDocuments(query, sort);
    }

    /**
     * Find tasks by four parameters.
     *
     * @param taskStatus
     *            as String
     * @param processingUser
     *            id of processing user
     * @param priority
     *            as Integer
     * @param typeAutomatic
     *            as boolean
     * @return list of task as JSONObject objects
     */
    private List<Map<String, Object>> findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus taskStatus,
            Integer processingUser, Integer priority, boolean typeAutomatic, SortBuilder sort) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_STATUS.getKey(), taskStatus.getValue(), true));
        query.must(createSimpleQuery(TaskTypeField.PROCESSING_USER_ID.getKey(), processingUser, true));
        query.must(createSimpleQuery(TaskTypeField.PRIORITY.getKey(), priority, true));
        query.must(createSimpleQuery(TaskTypeField.TYPE_AUTOMATIC.getKey(), String.valueOf(typeAutomatic), true));
        return findDocuments(query, sort);
    }

    @Override
    public TaskDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(getIdFromJSONObject(jsonObject));
        taskDTO.setTitle(TaskTypeField.TITLE.getStringValue(jsonObject));
        taskDTO.setLocalizedTitle(getLocalizedTitle(taskDTO.getTitle()));
        taskDTO.setPriority(TaskTypeField.PRIORITY.getIntValue(jsonObject));
        taskDTO.setOrdering(TaskTypeField.ORDERING.getIntValue(jsonObject));
        int taskStatus = TaskTypeField.PROCESSING_STATUS.getIntValue(jsonObject);
        taskDTO.setProcessingStatus(TaskStatus.getStatusFromValue(taskStatus));
        taskDTO.setProcessingStatusTitle(Helper.getTranslation(taskDTO.getProcessingStatus().getTitle()));
        int editType = TaskTypeField.EDIT_TYPE.getIntValue(jsonObject);
        taskDTO.setEditType(TaskEditType.getTypeFromValue(editType));
        taskDTO.setEditTypeTitle(Helper.getTranslation(taskDTO.getEditType().getTitle()));
        taskDTO.setProcessingTime(TaskTypeField.PROCESSING_TIME.getStringValue(jsonObject));
        taskDTO.setProcessingBegin(TaskTypeField.PROCESSING_BEGIN.getStringValue(jsonObject));
        taskDTO.setProcessingEnd(TaskTypeField.PROCESSING_END.getStringValue(jsonObject));
        taskDTO.setTypeAutomatic(TaskTypeField.TYPE_AUTOMATIC.getBooleanValue(jsonObject));
        taskDTO.setTypeMetadata(TaskTypeField.TYPE_METADATA.getBooleanValue(jsonObject));
        taskDTO.setTypeImagesWrite(TaskTypeField.TYPE_IMAGES_WRITE.getBooleanValue(jsonObject));
        taskDTO.setTypeImagesRead(TaskTypeField.TYPE_IMAGES_READ.getBooleanValue(jsonObject));
        taskDTO.setBatchStep(TaskTypeField.BATCH_STEP.getBooleanValue(jsonObject));
        taskDTO.setRolesSize(TaskTypeField.ROLES.getSizeOfProperty(jsonObject));

        /*
         * We read the list of the process but not the list of templates, because only process tasks
         * are displayed in the task list and reading the template list would result in
         * never-ending loops as the list of templates reads the list of tasks.
         */
        int process = TaskTypeField.PROCESS_ID.getIntValue(jsonObject);
        if (process > 0) {
            taskDTO.setProcess(ServiceManager.getProcessService().findById(process, true));
            taskDTO.setBatchAvailable(ServiceManager.getProcessService()
                    .isProcessAssignedToOnlyOneLogisticBatch(taskDTO.getProcess().getBatches()));
        }

        int processingUser = TaskTypeField.PROCESSING_USER_ID.getIntValue(jsonObject);
        if (processingUser > 0) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(processingUser);
            userDTO.setLogin(TaskTypeField.PROCESSING_USER_LOGIN.getStringValue(jsonObject));
            userDTO.setName(TaskTypeField.PROCESSING_USER_NAME.getStringValue(jsonObject));
            userDTO.setSurname(TaskTypeField.PROCESSING_USER_SURNAME.getStringValue(jsonObject));
            userDTO.setFullName(ServiceManager.getUserService().getFullName(userDTO));
            taskDTO.setProcessingUser(userDTO);
        }
        return taskDTO;
    }

    /**
     * Convert date of processing begin to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingBeginAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingBegin());
    }

    /**
     * Convert date of processing end to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingEndAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingEnd());
    }

    /**
     * Convert date of processing day to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingTimeAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingTime());
    }

    /**
     * Get localized (translated) title of task.
     *
     * @param title
     *            as String
     * @return localized title
     */
    public String getLocalizedTitle(String title) {
        return Helper.getTranslation(title);
    }

    /**
     * Get normalized title of task.
     *
     * @param title
     *            as String
     * @return normalized title
     */
    public String getNormalizedTitle(String title) {
        return title.replace(" ", "_");
    }

    /**
     * Get project(s). If the task belongs to a template, the projects are in
     * the template. If the task belongs to a process, the project is in the
     * process.
     *
     * @return value of project(s)
     */
    public static List<Project> getProjects(Task task) {
        Process process = task.getProcess();
        Template template = task.getTemplate();
        if (Objects.nonNull(process)) {
            return Arrays.asList(process.getProject());
        } else if (Objects.nonNull(template)) {
            return template.getProjects();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get roles list size.
     *
     * @param task
     *            object
     * @return size of roles assigned to task
     */
    public int getRolesSize(Task task) {
        return task.getRoles().size();
    }

    /**
     * Get title with user.
     *
     * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit
     *         vollständigem Namen
     */
    public String getTitleWithUserName(Task task) {
        String result = task.getTitle();
        User user = task.getProcessingUser();
        if (Objects.nonNull(user) && Objects.nonNull(user.getId())) {
            result += " (" + ServiceManager.getUserService().getFullName(user) + ")";
        }
        return result;
    }

    public String getProcessingStatusAsString(Task task) {
        return String.valueOf(task.getProcessingStatus().intValue());
    }

    public Integer setProcessingStatusAsString(String inputProcessingStatus) {
        return Integer.parseInt(inputProcessingStatus);
    }

    /**
     * Get script path.
     *
     * @param task
     *            object
     * @return script path as String
     */
    public String getScriptPath(Task task) {
        if (Objects.nonNull(task.getScriptPath()) && !task.getScriptPath().isEmpty()) {
            return task.getScriptPath();
        }
        return "";
    }

    /**
     * Execute script for task.
     *
     * @param task
     *            object
     * @param script
     *            String
     * @param automatic
     *            boolean
     * @return int
     */
    public boolean executeScript(Task task, String script, boolean automatic) throws DataException {
        if (Objects.isNull(script) || script.isEmpty()) {
            return false;
        }
        script = script.replace("{", "(").replace("}", ")");
        LegacyMetsModsDigitalDocumentHelper dd = null;
        Process po = task.getProcess();

        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(po.getRuleset());

        try {
            dd = ServiceManager.getProcessService()
                    .readMetadataFile(ServiceManager.getFileService().getMetadataFilePath(po), prefs)
                    .getDigitalDocument();
        } catch (IOException e2) {
            logger.error(e2);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, po, task);

        script = replacer.replace(script);
        boolean executedSuccessful = false;
        try {
            logger.info("Calling the shell: {}", script);

            CommandService commandService = ServiceManager.getCommandService();
            CommandResult commandResult = commandService.runCommand(script);
            executedSuccessful = commandResult.isSuccessful();
            finishOrReturnAutomaticTask(task, automatic, commandResult.isSuccessful());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return executedSuccessful;
    }

    /**
     * Execute all scripts for step.
     *
     * @param task
     *            StepObject
     * @param automatic
     *            boolean
     */
    public void executeScript(Task task, boolean automatic) throws DataException {
        String script = task.getScriptPath();
        boolean scriptFinishedSuccessful = true;
        logger.debug("starting script {}", script);
        if (Objects.nonNull(script) && !script.trim().isEmpty()) {
            scriptFinishedSuccessful = executeScript(task, script, automatic);
        }
        if (!scriptFinishedSuccessful) {
            abortTask(task);
        }
    }

    /**
     * Make the necessary changes when performing an automatic task.
     * 
     * @param task
     *            ongoing task
     * @param automatic
     *            if it is an automatic task
     * @param successful
     *            if the processing was successful
     * @throws DataException
     *             if the task cannot be saved
     * @throws IOException
     *             if the task cannot be closed
     */
    private void finishOrReturnAutomaticTask(Task task, boolean automatic, boolean successful)
            throws DataException, IOException {
        if (automatic) {
            task.setEditType(TaskEditType.AUTOMATIC.getValue());
            if (successful) {
                task.setProcessingStatus(TaskStatus.DONE.getValue());
                ServiceManager.getWorkflowControllerService().close(task);
            } else {
                task.setProcessingStatus(TaskStatus.OPEN.getValue());
                save(task);
            }
        }
    }

    private void abortTask(Task task) throws DataException {
        task.setProcessingStatus(TaskStatus.OPEN.getValue());
        task.setEditType(TaskEditType.AUTOMATIC.getValue());
        save(task);
    }

    /**
     * Performs creating images when this happens automatically in a task.
     *
     * @param executingThread
     *            Executing thread (displayed in the taskmanager)
     * @param task
     *            Task that generates images
     * @param automatic
     *            Whether it is an automatic task
     * @throws DataException
     *             if the task cannot be saved
     */
    public void generateImages(EmptyTask executingThread, Task task, boolean automatic) throws DataException {
        try {
            Process process = task.getProcess();
            Subfolder sourceFolder = new Subfolder(process, process.getProject().getGeneratorSource());
            List<Subfolder> foldersToGenerate = SubfolderFactoryService.createAll(process, task.getContentFolders());
            ImageGenerator generator = new ImageGenerator(sourceFolder, GenerationMode.ALL, foldersToGenerate);
            generator.setSupervisor(executingThread);
            generator.run();
            finishOrReturnAutomaticTask(task, automatic, Objects.isNull(executingThread.getException()));
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Execute DMS export.
     *
     * @param task
     *            as Task object
     */
    public void executeDmsExport(Task task) throws DataException {
        boolean automaticExportWithImages = ConfigCore
                .getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITH_IMAGES);
        boolean automaticExportWithOcr = ConfigCore
                .getBooleanParameterOrDefaultValue(ParameterCore.AUTOMATIC_EXPORT_WITH_OCR);
        Process process = task.getProcess();
        try {
            boolean validate = ServiceManager.getProcessService().startDmsExport(process, automaticExportWithImages,
                automaticExportWithOcr);
            if (validate) {
                ServiceManager.getWorkflowControllerService().close(task);
            } else {
                abortTask(task);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            abortTask(task);
        }
    }

    /**
     * Set shown only open tasks.
     *
     * @param onlyOpenTasks
     *            as boolean
     */
    public void setOnlyOpenTasks(boolean onlyOpenTasks) {
        this.onlyOpenTasks = onlyOpenTasks;
    }

    /**
     * Set shown only tasks owned by currently logged user.
     *
     * @param onlyOwnTasks
     *            as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        this.onlyOwnTasks = onlyOwnTasks;
    }

    /**
     * Set hide correction tasks.
     *
     * @param hideCorrectionTasks
     *            as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        this.hideCorrectionTasks = hideCorrectionTasks;
    }

    /**
     * Set show automatic tasks.
     *
     * @param showAutomaticTasks
     *            as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        this.showAutomaticTasks = showAutomaticTasks;
    }

    /**
     * Find open tasks for current user sorted according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     *
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenTasksForCurrentUser(SortBuilder sort) throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        List<Map<String,Object>> results = findByProcessingStatusAndUser(TaskStatus.INWORK, user.getId(), sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open tasks without correction for current user sorted according to
     * sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenTasksWithoutCorrectionForCurrentUser(SortBuilder sort) throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        List<Map<String, Object>> results = findByProcessingStatusUserAndPriority(TaskStatus.INWORK, user.getId(), 10, sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open not automatic tasks for current user sorted according to sort
     * query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenNotAutomaticTasksForCurrentUser(SortBuilder sort) throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        List<Map<String, Object>> results = findByProcessingStatusUserAndTypeAutomatic(TaskStatus.INWORK, user.getId(), false,
            sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Find open not automatic tasks without correction for current user sorted
     * according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of tasks as TaskDTO objects
     */
    public List<TaskDTO> findOpenNotAutomaticTasksWithoutCorrectionForCurrentUser(SortBuilder sort) throws DataException {
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        List<Map<String, Object>> results = findByProcessingStatusUserPriorityAndTypeAutomatic(TaskStatus.INWORK, user.getId(),
            10, false, sort);
        return convertJSONObjectsToDTOs(results, false);
    }

    /**
     * Get current tasks with exact title for batch with exact id.
     *
     * @param title
     *            of task as String
     * @param batchId
     *            id of batch as Integer
     * @return list of Task objects
     */
    public List<Task> getCurrentTasksOfBatch(String title, Integer batchId) {
        return dao.getCurrentTasksOfBatch(title, batchId);
    }

    /**
     * Get all tasks between two given ordering of tasks for given process id.
     *
     * @param orderingMax
     *            as Integer
     * @param orderingMin
     *            as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getAllTasksInBetween(Integer orderingMax, Integer orderingMin, Integer processId) {
        return dao.getAllTasksInBetween(orderingMax, orderingMin, processId);
    }

    /**
     * Get next tasks for problem solution for given process id.
     *
     * @param ordering
     *            of Task for which it searches next ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getNextTasksForProblemSolution(Integer ordering, Integer processId) {
        return dao.getNextTasksForProblemSolution(ordering, processId);
    }

    /**
     * Get previous tasks for problem solution for given process id.
     *
     * @param ordering
     *            of Task for which it searches previous ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getPreviousTasksForProblemReporting(Integer ordering, Integer processId) {
        return dao.getPreviousTasksForProblemReporting(ordering, processId);
    }

    /**
     * Set up matching error messages for unreachable tasks. Unreachable task is
     * this one which has no roles assigned to itself. Other possibility is that
     * given list is empty. It means that whole workflow is unreachable.
     *
     * @param tasks
     *            list of tasks for check
     */
    public void setUpErrorMessagesForUnreachableTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            Helper.setErrorMessage("noStepsInWorkflow");
        }
        for (Task task : tasks) {
            if (getRolesSize(task) == 0) {
                Helper.setErrorMessage("noUserInStep", new Object[] {task.getTitle() });
            }
        }
    }

    /**
     * The function determines, from projects, the folders whose contents can be
     * generated automatically.
     * 
     * <p>
     * This feature is needed once by the task in the template to determine
     * which folders show buttons in the interface to turn content creation on
     * or off. In addition, the function of the task in the process is required
     * to determine if there is at least one folder to be created in the task,
     * because then action links for generating are displayed, and not
     * otherwise.
     * 
     * <p>
     * To create content automatically, a folder must be defined as the template
     * folder in the project. The templates serve to create the contents in the
     * other folders to be created. Under no circumstances should the contents
     * of the template folder be automatically generated, even if, for example,
     * after a reconfiguration, this is still set as otherwise they would
     * overwrite themselves. Also, contents can not be created in folders where
     * nothing is configured. The folders that are left over can be created.
     * 
     * @param projects
     *            an object stream of projects that may have folders defined
     *            whose contents can be auto-generated
     * @return an object stream of generable folders
     */
    public static Stream<Folder> generatableFoldersFromProjects(Stream<Project> projects) {
        Stream<Project> projectsWithSourceFolder = skipProjectsWithoutSourceFolder(projects);
        Stream<Folder> allowedFolders = dropOwnSourceFolders(projectsWithSourceFolder);
        return removeFoldersThatCannotBeGenerated(allowedFolders);
    }

    /**
     * Only lets projects pass where a source folder is selected.
     * 
     * @param projects
     *            the unpurified stream of projects
     * @return a stream only of projects that define a source to generate images
     */
    private static Stream<Project> skipProjectsWithoutSourceFolder(Stream<Project> projects) {
        return projects.filter(project -> Objects.nonNull(project.getGeneratorSource()));
    }

    /**
     * Drops all folders to generate if they are their own source folder.
     *
     * @param projects
     *            projects whose folders allowed to be generated are to be
     *            determined
     * @return a stream of folders that are allowed to be generated
     */
    private static Stream<Folder> dropOwnSourceFolders(Stream<Project> projects) {
        Stream<Pair<Folder, Folder>> withSources = projects.flatMap(
            project -> project.getFolders().stream().map(folder -> Pair.of(folder, project.getGeneratorSource())));
        Stream<Pair<Folder, Folder>> filteredWithSources = withSources.filter(
            destinationAndSource -> !destinationAndSource.getLeft().equals(destinationAndSource.getRight()));
        return filteredWithSources.map(Pair::getLeft);
    }

    /**
     * Removes all folders to generate which do not have anything to generate
     * configured.
     * 
     * @param folders
     *            a stream of folders
     * @return a stream only of those folders where an image generation module
     *         has been selected
     */
    private static Stream<Folder> removeFoldersThatCannotBeGenerated(Stream<Folder> folders) {
        return folders.filter(folder -> folder.getDerivative().isPresent() || folder.getDpi().isPresent()
                || folder.getImageScale().isPresent() || folder.getImageSize().isPresent());
    }
}
