package com.tracetrail.ingestion.auth;

import com.tracetrail.ingestion.persistence.models.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantAuthInterceptor implements HandlerInterceptor {
    private final TenantResolver tenantResolver;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String apiKey = request.getHeader("X-Tenant-API-Key");
        if (apiKey == null || apiKey.isBlank()){
            writeError(response, 401, "missing X-Tenant-API-Key");
            return false;
        }
        Tenant tenant = tenantResolver.resolveByAPIKey(apiKey);
        if (tenant == null) {
            writeError(response, 401, "invalid tenant key");
            return false;
        }
        request.setAttribute("tenantId", tenant.getId());
        request.setAttribute("tenant", tenant);
        return true;
    }


    private void writeError(HttpServletResponse response,
                            int status,
                            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
