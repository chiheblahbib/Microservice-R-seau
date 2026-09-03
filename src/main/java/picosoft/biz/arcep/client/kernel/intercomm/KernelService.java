package picosoft.biz.arcep.client.kernel.intercomm;

import org.springframework.context.annotation.Bean;
import picosoft.biz.arcep.ArcepApplication;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.events.Event;
import picosoft.biz.arcep.client.kernel.model.global.*;
import picosoft.biz.arcep.client.kernel.model.objects.RulesDTO;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import picosoft.biz.arcep.client.kernel.model.pm.Variable;

import javax.persistence.Table;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.*;


@Service
public class KernelService {

    private final Logger log = LoggerFactory.getLogger(KernelService.class);
    private final KernelInterface kernelInterface;
    public static final String reseau_role_canCreateReseau = "reseau_can_create_reseau";
    public static final String reseau_role_canEditReseau = "reseau_can_edit_reseau";
    public static final String reseau_role_canReadReseau = "reseau_can_read_reseau";

    public static final String reseau_role_canCreateSite = "reseau_can_create_site";
    public static final String reseau_role_canEditSite = "reseau_can_edit_site";
    public static final String reseau_role_canReadSite = "reseau_can_read_site";

    public static final String SEQ_RESEAU = "seq_reseau";
    public static final String SEQ_SITE = "seq_site";


    public List<String> sequanceList = Arrays.asList(SEQ_RESEAU, SEQ_SITE);

    public List<String> roles = Arrays.asList(
            reseau_role_canCreateReseau,
            reseau_role_canEditReseau,
            reseau_role_canReadReseau,
            reseau_role_canCreateSite,
            reseau_role_canEditSite,
            reseau_role_canReadSite);


    // ------------------------------------------------------------------
    // Accesseurs consommes par la SpEL des @PreAuthorize :
    //     @PreAuthorize("hasAuthority(@kernelService.<role>())")
    // La forme avec parentheses designe une methode, pas un champ : sans ces
    // accesseurs l'expression ne resout rien le jour ou la securite methode
    // sera activee (elle ne l'est pas aujourd'hui, aucun @EnableGlobalMethodSecurity).
    // ------------------------------------------------------------------

    public String reseau_role_canCreateReseau() {
        return reseau_role_canCreateReseau;
    }

    public String reseau_role_canEditReseau() {
        return reseau_role_canEditReseau;
    }

    public String reseau_role_canReadReseau() {
        return reseau_role_canReadReseau;
    }

    public String reseau_role_canCreateSite() {
        return reseau_role_canCreateSite;
    }

    public String reseau_role_canEditSite() {
        return reseau_role_canEditSite;
    }

    public String reseau_role_canReadSite() {
        return reseau_role_canReadSite;
    }

    public KernelService(KernelInterface kernelInterface) {
        this.kernelInterface = kernelInterface;
    }

    public Boolean applySecurity(String clazz, Long id,
                                 List<String> authors,
                                 List<String> readers,
                                 List<String> tempReaders,
                                 String clazzParent, Long idParent,
                                 Boolean isCreated, Boolean isCumulative) {
        return kernelInterface.applySecurity(
                clazz, id, authors, readers, tempReaders, clazzParent, idParent, isCreated, isCumulative);
    }

    public String Authorize(AuthUser a) {
        return kernelInterface.Authorize(a);
    }

    public CurrentUser getCurrentUser() {
        return kernelInterface.getCurrentUser();
    }


    public String getToken(String apiKey) {
        return kernelInterface.getToken(apiKey);
    }


    public RulesDTO rulesByName(String ruleName) {
        return kernelInterface.rulesByName(ruleName);
    }

    public Event addEvent(String eventName, Object data, Long objectID, String classname) throws JsonProcessingException {
        if (data != null) {
            String json = ObjectToString(data);
            return kernelInterface.addEvent(eventName, json, objectID, classname, null, null, null);
        } else
            return kernelInterface.addEvent(eventName, "", objectID, classname, null, null, null);
    }


    public String ObjectToString(Object data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()).setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = mapper.writeValueAsString(data);
        return json;
    }

    public AclClass getAclClassByClassname(String classname) {
        return kernelInterface.getaclClassByClassName(classname);
    }

    public void initClass(InitClass initClass, String tableName, String shemaName) {
        initClass.setPackagename(ArcepApplication.class.getPackageName());
        kernelInterface.initClass(initClass, tableName, shemaName);
    }



    public String getBuildNumber() {

        Properties properties = new Properties();

        try {

            File file = ResourceUtils.getFile("classpath:build.properties");

            InputStream in = new FileInputStream(file);

            properties.load(in);

        } catch (IOException e) {

            log.error(e.getMessage());

        }

        String build = properties.getProperty("build.version");

        return build;

    }

    /**
     * Declare la classe ACL du dossier reseau aupres du kernel.
     *
     * Une seule classe ici, la ou l'implantation en declare deux : le reseau
     * est autorise d'un bloc, ses sites ne portent pas de circuit propre.
     */
    public void initClassDemandeReseau() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeReseau.class.getSimpleName());
            initClass.setName(DemandeReseau.class.getName());

            HashMap<String, String> event = new HashMap<>();
            HashMap<String, String> state = new HashMap<>();
            state.put("DRAFT", "Brouillon");

            initClass.setEvent(event);
            initClass.setState(state);
            initClass.setDefaultState("DRAFT");

            String tableName = Class.forName(initClass.getName()).getAnnotation(Table.class).name();
            String schemaName = Class.forName(initClass.getName()).getAnnotation(Table.class).schema();
            initClass(initClass, tableName, schemaName);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    public void initSequences() {
        for (String s : sequanceList) {
            try {
                String character = s.split("_")[1].substring(0, 1);
                RefSequenceFormat refSequenceFormat = new RefSequenceFormat();
                refSequenceFormat.setName(s);
                refSequenceFormat.setCounter(1L);
                refSequenceFormat.setdescription(s);
                refSequenceFormat.setStep(1);
                refSequenceFormat.setCounterDate(ZonedDateTime.now());
                refSequenceFormat.setResetInterval(ResetInterval.NEVER);
                refSequenceFormat.setFormat("${counter?string[\"0000\"]}-${.now?string('dd')}-${.now?string('MM')}-${.now?string('yyyy')}");
                kernelInterface.createRefSequenceFormat(refSequenceFormat);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void initVaraible() {
        try {
            InitVariable initVariable = new InitVariable();

            List<Variable> variable = new ArrayList<>();

            variable.add(new Variable(reseau_role_canCreateReseau, "'" + reseau_role_canCreateReseau + "'"));
            variable.add(new Variable(reseau_role_canEditReseau, "'" + reseau_role_canEditReseau + "'"));
            variable.add(new Variable(reseau_role_canReadReseau, "'" + reseau_role_canReadReseau + "'"));

            variable.add(new Variable(reseau_role_canCreateSite, "'" + reseau_role_canCreateSite + "'"));
            variable.add(new Variable(reseau_role_canEditSite, "'" + reseau_role_canEditSite + "'"));
            variable.add(new Variable(reseau_role_canReadSite, "'" + reseau_role_canReadSite + "'"));

            initVariable.setVariable(variable);
            initVariable.setRoleName(roles);

            List<InitVariable.InitProfile> profiles = new ArrayList<>();
            initVariable.setProfiles(profiles);


            kernelInterface.initVaraible(initVariable);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }



}
