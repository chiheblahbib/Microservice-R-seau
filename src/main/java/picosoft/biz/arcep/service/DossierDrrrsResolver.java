package picosoft.biz.arcep.service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;

/**
 * Retrouve le dossier DRRRS auquel une demande de complement est rattachee.
 *
 * Chez homologation, la demande pointe un dossier ASI (colonne asi_id). Ici elle
 * pointe l'un des douze types DRRRS par `typeDossier` (le nom simple de la classe,
 * ex. « DemandeReseau ») et `objectIdDossier` : c'est tout ce qu'il faut pour
 * suspendre puis reprendre son circuit.
 */
@Service
@Transactional(readOnly = true)
public class DossierDrrrsResolver {

    private static final Map<String, Class<?>> TYPES = Map.ofEntries(
            Map.entry("DemandeReseau", DemandeReseau.class),
            Map.entry("DemandeImplantation", DemandeImplantation.class),
            Map.entry("DemandeInstallateur", DemandeInstallateur.class),
            Map.entry("DemandeAeronef", DemandeAeronef.class),
            Map.entry("DemandeNavire", DemandeNavire.class),
            Map.entry("DemandeDeclaratif", DemandeDeclaratif.class),
            Map.entry("DemandeUssd", DemandeUssd.class),
            Map.entry("DemandeMmsi", DemandeMmsi.class),
            Map.entry("DemandePq", DemandePq.class),
            Map.entry("DemandeNumeroCourt", DemandeNumeroCourt.class),
            Map.entry("DemandeNumeroCourtUrgence", DemandeNumeroCourtUrgence.class),
            Map.entry("DemandeIspc", DemandeIspc.class));

    @PersistenceContext
    private EntityManager entityManager;

    /** Les typeDossier des demandes de complément DRRRS (nom simple de la classe du dossier). */
    public static java.util.Set<String> typesDrrrs() {
        return TYPES.keySet();
    }

    public boolean estDrrrs(String typeDossier) {
        return typeDossier != null && TYPES.containsKey(typeDossier);
    }

    /** Le wfProcessID du dossier, s'il existe et a demarre son circuit. */
    public Optional<String> wfProcessID(String typeDossier, Long id) {
        if (!estDrrrs(typeDossier) || id == null) {
            return Optional.empty();
        }
        Object dossier = entityManager.find(TYPES.get(typeDossier), id);
        if (dossier == null) {
            return Optional.empty();
        }
        try {
            Method m = dossier.getClass().getMethod("getWfProcessID");
            return Optional.ofNullable((String) m.invoke(dossier));
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }
}
