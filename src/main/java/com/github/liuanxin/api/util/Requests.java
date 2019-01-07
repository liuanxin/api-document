package com.github.liuanxin.api.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/** just use in Controller */
public final class Requests {

    public static String getDomain() {
        HttpServletRequest request = getRequestAttributes().getRequest();
        String scheme = request.getScheme();
        int port = request.getServerPort();
        if (port < 0) {
            port = 80;
        }

        StringBuilder sbd = new StringBuilder();
        // sbd.append(scheme).append(":");
        sbd.append("//").append(request.getServerName());
        boolean http = ("http".equalsIgnoreCase(scheme) && port != 80);
        boolean https = ("https".equalsIgnoreCase(scheme) && port != 443 && port != 80);
        if (http || https) {
            sbd.append(':').append(port);
        }
        return sbd.toString();
    }

    private static ServletRequestAttributes getRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    }
}
