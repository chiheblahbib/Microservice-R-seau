package picosoft.biz.arcep.Workflow.controller;

import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.http.ResponseEntity;
import picosoft.biz.arcep.Workflow.DTO.HistoricWF;
import picosoft.biz.arcep.Workflow.service.WorkflowService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping(path = "/api/workflow")
public class WorkflowRessource {

    @Autowired
    WorkflowService workflowService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected TaskService taskService;


    /**
     *
     * @param processInstanceId
     *     A MetaData of {@link String} : process instance id.
     * @return
     *     A respnse of {@link String} : return list of decisions
     */
    @RequestMapping(value = "/gatewayDecision", method = RequestMethod.GET)
    public List gatewayDecision(@RequestParam("processInstanceId") String processInstanceId) {
        return workflowService.getGatewayDecision(processInstanceId);
    }

    @RequestMapping(value = "/getGatewayDecisionByProcessKey", method = RequestMethod.GET)
    public List gatewayDecisionTest(@RequestParam("processKey") String processKey) throws FileNotFoundException {
        return workflowService.getGatewayDecisionByKey(processKey);
    }

    @RequestMapping(value = "/getInput", method = RequestMethod.GET)
    public Object getInput(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("name") String name, @RequestParam("type") String type) throws FileNotFoundException {
        return workflowService.getInput(processInstanceId, name, type);
    }

    /**
     * @param processInstanceId A MetaData of {@link String} : process instance id.
     * @return A respnse of {@link String} : return list of instance historics
     */
    @RequestMapping(value="/historicProcess", method= RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    public List<HistoricWF> historicProcess(@RequestParam("processInstanceId") String processInstanceId) {
        return workflowService.getHistoricProcess(processInstanceId);
    }

    @RequestMapping(value="/getActiveTaskInstance", method= RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    public String getActiveTaskInstance(@RequestParam("processInstanceId") String processInstanceId) {
        return workflowService.getActifTaskOfProcessInstance(processInstanceId).getName();
    }

    @PatchMapping(value = "/instance/{processInstance}/json")
    public List<Object> getInstanceEditorJson(@PathVariable String processInstance) throws Exception {
        return workflowService.getInstanceEditorJson(processInstance);
    }

    @PatchMapping(value = "/definition/{processDefinition}/json")
    public List<Object> getDefinitionEditorJson(@PathVariable String processDefinition) throws Exception {
        return workflowService.getDefinitionEditorJson(processDefinition);
    }

    @GetMapping("/initalizeProxy")
    public Object initalizeProxy(@RequestBody Object o) {
        return workflowService.initalizeProxy(o);
    }



    @RequestMapping(value = "/getFirstActivityNameByProcessKey", method = RequestMethod.GET)
    public String getFirstActivityNameWithoutProcessInstance(@RequestParam("processName") String processKey) throws FileNotFoundException {
        return workflowService.getFirstActivityNameByKey(processKey);
    }

    @GetMapping(value = "/getCandidateGroups")
    public List getCandidateGroups(@RequestParam("taskId") String taskId) throws FileNotFoundException, ParseException {
        return workflowService._getCandidateGroups(taskId);
    }

    @GetMapping(value = "/getCandidateUsers")
    public List getCandidateUsers(@RequestParam("taskId") String taskId) throws FileNotFoundException, ParseException {
        return workflowService._getCandidateUsers(taskId);
    }

    @GetMapping(value = "/getCandidates")
    public JSONObject getCandidates(@RequestParam("taskId") String taskId) throws FileNotFoundException, ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("authors", workflowService._getCandidateGroups(taskId)) ;
        jsonObject.put("readers", workflowService._getCandidateUsers(taskId)) ;

        return jsonObject;
    }

    @GetMapping("/process/{processInstanceId}/xml")
    public ResponseEntity<String> getProcessXml(@PathVariable String processInstanceId) throws IOException {

        HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        ProcessDefinition pd = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionId(pi.getProcessDefinitionId())
                .singleResult();

        InputStream is = repositoryService.getResourceAsStream(
                pd.getDeploymentId(),
                pd.getResourceName()
        );

        return ResponseEntity.ok(new String(is.readAllBytes(), StandardCharsets.UTF_8));
    }

    @GetMapping("/process-instance/{instanceId}/details")
    public Map<String, Object> getInstanceDetails(@PathVariable String instanceId) {

        List<String> completedActivityIds =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(instanceId)
                        .finished()
                        .list()
                        .stream()
                        .map(HistoricActivityInstance::getActivityId)
                        .distinct()
                        .toList();

        List<String> currentActivityIds =
                runtimeService.createExecutionQuery()
                        .processInstanceId(instanceId)
                        .list()
                        .stream()
                        .map(Execution::getActivityId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<HistoricTaskInstance> activeTasks =
                historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(instanceId)
                        .list();

        List<Map<String, Object>> tasks = activeTasks.stream()
                .map(task -> {

                    Map<String, Object> taskMap = new HashMap<>();

                    taskMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
                    taskMap.put("assignee", task.getAssignee());
                    taskMap.put("dateCreated", task.getCreateTime());
                    taskMap.put("dateEnded", task.getEndTime());
                    taskMap.put("dateDue", task.getDueDate());
                    taskMap.put("dateAlive", task.getDurationInMillis());

                    // Candidate Users
                    List<String> candidateUsers =
                            historyService.getHistoricIdentityLinksForTask(task.getId())
                                    .stream()
                                    .filter(link -> "candidate".equals(link.getType()) && link.getUserId() != null)
                                    .map(HistoricIdentityLink::getUserId)
                                    .toList();

                    taskMap.put("candidateUsers", candidateUsers);

                    // Candidate Groups
                    List<String> candidateGroups =
                            historyService.getHistoricIdentityLinksForTask(task.getId())
                                    .stream()
                                    .filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null)
                                    .map(HistoricIdentityLink::getGroupId)
                                    .toList();

                    taskMap.put("candidateGroups", candidateGroups);

                    return taskMap;
                })
                .toList();

        return Map.of(
                "currentActivities", currentActivityIds,
                "completedActivities", completedActivityIds,
                "tasks", tasks
        );

    }



}

