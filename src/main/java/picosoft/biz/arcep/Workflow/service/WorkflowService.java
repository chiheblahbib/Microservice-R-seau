package picosoft.biz.arcep.Workflow.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.flowable.bpmn.model.Process;
import picosoft.biz.arcep.Workflow.DTO.HistoricWF;
import picosoft.biz.arcep.Workflow.domain.BpmJob;
import picosoft.biz.arcep.Workflow.utils.CommandContextUtil;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.*;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.NativeHistoricActivityInstanceQuery;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.NativeTaskQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.history.NativeHistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.history.NativeHistoricVariableInstanceQuery;
import org.flowable.variable.service.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.flowable.variable.service.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.hibernate.proxy.HibernateProxy;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;

@Named("workflowService")
@Service
public class WorkflowService {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Logger log = LoggerFactory.getLogger(WorkflowService.class);


    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;


    @Autowired
    private CurrentUser currentUser;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    ManagementService managementService;

    @Autowired
    private Configuration config;

    public WorkflowService() {

    }

    /**
     *
     * @param currentUserSid
     *     A String of {@link String} : initiator name.
     * @param processKey
     *     A String of {@link String} : process definition key.
     * @param objet
     *     A String of {@link Object} : l'objet principale.
     * @return
     *     A respnse of {@link BpmJob} : Return BpmJob contains Object.
     */
    public BpmJob startProcessInstance(String currentUserSid, String processKey, Object objet) throws Exception {

          // initiate variables
          Map<String, Object> variables = new HashMap<>();

          variables.put("initiator", currentUserSid);

          variables.put("processKey", processKey);

          variables.put("data", objet);

          variables.put("sids", currentUser.getSid());

          variables.put("token", currentUser.getToken());
          variables.put("currentUser", currentUser);
          variables.put("dueDate", ZonedDateTime.now());

          // create new process instance using the process key and list of variables
          ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(variables.get("processKey").toString(), variables);

          // recuperate the first task of instance
          Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().singleResult();
          if (task == null) {
              for (int i = 0; i < 10 && task == null; i++) {
                  try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                  task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().singleResult();
              }
          }

          //DISPLAYNAME OF USER AUTHENTIFIED
          if (task != null) {
              runtimeService.setVariable(task.getProcessInstanceId(), task.getId() + " :authentifier", currentUser.getDisplayName());
          } else {
              runtimeService.setVariable(processInstance.getProcessInstanceId(), "authentifier", currentUser.getDisplayName());
          }

          //DISPLAYNAME OF EFFECTIVE USER
          if(currentUser.getEffectiveUser()!=null && currentUser.getEffectiveUser().getDisplayName()!=null)
              if (task != null) {
                  runtimeService.setVariable(task.getProcessInstanceId(), task.getId() + " :effectiveUser", currentUser.getEffectiveUser().getDisplayName());
              } else {
                  runtimeService.setVariable(processInstance.getProcessInstanceId(), "effectiveUser", currentUser.getEffectiveUser().getDisplayName());
              }

          // recuperate list of authors
          List<String> listAuthors = task != null ? _getCandidateGroups(task.getId()) : new ArrayList<>();

          // recuperate list of readers
          List<String> listReaders = task != null ? _getCandidateUsers(task.getId()) : new ArrayList<>();

          // calculate BpmJob data case instance unfinished
          BpmJob bpmJob = new BpmJob(new JSONObject(), task != null ? task.getName() : null, task != null ? task.getAssignee() : null, false, processInstance.getProcessInstanceId(), null, null);

          bpmJob.setProcessName(processInstance.getProcessDefinitionName());

          bpmJob.setProcessID(processInstance.getProcessInstanceId());

          bpmJob.setDataObject(task != null ? taskService.getVariable(task.getId(), "data") : objet);

          bpmJob.setAuthors(listAuthors);

          bpmJob.setReaders(listReaders);

          return bpmJob;
      }

    /**
     *
     * @param processInstanceId
     *     A String of {@link String} : process instance id.
     * @return
     *     A respnse of {@link ArrayList} : Return instance id.
     */
    public List getGatewayDecision(String processInstanceId) {

        try {
            // recuperate current task
            Task task = getActifTaskOfProcessInstance(processInstanceId);

            // recuperate process model
            Process process = getProcessModel(processInstanceId);

            // extract list of flow sequences from process model
            List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);

            // initialize list of decisions
            List<JSONObject> decisionlist = new ArrayList<>();
            SequenceFlow sequenceFlowTemp = null;
            // iterate and calculate decisions
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                sequenceFlowTemp = sequenceFlow;
                if (sequenceFlow.getSourceRef().equals(task.getTaskDefinitionKey())) {
                    if (sequenceFlow.getTargetFlowElement().getClass().equals(UserTask.class) || sequenceFlow.getTargetFlowElement().getClass().equals(EndEvent.class) || sequenceFlow.getTargetFlowElement().getClass().equals(ServiceTask.class) || sequenceFlow.getTargetFlowElement().getClass().equals(CallActivity.class)) {
                        if (sequenceFlow.getName().indexOf("sys_") == -1) {
                            boolean hidden = false;
                            Integer ordre = 0;
                            String icon = null;
                            String color = null;
                            String decisionMessageConfirmation = null;
                            String decisionComponentAlert = null;

                            for(ExtensionElement extensionElement: sequenceFlow.getExtensionElements().get("properties")) {
                                for(ExtensionElement extensionElementChild: extensionElement.getChildElements().get("property")) {
                                    int i = 0;
                                    if(extensionElementChild.getAttributes().size() > 0)
                                    for(i = 0; i < extensionElementChild.getAttributes().get("name").size(); i++){

                                        if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("hidden")){
                                            String expressionFM = extensionElementChild.getAttributes().get("value").get(i).getValue();

                                            Map<String, Object> variables=taskService.getVariables(task.getId());
                                            variables.put("currentUser", currentUser);
                                            Template t = new Template(null, expressionFM, config);
                                            //result must be true
                                            try {
                                                String result = FreeMarkerTemplateUtils.processTemplateIntoString(t, variables);
                                                hidden=Boolean.parseBoolean(result.trim());
                                            }
                                            catch (Exception e)
                                            {

                                            }
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("ordre")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                ordre = Integer.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("icon")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                icon = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("color")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                color = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionComponentAlert")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                decisionComponentAlert = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }
                                        else if (extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionMessageConfirmation")) { // 🟢 [NOUVEAU]
                                            if (extensionElementChild.getAttributes().get("value").size() > i
                                                    && extensionElementChild.getAttributes().get("value").get(i) != null
                                                    && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                decisionMessageConfirmation = extensionElementChild.getAttributes().get("value").get(i).getValue().trim(); // 🟢 [NOUVEAU]
                                        }

                                    }
                                }
                            }
                            JSONObject json = new JSONObject();
                            json.put("id", sequenceFlow.getId());
                            json.put("decision", sequenceFlow.getName());
                            json.put("decisionDocumentation", sequenceFlow.getDocumentation());
                            json.put("ordre", ordre);
                            json.put("color", color);
                            json.put("icon", icon);
                            json.put("decisionComponentAlert", decisionComponentAlert);
                            if (decisionMessageConfirmation != null) { // 🟢 [NOUVEAU]
                                json.put("decisionMessageConfirmation", decisionMessageConfirmation); // 🟢 [NOUVEAU]
                            }else{
                                json.put("decisionMessageConfirmation", null);
                            }
                            decisionlist.add(json);


                        }
                    } else {
                        Boolean test = false;
                        for (SequenceFlow sequenceFlow2 : sequenceFlows) {
                            if (sequenceFlow2.getSourceRef().equals(sequenceFlow.getTargetFlowElement().getId())) {
                                if (sequenceFlow2.getName().indexOf("sys_") == -1) {
                                    if(sequenceFlow2.getTargetFlowElement().getClass().equals(ExclusiveGateway.class)){
                                        test = true;

                                    }
                                }
                            }
                        }
                        if(test){
                            boolean hidden = false;
                            Integer ordre = 0;
                            String icon = null;
                            String color = null;
                            String decisionMessageConfirmation = null;
                            String decisionComponentAlert = null;

                            for(ExtensionElement extensionElement: sequenceFlow.getExtensionElements().get("properties")) {
                                for(ExtensionElement extensionElementChild: extensionElement.getChildElements().get("property")) {
                                    int i = 0;
                                    if(extensionElementChild.getAttributes().size() > 0)
                                    for(i = 0; i < extensionElementChild.getAttributes().get("name").size(); i++){

                                        if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("hidden")){
                                            String expressionFM = extensionElementChild.getAttributes().get("value").get(i).getValue();

                                            Map<String, Object> variables=taskService.getVariables(task.getId());
                                            variables.put("currentUser", currentUser);
                                            Template t = new Template(null, expressionFM, config);
                                            //result must be true
                                            try {
                                                String result = FreeMarkerTemplateUtils.processTemplateIntoString(t, variables);
                                                hidden=Boolean.parseBoolean(result.trim());
                                            }
                                            catch (Exception e)
                                            {

                                            }
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("ordre")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                ordre = Integer.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("icon")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                icon = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("color")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                color = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionComponentAlert")){
                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                decisionComponentAlert = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                        }
                                        else if (extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionMessageConfirmation")) { // 🟢 [NOUVEAU]
                                            if (extensionElementChild.getAttributes().get("value").size() > i
                                                    && extensionElementChild.getAttributes().get("value").get(i) != null
                                                    && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                decisionMessageConfirmation = extensionElementChild.getAttributes().get("value").get(i).getValue().trim(); // 🟢 [NOUVEAU]
                                        }

                                    }
                                }
                            }
                            if(sequenceFlowTemp.getName() != null) {
                                if (!sequenceFlowTemp.getName().trim().equals("")) {
                                    JSONObject json = new JSONObject();
                                    json.put("id", sequenceFlowTemp.getId());
                                    json.put("decision", sequenceFlowTemp.getName());
                                    json.put("decisionDocumentation", sequenceFlowTemp.getDocumentation());
                                    json.put("ordre", ordre);
                                    json.put("color", color);
                                    json.put("icon", icon);
                                    json.put("decisionComponentAlert", decisionComponentAlert);
                                    if (decisionMessageConfirmation != null) { // 🟢 [NOUVEAU]
                                        json.put("decisionMessageConfirmation", decisionMessageConfirmation); // 🟢 [NOUVEAU]
                                    }else{
                                        json.put("decisionMessageConfirmation", null);
                                    }
                                    return Collections.singletonList(json);
                                }
                            }
                        }
                        for (SequenceFlow sequenceFlow2 : sequenceFlows) {
                            if (sequenceFlow2.getSourceRef().equals(sequenceFlow.getTargetFlowElement().getId())) {
                                if (sequenceFlow2.getName().indexOf("sys_") == -1) {
                                    if(sequenceFlow2.getExtensionElements().size() != 0){
                                        if(sequenceFlow2.getExtensionElements().containsKey("properties")){
                                            boolean hidden = false;
                                            Integer ordre = 0;
                                            String icon = null;
                                            String color = null;
                                            String decisionMessageConfirmation = null;
                                            String decisionComponentAlert = null;

                                            for(ExtensionElement extensionElement: sequenceFlow2.getExtensionElements().get("properties")) {
                                                for(ExtensionElement extensionElementChild: extensionElement.getChildElements().get("property")) {
                                                    int i = 0;
                                                    if(extensionElementChild.getAttributes().size() > 0)
                                                    for(i = 0; i < extensionElementChild.getAttributes().get("name").size(); i++){

                                                        if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("hidden")){
                                                            String expressionFM = extensionElementChild.getAttributes().get("value").get(i).getValue();

                                                            Map<String, Object> variables=taskService.getVariables(task.getId());
                                                            variables.put("currentUser", currentUser);
                                                            Template t = new Template(null, expressionFM, config);
                                                            //result must be true
                                                            try {
                                                                String result = FreeMarkerTemplateUtils.processTemplateIntoString(t, variables);
                                                                hidden=Boolean.parseBoolean(result.trim());
                                                            }
                                                            catch (Exception e)
                                                            {

                                                            }
                                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("ordre")){
                                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                                ordre = Integer.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("icon")){
                                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                                icon = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("color")){
                                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                                color = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                                        }else if(extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionComponentAlert")){
                                                            if(extensionElementChild.getAttributes().get("value").size() > i && extensionElementChild.getAttributes().get("value").get(i) != null && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                                decisionComponentAlert = String.valueOf(extensionElementChild.getAttributes().get("value").get(i).getValue().trim());
                                                        }
                                                        else if (extensionElementChild.getAttributes().get("name").get(i).getValue().equals("decisionMessageConfirmation")) { // 🟢 [NOUVEAU]
                                                            if (extensionElementChild.getAttributes().get("value").size() > i
                                                                    && extensionElementChild.getAttributes().get("value").get(i) != null
                                                                    && extensionElementChild.getAttributes().get("value").get(i).getValue() != null)
                                                                decisionMessageConfirmation = extensionElementChild.getAttributes().get("value").get(i).getValue().trim(); // 🟢 [NOUVEAU]
                                                        }

                                                    }
                                                }
                                            }
                                            if (!hidden) {
                                                JSONObject json = new JSONObject();
                                                json.put("id", sequenceFlow2.getId());
                                                json.put("decision", sequenceFlow2.getName());
                                                json.put("decisionDocumentation", sequenceFlow2.getDocumentation());
                                                json.put("ordre", ordre);
                                                json.put("color", color);
                                                json.put("icon", icon);
                                                json.put("decisionComponentAlert", decisionComponentAlert);
                                                if (decisionMessageConfirmation != null) { // 🟢 [NOUVEAU]
                                                    json.put("decisionMessageConfirmation", decisionMessageConfirmation); // 🟢 [NOUVEAU]
                                                }else{
                                                    json.put("decisionMessageConfirmation", null);
                                                }
                                                decisionlist.add(json);
                                            }
                                        }else{
                                            JSONObject json = new JSONObject();
                                            json.put("id", sequenceFlow2.getId());
                                            json.put("decision", sequenceFlow2.getName());
                                            json.put("decisionDocumentation", sequenceFlow2.getDocumentation());
                                            json.put("ordre", 0);
                                            json.put("color", null);
                                            json.put("icon", null);
                                            json.put("decisionMessageConfirmation", null);
                                            decisionlist.add(json);
                                        }
                                    }else{
                                        JSONObject json = new JSONObject();
                                        json.put("id", sequenceFlow2.getId());
                                        json.put("decision", sequenceFlow2.getName());
                                        json.put("decisionDocumentation", sequenceFlow2.getDocumentation());
                                        json.put("ordre", 0);
                                        json.put("color", null);
                                        json.put("icon", null);
                                        json.put("decisionMessageConfirmation", null);
                                        decisionlist.add(json);
                                    }
                                }
                            }
                        }
                    }

                }
            }

            // return list of decisions
            return decisionlist;

        }catch (Exception e){
            return  new ArrayList();
        }
    }

    public List getGatewayDecisionByKey(String processKey) throws FileNotFoundException {

        List liste = new ArrayList<String>();

        ProcessDefinitionQuery listprocess = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).latestVersion();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(listprocess.list().get(0).getId());

        Process process = bpmnModel.getProcessById(processKey);

        List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);

        List<String> decisionlist = new ArrayList<>();


        for (SequenceFlow sequenceFlow : sequenceFlows) {
            for (SequenceFlow sequenceFlow1 : sequenceFlows) {
                if (sequenceFlow1.getTargetFlowElement().getId().equals(sequenceFlow.getSourceFlowElement().getId()) && sequenceFlow1.getSourceFlowElement().getClass().equals(StartEvent.class)) {
                    if (sequenceFlow.getTargetFlowElement().getClass().equals(UserTask.class) || sequenceFlow.getTargetFlowElement().getClass().equals(EndEvent.class) || sequenceFlow.getTargetFlowElement().getClass().equals(ServiceTask.class)) {
                        if (sequenceFlow.getName().indexOf("sys_") == -1) {
                            decisionlist.add(sequenceFlow.getName());
                        }
                    } else {
                        for (SequenceFlow sequenceFlow2 : sequenceFlows) {
                            if (sequenceFlow2.getSourceRef().equals(sequenceFlow.getTargetFlowElement().getId())) {
                                if (sequenceFlow2.getName().indexOf("sys_") == -1) {
                                    decisionlist.add(sequenceFlow2.getName());
                                }
                            }
                        }
                    }
                }

            }
        }

        return decisionlist;
    }


    /**
      *
      * @param processInstanceId
      *     A String of {@link String} : process instance id.
      * @return
      *     A respnse of {@link ArrayList} : Return historic instance list.
      */
     public List<HistoricProcessInstance> findAllHistoricProcessInstance(Pageable page, String processInstanceId) {
       if(!processInstanceId.equals("null")) {
         return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).list();
       }else{
         return historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list();
       }
     }

     /**
      *
      * @param processInstanceId
      *     A String of {@link String} : process instance id.
      * @return
      *     A respnse of {@link ArrayList} : Return historic instance list.
      */
     public Object getInput(String processInstanceId, String name, String type) {

         HistoricActivityInstance act = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceEndTime().desc().list().get(0);

         Process process = repositoryService.getBpmnModel(act.getProcessDefinitionId()).getMainProcess();

         // extract list of flow sequences from process model
         if (act.getActivityType().equals("endEvent")) {
             List<EndEvent> activities = process.findFlowElementsOfType(EndEvent.class);
             for (EndEvent activity : activities) {
                 if (activity.getId().equals(act.getActivityId())) {
                     if (activity.getExtensionElements().size() != 0) {
                         if (activity.getExtensionElements().containsKey("inputOutput")) {
                             for (ExtensionElement extensionElement : activity.getExtensionElements().get("inputOutput")) {
                                 for (ExtensionElement extensionElementChild : extensionElement.getChildElements().get("inputParameter")) {
                                     if (extensionElementChild.getAttributes().get("name").get(0).getValue().equals(name)) {
                                         if (extensionElementChild.getChildElements().get(type) != null) {
                                             if (type.equals("map")) {
                                                 Map<String, String> result = new HashMap<String, String>();
                                                 for (ExtensionElement extensionElementChild2 : extensionElementChild.getChildElements().get(type).get(0).getChildElements().get("entry")) {

                                                     result.put(extensionElementChild2.getAttributes().get("key").get(0).getValue(), extensionElementChild2.getElementText());

                                                 }
                                                 return result;
                                             } else if (type.equals("list")) {
                                                 List<String> result = new ArrayList<>();
                                                 for (ExtensionElement extensionElementChild2 : extensionElementChild.getChildElements().get(type).get(0).getChildElements().get("value")) {

                                                     result.add(extensionElementChild2.getElementText());

                                                 }
                                                 return result;

                                             }


                                         } else if (type.equals("string")) {
                                             String result = extensionElementChild.getElementText();
                                             return result;
                                         }
                                     }


                                 }
                             }
                         }
                     }
                 }
             }
         } else {
             List<Activity> activities = process.findFlowElementsOfType(Activity.class);
             for (Activity activity : activities) {
                 if (activity.getId().equals(act.getActivityId())) {
                     if (activity.getExtensionElements().size() != 0) {
                         if (activity.getExtensionElements().containsKey("inputOutput")) {
                             for (ExtensionElement extensionElement : activity.getExtensionElements().get("inputOutput")) {
                                 for (ExtensionElement extensionElementChild : extensionElement.getChildElements().get("inputParameter")) {
                                     if (extensionElementChild.getAttributes().get("name").get(0).getValue().equals(name)) {
                                         if (extensionElementChild.getChildElements().get(type) != null) {
                                             if (type.equals("map")) {
                                                 Map<String, String> result = new HashMap<String, String>();
                                                 for (ExtensionElement extensionElementChild2 : extensionElementChild.getChildElements().get(type).get(0).getChildElements().get("entry")) {

                                                     result.put(extensionElementChild2.getAttributes().get("key").get(0).getValue(), extensionElementChild2.getElementText());

                                                 }
                                                 return result;
                                             } else if (type.equals("list")) {
                                                 List<String> result = new ArrayList<>();
                                                 for (ExtensionElement extensionElementChild2 : extensionElementChild.getChildElements().get(type).get(0).getChildElements().get("value")) {

                                                     result.add(extensionElementChild2.getElementText());

                                                 }
                                                 return result;

                                             }


                                         } else if (type.equals("string")) {
                                             String result = extensionElementChild.getElementText();
                                             return result;
                                         }
                                     }


                                 }
                             }
                         }
                     }
                 }
             }
         }


         return null;
     }


    /**
       *
       * @param processInstanceId
       *     A String of {@link String} : process instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return historic instance list.
       */
      public List<HistoricWF> getHistoricProcess(String processInstanceId) {

        // initialize the return list of historics
        List<HistoricWF> his = new ArrayList<HistoricWF>();

        // cretae historic task instace query
        NativeHistoricTaskInstanceQuery taskQuery = historyService.createNativeHistoricTaskInstanceQuery();
        taskQuery.sql("SELECT * FROM act_hi_taskinst WHERE PROC_INST_ID_='" + processInstanceId + "' AND END_TIME_ IS NOT NULL ORDER BY START_TIME_ ASC");

        // initialize temp list of historics
        List<HistoricTaskInstance> copietasks = new ArrayList<HistoricTaskInstance>();

        // iterate query result and push to the temp list
        for (int d = 0; d < taskQuery.list().size(); d++)
          copietasks.add(taskQuery.list().get(d));

        List<HistoricTaskInstance> tasks = copietasks;

        // iterate historic and build the return list
        for (HistoricTaskInstance taskk : tasks) {
          NativeHistoricVariableInstanceQuery historicQuery = historyService.createNativeHistoricVariableInstanceQuery();
            String decision = "";
          if(historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :decision" + "'").list().size() > 0) {
              HistoricVariableInstance decisionVariable = historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :decision" + "'").list().get(0);
               decision = (decisionVariable != null) ? (String.valueOf(decisionVariable).substring(String.valueOf(decisionVariable).lastIndexOf("=") + 1, String.valueOf(decisionVariable).lastIndexOf("]"))) : "";
          }else{
              decision = "Automatique";
          }
          String description = "";
          try {
            HistoricVariableInstance descriptionVariable = historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :description" + "'").list().get(0);
            description = String.valueOf(descriptionVariable).substring(String.valueOf(descriptionVariable).lastIndexOf("=") + 1, String.valueOf(descriptionVariable).lastIndexOf("]"));
          } catch (Exception e) { }

          String authentifier = "";
          if(historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :authentifier" + "'").list().size() > 0) {
              try {
                  HistoricVariableInstance authentifierVariable = historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :authentifier" + "'").list().get(0);
                  authentifier = String.valueOf(authentifierVariable).substring(String.valueOf(authentifierVariable).lastIndexOf("=") + 1, String.valueOf(authentifierVariable).lastIndexOf("]"));
              } catch (Exception e) {
              }
          }else{
              authentifier = "Systeme";
          }
            String effectiveUser = "";
            try {
                HistoricVariableInstance effectiveUserVariable = historicQuery.sql("SELECT TEXT_ FROM act_hi_varinst WHERE NAME_ = '" + taskk.getId() + " :effectiveUser" + "'").list().get(0);
                effectiveUser = String.valueOf(effectiveUserVariable).substring(String.valueOf(effectiveUserVariable).lastIndexOf("=") + 1, String.valueOf(effectiveUserVariable).lastIndexOf("]"));
            } catch (Exception e) { }

            // Récupérer le nom de la transition (sequenceFlow) qui a mené à cette tâche
            String transitionName = "";
            try {
                NativeHistoricActivityInstanceQuery activityQuery = historyService.createNativeHistoricActivityInstanceQuery();
                String sql = "SELECT a.ACT_NAME_ FROM act_hi_actinst a " +
                        "JOIN act_hi_actinst b ON a.PROC_INST_ID_ = b.PROC_INST_ID_ " +
                        "WHERE b.TASK_ID_ = '" + taskk.getId() + "' " +
                        "AND a.ACT_TYPE_ = 'sequenceFlow' " +
                        "AND a.END_TIME_ <= b.START_TIME_ " +
                        "AND a.END_TIME_ = (SELECT MAX(c.END_TIME_) FROM act_hi_actinst c WHERE c.PROC_INST_ID_ = b.PROC_INST_ID_ AND c.ACT_TYPE_ = 'sequenceFlow' AND c.END_TIME_ <= b.START_TIME_) " +
                        "ORDER BY a.END_TIME_ DESC";
                List<HistoricActivityInstance> activities = activityQuery.sql(sql).list();
                if (!activities.isEmpty() && activities.get(0).getActivityName() != null) {
                    transitionName = activities.get(0).getActivityName();
                }
            } catch (Exception e) {
                // silently ignore if transition name cannot be retrieved
            }

            HistoricWF historicWF = new HistoricWF(taskk.getName(),
                    decision,
                    transitionName,
                    taskk.getAssignee(),
                    ZonedDateTime.ofInstant(taskk.getCreateTime().toInstant(), ZoneId.systemDefault()),
                    ZonedDateTime.ofInstant(taskk.getEndTime().toInstant(), ZoneId.systemDefault()),
                    description,
                    authentifier,effectiveUser);
            his.add(historicWF);
        }

        // return likst of historic
        return his;
      }


     /**
      * @return A respnse of {@link JSONObject} : Return historic instance list.
      * @Param A hash map of {@link Map} : contains a list of variables
      */
      public BpmJob _nextTask(String processInstanceId, String decision, String wfComment, Object data, AclClass aclClass) throws Exception {

          // initialize the set of variables to send it to flowable
          Map<String, Object> variables = new HashMap<>();

          variables.put("Decision", decision);

          variables.put("description", wfComment);

          variables.put("data", data);

          variables.put("sids", currentUser.getSid());

          // recuperate the current task before rooting
          Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();

          // complete the current instance
          _finishTask(task.getId(), variables);

          HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

          JSONObject object = new JSONObject();

          if(processInstance.getEndTime() != null) {

              HistoricActivityInstance activityInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityId(processInstance.getEndActivityId()).singleResult();

              Object dataObj = historyService.createHistoricVariableInstanceQuery().variableName("data").processInstanceId(processInstance.getId()).list().get(0).getValue();

              String endActivityName = activityInstance != null ? activityInstance.getActivityName() : task.getName();

              BpmJob bpmJob = new BpmJob(new JSONObject(), endActivityName, task.getAssignee(), false, task.getProcessInstanceId(), null, null);

              bpmJob.setProcessName(processInstance.getProcessDefinitionName());

              bpmJob.setEndProcess(true);

              bpmJob.setProcessID(processInstance.getId());

              bpmJob.setDataObject(dataObj);

              bpmJob.setAuthors(new ArrayList<>());

              bpmJob.setReaders(new ArrayList<>());

              return bpmJob;

          }else {

              task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();

              Object dataObj = taskService.getVariable(task.getId(), "data");

              // recuperate list of authors
              List<String> listAuthors = _getCandidateGroups(task.getId());

              // recuperate list of readers
              List<String> listReaders = _getCandidateUsers(task.getId());

              BpmJob bpmJob = new BpmJob(new JSONObject(), task.getName(), task.getAssignee(), false, task.getProcessInstanceId(), null, null);

              bpmJob.setProcessName(processInstance.getProcessDefinitionName());

              bpmJob.setProcessID(processInstance.getId());

              bpmJob.setDataObject(dataObj);

              bpmJob.setAuthors(listAuthors);

              bpmJob.setReaders(listReaders);

              return bpmJob;
          }



      }


     /**
      * @return A respnse of {@link JSONObject} : Return historic instance list.
      * @Param A hash map of {@link Map} : contains a list of variables
      */
      public BpmJob _initAndNextTask(String processKey, String decision, String wfComment, Object data, AclClass aclClass) throws Exception {

          // initialize the set of variables to send it to flowable
          Map<String, Object> variables = new HashMap<>();

          variables.put("Decision", decision);

          variables.put("description", wfComment);

          variables.put("data", data);

          variables.put("sids", currentUser.getSid());

          variables.put("processKey", processKey);

          variables.put("initiator", currentUser.getEmployeSid());

          ProcessInstance startedProcessInstance = runtimeService.startProcessInstanceByKey(variables.get("processKey").toString(), variables);

          // recuperate the current task before rooting
          Task task = taskService.createTaskQuery().processInstanceId(startedProcessInstance.getProcessInstanceId()).active().singleResult();

          if (task != null) {
            _finishTask(task.getId(), variables);
          }

          HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(startedProcessInstance.getProcessInstanceId()).singleResult();


          if(processInstance.getEndTime() != null) {

              Object dataObj = historyService.createHistoricVariableInstanceQuery().variableName("data").processInstanceId(processInstance.getId()).list().get(0).getValue();

              HistoricActivityInstance activityInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(startedProcessInstance.getProcessInstanceId()).activityId(processInstance.getEndActivityId()).singleResult();
              String endActivityName = activityInstance != null ? activityInstance.getActivityName() : task.getName();

              BpmJob bpmJob =  new BpmJob(new JSONObject(), endActivityName, task.getAssignee(), false, task.getProcessInstanceId(), null, null);

              bpmJob.setProcessName(processInstance.getProcessDefinitionName());

              bpmJob.setEndProcess(true);

              bpmJob.setProcessID(processInstance.getId());

              bpmJob.setDataObject(dataObj);

              bpmJob.setAuthors(new ArrayList<>());

              bpmJob.setReaders(new ArrayList<>());

              return bpmJob;

          }else {

              task = taskService.createTaskQuery().processInstanceId(startedProcessInstance.getProcessInstanceId()).active().singleResult();

              Object dataObj = task != null ? taskService.getVariable(task.getId(), "data") : historyService.createHistoricVariableInstanceQuery().variableName("data").processInstanceId(processInstance.getId()).singleResult().getValue();

              // recuperate list of authors
              List<String> listAuthors = task != null ? _getCandidateGroups(task.getId()) : new ArrayList<>();

              // recuperate list of readers
              List<String> listReaders = task != null ? _getCandidateUsers(task.getId()) : new ArrayList<>();

              BpmJob bpmJob =  new BpmJob(new JSONObject(), task.getName(), task.getAssignee(), false, task.getProcessInstanceId(), null, null);

              bpmJob.setProcessName(processInstance.getProcessDefinitionName());

              bpmJob.setEndProcess(false);

              bpmJob.setProcessID(processInstance.getId());

              bpmJob.setDataObject(dataObj);

              bpmJob.setAuthors(listAuthors);

              bpmJob.setReaders(listReaders);

              return bpmJob;
          }



      }

      /**
       *
       * @param taskId
       *     A String of {@link Task} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return list of authors.
       */
      public List<String> _getCandidateGroups(String taskId) {

        // initialize a new list of groups
        List<String> Groups = new ArrayList<String>();

        // extract all actors of the current task
        List<? extends IdentityLinkInfo> identityLinks = taskService.getIdentityLinksForTask(taskId);

        // iterate the extracted list of actors
        for (IdentityLinkInfo identityLink : identityLinks) {

          // check the type of actor (is a candidate & and is a group)
          if (IdentityLinkType.CANDIDATE.equals(identityLink.getType()) && identityLink.getGroupId() != null) {

            // pushed to the result list
            Groups.add(identityLink.getGroupId());

          }
        }

        // return the list of candidate groups
        return  Groups;
      }


      /**
       *
       * @param taskId
       *     A String of {@link Task} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return list of readers.
       */
      public List<String> _getCandidateUsers(String taskId) {

        // initialize a new list of groups
        List<String> users = new ArrayList<String>();

        // extract all actors of the current task
        List<? extends IdentityLinkInfo> identityLinks = taskService.getIdentityLinksForTask(taskId);

        // iterate the extracted list of actors
        for (IdentityLinkInfo identityLink : identityLinks) {

          // check the type of actor (is a candidate & and is a user)
          if (IdentityLinkType.CANDIDATE.equals(identityLink.getType()) && identityLink.getUserId() != null) {

            // pushed to the result list
            users.add(identityLink.getUserId());

          }
        }

        // return the list of candidate groups
        return  users;
      }


      /**
       *
       * @param processInstanceId
       *     A String of {@link String} : process instance id.
       * @return
       *     A respnse of {@link Task} : Return task Object.
       */
      public Task getActifTaskOfProcessInstance(String processInstanceId){
        try {
          return taskService.createTaskQuery().processInstanceId(processInstanceId).active().list().get(0);
        }catch(Exception e){
          return null;
        }
      }

      public void jumpToTask(String processInstanceId, String targetTaskDefinitionKey) {
        int tries = 0;
        while (tries < 5) {
          try {
            List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
            java.util.Set<String> sourceActivityIds = new java.util.HashSet<>();
            if (activeTasks != null && !activeTasks.isEmpty()) {
              for (Task t : activeTasks) {
                if (t.getTaskDefinitionKey() != null) sourceActivityIds.add(t.getTaskDefinitionKey());
              }
            } else {
              // No active user tasks yet: use active activity ids of root execution
              org.flowable.engine.runtime.Execution root =
                  runtimeService.createExecutionQuery().processInstanceId(processInstanceId).onlyProcessInstanceExecutions().singleResult();
              if (root != null) {
                List<String> actives = runtimeService.getActiveActivityIds(root.getId());
                if (actives != null) sourceActivityIds.addAll(actives);
              }
            }
            if (sourceActivityIds.isEmpty()) {
              tries++;
              try { Thread.sleep(100L * (tries + 1)); } catch (InterruptedException ignored) {}
              continue;
            }
            org.flowable.engine.runtime.ChangeActivityStateBuilder builder =
                runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId);
            builder.moveActivityIdsToSingleActivityId(new java.util.ArrayList<>(sourceActivityIds), targetTaskDefinitionKey).changeState();
            // wait a moment for the target user task to be created
            Task t = null;
            for (int i = 0; i < 10 && t == null; i++) {
              try { Thread.sleep(100); } catch (InterruptedException ignored) {}
              t = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
            }
            return;
          } catch (FlowableOptimisticLockingException e) {
            try { Thread.sleep(150L * (tries + 1)); } catch (InterruptedException ignored) {}
            tries++;
          } catch (Exception ignored) {
            return;
          }
        }
      }

      public BpmJob jumpToTaskAndBuildJob(String processInstanceId, String targetTaskDefinitionKey) {
        Object dataObj = null;
        try {
          dataObj = runtimeService.getVariable(processInstanceId, "data");
        } catch (Exception ignored) {}
        Task current = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        if (dataObj != null) {
          try {
            runtimeService.setVariable(processInstanceId, "data", dataObj);
            if (current != null) {
              runtimeService.setVariable(current.getExecutionId(), "data", dataObj);
              taskService.setVariable(current.getId(), "data", dataObj);
              taskService.setVariableLocal(current.getId(), "data", dataObj);
              runtimeService.setVariableLocal(processInstanceId, "data", dataObj);
            }
          } catch (Exception ignored) {}
        }
        jumpToTask(processInstanceId, targetTaskDefinitionKey);
        Task target = null;
        for (int i = 0; i < 10 && target == null; i++) {
          try { Thread.sleep(100); } catch (InterruptedException ignored) {}
          target = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(targetTaskDefinitionKey).active().singleResult();
        }
        if (target == null) {
          target = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        }
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (dataObj == null && target != null) {
          try { dataObj = taskService.getVariable(target.getId(), "data"); } catch (Exception ignored) {}
        }
        List<String> authors = new ArrayList<>();
        List<String> readers = new ArrayList<>();
        if (target != null) {
          try { authors = _getCandidateGroups(target.getId()); } catch (Exception ignored) {}
          try { readers = _getCandidateUsers(target.getId()); } catch (Exception ignored) {}
        }
        BpmJob bpmJob = new BpmJob(new JSONObject(), target != null ? target.getName() : null, target != null ? target.getAssignee() : null, false, processInstanceId, null, null);
        bpmJob.setProcessName(processInstance != null ? processInstance.getProcessDefinitionName() : null);
        bpmJob.setProcessID(processInstanceId);
        bpmJob.setDataObject(dataObj);
        bpmJob.setAuthors(authors != null ? authors : new ArrayList<>());
        bpmJob.setReaders(readers != null ? readers : new ArrayList<>());
        return bpmJob;
      }

      /**
       *
       * @param taskId
       *     A String of {@link String} : current activity instance id.
       * @param variable
       *     A String of {@link Map} : list of params to passed to workflow.
       * @return
       *     A respnse of {@link Void} : Return task Object.
       */
      public void _finishTask(String taskId, Map<String, Object> variable) throws JSONException, ParseException {

        //persist the decision in workflow variables
        taskService.setVariable(taskId, taskId + " :decision", variable.get("Decision"));

        //persist the commetaskService.setVariable(taskId, data.taskDtoList, task
        taskService.setVariable(taskId, taskId + " :description", variable.get("description"));

          // ajouter par ameni à modifer par displayname ofUser
        taskService.setVariable(taskId, taskId + " :authentifier", currentUser.getDisplayName());

        taskService.setVariable(taskId, "authentifier", currentUser.getDisplayName());

          //initialize variables to to passing them to workflow
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime localDateTime=LocalDateTime.now();
        String formattedString = localDateTime.format(formatter);
        variable.put("currentDate",formattedString );
        variable.put("dueDate",Date.from(ZonedDateTime.now().toInstant()));

        // finish th current task
        taskService.complete(taskId, variable);
      }

      /**
       * @param processInstanceId A String of {@link String} : current activity instance id.
       * @return A respnse of {@link Date} : Return task Object.
       */
      public Date getDueDateInstance(String processInstanceId) throws JSONException, ParseException {

          Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
          return task.getDueDate();
      }

    /**
     * @param processInstanceId A String of {@link String} : current activity instance id.
     * @return A respnse of {@link Date} : Return task Object.
     */
    public Date getCreateDateInstance(String processInstanceId) throws JSONException, ParseException {

        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        return task.getCreateTime();
    }

    /**
     * @param processInstanceId A String of {@link String} : current activity instance id.
     * @return A respnse of {@link Date} : Return task Object.
     */
    public void suspendProcessInstance(String processInstanceId) throws JSONException, ParseException {

        runtimeService.suspendProcessInstanceById(processInstanceId);
    }


    /**
     * @param processInstanceId A String of {@link String} : current activity instance id.
     * @return A respnse of {@link Date} : Return task Object.
     */
    public void activateProcessInstance(String processInstanceId) throws JSONException, ParseException {

        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    /**
     * Récupère le dueDate de la tâche active d'un process instance
     */
    public java.util.Date getDueDateOfActiveTask(String processInstanceId) {
        try {
            Task task = getActifTaskOfProcessInstance(processInstanceId);
            if (task != null) {
                return task.getDueDate();
            }
        } catch (Exception e) {
            log.error("Error getting dueDate for processInstanceId={}", processInstanceId, e);
        }
        return null;
    }

    /**
     * Met à jour le dueDate de la tâche active d'un process instance
     */
    public void updateDueDateOfActiveTask(String processInstanceId, java.util.Date newDueDate) {
        try {
            Task task = getActifTaskOfProcessInstance(processInstanceId);
            if (task != null) {
                taskService.setDueDate(task.getId(), newDueDate);
                log.info("DueDate updated for taskId={}, processInstanceId={}, newDueDate={}",
                        task.getId(), processInstanceId, newDueDate);
            } else {
                log.warn("No active task found for processInstanceId={}", processInstanceId);
            }
        } catch (Exception e) {
            log.error("Error updating dueDate for processInstanceId={}", processInstanceId, e);
        }
    }


    /**
       *
       * @param task
       *     A String of {@link Task} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return list of authors.
       */
      public List<String> getCandidateGroups(Task task) {
        List<String> Groups = new ArrayList<String>();


        try {
          List<? extends IdentityLinkInfo> identityLinks = taskService.getIdentityLinksForTask(task.getId());

          for (IdentityLinkInfo identityLink : identityLinks) {
            String type = identityLink.getType();
            String groupId = identityLink.getGroupId();
            if (IdentityLinkType.CANDIDATE.equals(type) && groupId != null) {
              Groups.add(groupId);
            }
          }
        }catch(Exception e){
            log.error("condidate group Not Fouwnd");
    //      System.out.println("condidate group Not Fouwnd");
        }

        return  Groups;
      }

      /**
       *
       * @param taskId
       *     A String of {@link Task} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return list of readers.
       */
      public List<String> getCandidateUsers(String taskId) {

        List<String> User = new ArrayList<String>();
        try{
          List<? extends IdentityLinkInfo> identityLinks = taskService.getIdentityLinksForTask(taskId);

          for (IdentityLinkInfo identityLink : identityLinks) {
            String type = identityLink.getType();
            String userId = identityLink.getUserId();
            if (IdentityLinkType.CANDIDATE.equals(type) && userId != null) {
              User.add(userId);
            }
          }
        }catch(Exception e){
            log.error("condidate users Not Fouwnd");
        }

        return  User;
      }

      /**
       *
       * @param processInstance
       *     A String of {@link String} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return list of readers.
       */
      public String getEndActivityName(String processInstance) {

        NativeHistoricActivityInstanceQuery query = historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM act_hi_actinst WHERE PROC_INST_ID_='" + processInstance + "' AND act_type_ = 'endEvent' ORDER BY START_TIME_ DESC");
        HistoricActivityInstance task = query.singleResult();

        return task.getActivityName();
      }

      /**
       *
       * @param taskid
       *     A String of {@link String} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return the process definition id.
       */
      public String getprocessInstanceOftask(String taskid) {
        NativeTaskQuery query = taskService.createNativeTaskQuery().sql("SELECT * FROM act_hi_taskinst where ID_='" + taskid + "'");
        Task task = query.list().get(0);
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(processInstanceId).singleResult();

        return processInstance.getProcessDefinitionId();

      }

      /**
       *
       * @param dob
       *     A String of {@link LocalDateTime} : start date and time of activity .
       * @param now
       *     A String of {@link LocalDateTime} : current date and time .
       * @return
       *     A respnse of {@link String} : Return the due date.
       */
      public String getTime(LocalDateTime dob, LocalDateTime now) {
        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), dob.getHour(), dob.getMinute(), dob.getSecond());
        Duration duration = Duration.between(today, now);
        long seconds = duration.getSeconds();
        long hours = seconds / Duration.ofHours(1).getSeconds();
        long minutes = ((seconds % Duration.ofHours(1).getSeconds()) / Duration.ofMinutes(1).getSeconds());
        long secs = (seconds % Duration.ofMinutes(1).getSeconds());
        return hours + "H " + minutes + "M " + secs + "S";
      }

      /**
       *
       * @param taskId
       *     A String of {@link String} : current activity instance id.
       * @return
       *     A respnse of {@link ArrayList} : Return the process model.
       */
      public Process getProcess(String taskId) {
        Process process = repositoryService.getBpmnModel(getprocessInstanceOftask(taskId)).getMainProcess();
        return process;
      }

      public Process getProcessModel(String processInstanceId) {
        Process process = repositoryService.getBpmnModel(runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).list().get(0).getProcessDefinitionId()).getMainProcess();
        return process;
      }

      public Object initalizeProxy(Object obj) {
        if (obj instanceof HibernateProxy) {
          return ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
        }
        return obj;
      }

      public List<String> get_activities_by_process(String processDefinitionKey) {

        List<String> activitys = new ArrayList<>();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).list().get(0).getId());

        Process process = bpmnModel.getProcessById(processDefinitionKey);

        List<UserTask> tasks = process.findFlowElementsOfType(UserTask.class);

        for(UserTask userTask : tasks){
          activitys.add(userTask.getName());
        }

        return  activitys;
      }

      public List getFirstActivityNameWithoutProcessInstance(String processId) throws FileNotFoundException {

        ProcessDefinitionQuery listprocess = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processId).orderByProcessDefinitionVersion().desc();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(listprocess.list().get(0).getId());

        Process process = bpmnModel.getProcessById(processId);

        List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);

        List<String> activity = new ArrayList<>();


        for (SequenceFlow sequenceFlow : sequenceFlows) {
          if (sequenceFlow.getSourceFlowElement().getClass().equals(StartEvent.class)) {
            activity.add(sequenceFlow.getTargetFlowElement().getName());
          }
        }

        return activity;
      }

      public List<Object> getInstanceEditorJson(String processInstance) throws Exception {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance).list().get(0).getProcessDefinitionId());
        if (bpmnModel.getLocationMap().size() == 0) {
          BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
          bpmnLayout.execute();
        }
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
        List<Object> result = new ArrayList<>();
        ObjectNode jsonNode = (ObjectNode) this.objectMapper.readTree(modelNode.toString());

        JSONParser parser = new JSONParser();
        JSONObject jsonObjectParsed = (JSONObject) parser.parse(modelNode.toString());


        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance).list();
        List<HistoricActivityInstance> historicActInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance).activityType("sequenceFlow").list();
        List<HistoricActivityInstance> historicstartEventInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance).activityType("startEvent").list();
        List<HistoricActivityInstance> historicendEventInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance).activityType("endEvent").list();
        List<HistoricActivityInstance> historicexclusiveGatewayInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance).activityType("exclusiveGateway").list();


        jsonObjectParsed.put("childShapes", addHistoricToJSON((List<JSONObject>) jsonObjectParsed.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
        // extracter la liste de tous les links de tous less activité from l'objet process
        List<SequenceFlow> sequenceFlows = bpmnModel.getMainProcess().findFlowElementsOfType(SequenceFlow.class);

        List<JSONObject> listFlows = new ArrayList<>();
        for(SequenceFlow sequenceFlow: sequenceFlows ){
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("id",sequenceFlow.getId());
          jsonObject.put("name",sequenceFlow.getName());
          List<JSONObject> listProps = new ArrayList<>();

          JSONObject jsonObjectTemp = new JSONObject();
          for(HistoricActivityInstance historicActivityInstance : historicActInstances) {
            if (sequenceFlow.getId().equals(historicActivityInstance.getActivityId())) {
              jsonObjectTemp = new JSONObject();
              jsonObjectTemp.put("name","Date Début");
              String startDate = formatter.format(ZonedDateTime.ofInstant(historicActivityInstance.getStartTime().toInstant(), ZoneId.systemDefault()));
              jsonObjectTemp.put("value",startDate);
              listProps.add(jsonObjectTemp);
            }
          }
          jsonObjectTemp = new JSONObject();
          jsonObjectTemp.put("name","Condition expression");
          jsonObjectTemp.put("value",sequenceFlow.getConditionExpression());
          listProps.add(jsonObjectTemp);
          jsonObject.put("properties",listProps);
          jsonObject.put("sourceRef",sequenceFlow.getSourceRef());
          jsonObject.put("targetRef",sequenceFlow.getTargetRef());
          jsonObject.put("type","sequenceFlow");
          List<JSONObject> jsonArrayTemp = new ArrayList<>();
          List<Integer> list = sequenceFlow.getWaypoints();
          for(int i = 0; i < list.size(); i = i + 2){
            JSONObject jsonTemp = new JSONObject();
            jsonTemp.put("x",list.get(i));
            jsonTemp.put("y",list.get(i+1));
            jsonArrayTemp.add(jsonTemp);
          }
          jsonObject.put("waypoints",jsonArrayTemp);
          for(HistoricActivityInstance historicActivityInstance : historicActInstances) {
            if (sequenceFlow.getId().equals(historicActivityInstance.getActivityId())) {
              jsonObject.put("active", true);
            }
          }
          listFlows.add(jsonObject);
        }
        result.add(jsonObjectParsed);
        result.add(listFlows);
        return result;
      }

      public List<Object> getDefinitionEditorJson(String processDefinition) throws Exception {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition);
        if (bpmnModel.getLocationMap().size() == 0) {
          BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
          bpmnLayout.execute();
        }
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
        List<Object> result = new ArrayList<>();
        ObjectNode jsonNode = (ObjectNode)this.objectMapper.readTree(modelNode.toString());

        JSONParser parser = new JSONParser();
        JSONObject jsonObjectParsed = (JSONObject) parser.parse(modelNode.toString());

        // extracter la liste de tous les links de tous less activité from l'objet process
        List<SequenceFlow> sequenceFlows = bpmnModel.getMainProcess().findFlowElementsOfType(SequenceFlow.class);

        List<JSONObject> listFlows = new ArrayList<>();
        for(SequenceFlow sequenceFlow: sequenceFlows ){
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("id",sequenceFlow.getId());
          jsonObject.put("name",sequenceFlow.getName());
          List<JSONObject> listProps = new ArrayList<>();
          JSONObject jsonObjectTemp = new JSONObject();
          jsonObjectTemp.put("name","Condition expression");
          jsonObjectTemp.put("value",sequenceFlow.getConditionExpression());
          listProps.add(jsonObjectTemp);
          jsonObject.put("properties",listProps);
          jsonObject.put("sourceRef",sequenceFlow.getSourceRef());
          jsonObject.put("targetRef",sequenceFlow.getTargetRef());
          jsonObject.put("type","sequenceFlow");
          List<JSONObject> jsonArrayTemp = new ArrayList<>();
          List<Integer> list = sequenceFlow.getWaypoints();
          for(int i = 0; i < list.size(); i = i + 2){
            JSONObject jsonTemp = new JSONObject();
            jsonTemp.put("x",list.get(i));
            jsonTemp.put("y",list.get(i+1));
            jsonArrayTemp.add(jsonTemp);
          }
          jsonObject.put("waypoints",jsonArrayTemp);
          listFlows.add(jsonObject);
        }
        result.add(jsonObjectParsed);
        result.add(listFlows);
        return result;
      }

      public List<JSONObject> addHistoricToJSON(List<JSONObject> jsonNode, List<HistoricTaskInstance> historicTaskInstances, List<HistoricActivityInstance> historicstartEventInstances, List<HistoricActivityInstance> historicendEventInstances, List<HistoricActivityInstance> historicexclusiveGatewayInstances){

            List<JSONObject> jsonNodeTemp = new ArrayList<>();
            for (JSONObject objectNode : jsonNode) {
                int index = 0;
                if (((JSONObject) objectNode.get("stencil")).get("id").toString().equals("UserTask")) {
                    for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {

                        if (objectNode.get("resourceId").toString().equals(historicTaskInstance.getTaskDefinitionKey())) {
                            JSONObject nodePropertiesOriginal = ((JSONObject) objectNode.get("properties"));
                            JSONObject nodeProperties = new JSONObject();

                            JSONObject nodePropertieUserTaskAssinment = new JSONObject();
                            JSONObject nodePropertieUserTaskAssinmentAssignment =  new JSONObject();

                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("Assignee")) {
                                nodePropertieUserTaskAssinmentAssignment.put("Assignee",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentAssignee = new ArrayList<>();
                            JSONObject Assignee = new JSONObject();
                            Assignee.put("value",historicTaskInstance.getAssignee());
                            nodePropertieUserTaskAssinmentAssignmentAssignee.add(Assignee);
                            nodePropertieUserTaskAssinmentAssignment.put("Assignee", nodePropertieUserTaskAssinmentAssignmentAssignee);

                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("actionaire")) {
                                nodePropertieUserTaskAssinmentAssignment.put("actionaire",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentActionaire = new ArrayList<>();
                            JSONObject actionaire = new JSONObject();
                            String actionnaire = "";
                            if(historyService.createHistoricVariableInstanceQuery().variableName(historicTaskInstance.getId() + " :authentifier").list().size() > 0) {
                                HistoricVariableInstance Actionaire = historyService.createHistoricVariableInstanceQuery().variableName(historicTaskInstance.getId() + " :authentifier").list().get(0);
                                actionnaire = Actionaire != null? Actionaire.getValue().toString() : null;
                            }
                            actionaire.put("value", actionnaire);
                            nodePropertieUserTaskAssinmentAssignmentActionaire.add(actionaire);
                            nodePropertieUserTaskAssinmentAssignment.put("actionaire", nodePropertieUserTaskAssinmentAssignmentActionaire);

                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("startDate")) {
                                nodePropertieUserTaskAssinmentAssignment.put("startDate",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentStartDate = new ArrayList<>();
                            JSONObject startDate = new JSONObject();
                            String startDateVal = formatter.format(ZonedDateTime.ofInstant(historicTaskInstance.getCreateTime().toInstant(), ZoneId.systemDefault()));
                            startDate.put("value", startDateVal);
                            nodePropertieUserTaskAssinmentAssignmentStartDate.add(startDate);
                            nodePropertieUserTaskAssinmentAssignment.put("startDate", nodePropertieUserTaskAssinmentAssignmentStartDate);

                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("endDate")) {
                                nodePropertieUserTaskAssinmentAssignment.put("endDate",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentEndDate = new ArrayList<>();
                            JSONObject endDate = new JSONObject();
                            String endDateVal = null;
                            if(historicTaskInstance.getEndTime() != null) {
                                endDateVal = formatter.format(ZonedDateTime.ofInstant(historicTaskInstance.getEndTime().toInstant(), ZoneId.systemDefault()));
                            }
                            endDate.put("value", endDateVal);
                            nodePropertieUserTaskAssinmentAssignmentEndDate.add(endDate);
                            nodePropertieUserTaskAssinmentAssignment.put("endDate", nodePropertieUserTaskAssinmentAssignmentEndDate);


                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("candidateGroups")) {
                                nodePropertieUserTaskAssinmentAssignment.put("candidateGroups",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentCandidateGroups = new ArrayList<>();
                            JSONObject candidateGroup = new JSONObject();
                            try {
                                List<HistoricIdentityLink> identitys = historyService.getHistoricIdentityLinksForTask(historicTaskInstance.getId());
                                if (identitys != null)
                                    for (HistoricIdentityLink grp : identitys) {
                                        if (grp.getGroupId() != null && !grp.getGroupId().isEmpty()) {
                                            candidateGroup.put("value", grp.getGroupId());
                                            nodePropertieUserTaskAssinmentAssignmentCandidateGroups.add(candidateGroup);
                                        }
                                    }
                            }catch (Exception e){
                                log.error("condidate user Not Fouwnd");
    //              System.out.println("condidate user Not Fouwnd");

                            }
                            nodePropertieUserTaskAssinmentAssignment.put("candidateGroups", nodePropertieUserTaskAssinmentAssignmentCandidateGroups);

                            if(!nodePropertieUserTaskAssinmentAssignment.containsKey("candidateUsers")) {
                                nodePropertieUserTaskAssinmentAssignment.put("candidateUsers",new ArrayList<>());
                            }
                            List<JSONObject> nodePropertieUserTaskAssinmentAssignmentCandidateUsers = new ArrayList<>();
                            JSONObject candidateUser = new JSONObject();
                            try{
                                List<HistoricIdentityLink> identitys = historyService.getHistoricIdentityLinksForTask(historicTaskInstance.getId());
                                if(identitys != null)
                                    for(HistoricIdentityLink usr : identitys){
                                        if(usr.getUserId() != null && !usr.getUserId().isEmpty()){
                                            if(!candidateGroup.containsValue(usr.getUserId())) {
                                                candidateUser.put("value", usr.getUserId());
                                                nodePropertieUserTaskAssinmentAssignmentCandidateUsers.add(candidateUser);
                                            }
                                        }
                                    }
                            }catch (Exception e){
                                log.error("condidate user Not Fouwnd");
    //              System.out.println("condidate user Not Fouwnd");
                            }
                            nodePropertieUserTaskAssinmentAssignment.put("candidateUsers", nodePropertieUserTaskAssinmentAssignmentCandidateUsers);
                            nodePropertieUserTaskAssinment.put("assignment", nodePropertieUserTaskAssinmentAssignment);
                            nodeProperties.put("usertaskassignment", nodePropertieUserTaskAssinment);
                            if (!nodePropertiesOriginal.containsKey("current") || (nodePropertiesOriginal.containsKey("current") && !((Boolean) nodePropertiesOriginal.get("current"))))
                                if (historicTaskInstance.getEndTime() == null) {
                                    nodePropertiesOriginal.put("current", true);
                                } else {
                                    nodePropertiesOriginal.put("current", false);
                                }
                            nodePropertiesOriginal.put("active",true);
                            objectNode.put("properties", nodePropertiesOriginal);
                            objectNode.put("properties"+index, nodeProperties);
                            index++;
                        }

                    }
                    objectNode.put("childShapes", addHistoricToJSON((List<JSONObject>)objectNode.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
                    jsonNodeTemp.add(objectNode);
                }else if(((JSONObject)objectNode.get("stencil")).get("id").toString().equals("EndNoneEvent")){
                    for(HistoricActivityInstance historicActivityInstance : historicendEventInstances){
                        if(((JSONObject)objectNode.get("properties")).get("overrideid").toString().equals(historicActivityInstance.getActivityId())){
                            JSONObject nodeProperties = ((JSONObject)objectNode.get("properties"));
                            nodeProperties.put("active",true);
                            objectNode.put("properties", nodeProperties);
                        }
                    }
                    objectNode.put("childShapes", addHistoricToJSON((List<JSONObject>)objectNode.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
                    jsonNodeTemp.add(objectNode);

                }else if(((JSONObject)objectNode.get("stencil")).get("id").toString().equals("ExclusiveGateway")){
                    for(HistoricActivityInstance historicActivityInstance : historicexclusiveGatewayInstances){
                        if(((JSONObject)objectNode.get("properties")).get("overrideid").toString().equals(historicActivityInstance.getActivityId())){
                            JSONObject nodeProperties = ((JSONObject)objectNode.get("properties"));
                            nodeProperties.put("active",true);
                            objectNode.put("properties", nodeProperties);
                        }
                    }
                    objectNode.put("childShapes", addHistoricToJSON((List<JSONObject>)objectNode.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
                    jsonNodeTemp.add(objectNode);

                }else if(((JSONObject)objectNode.get("stencil")).get("id").toString().equals("StartNoneEvent")){
                    for(HistoricActivityInstance historicActivityInstance : historicstartEventInstances){
                        if(((JSONObject)objectNode.get("properties")).get("overrideid").toString().equals(historicActivityInstance.getActivityId())){
                            JSONObject nodeProperties = ((JSONObject)objectNode.get("properties"));
                            nodeProperties.put("active",true);
                            objectNode.put("properties", nodeProperties);
                        }
                    }
                    objectNode.put("childShapes", addHistoricToJSON((List<JSONObject>)objectNode.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
                    jsonNodeTemp.add(objectNode);
                }else{
                    objectNode.put("childShapes", addHistoricToJSON((List<JSONObject>)objectNode.get("childShapes"), historicTaskInstances, historicstartEventInstances, historicendEventInstances, historicexclusiveGatewayInstances));
                    jsonNodeTemp.add(objectNode);
                }
            }
            return jsonNodeTemp;
        }

      public void synchronizehistory(DelegateExecution execution) {
        System.out.println(execution.getVariable("data"));
        managementService.executeCommand(new Command<Void>() {

            @SneakyThrows
            @Override
            public Void execute(CommandContext commandContext) {

                try {
                    HistoricVariableInstanceEntityManager historicVariableInstanceEntityManager = CommandContextUtil.getHistoricVariableInstanceEntityManager(commandContext);
                    HistoricVariableInstanceEntity historicVariableInstanceByVariableInstanceId = historicVariableInstanceEntityManager.findHistoricVariableInstanceByVariableInstanceId(historyService.createHistoricVariableInstanceQuery().processInstanceId(execution.getProcessInstanceId()).variableName("data").list().get(0).getId());
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteOut);
                    out.writeObject(execution.getVariable("data"));
                    historicVariableInstanceByVariableInstanceId.setBytes(byteOut.toByteArray());
                    historicVariableInstanceEntityManager.update(historicVariableInstanceByVariableInstanceId, false);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                return null;
            }
        });
    }

      public String getFirstActivityNameByKey(String processKey) throws FileNotFoundException {

        ProcessDefinitionQuery listprocess = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).orderByProcessDefinitionVersion().desc();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(listprocess.list().get(0).getId());

        Process process = bpmnModel.getProcessById(processKey);

        List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);

        List<String> activity = new ArrayList<>();


        for (SequenceFlow sequenceFlow : sequenceFlows) {
          if (sequenceFlow.getSourceFlowElement().getClass().equals(StartEvent.class)) {
            activity.add(sequenceFlow.getTargetFlowElement().getName());
          }
        }

        return activity.get(0);
      }

}

