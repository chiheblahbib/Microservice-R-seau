package picosoft.biz.arcep.Workflow.service;


import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapstruct.Named;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import picosoft.biz.arcep.client.kernel.model.global.JRXMLEventCriteria;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;



@Service
@Named("createEvent")
public class createEvent {
  private final org.slf4j.Logger log = LoggerFactory.getLogger(createEvent.class);

  @Autowired
  WorkflowService workflowService;

  @Autowired
  private KernelInterface kernelInterface;

  @Autowired
  private Gson gson;



  public void _execute(DelegateExecution execution, String eventTypeAlias) throws Exception, InterruptedException {

    processExecuteEventAsync(execution, eventTypeAlias);
  }

  public void _execute(DelegateExecution execution, String eventTypeAlias, String reportName, String className) throws Exception, InterruptedException {

    processExecuteEventAsync(execution, eventTypeAlias, reportName, className);
  }


  @Async
  public CompletableFuture<String> processExecuteEventAsync(DelegateExecution execution, String eventTypeAlias) throws ParseException, InterruptedException {
    if (execution.getVariable("data") != null) {

      // extract execution entity from execution
      ExecutionEntity executionEntity = ((ExecutionEntity) execution);

      // initialize temp simple.JSONObject
      org.json.simple.JSONObject object = new org.json.simple.JSONObject();

      // extract object from variables
      object.putAll((Map) new JSONParser().parse(gson.toJson(execution.getVariable("data"))));

      // extract Object from flowabled
      JSONObject objectEvt = new JSONObject(object);

      // invoke add event to create event
      kernelInterface.addEvent(
              eventTypeAlias,
              object.toJSONString(),
              Long.valueOf(objectEvt.get("id").toString()),
              objectEvt.get("className").toString(), null, null, null);

    }else{
      log.error("=> NO EVENT TO SAVE");
    }
    return CompletableFuture.completedFuture("Execution completed");

  }
  public CompletableFuture<String> processExecuteEventAsync(DelegateExecution execution, String eventTypeAlias, String reportName,String className) throws ParseException, InterruptedException {
    if (execution.getVariable("data") != null) {

      // extract execution entity from execution
      ExecutionEntity executionEntity = ((ExecutionEntity) execution);

      // initialize temp simple.JSONObject
      org.json.simple.JSONObject object = new org.json.simple.JSONObject();

      // extract object from variables
      object.putAll((Map) new JSONParser().parse(gson.toJson(execution.getVariable("data"))));

      // extract Object from flowabled
      JSONObject objectEvt = new JSONObject(object);

      if(reportName != null) {
        JRXMLEventCriteria jrxmlEventCriteria = new JRXMLEventCriteria();
        LongFilter l1 = new LongFilter();
        l1.setEquals(Long.valueOf(objectEvt.get("id").toString()));
        jrxmlEventCriteria.setObjectID(l1);

        StringFilter l2 = new StringFilter();
        l2.setEquals(className);
        jrxmlEventCriteria.setClassname(l2);


        StringFilter l3 = new StringFilter();
        l3.setEquals(reportName);
        jrxmlEventCriteria.setJrxmlTemplateDTOName(l3);

        org.json.simple.JSONObject jsonObject = kernelInterface.findAllJRXMLEventPOST(jrxmlEventCriteria, Pageable.unpaged());
        org.json.simple.JSONObject temp = new org.json.simple.JSONObject();
        if(((List<LinkedHashMap>) jsonObject.get("content")).get(0).get("pdfContent") != null)
          temp.put("fileBytes", Base64.getDecoder().decode(((List<LinkedHashMap>) jsonObject.get("content")).get(0).get("pdfContent").toString()));
        else
          temp.put("fileBytes", Base64.getDecoder().decode(""));
        temp.put("fileName", reportName + ".pdf");

        objectEvt.put("attachmentsTemp", temp);
      }

      // invoke add event to create event
      kernelInterface.addEvent(
              eventTypeAlias,
              objectEvt.toString(),
              Long.valueOf(objectEvt.get("id").toString()),
              objectEvt.get("className").toString(), null, null, null);

    }else{
      log.error("=> NO EVENT TO SAVE");
    }
    return CompletableFuture.completedFuture("Execution completed");

  }

}
