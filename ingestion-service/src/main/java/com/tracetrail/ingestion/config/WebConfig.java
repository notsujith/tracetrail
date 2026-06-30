package com.tracetrail.ingestion.config;

import com.tracetrail.ingestion.auth.TenantAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    TenantAuthInterceptor tenantAuthInterceptor;

    public WebConfig(TenantAuthInterceptor tenantAuthInterceptor) {
        this.tenantAuthInterceptor = tenantAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(tenantAuthInterceptor)
                .addPathPatterns("/v1/**");
    }
}
