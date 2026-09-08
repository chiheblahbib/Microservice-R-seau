package picosoft.biz.arcep.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToZonedDateTimeConverter());
    }

    /**
     * CORS, declare au niveau MVC et non au niveau securite.
     *
     * POURQUOI ICI, ET NON DANS ArcepApplication
     *
     * `ArcepApplication` declare bien un bean `CorsConfigurationSource` -- mais
     * c'est une construction de Spring Security, et seul `spring-security-acl`
     * est au classpath : il n'y a aucune chaine de filtres pour le consommer.
     * Ce bean ne servait donc a rien. Mesure sur le service en marche : une
     * requete preliminaire recevait 403, et une requete simple repondait 200
     * SANS aucun en-tete `Access-Control-*` -- le navigateur bloquait donc la
     * lecture de la reponse.
     *
     * Il n'est pas retire : il pourrait redevenir actif le jour ou une chaine
     * de securite est ajoutee, et les deux declarations disent la meme chose.
     *
     * CE QUE CELA DEBLOQUE
     *
     * Le front vise desormais ce service en URL ABSOLUE
     * (`http://localhost:8080/reseau/api/`), donc en cross-origin depuis le
     * serveur de developpement. Sans CORS, aucun appel n'aboutissait.
     *
     * L'EN-TETE `Application` EST INDISPENSABLE : l'intercepteur du front le
     * pose sur CHAQUE requete. L'omettre de la liste ferait echouer toutes les
     * requetes preliminaires, y compris celles qui ne portent qu'un jeton.
     *
     * `allowedOriginPatterns` et non `allowedOrigins` : le premier accepte le
     * joker en presence de `allowCredentials`, que le second refuse.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                // HEAD y figure bien qu'aucun ecran ne l'emette : sans lui, un
                // simple `curl -I` recoit 403 et fait croire a une panne de
                // CORS alors que le GET correspondant passe.
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Version", "UUID", "Response_Token")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
