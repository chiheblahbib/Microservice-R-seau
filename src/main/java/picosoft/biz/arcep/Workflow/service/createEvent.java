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



  /**
   * Notifier ne doit pas pouvoir faire echouer le depot.
   *
   * Flowable evalue cette expression DANS la transaction du circuit : toute
   * exception qui remonte d'ici annule le depot entier. Un seul type
   * d'evenement absent du kernel suffisait donc a rendre un dossier non
   * deposable -- constate le 21/09/2026, « DepotReseauNotif » manquant, depot
   * en 500 alors que le dossier etait valide et la classe ACL resolue.
   *
   * CE N'EST PAS UNE NOUVELLE POLITIQUE, c'est celle que le code annonce
   * deja. `processExecuteEventAsync` porte `@Async` et rend un
   * `CompletableFuture` que personne ne consomme : si l'appel passait par le
   * proxy Spring, l'exception y serait capturee et ne remonterait jamais. Elle
   * ne remonte que parce que `_execute` appelle la methode SUR LUI-MEME, et
   * qu'une auto-invocation ne traverse pas le proxy -- l'annotation reste sans
   * effet, l'appel est synchrone. On retablit l'effet attendu.
   *
   * ET ON LE REND VISIBLE : la panne est journalisee, la ou le Future
   * silencieux l'aurait perdue. Une notification qui manque doit se voir dans
   * les traces, pas seulement ne plus nuire.
   *
   * Le gabarit porte le meme cablage (createEvent d'homologation, methodes
   * identiques). Il ne s'en apercoit pas : ses types d'evenement sont declares
   * de longue date. Ce n'est donc pas un ecart de DRRRS, c'est un defaut de la
   * famille que seul DRRRS rencontre aujourd'hui.
   *
   * A RETIRER ? Non. Meme une fois les 80 types declares au kernel, un kernel
   * indisponible ne doit pas empecher un depot.
   */
  public void _execute(DelegateExecution execution, String eventTypeAlias) throws Exception, InterruptedException {
    try {
      processExecuteEventAsync(execution, eventTypeAlias);
    } catch (Exception e) {
      log.error("Notification '{}' non enregistree, le circuit continue sans elle : {}",
                eventTypeAlias, e.toString());
    }
  }

  /** Meme garde, pour la variante qui joint un rapport. Voir ci-dessus. */
  public void _execute(DelegateExecution execution, String eventTypeAlias, String reportName, String className) throws Exception, InterruptedException {
    try {
      processExecuteEventAsync(execution, eventTypeAlias, reportName, className);
    } catch (Exception e) {
      log.error("Notification '{}' (rapport '{}') non enregistree, le circuit continue sans elle : {}",
                eventTypeAlias, reportName, e.toString());
    }
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
