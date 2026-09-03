package picosoft.biz.arcep.configuration.slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

public class Slf4jMDCFilter implements Filter {

    private final String responseHeader;
    private final String mdcTokenKey;
    private final String mdcClientIpKey;
    private final String requestHeader;

    public Slf4jMDCFilter() {
        this.responseHeader = Slf4jMDCFilterConfiguration.DEFAULT_RESPONSE_TOKEN_HEADER;
        this.mdcTokenKey = Slf4jMDCFilterConfiguration.DEFAULT_MDC_UUID_TOKEN_KEY;
        this.mdcClientIpKey = Slf4jMDCFilterConfiguration.DEFAULT_MDC_CLIENT_IP_KEY;
        this.requestHeader = null;
    }

    public Slf4jMDCFilter(String responseHeader, String mdcTokenKey, String mdcClientIpKey, String requestHeader) {
        this.responseHeader = responseHeader;
        this.mdcTokenKey = mdcTokenKey;
        this.mdcClientIpKey = mdcClientIpKey;
        this.requestHeader = requestHeader;
    }

    private String extractToken(HttpServletRequest request) {
        if (StringUtils.hasText(requestHeader) && StringUtils.hasText(request.getHeader(requestHeader))) {
            return request.getHeader(requestHeader);
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private String extractClientIP(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null) {
            return request.getHeader("X-Forwarded-For").split(",")[0];
        } else {
            return request.getRemoteAddr();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            final String token = extractToken(request);
            final String clientIP = extractClientIP(request);

            MDC.put(mdcClientIpKey, clientIP);
            MDC.put(mdcTokenKey, token);

            if (StringUtils.hasText(responseHeader)) {
                response.addHeader(responseHeader, token);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(mdcTokenKey);
            MDC.remove(mdcClientIpKey);
        }
    }

    @Override
    public void destroy() {
        // No resources to release
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }
}
