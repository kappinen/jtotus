/*
 *
 *
 *
 *
 * http://viralpatel.net/blogs/2009/04/http-proxy-setting-java-setting-proxy-java.html
 * http://www.jguru.com/faq/view.jsp?EID=9920
 * http://stackoverflow.com/questions/1626549/authenticated-http-proxy-with-java
 */
package org.jtotus.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 *
 * @author Evgeni Kappinen
 */
public class JtotusProxy {

    private String httpProxyURL = null;
    private int httpProxyPort = 8080;

    public String dumpSystemProxy() {

        Properties prop = System.getProperties();

        System.out.printf("host:%s:%s isSet:%s host:%s:%s user:%s pass:%s use:%s\n",
                prop.getProperty("http.proxyHost"),
                prop.getProperty("http.proxyPort"),
                prop.getProperty("proxySet"),
                prop.getProperty("ProxyHost"),
                prop.getProperty("ProxyPort"),
                prop.getProperty("http.proxyUser"),
                prop.getProperty("http.proxyPassword"),
                System.getProperty("java.net.useSystemProxies"));

        return null;
    }


//        System.setProperty("socksProxyHost", proxyAddress);
//        System.setProperty("socksProxyPort", String.valueOf(proxyPort));

    public void setSystemProxy(String proxyAddress, int proxyPort,
                               String user, String password) {
        final String authUser = user;
        final String authPassword = password;

        System.setProperty("proxySet", "true");
        System.setProperty("ProxyHost", proxyAddress);
        System.setProperty("ProxyPort", String.valueOf(proxyPort));
        
        System.setProperty("http.proxyHost", proxyAddress);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));

        System.setProperty("http.proxyHost", proxyAddress);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);

        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                });

    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public String getHttpProxyURL() {
        return httpProxyURL;
    }

    public void setHttpProxyURL(String httpProxyURL) {
        this.httpProxyURL = httpProxyURL;
    }
}
