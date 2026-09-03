package picosoft.biz.arcep.configuration.audit;

import picosoft.biz.arcep.configuration.slf4j.Slf4jMDCFilterConfiguration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;


public class AuditRevisionListener implements RevisionListener, AuditorAware<String> {

    private final Logger log = LoggerFactory.getLogger(AuditRevisionListener.class);
    private String adrIp;


    public AuditRevisionListener() {
    }


    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity rev = (AuditRevisionEntity) revisionEntity;
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                rev.setUsr(authentication.getName());
            }
            HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

            if(response!=null) {
                String uuid = response.getHeaders(Slf4jMDCFilterConfiguration.DEFAULT_RESPONSE_TOKEN_HEADER).toString();

                uuid = uuid.replace("[", "");
                uuid = uuid.replace("]", "");
                rev.setUuid(uuid);
            }
            if(request!=null) {
                if (request.getHeader("X-Forwarded-For") != null) {
                    adrIp = request.getHeader("X-Forwarded-For").split(",")[0];
                } else {
                    adrIp = request.getLocalAddr();
                }
                rev.setIpAdr(adrIp);
            }
        }
        catch (Exception e ){
            log.error(e.getMessage());
        }
    }


    public Optional<String> getCurrentAuditor() {
        return Optional.of("");
    }
}
