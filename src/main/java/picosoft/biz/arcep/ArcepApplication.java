package picosoft.biz.arcep;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@Configuration
@EnableFeignClients(basePackages = "picosoft.biz.arcep.client")
@EnableJpaAuditing
@EnableAsync
public class ArcepApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArcepApplication.class, args);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        // TOUS LES EN-TETES, et non la liste de trois qui figurait ici.
        //
        // Le front n'envoie pas que `Authorization` et `Content-type` : son
        // intercepteur ajoute `application`, et le preflight echouait dessus --
        // « Request header field application is not allowed ». Enumerer les
        // en-tetes obligerait a revenir ici chaque fois que le front en ajoute
        // un. L'origine reste `*` sans identifiants, donc `*` est acceptable
        // ici : c'est aussi ce que fait la passerelle, qui renvoie les en-tetes
        // qu'on lui presente.
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues());
        return source;
    }

    /**
     * Ce qui fait REPONDRE le bean ci-dessus.
     *
     * Declare seul, `CorsConfigurationSource` ne sert a rien : Spring MVC ne le
     * regarde pas, et la chaine de filtres qui le consommerait viendrait de
     * spring-security-web, que ce service n'a pas -- il n'a que
     * spring-security-acl. Le bean etait donc mort, ici comme chez homologation.
     *
     * `CorsFilter` est un filtre de servlet ordinaire, de spring-web : Spring
     * Boot l'enregistre du seul fait qu'il soit un bean, sans securite.
     *
     * ETEINT PAR DEFAUT, et c'est le point. En production comme sur arcep-dev,
     * c'est la PASSERELLE qui pose les en-tetes CORS -- mesure le 9 septembre sur
     * homologation. Deux sources poseraient deux `Access-Control-Allow-Origin`,
     * et le navigateur refuse une reponse qui en porte deux. Ce filtre n'existe
     * que pour le cas ou l'on attaque le service EN DIRECT, sans passerelle :
     * un front lance en local, un client HTTP, une verification a la main. On
     * l'allume alors explicitement :
     *
     *     --arcep.cors.enabled=true
     *
     * QUALIFIE PAR SON NOM : Spring MVC expose lui aussi un
     * `CorsConfigurationSource` -- `mvcHandlerMappingIntrospector` -- et sans le
     * `@Qualifier` l'injection est ambigue, le service ne demarre pas.
     */
    @Bean
    @ConditionalOnProperty(name = "arcep.cors.enabled", havingValue = "true")
    CorsFilter corsFilter(@Qualifier("corsConfigurationSource") CorsConfigurationSource source) {
        return new CorsFilter(source);
    }

}
