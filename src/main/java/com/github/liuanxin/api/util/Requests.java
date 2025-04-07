package com.github.liuanxin.api.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class Requests {

    public static String getDomain() {
        HttpServletRequest request = getRequestAttributes().getRequest();
        String scheme = request.getScheme();
        int port = request.getServerPort();

        StringBuilder sbd = new StringBuilder();
        // sbd.append(scheme).append(":");
        sbd.append("//").append(request.getServerName());
        boolean notHttp = ("http".equalsIgnoreCase(scheme) && port != 80);
        boolean notHttps = ("https".equalsIgnoreCase(scheme) && port != 443 && port != 80);
        if (notHttp || notHttps) {
            sbd.append(':').append(port);
        }
        return sbd.toString();
    }

    private static ServletRequestAttributes getRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    }
}
