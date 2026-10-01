# Modèles Jasper du dossier Réseau (DRRRS)

Modèles JasperReports 6.21.2 du circuit `processReseau`. Ils reprennent le style et les conventions
des modèles ASI du kernel (`création des doc/attestation asi.txt` et `déchageAsi.txt`) :

- en-tête : logo ARCEP à gauche, armoiries de la République à droite, liseré vert / jaune / bleu ;
- bandeaux de section gris ;
- lignes libellé | valeur cadrées `#BFC9D1`, en Arial ;
- en bas de dernière page : QR code avec le petit logo au centre, « Signature Electronique Unique »,
  liseré et adresse.

Le fichier `déchageAsi.txt` fourni (et sa copie `rapport technique asi.txt`) contient en réalité le
**rapport technique** ASI. La décharge Réseau reprend donc les rubriques de la décharge ASI du kernel
(modèle `Decharge`, lu sur ARCEP-DEV le 25/09/2026), dans ce même style.

| Fichier | Nom du modèle au kernel | Document | Généré |
|---|---|---|---|
| `DechargeReseau.jrxml` | `DechargeReseau` | Accusé de réception de la demande | à la **Numérotation**, sur « Accepter » (comme l'ASI) |
| `RapportTechniqueReseau.jrxml` | `RapportTechniqueReseau` | Rapport technique | par l'**événement** du même nom, sur « Pour Validation » de l'**Étude Technique** |
| `FicheFacturationReseau.jrxml` | `FicheFacturationReseau` | Fiche de facturation (frais de dossier) | par l'**événement** du même nom, sur « Pour Validation » d'**Établir l'Ordre de Recette** |
| `AttestationReseau.jrxml` | `AttestationReseau` | Autorisation d'établissement et d'exploitation d'un réseau privé, certificat d'une page | aux signatures (Président, puis Chef Centre) |
| `AutorisationReseauPrive.jrxml` | `AutorisationReseauPrive` | Variante : l'acte réglementaire complet (18 articles et son annexe, 3 pages), sur le modèle de l'acte 004/AUT/RP/ARCEP/2024 | au choix du métier |

## Déclaration dans le kernel (Paramétrage Jasper)

Pour chaque modèle :

- **Nom** : celui du tableau (40 caractères au plus).
- **Format de sortie** : PDF.
- **Code** : tout le contenu du fichier `.jrxml`.
- **Ressources**, sauf pour `AutorisationReseauPrive` : joindre les trois images de `ressources/`,
  qui sont celles des modèles ASI. Le nom de chaque ressource doit être exactement celui du
  paramètre : `arcepLogo`, `RGUTJ`, `logo-arcep-1`. Les modèles les reçoivent en `java.io.InputStream`,
  comme l'ASI. Une image absente laisse sa place vide sans faire échouer le rapport.
- `AutorisationReseauPrive` n'a besoin d'aucune ressource : son logo et son filigrane sont intégrés.

Sur ARCEP-DEV, les trois premiers modèles sont déclarés sous les id 33, 34 et 35. Leur code
est celui de ce dossier, recopié le 30/09/2026. `FicheFacturationReseau` y est déclaré
depuis le 01/10/2026, avec les mêmes ressources.

## Données attendues

La racine du JSON est le dossier : `DemandeReseauOutputDTO`, tel que le moteur le pose dans la
variable Flowable `data`, ou tel que le front l'envoie à `jrxmlTemplateTest`. Aucun champ n'est
obligatoire ; un champ absent s'imprime « — » ou laisse la ligne vide.

| Donnée | Chemin JSON | Décharge | Rapport | Attestation |
|---|---|---|---|---|
| Référence du dossier | `reference` | ✓ | ✓ | ✓ (QR) |
| Date et heure de réception | `sendedDate`, sinon `createdDate` | ✓ | | |
| Titulaire, RCCM, adresse, téléphone | `client.company` / `clientName`, `tradeRegisterNumber`, `address`, `phone` | ✓ | ✓ | ✓ |
| Demandeur, qualité, nationalité, e-mail | `applicant.applicantName`, `qualification`, `nationalityComplet`, `email` | ✓ | | ✓ |
| Nature du réseau / de la demande | `natureReseau`, `natureDemande`, `referenceAutorisationAnterieure` | ✓ | ✓ | ✓ |
| Types, services, sites, liaisons | `typesReseau[]`, `services[]`, `sites[]`, `liaisons[]` | types, services, sites | ✓ | ✓ |
| Frais de dossier | `fraisDossier`, `deviseFrais` | ✓ | | |
| Pièces reçues / manquantes | `labelsPostAttachments`, `labelsMissingPostAttachments` | ✓ | | |
| Agent récepteur | `agentDecision`, sinon `assignee` | ✓ | | |
| Dossier traité par | `agentTechnique`, sinon `traitedBy`, sinon `assignee` | | ✓ | |
| Conclusion (ajoutée au JSON par le front) | `conclusion`, sinon `rapportTechnique.conclusion` | | ✓ (sinon « Conclusion non renseignée ») | |
| Numéro et date de l'autorisation | `attestations[0].reference`, `attestations[0].sysdateCreated` | | | ✓ |
| Description des sites | `sites[].description` | | ✓ | |
| Signatures (image base64) | `signatureAgentTechnique`, `signatureChefCentre`, `signaturePresidence` (sinon `signatureDirecteurCommerce`) | | agent technique, chef centre | chef centre, président |

La fiche de facturation lit en plus, ajoutés au JSON par le front :

| Donnée | Chemin JSON | Défaut |
|---|---|---|
| Lignes facturées | `lignesFacturation[]` : `designation`, `codeNature`, `quantite`, `prixUnitaire`, `montant` | une ligne « Frais de dossier — nature du réseau » au montant de `fraisDossier` (`codeNatureFraisDossier` pour son code) |
| Montant en toutes lettres | `montantEnLettres` | ligne omise |
| Agent qui établit la fiche | `agentComptable`, sinon `approvedBy` | « le service Comptabilité » |
| Signatures | `signatureAgentComptable` (sinon `signatureAgentTechnique`, nom ASI), `signatureChefCentre` | cadre vide |

Le total est la somme des montants (`quantite × prixUnitaire` quand `montant` manque). Sans ligne ni
`fraisDossier`, la fiche l'indique et le total s'imprime « — ». Paramètre facultatif `DATE_EMISSION`
(date imprimée, par défaut la date de génération).

`assignee` n'est pas une personne : c'est le libellé de l'étape (`flowable:assignee`, par
exemple « Service Numérotation »). Les modèles le gardent seulement en dernier recours.

- `agentDecision` : le nom affiché de l'agent qui prend la décision en cours. drrrs-back le pose
  sur `data` juste avant de faire avancer le circuit (`DemandeReseauService.submit` et
  `initAndSubmit`). À l'« Accepter » de la Numérotation, c'est donc l'agent qui émet la décharge.
  Le nom vient de `CurrentUser.nomPourCommentaire()` : nom affiché du profil kernel, sinon nom
  de l'employé, sinon nom du jeton Keycloak (`name`, ou prénom et nom), sinon le login. Le
  kernel met le login dans `displayName` quand le profil n'a pas de nom : un nom égal au login
  est donc ignoré.
- `traitedBy` : le login de l'agent technique affecté par le Chef Centre (`updateStep`).
- `agentTechnique` : facultatif. C'est le nom que le front peut ajouter au JSON en générant le
  rapport, pour imprimer un nom plutôt qu'un login.

Le DTO `DemandeReseauOutputDTO` porte un `serialVersionUID` figé : Flowable le stocke sérialisé
dans `data`, et un champ ajouté sans cette précaution rendrait illisibles les dossiers en cours.

Les signatures suivent la convention ASI : le front les ajoute au JSON (voir
`genererModeleAttestation` en ASI). Le préfixe `data:image/...;base64,` est accepté.

Les dates au format Gson (`2026-09-28T13:19:05.48+02:00[Europe/Paris]`) comme au format
Jackson (`…Z`) sont imprimées à l'heure de Libreville.

Paramètres facultatifs, en plus des trois images :

| Paramètre | Modèles | Rôle | Défaut |
|---|---|---|---|
| `FUSEAU_HORAIRE` | tous | fuseau d'impression | `Africa/Libreville` |
| `DATE_TRAITEMENT` | rapport | date de traitement imprimée | date de génération |
| `DATE_SIGNATURE` | attestation | date après « Le : » | date de création de l'attestation, sinon date de génération |
| `LIBELLES` | tous | libellés des énumérations | ceux de `reseau-vocabulaire.ts` |

## Ce qui a été vérifié

Les modèles ont été compilés et remplis avec les bibliothèques de Jaspersoft Studio 6.21.2, sur
les cinq exemples de `exemples/` :

| Exemple | Contenu |
|---|---|
| 1 | dossier 29 de la recette |
| 2 | réseau complet : 3 sites, 3 liaisons, renouvellement, pièces manquantes |
| 3 | signature du Président, et réseau ouvert au public |
| 4 | dossier presque vide |
| 5 | exemple 2 avec toutes les signatures et une conclusion |
| 7 | dossier 30 de la recette, avec `agentDecision` et `traitedBy` |
| 8 | dossier 30 avec les lignes de facturation, le montant en lettres et l'agent comptable (fiche de facturation) |

Chaque exemple a été rempli de trois façons :

- avec la source JSON passée au rapport ;
- en laissant la requête du modèle lire le flux JSON ;
- sans la police DejaVu.

Un dossier courant (un site) tient sur une page. Un grand réseau déborde sur une deuxième page,
mais aucun bloc ne s'y retrouve isolé :

- **Attestation** : le texte, les signatures et le QR code restent en page 1 ; seule l'annexe
  (sites, liaisons) passe en page 2.
- **Décharge** : la section 5 passe entière, avec le QR code.
- **Rapport** : les visas restent avec la conclusion.

Une revue indépendante (30/09/2026) a éprouvé les modèles sur 40 entrées limites : valeurs nulles
ou absentes, caractères spéciaux, nombres en texte ou en décimal, dates dans d'autres formats,
identifiants inconnus, signatures invalides. Les défauts relevés ont été corrigés. Une date
illisible s'imprime « — » au lieu de faire échouer le rapport, et un « null » n'est plus imprimé.

Le moteur du kernel doit :

- être en JasperReports 6.4 ou plus (expressions Java 8 avec lambdas, comme l'ASI) ;
- avoir Batik et zxing pour le QR code, comme pour les modèles ASI.

Pour ouvrir un modèle dans Studio : ajouter un adaptateur « JSON File » sur un fichier de
`exemples/` et laisser la requête vide. Les images ne s'affichent pas en aperçu tant qu'elles ne
sont pas fournies en paramètres.

## Choix à faire valider par le métier

1. **Numéro de l'autorisation.** La référence de la séquence kernel, `0084-28-09-2026`, est
   imprimée au format de l'acte officiel : `N° : 0084/AUT/RP/ARCEP/2026`.
2. **Signataires de l'attestation.** Deux blocs, sur le modèle de l'ASI :
   - « VISA DU CHEF DE CENTRE », avec `signatureChefCentre` ;
   - « LE PRÉSIDENT DU CONSEIL DE RÉGULATION », avec `signaturePresidence`, ou à défaut
     `signatureDirecteurCommerce` : dans `processReseau`, la tâche « Signature du Président »
     est portée par le groupe DirectionCommerce.

   Le QR code se place entre les deux, comme sur l'attestation ASI.
3. **Rapport technique.** drrrs-back n'a pas d'entité `RapportTechnique` pour le réseau (voir
   `domain/drrrs/RapportTechnique.java` : « l'implantation et le reseau n'ont pas de rapport
   technique »). Le modèle décrit le réseau déclaré, sites avec leur description. Il imprime
   la conclusion si le front l'ajoute au JSON ; sinon, « Conclusion non renseignée ».
4. **Service émetteur.** Le visa reste « VISA DU CHEF DE CENTRE DU GUICHET UNIQUE », comme le
   rapport ASI : les groupes du circuit Réseau (ChefCentre, ServiceTechnique…) sont les mêmes.
5. **Adresse.** Le pied de page porte l'adresse actuelle (1030 avenue Paul Moukambi, Kalikak).
   Celle des modèles ASI (Haut de Gué-Gué, +241 01 44 88 11/12) est périmée.
6. **Réseau ouvert au public.** L'attestation est rédigée pour un réseau privé ; un dossier
   `OUVERT_AU_PUBLIC` affiche un bandeau rouge « Modèle réservé aux réseaux privés ».

## Reste à brancher (hors de ces fichiers)

Le fichier local `processReseau.bpmn20.xml` est aligné sur la v22 de l'administrateur,
redéployée en v24.

- **Décharge, à la Numérotation, comme l'ASI :**
  - fait (v25 du 30/09/2026) : `Flow_accepter` (« Accepter ») appelle
    `${createEvent._execute(execution,'DechargeReseau')}`, comme l'ASI appelle `Decharge` ;
  - fait (30/09/2026) : l'événement `DechargeReseau` est déclaré au kernel ARCEP-DEV (id 52), sur
    le modèle de l'événement ASI `Decharge` (id 43) :
    - alias et nom : `DechargeReseau` ;
    - classe : `DemandeReseau` (`picosoft.biz.arcep.domain.reseau.DemandeReseau`) ;
    - modèle de rapport : `DechargeReseau` (id 33), valable depuis le 30/09/2026 ;
    - ni courriel ni notification, comme l'ASI.

    Sur un kernel où il n'existe pas, l'appel est seulement journalisé et le circuit continue ;
  - optionnellement, déclarer `sendDechargeReseau` pour l'envoi par courriel, comme
    `sendDechargeAsi` ;
  - fait : le bouton « Visualiser décharge » du front lit `jrxml-events`.
- **Rapport technique et fiche de facturation : documents d'événement (01/10/2026)**, comme la
  décharge. Ce ne sont pas des pièces jointes :
  - `Flow_pour_validation` (Étude Technique → Validation Rapport) appelle
    `${createEvent._execute(execution,'RapportTechniqueReseau')}` ;
  - `Flow_ordre_validation` (Ordre de Recette → Validation Ordre) appelle
    `${createEvent._execute(execution,'FicheFacturationReseau')}` ;
  - `data` porte la conclusion, saisie par le technicien et enregistrée sur le dossier
    (`DemandeReseau.conclusion`), et le nom de l'agent qui décide (`agentDecision`, imprimé
    « Dossier traité par » / « Établie par ») ;
  - déclarés au kernel ARCEP-DEV le 01/10/2026, par copie de `DechargeReseau` (id 52) dans
    `kernel.k_e_pb_event_types`, liés à leur modèle par `kernel.k_e_pj_et_rt` :
    `RapportTechniqueReseau` (événement 53 → modèle 34) et `FicheFacturationReseau`
    (événement 54 → modèle 36), classe `DemandeReseau` (45), ni courriel ni notification ;
  - l'écran du dossier relit le PDF dans `jrxml-events` (bouton « Visualiser »), comme la
    décharge. Produits par le kernel, ils n'ont ni signature ajoutée par le front ni montant
    en lettres.
- **Autorisation**, comme `genererModeleAttestation` en ASI : générée par le signataire
  (Signature du Président ou du Dossier) avec `AttestationReseau`, signatures Presidence (à
  défaut DirectionCommerce) et ChefCentre (rôle `can_generate_signed_attestation`), déposée
  avec `CreateAttachement` sur `attestations[0]`.
