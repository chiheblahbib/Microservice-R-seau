# -*- coding: utf-8 -*-
"""Genere les deux diagrammes BPMN 2.0 du service implantation.

Mecanismes reproduits a l'identique depuis les circuits deployes :
  - conditions          ${Decision == '<id du flux porteur>'}   (jamais le libelle)
  - flux automatiques   nom prefixe 'sys_' -> exclu de getGatewayDecision (WorkflowService:200)
  - habilitations       flowable:candidateGroups / flowable:assignee
  - echeances           flowable:dueDate
  - boutons du front    flowable:properties : ordre, color, icon, decisionMessageConfirmation
  - notifications       ${createEvent._execute(execution,'XxxNotif')}
  - composants du front flowable:inputOutput / components
"""
import io, os

SORTIE = r"C:/Users/chyhe/Desktop/implantation-back/bpmn"
os.makedirs(SORTIE, exist_ok=True)

VERT = "bg-green-600 hover:bg-green-700 text-white"
ROUGE = "bg-red-600 hover:bg-red-700 text-white"
BLEU = "bg-blue-600 hover:bg-blue-700 text-white"

LARGEUR_T, HAUTEUR_T = 130, 70
LARGEUR_G = 40
HAUTEUR_LANE = 150
X0, Y0 = 120, 40


def esc(t):
    return (t.replace("&", "&#38;").replace("<", "&#60;").replace(">", "&#62;")
             .replace('"', "&#34;").replace("'", "&#39;"))


def attr(t):
    """echappement pour un contenu d'attribut XML"""
    return t.replace("&", "&amp;").replace('"', "&quot;").replace("<", "&lt;")


class Diagramme:
    def __init__(self, cle, nom, lanes):
        self.cle, self.nom = cle, nom
        self.lanes = lanes                    # [(id, nom)]
        self.noeuds = []                      # dicts
        self.flux = []
        self.y_lane = {l[0]: Y0 + i * HAUTEUR_LANE for i, l in enumerate(lanes)}

    # ------------------------------------------------------------- noeuds
    def tache(self, nid, nom, lane, x, assignee=None, groupes=None, due=None,
              listeners=(), composants=None):
        self.noeuds.append(dict(type="userTask", id=nid, nom=nom, lane=lane, x=x,
                                assignee=assignee, groupes=groupes, due=due,
                                listeners=list(listeners), composants=composants,
                                w=LARGEUR_T, h=HAUTEUR_T))

    def gateway(self, nid, lane, x, nom=None):
        self.noeuds.append(dict(type="exclusiveGateway", id=nid, nom=nom, lane=lane, x=x,
                                listeners=[], w=LARGEUR_G, h=LARGEUR_G))

    def debut(self, nid, nom, lane, x):
        self.noeuds.append(dict(type="startEvent", id=nid, nom=nom, lane=lane, x=x,
                                listeners=[], w=30, h=30))

    def fin(self, nid, nom, lane, x, listeners=()):
        self.noeuds.append(dict(type="endEvent", id=nid, nom=nom, lane=lane, x=x,
                                listeners=list(listeners), w=28, h=28, composants=None))

    # --------------------------------------------------------------- flux
    def f(self, fid, src, tgt, nom=None, condition=None, ordre=None, couleur=None,
          icone=None, confirmation=None, listeners=()):
        """condition=None -> ${Decision == '<fid>'} si le flux est nomme et non sys_ ;
        condition='' -> aucune condition ; sinon expression libre."""
        self.flux.append(dict(id=fid, src=src, tgt=tgt, nom=nom, condition=condition,
                              ordre=ordre, couleur=couleur, icone=icone,
                              confirmation=confirmation, listeners=list(listeners)))

    # ---------------------------------------------------------------- XML
    def _noeud(self, n):
        pos = "      "
        if n["type"] == "userTask":
            a = ['<userTask id="%s" name="%s"' % (n["id"], esc(n["nom"]))]
            if n["assignee"]:
                a.append('flowable:assignee="%s"' % attr(n["assignee"]))
            if n["groupes"]:
                a.append('flowable:candidateGroups="%s"' % attr(n["groupes"]))
            if n["due"]:
                a.append('flowable:dueDate="%s"' % n["due"])
            a.append('flowable:formFieldValidation="true"')
            ouvre = pos[2:] + " ".join(a) + ">"
        elif n["type"] == "exclusiveGateway":
            nom = ' name="%s"' % esc(n["nom"]) if n["nom"] else ""
            if not n["listeners"]:
                return pos[2:] + '<exclusiveGateway id="%s"%s />' % (n["id"], nom)
            ouvre = pos[2:] + '<exclusiveGateway id="%s"%s>' % (n["id"], nom)
        else:
            nom = ' name="%s"' % esc(n["nom"]) if n["nom"] else ""
            if not n["listeners"] and not n.get("composants"):
                return pos[2:] + '<%s id="%s"%s />' % (n["type"], n["id"], nom)
            ouvre = pos[2:] + '<%s id="%s"%s>' % (n["type"], n["id"], nom)

        corps = [pos + "<extensionElements>"]
        for ev, expr in n["listeners"]:
            corps.append(pos + '  <flowable:executionListener expression="%s" event="%s" />'
                         % (attr(expr), ev))
        if n.get("composants"):
            corps.append(pos + "  <flowable:inputOutput>")
            corps.append(pos + '    <flowable:inputParameter name="components">')
            corps.append(pos + "      <flowable:map>")
            for cle, val in n["composants"]:
                corps.append(pos + '        <flowable:entry key="%s">%s</flowable:entry>' % (cle, val))
            corps.append(pos + "      </flowable:map>")
            corps.append(pos + "    </flowable:inputParameter>")
            corps.append(pos + "  </flowable:inputOutput>")
        corps.append(pos + "</extensionElements>")
        ferme = pos[2:] + "</%s>" % ("userTask" if n["type"] == "userTask" else n["type"])
        return "\n".join([ouvre] + corps + [ferme])

    def _flux(self, fl):
        pos = "      "
        nom = ' name="%s"' % esc(fl["nom"]) if fl["nom"] else ""
        ouvre = pos[2:] + '<sequenceFlow id="%s"%s sourceRef="%s" targetRef="%s">' % (
            fl["id"], nom, fl["src"], fl["tgt"])

        props = [(k, v) for k, v in [("ordre", fl["ordre"]), ("color", fl["couleur"]),
                                     ("icon", fl["icone"]),
                                     ("decisionMessageConfirmation", fl["confirmation"])]
                 if v is not None]
        lignes = []
        if fl["listeners"] or props:
            lignes.append(pos + "<extensionElements>")
            for ev, expr in fl["listeners"]:
                lignes.append(pos + '  <flowable:executionListener expression="%s" event="%s" />'
                              % (attr(expr), ev))
            if props:
                lignes.append(pos + "  <flowable:properties>")
                for k, v in props:
                    lignes.append(pos + '    <flowable:property name="%s" value="%s" />'
                                  % (k, attr(str(v))))
                lignes.append(pos + "  </flowable:properties>")
            lignes.append(pos + "</extensionElements>")

        cond = fl["condition"]
        if cond is None:
            cond = "${Decision == '%s'}" % fl["id"] if fl["nom"] else ""
        if cond:
            lignes.append(pos + '<conditionExpression xsi:type="tFormalExpression">%s</conditionExpression>' % cond)

        if not lignes:
            return pos[2:] + '<sequenceFlow id="%s"%s sourceRef="%s" targetRef="%s" />' % (
                fl["id"], nom, fl["src"], fl["tgt"])
        return "\n".join([ouvre] + lignes + [pos[2:] + "</sequenceFlow>"])

    def rendre(self):
        pool = "pool_" + self.cle
        largeur = max(n["x"] + n["w"] for n in self.noeuds) + 80
        hauteur = len(self.lanes) * HAUTEUR_LANE + 20

        L = []
        L.append('<?xml version="1.0" encoding="UTF-8"?>')
        L.append('<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"'
                 ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"'
                 ' xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"'
                 ' xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"'
                 ' xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"'
                 ' xmlns:xsd="http://www.w3.org/2001/XMLSchema"'
                 ' xmlns:flowable="http://flowable.org/bpmn"'
                 ' targetNamespace="http://flowable.org/test">')
        L.append('  <collaboration id="Collaboration_%s">' % self.cle)
        L.append('    <participant id="%s" name="%s" processRef="%s" />' % (pool, esc(self.nom), self.cle))
        L.append("  </collaboration>")
        L.append('  <process id="%s" name="%s" isExecutable="true">' % (self.cle, esc(self.nom)))

        L.append('    <laneSet id="laneSet_%s">' % self.cle)
        for lid, lnom in self.lanes:
            L.append('      <lane id="%s" name="%s">' % (lid, esc(lnom)))
            for n in self.noeuds:
                if n["lane"] == lid:
                    L.append("        <flowNodeRef>%s</flowNodeRef>" % n["id"])
            L.append("      </lane>")
        L.append("    </laneSet>")

        for n in self.noeuds:
            L.append(self._noeud(n))
        for fl in self.flux:
            L.append(self._flux(fl))
        L.append("  </process>")

        # ------------------------------------------------------------- DI
        L.append('  <bpmndi:BPMNDiagram id="BPMNDiagram_%s">' % self.cle)
        L.append('    <bpmndi:BPMNPlane id="BPMNPlane_%s" bpmnElement="Collaboration_%s">' % (self.cle, self.cle))
        L.append('      <bpmndi:BPMNShape id="BPMNShape_%s" bpmnElement="%s" isHorizontal="true">' % (pool, pool))
        L.append('        <omgdc:Bounds x="40" y="20" width="%d" height="%d" />' % (largeur, hauteur))
        L.append("      </bpmndi:BPMNShape>")
        for i, (lid, _) in enumerate(self.lanes):
            L.append('      <bpmndi:BPMNShape id="BPMNShape_%s" bpmnElement="%s" isHorizontal="true">' % (lid, lid))
            L.append('        <omgdc:Bounds x="70" y="%d" width="%d" height="%d" />'
                     % (20 + i * HAUTEUR_LANE, largeur - 30, HAUTEUR_LANE))
            L.append("      </bpmndi:BPMNShape>")

        centre = {}
        for n in self.noeuds:
            y = self.y_lane[n["lane"]] + (HAUTEUR_LANE - n["h"]) // 2 - 20
            centre[n["id"]] = (n["x"] + n["w"] / 2.0, y + n["h"] / 2.0, n["x"], y, n["w"], n["h"])
            L.append('      <bpmndi:BPMNShape id="BPMNShape_%s" bpmnElement="%s">' % (n["id"], n["id"]))
            L.append('        <omgdc:Bounds x="%d" y="%d" width="%d" height="%d" />'
                     % (n["x"], y, n["w"], n["h"]))
            L.append("      </bpmndi:BPMNShape>")

        for fl in self.flux:
            sx, sy, _, _, sw, sh = centre[fl["src"]]
            tx, ty, _, _, tw, th = centre[fl["tgt"]]
            L.append('      <bpmndi:BPMNEdge id="BPMNEdge_%s" bpmnElement="%s">' % (fl["id"], fl["id"]))
            L.append('        <omgdi:waypoint x="%d" y="%d" />' % (sx, sy))
            if abs(sy - ty) > 5 and abs(sx - tx) > 5:
                L.append('        <omgdi:waypoint x="%d" y="%d" />' % (sx, ty))
            L.append('        <omgdi:waypoint x="%d" y="%d" />' % (tx, ty))
            L.append("      </bpmndi:BPMNEdge>")

        L.append("    </bpmndi:BPMNPlane>")
        L.append("  </bpmndi:BPMNDiagram>")
        L.append("</definitions>")
        return "\n".join(L) + "\n"


def ecrire(d, nom_fichier, entete):
    import re
    # '--' est interdit DANS un commentaire XML : on neutralise le contenu,
    # sans toucher aux delimiteurs <!-- et -->
    if entete.startswith("<!--") and entete.endswith("-->"):
        interieur = entete[4:-3]
        interieur = re.sub(r"-{2,}", lambda m: "~" * len(m.group(0)), interieur)
        entete = "<!--" + interieur + "-->"
    xml = d.rendre()
    xml = xml.replace("<definitions", entete + "\n<definitions", 1)
    p = os.path.join(SORTIE, nom_fichier)
    io.open(p, "w", encoding="utf-8", newline="\n").write(xml)
    n_cond = xml.count("<conditionExpression")
    n_sys = xml.count('name="sys_')
    print("%-34s %6d octets | %2d taches | %2d flux | %2d conditions | %d automatiques"
          % (nom_fichier, len(xml.encode("utf-8")),
             sum(1 for n in d.noeuds if n["type"] == "userTask"), len(d.flux), n_cond, n_sys))
    return p
