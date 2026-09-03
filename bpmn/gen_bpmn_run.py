# -*- coding: utf-8 -*-
import sys, io, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_bpmn import Diagramme, ecrire, VERT, ROUGE, BLEU

NOTIF = lambda alias: ("start", "${createEvent._execute(execution,'%s')}" % alias)

ENTETE_PARENT = """<!--
  ============================================================================================
  processImplantation - dossier parent d'implantation de station
  ============================================================================================
  A DEPLOYER EN XML DIRECT. Ne pas passer par "Import Process" du Modeler : l'import
  supprime les blocs <flowable:properties> (ordre / color / icon / decisionMessageConfirmation)
  et <flowable:inputOutput>, mesure avec BpmnJsonConverter 6.7.2. Le process de production
  subit le meme sort : c'est la chaine d'outils, pas ce fichier.

  CONVENTIONS REPRODUITES DEPUIS LES CIRCUITS DEPLOYES
  ---------------------------------------------------
  [1] Les conditions testent l'ID DU FLUX QUI LES PORTE, jamais son libelle :
          ${Decision == 'Flow_xxx'}
      getGatewayDecision renvoie au front 'id' (l'id du flux) et 'decision' (son name,
      pour le texte du bouton). C'est l'id qui revient dans la variable Decision.

  [2] Les flux nommes 'sys_...' sont EXCLUS de la liste des decisions
      (WorkflowService:200). C'est ainsi qu'on cache une branche automatique.

      Ce circuit a deux aiguillages pilotes par la DONNEE et non par l'utilisateur :
      Gw_statut (ordre de recette requis ?) et Gw_type (quel signataire ?).
      Sur chacun, UN SEUL flux reste visible : il fournit le bouton, la branche
      opposee est en 'sys_'. Le routage se fait ensuite sur la condition de donnee,
      la valeur de Decision n'etant pas testee.

      Attention : mettre 'sys_' sur TOUTES les sorties d'une gateway rend muette la
      tache qui la precede -- getGatewayDecision renvoie alors une liste vide et le
      dossier se bloque, sans erreur. C'est le defaut constate sur le brouillon
      dossier_homologation, ou 5 taches sur 8 etaient muettes.

  [3] Les sorties uniques de userTask ne portent PAS de condition : sur une sortie
      unique elle n'apporte aucun routage et transforme un Decision null en
      FlowableException au premier submit.

  TYPAGE DE statutDossier ET typeDossier
  --------------------------------------
  Les deux sont des String, jamais des enumerations : c'est la convention de toute la
  suite (Asi.java:130 et :152) et c'est ce que les circuits deployes comparent, a des
  chaines nues -- ${data.statutDossier== 'soumis'}, ${data.typeDossier== 'ASI'}.
  DemandeImplantation.statutDossier est donc un String, pose par le front.

  SEULE VALEUR RESTANT A CONFIRMER
  --------------------------------
  ${data.typeDossier== 'COMMERCE'} aiguille vers la signature Direction Commerce.
  'COMMERCE' est une valeur SUPPOSEE : le circuit ASI utilise 'ASI' pour son propre
  aiguillage, mais la valeur qui designe un dossier commercial n'est documentee nulle
  part. A confirmer avant mise en service ; sans quoi tous les dossiers partiront
  vers la signature Chef Centre, ce qui est la branche par defaut.
  ============================================================================================
-->"""

ENTETE_ENFANT = """<!--
  ============================================================================================
  processStation - circuit enfant, une instance par station declaree
  ============================================================================================
  A DEPLOYER EN XML DIRECT (voir l'en-tete de processImplantation).

  Calque du process d'homologation deploye : memes etapes de visa puis de signature,
  meme signataire dynamique, meme decharge finale.

  L'AUTORISATION D'IMPLANTATION est creee ici, et seulement ici : une station, une
  autorisation. Le circuit parent n'en cree aucune. L'appel est GARDE contre les
  boucles de retour :
      ${empty data.attestations
          ? attestationService.createAttestationForAutorisation(data)
          : execution.setVariable('isCreatedAtt', true)}
  Sans cette garde, un aller-retour "Pour Verification" recreerait une autorisation
  a chaque passage -- AttestationService n'est pas idempotent.
  ============================================================================================
-->"""

# =========================================================== CIRCUIT PARENT
p = Diagramme("processImplantation", "Processus d'implantation", [
    ("lane_reception", "Réception"),
    ("lane_numerotation", "Numérotation"),
    ("lane_chefcentre", "Chef Centre"),
    ("lane_technique", "Service Technique"),
    ("lane_comptabilite", "Comptabilité"),
    ("lane_commerce", "Direction Commerce"),
])

COMP_ECRITURE = [("SectionDemandeur", "WRITE"), ("SectionStation", "WRITE"),
                 ("SectionSite", "WRITE"), ("SectionAttachment", "WRITE")]
COMP_LECTURE = [("SectionDemandeur", "READ"), ("SectionStation", "READ"),
                ("SectionSite", "READ"), ("SectionAttachment", "READ")]

p.debut("Start_implantation", "Début", "lane_reception", 130)
p.tache("Task_saisie", "Saisie & Étude Complétude Dossier", "lane_reception", 210,
        assignee="Réception", groupes="Reception",
        listeners=[("start", "${execution.setVariable('${data.state}','En attente')}"),
                   NOTIF("DepotImplantationNotif")], composants=COMP_ECRITURE)
p.tache("Task_numerotation", "Contrôle & Numérotation du Dossier", "lane_numerotation", 400,
        assignee="Service Numérotation", groupes="Numerotation", due="P3D",
        listeners=[("start", "${execution.setVariable('${data.state}','En cours')}"),
                   NOTIF("NumeriserImplantationNotif")], composants=COMP_ECRITURE)
p.gateway("Gw_retour_numerotation", "lane_numerotation", 570)
p.tache("Task_orientation", "Orientation du Dossier", "lane_chefcentre", 650,
        assignee="Chef Centre", groupes="ChefCentre", due="P2D",
        listeners=[NOTIF("OrientationImplantationNotif")], composants=COMP_LECTURE)
p.gateway("Gw_retour_orientation", "lane_chefcentre", 820)
p.tache("Task_etude", "Étude Technique", "lane_technique", 900,
        assignee="Service Technique", groupes="ServiceTechnique",
        listeners=[NOTIF("EtudeTechniqueImplantationNotif")], composants=COMP_ECRITURE)
p.gateway("Gw_retour_etude", "lane_technique", 1070)
p.tache("Task_validation_rapport", "Validation Rapport Technique", "lane_chefcentre", 1150,
        assignee="Chef Centre", groupes="ChefCentre",
        listeners=[NOTIF("ValidationRapportImplantationNotif")], composants=COMP_LECTURE)
p.gateway("Gw_statut", "lane_chefcentre", 1320, nom="Ordre de recette requis ?")
p.tache("Task_ordre_recette", "Établir l'Ordre de Recette", "lane_comptabilite", 1400,
        assignee="Service Comptabilité", groupes="Comptabilite",
        listeners=[NOTIF("OrdreRecetteNotif")], composants=COMP_ECRITURE)
p.tache("Task_validation_ordre", "Validation Ordre de Recette", "lane_chefcentre", 1570,
        assignee="Chef Centre", groupes="ChefCentre",
        listeners=[NOTIF("ValidationOrdreRecetteNotif")], composants=COMP_LECTURE)
p.gateway("Gw_type", "lane_chefcentre", 1740, nom="Type de dossier ?")
p.tache("Task_signature_commerce", "Signature Direction Commerce", "lane_commerce", 1820,
        assignee="Direction Commerce", groupes="DirectionCommerce",
        listeners=[NOTIF("SignatureCommerceNotif")], composants=COMP_LECTURE)
p.tache("Task_signature_chef", "Signature du Dossier", "lane_chefcentre", 1820,
        assignee="Chef Centre", groupes="ChefCentre",
        listeners=[NOTIF("SignatureDossierNotif")], composants=COMP_LECTURE)
p.fin("End_implantation", "Clôturé", "lane_chefcentre", 2010,
      listeners=[("start", "${execution.setVariable('${data.state}','Clôturé')}")])

# --- flux : sortie unique de tache -> aucune condition (convention [3])
p.f("Flow_debut", "Start_implantation", "Task_saisie", condition="")
p.f("Flow_soumettre", "Task_saisie", "Task_numerotation", nom="Soumettre",
    condition="", ordre=0, couleur=VERT, icone="submit",
    confirmation="Soumettre le dossier au service numérotation")
p.f("Flow_vers_gw_num", "Task_numerotation", "Gw_retour_numerotation", condition="")
p.f("Flow_accepter", "Gw_retour_numerotation", "Task_orientation", nom="Accepter",
    ordre=0, couleur=VERT, icone="submit", confirmation="Accepter et orienter le dossier")
p.f("Flow_num_retour", "Gw_retour_numerotation", "Task_saisie", nom="Retourner",
    ordre=1, couleur=ROUGE, icone="undo", confirmation="Retourner le dossier à la réception")
p.f("Flow_vers_gw_orientation", "Task_orientation", "Gw_retour_orientation", condition="")
p.f("Flow_pour_etude", "Gw_retour_orientation", "Task_etude", nom="Pour Étude",
    ordre=0, couleur=VERT, icone="submit", confirmation="Transmettre au service technique")
p.f("Flow_orientation_verif", "Gw_retour_orientation", "Task_numerotation",
    nom="Pour Vérification", ordre=1, couleur=ROUGE, icone="undo",
    confirmation="Renvoyer au service numérotation")
p.f("Flow_vers_gw_etude", "Task_etude", "Gw_retour_etude", condition="")
p.f("Flow_pour_validation", "Gw_retour_etude", "Task_validation_rapport",
    nom="Pour Validation", ordre=0, couleur=VERT, icone="submit",
    confirmation="Soumettre le rapport technique à validation")
p.f("Flow_etude_retour", "Gw_retour_etude", "Task_orientation", nom="Retourner",
    ordre=1, couleur=ROUGE, icone="undo", confirmation="Retourner au chef de centre")
p.f("Flow_vers_gw_statut", "Task_validation_rapport", "Gw_statut", condition="")
# --- aiguillage AUTOMATIQUE : nom sys_ -> pas de bouton (convention [2])
# le flux visible fournit le BOUTON ; le routage se fait sur la donnee, pas sur Decision
p.f("Flow_sys_avec_recette", "Gw_statut", "Task_ordre_recette", nom="Soumettre",
    ordre=0, couleur=VERT, icone="submit", confirmation="Soumettre le dossier a la suite du circuit",
    condition="${data.statutDossier== 'soumis'}")
p.f("Flow_sys_sans_recette", "Gw_statut", "Gw_type", nom="sys_sans_ordre_recette",
    condition="${data.statutDossier!= 'soumis'}")
p.f("Flow_ordre_validation", "Task_ordre_recette", "Task_validation_ordre",
    nom="Pour Validation", condition="", ordre=0, couleur=VERT, icone="submit",
    confirmation="Soumettre l'ordre de recette à validation")
p.f("Flow_vers_gw_type", "Task_validation_ordre", "Gw_type", condition="")
p.f("Flow_sys_commerce", "Gw_type", "Task_signature_commerce", nom="Pour Signature",
    ordre=0, couleur=VERT, icone="submit", confirmation="Transmettre a la signature",
    condition="${data.typeDossier== 'COMMERCE'}")
p.f("Flow_sys_chef", "Gw_type", "Task_signature_chef", nom="sys_signature_chef",
    condition="${data.typeDossier!= 'COMMERCE'}")
p.f("Flow_commerce_fin", "Task_signature_commerce", "End_implantation", nom="Confirmer",
    condition="", ordre=0, couleur=VERT, icone="check", confirmation="Signer et clôturer le dossier")
p.f("Flow_chef_fin", "Task_signature_chef", "End_implantation", nom="Confirmer",
    condition="", ordre=0, couleur=VERT, icone="check", confirmation="Signer et clôturer le dossier")

# =========================================================== CIRCUIT ENFANT
e = Diagramme("processStation", "Processus station d'implantation", [
    ("lane_reception", "Réception"),
    ("lane_numerotation", "Numérotation"),
    ("lane_chefcentre", "Chef Centre"),
    ("lane_technique", "Service Technique"),
    ("lane_presidence", "Présidence de l'Autorité"),
])

CS_W = [("SectionStation", "WRITE"), ("SectionSite", "WRITE"),
        ("SectionFrequence", "WRITE"), ("SectionAttachment", "WRITE")]
CS_R = [("SectionStation", "READ"), ("SectionSite", "READ"),
        ("SectionFrequence", "READ"), ("SectionAttachment", "READ")]

e.debut("Start_station", "Début", "lane_reception", 130)
e.tache("Task_s_saisie", "Saisie & Étude Complétude", "lane_reception", 210,
        assignee="Réception", groupes="Reception",
        listeners=[("start", "${execution.setVariable('${data.state}','En attente')}"),
                   NOTIF("DepotStationNotif")], composants=CS_W)
e.tache("Task_s_numerotation", "Contrôle & Numérotation", "lane_numerotation", 400,
        assignee="Service Numérotation", groupes="Numerotation",
        listeners=[("start", "${execution.setVariable('${data.state}','En cours')}"),
                   NOTIF("NumeriserStationNotif")], composants=CS_W)
e.tache("Task_s_orientation", "Orientation du Dossier", "lane_chefcentre", 590,
        assignee="Chef Centre", groupes="ChefCentre", due="P2D",
        listeners=[NOTIF("OrientationStationNotif")], composants=CS_R)
e.gateway("Gw_s_orientation", "lane_chefcentre", 760)
e.tache("Task_s_etude", "Étude Technique", "lane_technique", 840,
        assignee="Service Technique", groupes="ServiceTechnique",
        listeners=[NOTIF("EtudeTechniqueStationNotif")], composants=CS_W)
e.gateway("Gw_s_etude", "lane_technique", 1010)
e.tache("Task_s_visa", "Viser les Certificats", "lane_chefcentre", 1090,
        assignee="Chef Centre", groupes="ChefCentre",
        listeners=[NOTIF("VisaCertificatStationNotif")], composants=CS_R)
e.gateway("Gw_s_visa", "lane_chefcentre", 1260)
e.tache("Task_s_signature", "Signer l'Autorisation", "lane_presidence", 1340,
        assignee="${data.signataire}", groupes="${data.signataire}",
        listeners=[NOTIF("SignatureAutorisationNotif")], composants=CS_R)
e.tache("Task_s_informer", "Informer l'opérateur", "lane_chefcentre", 1530,
        assignee="Chef Centre", groupes="ChefCentre",
        listeners=[NOTIF("InformerOperateurStationNotif")], composants=CS_R)
e.tache("Task_s_decharge", "Décharger la livraison", "lane_numerotation", 1720,
        assignee="Reception & Numerotation", groupes="Numerotation,Reception",
        listeners=[NOTIF("DechargeStationNotif")], composants=CS_R)
e.fin("End_station", "Confirmé & Clôturé", "lane_numerotation", 1910,
      listeners=[("start", "${execution.setVariable('${data.state}','Confirmé & Clôturé')}")])

e.f("Flow_s_debut", "Start_station", "Task_s_saisie", condition="")
e.f("Flow_s_soumettre", "Task_s_saisie", "Task_s_numerotation", nom="Soumettre",
    condition="", ordre=0, couleur=VERT, icone="submit",
    confirmation="Soumettre la station au service numérotation")
e.f("Flow_s_pour_validation", "Task_s_numerotation", "Task_s_orientation",
    nom="Pour Validation", condition="", ordre=0, couleur=VERT, icone="submit",
    confirmation="Transmettre au chef de centre")
e.f("Flow_s_vers_gw_orientation", "Task_s_orientation", "Gw_s_orientation", condition="")
e.f("Flow_s_pour_etude", "Gw_s_orientation", "Task_s_etude", nom="Pour Étude",
    ordre=0, couleur=VERT, icone="submit", confirmation="Transmettre au service technique")
e.f("Flow_s_orientation_verif", "Gw_s_orientation", "Task_s_numerotation",
    nom="Pour Vérification", ordre=1, couleur=ROUGE, icone="undo",
    confirmation="Renvoyer au service numérotation")
e.f("Flow_s_vers_gw_etude", "Task_s_etude", "Gw_s_etude", condition="")
e.f("Flow_s_visa", "Gw_s_etude", "Task_s_visa", nom="Visa des Certificats",
    ordre=0, couleur=VERT, icone="submit", confirmation="Soumettre les certificats au visa")
e.f("Flow_s_etude_retour", "Gw_s_etude", "Task_s_orientation", nom="Retourner",
    ordre=1, couleur=ROUGE, icone="undo", confirmation="Retourner au chef de centre")
e.f("Flow_s_vers_gw_visa", "Task_s_visa", "Gw_s_visa", condition="")
# --- l'autorisation est creee ici, gardee contre les boucles de retour
e.f("Flow_s_pour_signature", "Gw_s_visa", "Task_s_signature", nom="Envoyer pour Signature",
    ordre=0, couleur=VERT, icone="submit", confirmation="Envoyer l'autorisation à la signature",
    listeners=[("take", "${empty data.attestations ? "
                        "attestationService.createAttestationForAutorisation(data) : "
                        "execution.setVariable('isCreatedAtt', true)}")])
e.f("Flow_s_visa_verif", "Gw_s_visa", "Task_s_etude", nom="Pour Vérification",
    ordre=1, couleur=ROUGE, icone="undo", confirmation="Renvoyer au service technique")
e.f("Flow_s_informer", "Task_s_signature", "Task_s_informer", nom="Soumettre",
    condition="", ordre=0, couleur=VERT, icone="submit", confirmation="Informer l'opérateur")
e.f("Flow_s_decharge", "Task_s_informer", "Task_s_decharge", nom="Soumettre",
    condition="", ordre=0, couleur=VERT, icone="submit", confirmation="Transmettre pour décharge")
e.f("Flow_s_confirmer", "Task_s_decharge", "End_station", nom="Confirmer",
    condition="", ordre=0, couleur=VERT, icone="check", confirmation="Confirmer et clôturer")

print("BPMN generes :")
ecrire(p, "processImplantation.bpmn20.xml", ENTETE_PARENT)
ecrire(e, "processStation.bpmn20.xml", ENTETE_ENFANT)
