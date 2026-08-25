package com.epam.lenda.gymapp.report.util;

import com.epam.lenda.gymapp.common.security.ServiceTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ReportAuthorization {
    public static final String MAIN_SERVICE_NAME = "main-service";

    public boolean isMainService(HttpServletRequest request) {
        return MAIN_SERVICE_NAME.equals(request.getAttribute(ServiceTokenFilter.CALLING_SERVICE_ATTRIBUTE));
    }
}
