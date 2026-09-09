package picosoft.biz.arcep.client.kernel.intercomm;

import org.springframework.context.annotation.Bean;
import picosoft.biz.arcep.ArcepApplication;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.events.Event;
import picosoft.biz.arcep.client.kernel.model.global.*;
import picosoft.biz.arcep.client.kernel.model.objects.RulesDTO;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
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
    public static final String aeronef_role_canCreateAeronef = "aeronef_can_create_aeronef";
    public static final String aeronef_role_canEditAeronef = "aeronef_can_edit_aeronef";
    public static final String aeronef_role_canReadAeronef = "aeronef_can_read_aeronef";

    public static final String declaratif_role_canCreateDeclaratif = "declaratif_can_create_declaratif";
    public static final String declaratif_role_canEditDeclaratif = "declaratif_can_edit_declaratif";
    public static final String declaratif_role_canReadDeclaratif = "declaratif_can_read_declaratif";

    public static final String implantation_role_canCreateImplantation = "implantation_can_create_implantation";
    public static final String implantation_role_canEditImplantation = "implantation_can_edit_implantation";
    public static final String implantation_role_canReadImplantation = "implantation_can_read_implantation";

    public static final String station_role_canCreateStation = "station_can_create_station";
    public static final String station_role_canEditStation = "station_can_edit_station";
    public static final String station_role_canReadStation = "station_can_read_station";

    public static final String installateur_role_canCreateInstallateur = "installateur_can_create_installateur";
    public static final String installateur_role_canEditInstallateur = "installateur_can_edit_installateur";
    public static final String installateur_role_canReadInstallateur = "installateur_can_read_installateur";

    public static final String ispc_role_canCreateIspc = "ispc_can_create_ispc";
    public static final String ispc_role_canEditIspc = "ispc_can_edit_ispc";
    public static final String ispc_role_canReadIspc = "ispc_can_read_ispc";

    public static final String mmsi_role_canCreateMmsi = "mmsi_can_create_mmsi";
    public static final String mmsi_role_canEditMmsi = "mmsi_can_edit_mmsi";
    public static final String mmsi_role_canReadMmsi = "mmsi_can_read_mmsi";

    public static final String navire_role_canCreateNavire = "navire_can_create_navire";
    public static final String navire_role_canEditNavire = "navire_can_edit_navire";
    public static final String navire_role_canReadNavire = "navire_can_read_navire";

    public static final String numerocourt_role_canCreateNumeroCourt = "numerocourt_can_create_numerocourt";
    public static final String numerocourt_role_canEditNumeroCourt = "numerocourt_can_edit_numerocourt";
    public static final String numerocourt_role_canReadNumeroCourt = "numerocourt_can_read_numerocourt";

    public static final String numerocourturgence_role_canCreateNumeroCourtUrgence = "numerocourturgence_can_create_numerocourturgence";
    public static final String numerocourturgence_role_canEditNumeroCourtUrgence = "numerocourturgence_can_edit_numerocourturgence";
    public static final String numerocourturgence_role_canReadNumeroCourtUrgence = "numerocourturgence_can_read_numerocourturgence";

    public static final String pq_role_canCreatePq = "pq_can_create_pq";
    public static final String pq_role_canEditPq = "pq_can_edit_pq";
    public static final String pq_role_canReadPq = "pq_can_read_pq";

    public static final String reseau_role_canCreateReseau = "reseau_can_create_reseau";
    public static final String reseau_role_canEditReseau = "reseau_can_edit_reseau";
    public static final String reseau_role_canReadReseau = "reseau_can_read_reseau";

    public static final String reseau_role_canCreateSite = "reseau_can_create_site";
    public static final String reseau_role_canEditSite = "reseau_can_edit_site";
    public static final String reseau_role_canReadSite = "reseau_can_read_site";

    public static final String ussd_role_canCreateUssd = "ussd_can_create_ussd";
    public static final String ussd_role_canEditUssd = "ussd_can_edit_ussd";
    public static final String ussd_role_canReadUssd = "ussd_can_read_ussd";

    public static final String SEQ_AERONEF = "seq_aeronef";
    public static final String SEQ_DECLARATIF = "seq_declaratif";
    public static final String SEQ_IMPLANTATION = "seq_implantation";
    public static final String SEQ_STATION = "seq_station";
    public static final String SEQ_INSTALLATEUR = "seq_installateur";
    public static final String SEQ_ISPC = "seq_ispc";
    public static final String SEQ_MMSI = "seq_mmsi";
    public static final String SEQ_NAVIRE = "seq_navire";
    public static final String SEQ_NUMEROCOURT = "seq_numerocourt";
    public static final String SEQ_NUMEROCOURTURGENCE = "seq_numerocourturgence";
    public static final String SEQ_PQ = "seq_pq";
    public static final String SEQ_RESEAU = "seq_reseau";
    public static final String SEQ_SITE = "seq_site";
    public static final String SEQ_USSD = "seq_ussd";
    public static final String SEQ_RAPPORT_TECHNIQUE = "seq_rapport_technique";


    public List<String> sequanceList = Arrays.asList(
            SEQ_AERONEF,
            SEQ_DECLARATIF,
            SEQ_IMPLANTATION,
            SEQ_STATION,
            SEQ_INSTALLATEUR,
            SEQ_ISPC,
            SEQ_MMSI,
            SEQ_NAVIRE,
            SEQ_NUMEROCOURT,
            SEQ_NUMEROCOURTURGENCE,
            SEQ_PQ,
            SEQ_RESEAU,
            SEQ_SITE,
            SEQ_USSD,
            SEQ_RAPPORT_TECHNIQUE);

    public List<String> roles = Arrays.asList(
            aeronef_role_canCreateAeronef,
            aeronef_role_canEditAeronef,
            aeronef_role_canReadAeronef,
            declaratif_role_canCreateDeclaratif,
            declaratif_role_canEditDeclaratif,
            declaratif_role_canReadDeclaratif,
            implantation_role_canCreateImplantation,
            implantation_role_canEditImplantation,
            implantation_role_canReadImplantation,
            station_role_canCreateStation,
            station_role_canEditStation,
            station_role_canReadStation,
            installateur_role_canCreateInstallateur,
            installateur_role_canEditInstallateur,
            installateur_role_canReadInstallateur,
            ispc_role_canCreateIspc,
            ispc_role_canEditIspc,
            ispc_role_canReadIspc,
            mmsi_role_canCreateMmsi,
            mmsi_role_canEditMmsi,
            mmsi_role_canReadMmsi,
            navire_role_canCreateNavire,
            navire_role_canEditNavire,
            navire_role_canReadNavire,
            numerocourt_role_canCreateNumeroCourt,
            numerocourt_role_canEditNumeroCourt,
            numerocourt_role_canReadNumeroCourt,
            numerocourturgence_role_canCreateNumeroCourtUrgence,
            numerocourturgence_role_canEditNumeroCourtUrgence,
            numerocourturgence_role_canReadNumeroCourtUrgence,
            pq_role_canCreatePq,
            pq_role_canEditPq,
            pq_role_canReadPq,
            reseau_role_canCreateReseau,
            reseau_role_canEditReseau,
            reseau_role_canReadReseau,
            reseau_role_canCreateSite,
            reseau_role_canEditSite,
            reseau_role_canReadSite,
            ussd_role_canCreateUssd,
            ussd_role_canEditUssd,
            ussd_role_canReadUssd);


    // ------------------------------------------------------------------
    // Accesseurs consommes par la SpEL des @PreAuthorize :
    //     @PreAuthorize("hasAuthority(@kernelService.<role>())")
    // La forme avec parentheses designe une methode, pas un champ : sans ces
    // accesseurs l'expression ne resout rien le jour ou la securite methode
    // sera activee (elle ne l'est pas aujourd'hui, aucun @EnableGlobalMethodSecurity).
    // ------------------------------------------------------------------

    public String aeronef_role_canCreateAeronef() {
        return aeronef_role_canCreateAeronef;
    }

    public String aeronef_role_canEditAeronef() {
        return aeronef_role_canEditAeronef;
    }

    public String aeronef_role_canReadAeronef() {
        return aeronef_role_canReadAeronef;
    }

    public String declaratif_role_canCreateDeclaratif() {
        return declaratif_role_canCreateDeclaratif;
    }

    public String declaratif_role_canEditDeclaratif() {
        return declaratif_role_canEditDeclaratif;
    }

    public String declaratif_role_canReadDeclaratif() {
        return declaratif_role_canReadDeclaratif;
    }

    public String implantation_role_canCreateImplantation() {
        return implantation_role_canCreateImplantation;
    }

    public String implantation_role_canEditImplantation() {
        return implantation_role_canEditImplantation;
    }

    public String implantation_role_canReadImplantation() {
        return implantation_role_canReadImplantation;
    }

    public String station_role_canCreateStation() {
        return station_role_canCreateStation;
    }

    public String station_role_canEditStation() {
        return station_role_canEditStation;
    }

    public String station_role_canReadStation() {
        return station_role_canReadStation;
    }

    public String installateur_role_canCreateInstallateur() {
        return installateur_role_canCreateInstallateur;
    }

    public String installateur_role_canEditInstallateur() {
        return installateur_role_canEditInstallateur;
    }

    public String installateur_role_canReadInstallateur() {
        return installateur_role_canReadInstallateur;
    }

    public String ispc_role_canCreateIspc() {
        return ispc_role_canCreateIspc;
    }

    public String ispc_role_canEditIspc() {
        return ispc_role_canEditIspc;
    }

    public String ispc_role_canReadIspc() {
        return ispc_role_canReadIspc;
    }

    public String mmsi_role_canCreateMmsi() {
        return mmsi_role_canCreateMmsi;
    }

    public String mmsi_role_canEditMmsi() {
        return mmsi_role_canEditMmsi;
    }

    public String mmsi_role_canReadMmsi() {
        return mmsi_role_canReadMmsi;
    }

    public String navire_role_canCreateNavire() {
        return navire_role_canCreateNavire;
    }

    public String navire_role_canEditNavire() {
        return navire_role_canEditNavire;
    }

    public String navire_role_canReadNavire() {
        return navire_role_canReadNavire;
    }

    public String numerocourt_role_canCreateNumeroCourt() {
        return numerocourt_role_canCreateNumeroCourt;
    }

    public String numerocourt_role_canEditNumeroCourt() {
        return numerocourt_role_canEditNumeroCourt;
    }

    public String numerocourt_role_canReadNumeroCourt() {
        return numerocourt_role_canReadNumeroCourt;
    }

    public String numerocourturgence_role_canCreateNumeroCourtUrgence() {
        return numerocourturgence_role_canCreateNumeroCourtUrgence;
    }

    public String numerocourturgence_role_canEditNumeroCourtUrgence() {
        return numerocourturgence_role_canEditNumeroCourtUrgence;
    }

    public String numerocourturgence_role_canReadNumeroCourtUrgence() {
        return numerocourturgence_role_canReadNumeroCourtUrgence;
    }

    public String pq_role_canCreatePq() {
        return pq_role_canCreatePq;
    }

    public String pq_role_canEditPq() {
        return pq_role_canEditPq;
    }

    public String pq_role_canReadPq() {
        return pq_role_canReadPq;
    }

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

    public String ussd_role_canCreateUssd() {
        return ussd_role_canCreateUssd;
    }

    public String ussd_role_canEditUssd() {
        return ussd_role_canEditUssd;
    }

    public String ussd_role_canReadUssd() {
        return ussd_role_canReadUssd;
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

    /** Declare la classe ACL de DemandeAeronef aupres du kernel. */
    public void initClassDemandeAeronef() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeAeronef.class.getSimpleName());
            initClass.setName(DemandeAeronef.class.getName());

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

    /** Declare la classe ACL de DemandeDeclaratif aupres du kernel. */
    public void initClassDemandeDeclaratif() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeDeclaratif.class.getSimpleName());
            initClass.setName(DemandeDeclaratif.class.getName());

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

    /** Declare la classe ACL de DemandeImplantation aupres du kernel. */
    public void initClassDemandeImplantation() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeImplantation.class.getSimpleName());
            initClass.setName(DemandeImplantation.class.getName());

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

    /** Declare la classe ACL de Station aupres du kernel. */
    public void initClassStation() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(Station.class.getSimpleName());
            initClass.setName(Station.class.getName());

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

    /** Declare la classe ACL de DemandeInstallateur aupres du kernel. */
    public void initClassDemandeInstallateur() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeInstallateur.class.getSimpleName());
            initClass.setName(DemandeInstallateur.class.getName());

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

    /** Declare la classe ACL de DemandeIspc aupres du kernel. */
    public void initClassDemandeIspc() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeIspc.class.getSimpleName());
            initClass.setName(DemandeIspc.class.getName());

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

    /** Declare la classe ACL de DemandeMmsi aupres du kernel. */
    public void initClassDemandeMmsi() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeMmsi.class.getSimpleName());
            initClass.setName(DemandeMmsi.class.getName());

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

    /** Declare la classe ACL de DemandeNavire aupres du kernel. */
    public void initClassDemandeNavire() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeNavire.class.getSimpleName());
            initClass.setName(DemandeNavire.class.getName());

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

    /** Declare la classe ACL de DemandeNumeroCourt aupres du kernel. */
    public void initClassDemandeNumeroCourt() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeNumeroCourt.class.getSimpleName());
            initClass.setName(DemandeNumeroCourt.class.getName());

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

    /** Declare la classe ACL de DemandeNumeroCourtUrgence aupres du kernel. */
    public void initClassDemandeNumeroCourtUrgence() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeNumeroCourtUrgence.class.getSimpleName());
            initClass.setName(DemandeNumeroCourtUrgence.class.getName());

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

    /** Declare la classe ACL de DemandePq aupres du kernel. */
    public void initClassDemandePq() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandePq.class.getSimpleName());
            initClass.setName(DemandePq.class.getName());

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

    /** Declare la classe ACL de DemandeReseau aupres du kernel. */
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

    /** Declare la classe ACL de DemandeUssd aupres du kernel. */
    public void initClassDemandeUssd() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(DemandeUssd.class.getSimpleName());
            initClass.setName(DemandeUssd.class.getName());

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

    /** Declare la classe ACL de RapportTechnique aupres du kernel. */
    public void initClassRapportTechnique() {
        try {
            InitClass initClass = new InitClass();
            initClass.setSimpleName(RapportTechnique.class.getSimpleName());
            initClass.setName(RapportTechnique.class.getName());

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

            variable.add(new Variable(aeronef_role_canCreateAeronef, "'" + aeronef_role_canCreateAeronef + "'"));
            variable.add(new Variable(aeronef_role_canEditAeronef, "'" + aeronef_role_canEditAeronef + "'"));
            variable.add(new Variable(aeronef_role_canReadAeronef, "'" + aeronef_role_canReadAeronef + "'"));

            variable.add(new Variable(declaratif_role_canCreateDeclaratif, "'" + declaratif_role_canCreateDeclaratif + "'"));
            variable.add(new Variable(declaratif_role_canEditDeclaratif, "'" + declaratif_role_canEditDeclaratif + "'"));
            variable.add(new Variable(declaratif_role_canReadDeclaratif, "'" + declaratif_role_canReadDeclaratif + "'"));

            variable.add(new Variable(implantation_role_canCreateImplantation, "'" + implantation_role_canCreateImplantation + "'"));
            variable.add(new Variable(implantation_role_canEditImplantation, "'" + implantation_role_canEditImplantation + "'"));
            variable.add(new Variable(implantation_role_canReadImplantation, "'" + implantation_role_canReadImplantation + "'"));

            variable.add(new Variable(station_role_canCreateStation, "'" + station_role_canCreateStation + "'"));
            variable.add(new Variable(station_role_canEditStation, "'" + station_role_canEditStation + "'"));
            variable.add(new Variable(station_role_canReadStation, "'" + station_role_canReadStation + "'"));

            variable.add(new Variable(installateur_role_canCreateInstallateur, "'" + installateur_role_canCreateInstallateur + "'"));
            variable.add(new Variable(installateur_role_canEditInstallateur, "'" + installateur_role_canEditInstallateur + "'"));
            variable.add(new Variable(installateur_role_canReadInstallateur, "'" + installateur_role_canReadInstallateur + "'"));

            variable.add(new Variable(ispc_role_canCreateIspc, "'" + ispc_role_canCreateIspc + "'"));
            variable.add(new Variable(ispc_role_canEditIspc, "'" + ispc_role_canEditIspc + "'"));
            variable.add(new Variable(ispc_role_canReadIspc, "'" + ispc_role_canReadIspc + "'"));

            variable.add(new Variable(mmsi_role_canCreateMmsi, "'" + mmsi_role_canCreateMmsi + "'"));
            variable.add(new Variable(mmsi_role_canEditMmsi, "'" + mmsi_role_canEditMmsi + "'"));
            variable.add(new Variable(mmsi_role_canReadMmsi, "'" + mmsi_role_canReadMmsi + "'"));

            variable.add(new Variable(navire_role_canCreateNavire, "'" + navire_role_canCreateNavire + "'"));
            variable.add(new Variable(navire_role_canEditNavire, "'" + navire_role_canEditNavire + "'"));
            variable.add(new Variable(navire_role_canReadNavire, "'" + navire_role_canReadNavire + "'"));

            variable.add(new Variable(numerocourt_role_canCreateNumeroCourt, "'" + numerocourt_role_canCreateNumeroCourt + "'"));
            variable.add(new Variable(numerocourt_role_canEditNumeroCourt, "'" + numerocourt_role_canEditNumeroCourt + "'"));
            variable.add(new Variable(numerocourt_role_canReadNumeroCourt, "'" + numerocourt_role_canReadNumeroCourt + "'"));

            variable.add(new Variable(numerocourturgence_role_canCreateNumeroCourtUrgence, "'" + numerocourturgence_role_canCreateNumeroCourtUrgence + "'"));
            variable.add(new Variable(numerocourturgence_role_canEditNumeroCourtUrgence, "'" + numerocourturgence_role_canEditNumeroCourtUrgence + "'"));
            variable.add(new Variable(numerocourturgence_role_canReadNumeroCourtUrgence, "'" + numerocourturgence_role_canReadNumeroCourtUrgence + "'"));

            variable.add(new Variable(pq_role_canCreatePq, "'" + pq_role_canCreatePq + "'"));
            variable.add(new Variable(pq_role_canEditPq, "'" + pq_role_canEditPq + "'"));
            variable.add(new Variable(pq_role_canReadPq, "'" + pq_role_canReadPq + "'"));

            variable.add(new Variable(reseau_role_canCreateReseau, "'" + reseau_role_canCreateReseau + "'"));
            variable.add(new Variable(reseau_role_canEditReseau, "'" + reseau_role_canEditReseau + "'"));
            variable.add(new Variable(reseau_role_canReadReseau, "'" + reseau_role_canReadReseau + "'"));

            variable.add(new Variable(reseau_role_canCreateSite, "'" + reseau_role_canCreateSite + "'"));
            variable.add(new Variable(reseau_role_canEditSite, "'" + reseau_role_canEditSite + "'"));
            variable.add(new Variable(reseau_role_canReadSite, "'" + reseau_role_canReadSite + "'"));

            variable.add(new Variable(ussd_role_canCreateUssd, "'" + ussd_role_canCreateUssd + "'"));
            variable.add(new Variable(ussd_role_canEditUssd, "'" + ussd_role_canEditUssd + "'"));
            variable.add(new Variable(ussd_role_canReadUssd, "'" + ussd_role_canReadUssd + "'"));

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
