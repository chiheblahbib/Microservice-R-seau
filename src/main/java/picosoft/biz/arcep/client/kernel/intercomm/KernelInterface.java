package picosoft.biz.arcep.client.kernel.intercomm;

import picosoft.biz.arcep.client.kernel.model.global.AclClassFilesDto;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import picosoft.biz.arcep.client.config.FeignConfig;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.model.acl.dto.EmployeWithUserDTO;
import picosoft.biz.arcep.client.kernel.model.acl.dto.KeycloakUserDTO;
import picosoft.biz.arcep.client.kernel.model.acl.dto.PhysicalSignatureImageDTO;
import picosoft.biz.arcep.client.kernel.model.acl.enumeration.Access;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.events.Event;
import picosoft.biz.arcep.client.kernel.model.global.*;
import picosoft.biz.arcep.client.kernel.model.objects.*;
import picosoft.biz.arcep.client.kernel.model.objects.ObjectsDTO;
import picosoft.biz.arcep.client.kernel.model.orga.EmployeDTO;
import picosoft.biz.arcep.client.kernel.model.pm.ActivityType;
import picosoft.biz.arcep.client.kernel.model.pm.UserActivity;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@FeignClient(value = "${feign.kernel.name}", url = "${feign.kernel.url}", configuration = FeignConfig.class)
public interface KernelInterface {

    @RequestMapping(method = RequestMethod.GET, value = "/getToken")
    String getToken(@RequestParam("apiKey") String apiKey);

    @PostMapping(value = "/Authenticate")
    String Authorize(@RequestBody AuthUser a);

    @RequestMapping(method = RequestMethod.GET, value = "/currentUser")
    CurrentUser getCurrentUser();

    @RequestMapping(method = RequestMethod.POST, value = "/events")
    Event addEvent(
            @RequestParam String eventName, @RequestBody(required = false) String data,
            @RequestParam Long objectID, @RequestParam String classname, @RequestParam(required = false) String[] filePath,
            @RequestParam(required = false) MultipartFile[] multipartFiles,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime remindDate);


    @PostMapping("/findAllJRXMLEvent")
    public org.json.simple.JSONObject findAllJRXMLEventPOST(@RequestBody JRXMLEventCriteria criteria,@RequestParam Pageable pageable);

    @GetMapping("/inscriptionTemplate/{id}")
    public ResponseEntity<InscriptionTemplateDTO> getInscriptionTemplate(@PathVariable Long id);

    @RequestMapping(method = RequestMethod.GET, value = "/variables-by-name")
    String getValeur(@RequestParam String variableName);

    @RequestMapping(method = RequestMethod.PUT, value = "/adjustAttachmentSecurity")
    void adjustAttachmentSecurity(@RequestParam("classId") Long classId,
                                  @RequestParam("objectId") Long objectId,
                                  @RequestParam("objectDatasecuriteLevel") Integer objectDatasecuriteLevel);

    @RequestMapping(method = RequestMethod.GET, value = "/applySecurity")
    Boolean applySecurity(
            @RequestParam("clazz") String clazz, @RequestParam("id") Long id,
            @RequestParam("authors") List<String> authors,
            @RequestParam("readers") List<String> readers,
            @RequestParam("tempReaders") List<String> tempReaders,
            @RequestParam(value = "clazzParent", required = false) String clazzParent,
            @RequestParam(value = "idParent", required = false) Long idParent,
            @RequestParam("isCreated") Boolean isCreated, @RequestParam("isCumulative") Boolean isCumulative);


    @RequestMapping(method = RequestMethod.POST, value = "/workflow/startProcessInstance")
    org.json.simple.JSONObject startProcessInstance(@RequestBody Map<String, Object> variables);

    @RequestMapping(method = RequestMethod.POST, value = "/user-activitie")
    UserActivity addUserActivity(@RequestParam ActivityType activityType, @RequestParam Long objectId, @RequestParam String classe,
                                 @RequestParam String localAdr, @RequestParam String remoteAdr);

    /**
     * Le referentiel des pieces attendues pour une classe, tenu par le kernel.
     *
     * Le parametre est le nom SIMPLE de la classe -- "DemandeImplantation", pas son
     * nom pleinement qualifie : c'est ce que passe le front d'ASI, et l'endpoint
     * l'attend en partie de formulaire multipart. SpringFormEncoder, deja configure
     * dans FeignConfig, s'en charge.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/acl-class-fileDefinition-className",
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AclClassFilesDto getFileDefinitionsByClassName(@RequestPart("className") String className);

    @RequestMapping(method = RequestMethod.GET, value = "/attachements/count")
    Long countAttachements(@RequestParam(value = "objectId") Long objectId,
                           @RequestParam(value = "classId") Long classId);

    @GetMapping(value = "/rulesByName")
    RulesDTO rulesByName(@RequestParam String ruleName);

    @RequestMapping(method = RequestMethod.GET, value = "/checkSecurity")
    String checkSecurity(@RequestParam("simpleName") String simpleName, @RequestParam("id") Long id, @RequestParam("sids") List<String> sids);

    @RequestMapping(method = RequestMethod.POST, value = "/workflow/_nextTask")
    org.json.simple.JSONObject _nextTask(@RequestBody Map<String, Object> variables);


    @RequestMapping(method = RequestMethod.GET, value = "/getInput")
    String getInput(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("name") String name, @RequestParam("type") String type);

    @RequestMapping(method = RequestMethod.POST, value = "/init-class")
    void initClass(@RequestBody InitClass initClass,
                   @RequestParam("tableName") String tableName,
                   @RequestParam("shemaName") String shemaName);

    @RequestMapping(method = RequestMethod.POST, value = "/init-var")
    void initVaraible(@RequestBody InitVariable initVariable);
    @PostMapping("/sequence_number")
    String getSequenceNumberByClass(@RequestBody String jsonObject, @RequestParam String fullClassName);

    @RequestMapping(method = RequestMethod.POST, value = "/objects")
    ObjectsDTO getobjectsDto(@RequestBody ObjectDTO objectDTO);

    @PostMapping("/ref-sequence-formats")
    ResponseEntity<RefSequenceFormat> createRefSequenceFormat(@RequestBody RefSequenceFormat refSequenceFormat);

    @RequestMapping(method = RequestMethod.POST, value = "/init-module")
    void initModule(@RequestBody InitModule initModule);

    @RequestMapping(method = RequestMethod.GET, value = "/acl-class-by-classname")
    AclClass getaclClassByClassName(@RequestParam String classname);


    @GetMapping("/getSids")
    public List<String> getSids(@RequestParam("sid") String sid);



    @RequestMapping(method = RequestMethod.GET, value = "/checkAccess")
    Access checkAccess(@RequestParam("authors") List<String> authors,
                       @RequestParam("readers") List<String> readers,
                       @RequestParam("securityLevel") Integer securityLevel);

    @PostMapping(value = "/publicAttachement")
    void publicAttachement(@RequestBody PublicAttachementDto publicInboundDto);

    @RequestMapping(method = RequestMethod.GET, value = "/find-acl-object-identity")
    Integer findACLObjectIdentity(@RequestParam Long classId, @RequestParam Long objectId);

    @RequestMapping(method = RequestMethod.POST, value = "/encryptFileAccessToken")
    String encryptFileAccessToken(@RequestParam(value = "strToEncrypt") String strToEncrypt);

    @DeleteMapping(value = "/DeleteAttachement")
    public void deleteFileRessource(@RequestParam String uuid,
                                    @RequestParam(value = "fileAccessToken") String fileAccessToken
    );


    @GetMapping("/GetAllAttachement")
    public ResponseEntity<List<AttachementInputDTO>> AttachmentsByClassIdAndObjectId(@RequestParam("classId") Long classId, @RequestParam("objectId") Long objectId,
                                                                                   @RequestParam(value = "fileAccessToken") String fileAccessToken,
                                                                                   @RequestParam(defaultValue = "false", required = false) boolean areHidden);
    @PostMapping("/CloneAttachments")
    public ResponseEntity<AttachementInputDTO> cloneAttachments(
            @RequestParam("sourceClassId") Long sourceClassId,
            @RequestParam("sourceObjectId") Long sourceObjectId,
            @RequestParam("sourceFileAccessToken") String sourceFileAccessToken,
            @RequestParam("targetClassId") Long targetClassId,
            @RequestParam("targetObjectId") Long targetObjectId,
            @RequestParam("targetFileAccessToken") String targetFileAccessToken
    );

    @RequestMapping(method = RequestMethod.POST, value = "/cloneAttachements")
    void cloneAttachements(
            @RequestParam("oldObjectId") Long oldObjectId,
            @RequestParam("oldClassId") Long oldClassId,
            @RequestParam("newObjectId") Long newObjectId,
            @RequestParam("newClassId") Long newClassId,
            @RequestParam("fileAccessToken") String fileAccessToken
    ) throws Exception;

    @GetMapping("/contacts/{id}")
    ResponseEntity<picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO> getContact(@PathVariable Long id);

    @GetMapping("/groups/{groupName}/users")
    ResponseEntity<List<picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO>> getUsersByGroup(@PathVariable String groupName);
    @GetMapping("/ps-sign-image-by-employee/{matricule}")
     ResponseEntity<List<PhysicalSignatureImageDTO>> getAllPhysicalSignaturesImagesByEmployee(@PathVariable String matricule) ;

    @GetMapping("/keycloak/usersByGroupName")
     List<KeycloakUserDTO> usersByGroupName(@RequestParam("groupName") String groupName);
    @GetMapping("/employes-by-keyclock-id")
     ResponseEntity<EmployeDTO> getEmployeByKeyclockId(@RequestParam String keyclockId) ;
}


