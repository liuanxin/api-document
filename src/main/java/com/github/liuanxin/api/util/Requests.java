package com.github.liuanxin.api.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class Requests {

    public static String getDomain() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return "";
        }

        String scheme = requestAttributes.getRequest().getScheme();
        int port = requestAttributes.getRequest().getServerPort();
        String serverName = requestAttributes.getRequest().getServerName();

        StringBuilder sbd = new StringBuilder();
        sbd.append(scheme).append(":").append("//").append(serverName);
        boolean notHttp = ("http".equalsIgnoreCase(scheme) && port != 80);
        boolean notHttps = ("https".equalsIgnoreCase(scheme) && port != 443 && port != 80);
        if (port > 0 && (notHttp || notHttps)) {
            sbd.append(':').append(port);
        }
        return sbd.toString();
    }
}
