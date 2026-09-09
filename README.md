# drrrs

Microservice ARCEP de la **Direction des Réseaux, Radiocommunications et Ressources en
numérotation**. Il porte les **douze formulaires** de la direction, réunis le
9 septembre 2026 en un seul service — ils étaient jusque-là douze microservices
distincts, un par imprimé.

Il descend de `reseau-back`, dont il garde l'historique git ; les onze autres y
ont été versés. Il reprend l'architecture d'`homologation` et d'`implantation`.

La carte de la famille est dans [../README.md](../README.md), les décisions qui
traversent plusieurs services dans [../JOURNAL.md](../JOURNAL.md).

## La fusion des douze services

### Pourquoi

Un microservice par imprimé était la règle jusqu'ici. Elle coûtait douze fois le
même squelette — 147 fichiers rigoureusement identiques dans chacun — douze
schémas, douze déclarations au kernel, douze déploiements, et un front obligé de
viser douze adresses. Les formulaires ne sont pourtant pas douze métiers : c'est
une même direction, un même demandeur, un même circuit d'instruction, douze
rubriques différentes.

### Ce que la fusion a demandé

**Les paquets `domain` ne se marchaient pas dessus** : `domain.aeronef`,
`domain.reseau`… restent tels quels. Rien à renommer de ce côté, la racine
`picosoft.biz.arcep` était déjà commune aux douze.

**Trois tables portaient le même nom pour des choses différentes.** Un seul
schéma ne peut pas les tenir toutes ; elles sont donc distinguées :

| était | devient |
|---|---|
| `equipement_bord` (aéronef **et** navire) | `equipement_bord_aeronef`, `equipement_bord_navire` |
| `numero_rattachement` (numéro court **et** urgence) | `numero_rattachement_court`, `numero_rattachement_urgence` |
| `service_declare` (déclaratif **et** réseau) | `service_declare_declaratif`, `service_declare` |

Les **entités** gardent leur nom, elles vivent dans des paquets distincts. Seuls
les `Repository`, `DTO` et `Mapper` — qui vivent, eux, dans des paquets **plats** —
ont été suffixés : `EquipementBordAeronefDTO`, `NumeroRattachementCourtMapper`…

**Deux tables se répétaient à l'identique et deviennent partagées :**

- `ref_tarif`, douze copies au caractère près. Elle portait déjà une colonne
  `service` : une seule table suffit, les lignes se distinguent d'elles-mêmes.
- `rapport_technique`, dix copies. L'en-tête de ces copies disait pourquoi elles
  existaient : « chaque service est déployable seul ». Cette raison tombe avec la
  fusion. La table revient à la forme d'homologation — une table, une clé
  étrangère nullable par type de dossier.

**Les tables partagées s'ouvrent aux douze dossiers.** `client`, `applicant` et
`attestation` (schéma `homologation`) portaient une clé étrangère vers *le*
dossier de leur service. Elles en portent maintenant douze, nullables, comme
`client` d'homologation porte déjà `asi_id` et `homologation_id`. L'attestation
fait exception sur un point : l'implantation ne l'accroche pas au dossier mais à
la **station** — onze clés vers un dossier, une douzième vers la station.

> Ce que cela fait aux tables qu'utilise **homologation** : rien que des colonnes
> nulles de plus. `ddl-auto=update` ajoute, il n'enlève jamais, et le service
> homologation ne lit aucune de ces colonnes.

**Un seul `KernelService`**, assemblé depuis les douze : 42 rôles, 15 séquences,
14 classes ACL. Les noms de rôles sont repris **mot pour mot** — `aeronef_can_read_aeronef`
reste tel quel, renommer aurait invalidé des habilitations déjà posées en base.

**`seq_rapport_technique` n'existait nulle part.** `ouvrirRapportTechnique`
demandait pourtant une référence au kernel : le rapport sortait sans numéro. Le
format est déclaré avec les quatorze autres.

**Deux adresses entraient en collision.** Tant que les services étaient séparés,
chacun pouvait offrir `/api/initSequences` — il était seul sur son contexte.
Réunis, ils étaient douze à la revendiquer, et Spring refuse de démarrer sur un
chemin ambigu ; `/api/initClassRapportTechnique` posait le même problème à dix
voix. Les deux sont passées dans [`InitKernelController`](src/main/java/picosoft/biz/arcep/controller/InitKernelController.java).
Ce qui reste par formulaire — `/api/initClassDemande<X>` — ne se marche pas dessus.

### Les douze formulaires

| formulaire | dossier | chemin REST | circuit | séquence |
|---|---|---|---|---|
| aéronef | `DemandeAeronef` | `/api/demande-aeronefs` | `processAeronef` | `seq_aeronef` |
| déclaratif | `DemandeDeclaratif` | `/api/demande-declaratifs` | `processDeclaratif` | `seq_declaratif` |
| implantation | `DemandeImplantation` | `/api/demande-implantations` | `processImplantation` | `seq_implantation` |
| implantation (station) | `Station` | `/api/stations` | `processStation` | `seq_station` |
| installateur | `DemandeInstallateur` | `/api/demande-installateurs` | `processInstallateur` | `seq_installateur` |
| ISPC | `DemandeIspc` | `/api/demande-ispcs` | `processIspc` | `seq_ispc` |
| MMSI | `DemandeMmsi` | `/api/demande-mmsis` | `processMmsi` | `seq_mmsi` |
| navire | `DemandeNavire` | `/api/demande-navires` | `processNavire` | `seq_navire` |
| numéro court | `DemandeNumeroCourt` | `/api/demande-numerocourts` | `processNumeroCourt` | `seq_numerocourt` |
| numéro d'urgence | `DemandeNumeroCourtUrgence` | `/api/demande-numerocourturgences` | `processNumeroCourtUrgence` | `seq_numerocourturgence` |
| préfixe / PQ | `DemandePq` | `/api/demande-pqs` | `processPq` | `seq_pq` |
| réseau | `DemandeReseau` | `/api/demande-reseaux` | `processReseau` | `seq_reseau` |
| USSD | `DemandeUssd` | `/api/demande-ussds` | `processUssd` | `seq_ussd` |
| *(rapport d'instruction)* | `RapportTechnique` | — | — | `seq_rapport_technique` |

Identité du service : `spring.application.name=drrrs`, context-path `/drrrs`,
artefact maven `drrrs`.

## Le formulaire réseau

Ce qui suit décrit le **formulaire réseau**, dont ce dépôt est issu et qui reste
le mieux documenté des douze. Les onze autres gardent leur documentation dans
leur dépôt d'origine, qui n'est plus déployé.

### État

### État

Le service **démarre et fonctionne** contre un PostgreSQL local (vérifié le
03/09/2026) :

- les 7 entités créent leurs tables dans le schéma `drrrs` — elles étaient
  dans `reseau` jusqu'au 8 septembre 2026 ;
- Flowable **déploie `processReseau` v1** au démarrage (`act_re_procdef`) ;
- le CRUD fonctionne de bout en bout : `POST` d'un graphe imbriqué complet
  (dossier + titulaire + 2 personnes + 2 types + 2 services + 2 sites +
  1 liaison), relecture par `GET`, mise à jour par `PUT` — **clés étrangères
  vérifiées en base**, et aucune ligne dupliquée par la mise à jour ;
- la reprise en deux temps des liaisons (voir plus bas) a été rejouée : la
  seconde écriture rattache bien les extrémités aux sites ;
- Envers actif, contexte Spring complet.

**Ce qui n'est pas testé** : tout ce qui passe par le kernel — workflow, ACL,
séquences, pièces jointes, notifications. `CurrentUser` interroge le kernel à
chaque requête et il n'est joignable que depuis le LAN du bureau. Le projet n'a
pas de tests automatisés, comme `homologation` et `implantation`.

Les écrans Angular du module DRRRS (`/drrrs/demandes-autorisations-erpt`,
`/drrrs/reseau/...`) compilent, et la charge utile que produit le formulaire a
été rejouée telle quelle contre ce service — mais uniquement par le chemin CRUD,
jamais par le chemin workflow.

#### Quatre défauts trouvés en démarrant, et corrigés

Le service compilait avant chacun d'eux : ils ne se voyaient qu'à l'exécution.

1. `AttestationRepository.findByStationId` — Spring Data dérive ses requêtes au
   démarrage : **tout le contexte échouait**. L'autorisation se rattache au
   dossier, pas à une station : `findByDemandeReseauId`.
2. `mapSortProperty` traduisait `applicantName` vers `applicant.applicantName`,
   propriété qui n'existe pas ici — **HTTP 500 mesuré**. Et le garde-fou des
   tris sur collection nommait encore `stations.` / `frequences.` : il ne
   gardait plus rien. Le tri passe désormais par une **liste blanche**.
3. `exigerPourSoumission` exigeait `client.company`, interdisant à un titulaire
   **particulier** de déposer — alors que le formulaire lui ouvre ce droit.
4. `RefTarifRepository.enVigueur` triait `order by dateEffet desc` sur une
   colonne nullable : PostgreSQL place les NULL en tête, si bien qu'une ligne
   **sans date d'effet l'emportait sur toute révision**. `nulls last` ajouté.

#### Un trou fonctionnel corrigé depuis (03/09/2026, `processReseau` v2)

**Aucune autorisation n'était jamais créée.** `AttestationService.createAttestationForAutorisation`
n'avait aucun appelant : ni Java, ni le diagramme. Chez implantation, c'était le circuit ENFANT
qui la déclenchait (`processStation.bpmn20.xml`) ; le réseau n'en a pas. Corrigé en posant
l'écouteur sur la passerelle `Gw_signataire` (les deux branches — président ou chef centre —
mènent à une signature, et l'autorisation doit exister avant d'être signée) :

```
${empty data.attestations
    ? attestationService.createAttestationForAutorisation(data)
    : execution.setVariable('isCreatedAtt', true)}
```

Trois changements côté Java pour que ça tienne : `DemandeReseau.attestations` (collection
`@OneToMany`, sans `REMOVE` ni `orphanRemoval` — un titre délivré ne disparaît pas avec un
nettoyage de dossier), `DemandeReseauOutputDTO.attestations` (**indispensable** : la garde
`${empty data.attestations}` déréférence ce champ sur le DTO sérialisé dans `data`, une
`PropertyNotFoundException` sinon), et les deux mappers de sortie mis à jour (`uses = {...,
AttestationMapper.class}`). `DemandeReseauService.delete` refuse désormais un dossier qui porte
une attestation, au même titre qu'un dossier engagé.

Déployé et vérifié : `act_re_procdef` porte `processReseau v1` et `v2`, le v2 démarré sans
erreur contre la base locale. **Pas encore rejoué contre un dossier qui va jusqu'à la
signature** — la garde d'idempotence et le rattachement du titre au bon dossier restent à
confirmer sur un circuit complet.

**`createdDate` reste nulle sur un brouillon** : elle n'est posée qu'au dépôt. Le DTO expose
`sysdateCreated` à côté, sur laquelle les écrans se rabattent — sinon la colonne « Date » du
suivi était vide pour tout dossier non encore soumis.

### Premier démarrage

```bash
psql -h localhost -U postgres -f local-setup.sql
```

```bash
.\mvnw.cmd -Plocal spring-boot:run
```

Le profil `local` pointe sur `localhost`, le profil `dev` sur le LAN du bureau.

Les schémas créés par `local-setup.sql` sont **tous** nécessaires : Hibernate crée les tables
mais jamais les schémas. Deux ne sont pas évidents — `kernel` porte une entité ACL
(`StateWorkflow` vers la table `k_e_pa_states`) et `audit` reçoit les révisions Envers **dans
la base primaire**, pas dans `ARCEP-AUDIT` comme son nom le suggère.

### Démarrage en dev (LAN)

```bash
.\mvnw.cmd -Pdev spring-boot:run
```

Pointe sur `ARCEP-DEV` (`192.168.10.135`) et le kernel du bureau (`192.168.10.192:8002`) —
prérequis réseau : LAN, pas de VPN sur ce poste. `application-dev.properties` fixe
`server.port=8080` et `server.servlet.context-path=/reseau` : pour tester sur les mêmes ports
que le front (`proxy.conf.json` attend 8005, racine `/`), surcharger les deux au lancement.

**Le piège** : `-Dserver.servlet.context-path=/` échoue —
`IllegalArgumentException: ContextPath must start with '/' and not end with '/'`. Spring veut
une **chaîne vide** pour la racine, pas `/`. Et le chemin de ce dépôt contenant un espace
(`Arcep Original`), passer les surcharges par `-Dspring-boot.run.jvmArguments="..."` casse le
découpage d'arguments du shell. `SPRING_APPLICATION_JSON` évite les deux :

```bash
export SPRING_APPLICATION_JSON='{"server":{"port":8005,"servlet":{"context-path":""}},"management":{"server":{"port":8005}}}'
.\mvnw.cmd -Pdev spring-boot:run
```

**Ce qui se passe sur la base partagée au premier démarrage** — vérifié le 03/09/2026 :
Hibernate crée les 7 tables dans le schéma `drrrs`, et `ddl-auto=update`
ajoute `client.demande_reseau_id` à une table dont **homologation reste propriétaire**
(217 lignes existantes intactes — `update` n'ajoute que des colonnes, elle est déjà partagée
avec `implantation`, `asi` et `homologation` elle-même via leurs propres FK). Le circuit se
déploie dans le moteur Flowable partagé, à côté de `process`, `processAsi`, `processImplantation`
et `processStation` en production.

**Ce que ça implique pendant que le service tourne** : la JVM rejoint le **cluster Flowable**
du bureau (`flowable.async-executor-activate=true`, aucune propriété ne le désactive côté
`dev`) — elle peut acquérir et exécuter des jobs asynchrones appartenant aux circuits des
autres services. L'arrêter en plein job d'un autre module le laisse verrouillé jusqu'à
expiration du bail. Pas de mode « lecture seule » pour l'éviter aujourd'hui.

Avant qu'une soumission puisse réellement démarrer le circuit : la classe ACL doit être
enregistrée au kernel — `GET /initClassDemandeReseau` sur ce service. C'est un `GET` qui
**écrit** dans le kernel (il crée la ligne `acl_class`, appelle `initVaraible()` et
`initSequences()` au passage) : à déclencher sciemment, pas par curiosité. Il ne pose PAS
`fw_process` — un second geste, en SQL ou via l'admin du kernel, reste nécessaire pour associer
la classe à `processReseau`, sans quoi `getaclClassByClassName` trouve la classe mais aucun
circuit à démarrer.

### Build

Un profil Maven est **obligatoire** (`application.properties` contient
`spring.profiles.active=@spring.profiles.active@`, résolu par filtrage de ressources).
Un `mvn compile` sans `-Plocal` laisse le jeton non résolu : le service démarrerait alors sur
le port 8088 sans source de données.

```bash
.\mvnw.cmd -Plocal clean compile
```

Port **8005**, context-path `/` en local ; 8080 et `/reseau` en `dev`. Le serveur de
développement Angular relaie `/reseau/api` vers ce port (`proxy.conf.json`).

### Déploiement du diagramme

`src/main/resources/processes/processReseau.bpmn20.xml` est **auto-déployé au démarrage** par
Flowable. Aucune propriété à configurer, `checkProcessDefinitions=true` et
`processDefinitionLocationPrefix=classpath*:/processes/` étant les valeurs par défaut.
Flowable dédoublonne par contenu : redémarrer ne crée une version que si le fichier a changé.

Cela vaut pour **tous** les profils : un `-Pdev spring-boot:run` au bureau publiera le circuit
sur `ARCEP-DEV`. Sans danger, la clé est neuve — mais à savoir.

**Ne jamais publier ce fichier via `Import Process` du Modeler** : l'import supprime les blocs
`flowable:properties` (ordre, couleur, icône, message de confirmation) et
`flowable:inputOutput`, mesuré avec `BpmnJsonConverter` 6.7.2. C'est la chaîne d'outils, pas
ce fichier.

### Ce qui est partagé avec homologation

Base `ARCEP-DEV`, schéma `drrrs` pour les tables propres. Trois conséquences :

- **Le moteur Flowable est partagé** en profil `dev` : `processReseau` se déploie dans les
  mêmes tables `ACT_*` que `process`, `processAsi` et `processImplantation`. Plusieurs moteurs
  concurrents sur les mêmes tables de jobs — c'est le mode cluster de Flowable.
- **`Client`, `Commentaire`, `Attestation` sont des copies**, mappées sur le schéma
  `homologation`. **Homologation en reste propriétaire** : ne jamais les modifier depuis ici.
  Les services tournent en `ddl-auto=update`, qui n'ajoute que des colonnes et n'en supprime
  jamais — une divergence produirait une accumulation silencieuse, pas une perte de données.
- `Client` porte ici une FK `demande_reseau_id` en plus des `homologation_id` / `asi_id` /
  `demande_implantation_id` existantes, que ce service ne mappe pas. Ce n'est pas un
  référentiel d'opérateurs : c'est **un enregistrement par dossier**, en 1-1.
  `Commentaire` n'a aucune FK — il se lie par le couple générique `classId` + `objectID`.

### Modèle

```
DemandeReseau ──── dossier unique, porte le circuit processReseau
├─ Client              1-1  le TITULAIRE de l'autorisation (table partagée)
├─ PersonneReseau[]    1-N  le requérant et le responsable, séparés par leur rôle
├─ TypeReseauDeclare[] 1-N  rubrique 6, plusieurs choix possibles
├─ ServiceDeclare[]    1-N  rubrique 7, avec portée pour les seules téléphonies
├─ SiteReseau[]        1-N  coordonnées d/m/s avec hémisphère et méridien
└─ LiaisonReseau[]     1-N  relie deux SiteReseau du même dossier, par identifiant
```

Huit énumérations dans `domain/reseau/enumeration/`.

**Pourquoi `PersonneReseau` et non `Applicant`.** La table partagée `applicant` ne porte pas
de pièce d'identité, que le formulaire exige des deux personnes physiques (rubriques 2 et 3).
L'y ajouter aurait modifié une table dont homologation est propriétaire.

`statutDossier` et `typeDossier` sont des **String**, jamais des énumérations : c'est la
convention de toute la suite et c'est ce que le diagramme compare —
`${data.statutDossier== 'soumis'}`. Le front en est propriétaire.

### Le circuit unique — et pourquoi un seul

#### La différence de fond avec implantation

Implantation a **deux** circuits : `processImplantation` (le dossier administratif) et
`processStation` (l'autorisation technique, une instance par station, engendrée par
`${stationService.initFromDemande(data.id)}` au flux `Pour Validation` du parent). La
séparation existe là-bas parce que `Station` porte un titre (`Attestation`) **réémissible**
indépendamment du dossier — renouvellement, contrôle annuel — sans rouvrir la numérotation ni
la signature commerciale du dossier d'origine.

Le réseau n'a pas cette raison d'être : **une demande vise le réseau entier**, sites et
liaisons compris, et une seule autorisation le couvre — décision métier, pas limite technique.
Il n'y a donc rien à séparer : `DemandeReseau` porte tout, dossier et titre, dans un seul
circuit. `processReseau` a été transposé de `processImplantation` (parent) ; l'écouteur
d'éclatement `${stationService.initFromDemande(data.id)}` qu'il portait a été retiré à cette
occasion — il n'a plus rien vers quoi éclater.

Ce n'est pas non plus la même position que `homologation`, où l'éclatement existe (`Homologation`
par équipement soumis, `initHomologationFromAsi`) mais où le titre se crée dans le circuit
PARENT (`processAsi`) plutôt que dans l'enfant. Trois services, trois choix : implantation
sépare et met le titre dans l'enfant, homologation sépare et met le titre dans le parent,
réseau ne sépare pas du tout.

#### `processReseau` — l'unique circuit

Transposé de `processImplantation`, avec les six premières étapes reprises à l'identique, et
la fin adaptée au signataire du formulaire réseau :

```
Début
 → Saisie & Étude Complétude Dossier ──Soumettre──►
 → Contrôle & Numérotation du Dossier
     ⟲ Retourner
     → Accepter
 → Orientation du Dossier
     ⟲ Pour Vérification
     → Pour Étude
 → Étude Technique
     ⟲ Retourner
     → Pour Validation
 → Validation Rapport Technique
 → [gw] Frais de dossier à percevoir ?
     ├─ oui → Établir l'Ordre de Recette → Validation Ordre de Recette
     └─ non ──┐
 → [gw] Quel signataire ?          ◄── crée l'Attestation (voir plus haut)
     ├─ COMMERCE → Signature du Président du Conseil de Régulation ──┐
     └─ sinon (défaut) → Signature du Dossier ─────────────────────┴──► Clôturé
```

Un seul objet, un seul historique au kernel, une seule classe ACL (`DemandeReseau` →
`fw_process = processReseau`). Pas de tâche technique distincte pour l'installateur ni pour
les équipements — l'étude technique unique du circuit est censée couvrir la validité des
autorisations ARCEP des installateurs déclarés (item d du formulaire) et les justificatifs
d'homologation des équipements (item g) ; à séparer en tâches si l'instruction les confie à
des agents différents.

### Trois pièges hérités, déjà rencontrés sur implantation

**Les références retour doivent être posées avant de persister.** La cascade écrit bien les
enfants, mais c'est l'enfant qui porte la FK. Sans câblage explicite ils sont persistés en
orphelins — et la réponse HTTP paraît correcte, puisqu'elle sérialise le graphe **en mémoire**
et non ce qui est relu de la base. D'où `rattacherEnfants()` dans `DemandeReseauService`,
appelée aussi bien par le CRUD que par le chemin workflow.

**`uuid` est `nullable = false`** et se génère par `@PrePersist` sur l'entité, pas dans le
service : un enfant créé par cascade ne passe jamais par un service dédié.

**`ddl-auto=update` crée les TABLES mais jamais les SCHÉMAS.** D'où `local-setup.sql`.

### Une liaison désigne des sites par leur identifiant

`LiaisonReseau.siteOrigineId` et `siteExtremiteId` pointent des `SiteReseau` du même dossier.
Un site saisi à l'instant n'a pas encore d'identifiant : le back **ne résout pas** de
références symboliques, et une liaison déposée en même temps que ses sites part donc sans
extrémités.

Le formulaire Angular contourne cela en rejouant l'enregistrement une seconde fois, avec les
identifiants que le service vient d'attribuer (`FormulaireReseauComponent.reposerLiaisons`).
**Une résolution côté serveur serait plus propre** — accepter un indice de la liste `sites`
dans la charge utile — mais elle n'a pas été écrite : elle demandait de pouvoir tester, ce que
l'absence de schéma interdit aujourd'hui.

### Transaction canonique

`DemandeReseauService.initAndSubmit` / `submit` reproduisent celle de `HomologationService` :

1. résoudre l'`AclClass` via `kernelInterface.getaclClassByClassName(...)` ;
2. persister l'entité et obtenir sa référence par `getSequenceNumberByClass(...)` ;
3. appeler `workflowService._initAndNextTask(...)` au premier dépôt, `_nextTask(...)` ensuite ;
4. repousser auteurs et lecteurs au kernel par `applySecurity(...)` et rattacher
   l'`AclObjectIdentity`.

### Deux avertissements sur le diagramme

**`Decision` transporte un id de sequence flow**, jamais un libellé. `getGatewayDecision`
renvoie au front `id` (l'id du flux) et `decision` (son `name`, pour le texte du bouton) ;
c'est l'`id` qui revient dans la variable. Un diagramme qui teste le libellé se bloque à
chaque aiguillage, sans erreur.

**Un flux nommé `sys_...` est exclu de la liste des décisions.** C'est ainsi qu'on cache une
branche automatique. Mais mettre `sys_` sur **toutes** les sorties d'une gateway rend muette
la tâche qui la précède : plus aucun bouton, le dossier se bloque.

**Les `@PreAuthorize` ne sont pas actifs.** Il n'y a ni `@EnableGlobalMethodSecurity`, ni
`SecurityFilterChain`, ni `spring-boot-starter-security` — seul `spring-security-acl` est au
pom, pour le modèle ACL. Les annotations sont décoratives, ici comme dans `homologation` ;
l'autorisation est portée en amont par le kernel.

### Ce qui reste à confirmer avec le métier

Le circuit `processReseau` est **transposé** de celui de l'implantation, confirmé le
27/08/2026 pour ce dernier — **rien n'a été validé pour le réseau** :

| № | Question | Hypothèse posée dans le code |
|---|---|---|
| 1 | La suite Saisie → Numérotation → Orientation → Étude technique → Validation du rapport | Supposée identique à l'implantation |
| 2 | Le signataire final | « Signature du Président du Conseil de Régulation », le formulaire lui étant adressé. Branche par défaut : Chef Centre |
| 3 | L'ordre de recette est-il systématique ? | La passerelle `Gw_frais` teste encore `${data.statutDossier== 'soumis'}`, condition héritée. Les frais étant dus dans tous les cas, la branche `Flow_sys_sans_frais` est probablement à supprimer |
| 4 | Valeur de `typeDossier` pour la branche « Présidence » | `'COMMERCE'`, valeur héritée, conservée pour ne pas casser la condition |
| 5 | Barème | 300 000 FCFA pour un réseau PMR seul, 500 000 pour tout autre assemblage. Repris du formulaire, non confirmé |

Deux vérifications que le formulaire demande et que le circuit **ne matérialise pas** en
tâches distinctes, l'étude technique étant censée les couvrir :

- la validité des autorisations ARCEP des installateurs déclarés (item d) ;
- les justificatifs d'homologation des équipements (item g).

À séparer en tâches si l'instruction les confie à des agents différents.

### Un risque qui reste ouvert

`typeDossier` et `statutDossier` sont des **String libres**, posés par le front. Rien côté
serveur ne contraint leurs valeurs : une faute de frappe sur `'COMMERCE'` enverra
silencieusement le dossier au Chef Centre, et une valeur autre que `'soumis'` fera sauter
l'ordre de recette. Aucune erreur ne sera levée. Un contrôle des valeurs acceptées à l'entrée
du service fermerait ce risque.


### Le schéma est passé de `reseau` à `drrrs`

Les sept entités déclarent désormais `schema = "drrrs"`. Les quatre tables
partagées — `Client`, `Applicant`, `Attestation`, `Commentaire` — restent dans
`homologation`, dont il est propriétaire.

**Le schéma doit exister avant le démarrage** : `ddl-auto=update` crée les
tables, jamais les schémas. Vérifié le 8 septembre 2026 : `drrrs` existe sur
`ARCEP-DEV`, mais **pas sur la base locale**, où seul `reseau` subsiste avec
ses sept tables. Un démarrage en profil `local` échouera tant que le schéma
n'aura pas été créé :

```sql
CREATE SCHEMA IF NOT EXISTS drrrs;
```

Les anciennes tables du schéma `reseau` ne sont pas migrées : elles restent
là, vides ou non, sans que rien ne les lise.

### La rubrique demandeur suit celle d'ASI

Le requérant — rubrique 1 — est porté par `Applicant`, la table partagée, et
le titulaire — rubrique 2 — par `Client`. Mêmes huit champs que la rubrique
« Demandeur » d'ASI, et **le référentiel pilote la saisie** : choisir une
société renseigne ses champs et charge ses contacts, choisir un contact
renseigne le demandeur.

Le responsable du réseau — rubrique 3 — garde sa `PersonneReseau` : ce n'est
pas le demandeur mais un tiers, qui répond techniquement du réseau.

**La nature et le numéro de la pièce d'identité ne sont plus saisis.** Le
formulaire les demande, mais les tables partagées ne les portent pas et on
n'y ajoute rien. Le document reste exigé comme pièce jointe.

## Reste à faire, dans l'ordre

### Ce que la fusion laisse ouvert

0. **Le service n'a jamais démarré sous cette forme.** Il compile ; le contexte
   Spring n'a pas été monté, faute d'une base sur laquelle le faire sans écrire.
   Le premier démarrage **créera** dans le schéma `drrrs` la cinquantaine de
   tables des douze formulaires, et **ajoutera** aux trois tables partagées du
   schéma `homologation` les colonnes de clés étrangères décrites plus haut.
   C'est une écriture réelle : à déclencher sciemment.
1. **Les personnes ne sont pas encore toutes sur `client` et `applicant`.** Dix
   formulaires gardent une table `personne_<x>` propre. Elle doit disparaître au
   profit des deux tables partagées, avec un rôle porté par la ligne.
2. **`reseau-back` reste sur le disque** et doit être supprimé. Il était tenu par
   l'IDE au moment de la fusion, d'où une copie plutôt qu'un renommage.
3. **Les onze dépôts d'origine** ne sont plus la source de vérité. Ils gardent
   leur documentation, pas leur code.

### Ce qui restait du formulaire réseau

1. **Enregistrer la classe au kernel.** `KernelService.initClassDemandeReseau()`
   existe et le contrôleur l'expose (`GET /initClassDemandeReseau`) ; sans une
   entrée `acl_class` correspondante, `getaclClassByClassName` renvoie `null` et
   aucun circuit ne démarre. `acl_class.fw_process` doit valoir `processReseau`,
   et `acl_class.class_name` `picosoft.biz.arcep.domain.reseau.DemandeReseau`.

   Ce point d'entrée **n'a jamais été appelé** : c'est un `GET` qui écrit dans
   le kernel. Il est à déclencher sciemment, une fois, depuis le bureau — le
   kernel n'est pas joignable hors du LAN.
2. **Déclarer les pièces attendues** au référentiel, pour la classe
   `DemandeReseau` : c'est `acl-class-fileDefinition-className` qui les rend aux
   deux écrans.
3. **Trancher la création de l'autorisation** (voir « trous fonctionnels »
   ci-dessus) : sans elle, le circuit se termine sans délivrer de titre.
4. **Confirmer le circuit avec le métier** — le tableau plus bas liste les cinq
   hypothèses posées dans le code, dont aucune n'a été validée pour le réseau.
5. **Dérouler un dossier de bout en bout au bureau**, en vérifiant à chaque
   étape que les boutons attendus apparaissent. C'est le seul test qui prouve
   que les conditions du diagramme correspondent.
6. **Rejouer les écrans Angular** contre le chemin workflow : seul le CRUD a
   été confronté à une réponse réelle.
