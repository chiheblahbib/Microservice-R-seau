package picosoft.biz.arcep.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;

/**
 * Serialisation des erreurs metier.
 *
 * ExceptionTranslator fait remonter le bon code HTTP, mais sans ce module Jackson
 * l'exception est serialisee comme un bean ordinaire : la reponse contient alors la
 * pile d'appels complete -- noms de classes, fichiers, numeros de ligne -- au lieu
 * du corps Problem attendu.
 *
 * Avec le module, une erreur metier ressort sous la forme :
 *   { "title": "...", "status": 400, "message": "IMPLANTATION.ERROR.OBJECT_ENGAGED",
 *     "entityName": "...", "errorKey": "..." }
 * exploitable directement par le front.
 */
@Configuration
public class ProblemConfiguration {

    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule().withStackTraces(false);
    }
}
