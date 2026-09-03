package picosoft.biz.arcep.controller.errors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/**
 * Traduit les exceptions metier en reponses HTTP.
 *
 * Sans ce @ControllerAdvice, BadRequestAlertException -- qui etend pourtant
 * AbstractThrowableProblem et declare Status.BAD_REQUEST -- remonte a Spring comme
 * une RuntimeException quelconque : la reponse part en 500 avec un corps generique,
 * et le front ne peut plus distinguer une erreur metier d'un plantage.
 *
 * problem-spring-web est au pom depuis le debut mais n'avait jamais ete branche,
 * ici comme dans homologation. Une seule interface suffit : ProblemHandling apporte
 * des implementations par defaut pour tout.
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling {

    /**
     * On n'expose pas la chaine des causes : elle fait fuiter la structure interne
     * (noms de tables, requetes SQL) dans la reponse HTTP.
     */
    @Override
    public boolean isCausalChainsEnabled() {
        return false;
    }
}
