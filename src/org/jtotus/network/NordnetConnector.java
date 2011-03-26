/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jtotus.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author work
 *
 *
 *
 *
 * http://www.java-tips.org/other-api-tips/httpclient/how-to-use-http-cookies.html
 * 
 */
public class NordnetConnector implements BrokerConnector {
    private DefaultHttpClient httpclient = null;




    private void dumpInfo(BasicHttpParams params) {
        System.out.printf("HttpClient parameters:\n");
        System.out.println(params.getParameter(
                CoreProtocolPNames.PROTOCOL_VERSION));
            System.out.println(params.getParameter(
                CoreProtocolPNames.HTTP_CONTENT_CHARSET));
            System.out.println(params.getParameter(
                CoreProtocolPNames.USE_EXPECT_CONTINUE));
            System.out.println(params.getParameter(
                CoreProtocolPNames.USER_AGENT));
            System.out.println(params.getParameter(
                CoreConnectionPNames.SO_TIMEOUT));
    }
    private DefaultHttpClient getClient() {

        if (httpclient != null) {
            return httpclient;
        }

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                 new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(schemeRegistry);

        BasicHttpParams params = new BasicHttpParams();
        params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
        params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//        params.setIntParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 5000000);
//        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000000);
//        params.setIntParameter(HttpConnectionParams.SO_TIMEOUT, 9900000);
//        params.setIntParameter(CoreConnectionPNames.SO_LINGER, 99000);
        

        dumpInfo(params);

        httpclient = new DefaultHttpClient(cm, params);

//        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        Properties prop = System.getProperties();
        if (prop.getProperty("https.proxyHost") != null
                && prop.getProperty("https.proxyPort") != null) {

            ((DefaultHttpClient)httpclient).getCredentialsProvider().setCredentials(
                    new AuthScope(prop.getProperty("https.proxyHost"),
                    Integer.parseInt(prop.getProperty("https.proxyPort"))),
                    new UsernamePasswordCredentials(prop.getProperty("https.proxyUser"),
                    prop.getProperty("https.proxyPassword")));

            HttpHost proxy = new HttpHost(prop.getProperty("https.proxyHost"),
                    Integer.parseInt(prop.getProperty("https.proxyPort")));

            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        
        return httpclient;
    }


    private void dumpHeaders(HttpGet url) {
        CookieStore cookieStore = null;
        
        Header [] headers = url.getAllHeaders();
        for (int i = 0;i < headers.length; i++ ) {
            Header tmp = headers[i];
            System.out.printf("Headers Name: %s Value:%s\n", tmp.getName(), tmp.getValue());
        }

        if (httpclient != null)
            cookieStore = httpclient.getCookieStore();
        if (cookieStore != null) {
        List<Cookie> listOfCookies = cookieStore.getCookies();
            for (Cookie me : listOfCookies) {
                System.out.printf("Cookie dump: %s version:%d!\n", me.getName(), me.getVersion());
                if (me.getName().equalsIgnoreCase("Cookie2")) {
                    System.out.printf("Removing cookie!\n");
                    listOfCookies.remove(me);
                }
            }
        }
    }

    
    public HttpGet getMethod(String url) {
        HttpGet httpget = new HttpGet(url);
//        httpget.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
//        httpget.setHeader("Content-Type","text/html;charset=UTF-8");
//        httpget.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
//        httpget.setHeader("Keep-Alive", "9000");
//
        dumpHeaders(httpget);
        
        return httpget;
    }

    
    public String getPage(String url) {
        return this.fetchPage(this.getMethod(url));
    }

    
    public String getPage(String pattern, Object... arg) {
        //System.out.printf("Fetching-->:%s\n", String.format(pattern, arg));
        return this.fetchPage(this.getMethod(String.format(pattern, arg)));
    }


    public String fetchPage(HttpUriRequest url) {

        System.out.printf("fetching: %s:%s?%s [%s:%s:%s] %s\n",
                          url.getURI().getHost(),
                          url.getURI().getRawPath(),
                          url.getURI().getQuery(),
                          url.getURI().getRawFragment(),
                          url.getURI().getRawSchemeSpecificPart(),
                          url.getURI().getRawUserInfo(), url.getParams().toString());

        HttpResponse response = null;

        try {
            response = this.getClient().execute(url);
            HttpEntity entity = response.getEntity();
            //Fixme: encoding!!!
            
            return EntityUtils.toString(entity, "UTF-8");

//            StringBuilder respond = new StringBuilder();
//            InputStream instream = entity.getContent();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, Charset.forName("UTF-8")));
//
//            // do something useful with the response
//            System.out.printf("done:%s : %s content type:%s\n", url, response.getStatusLine().toString(), entity.getContentType());
//            String line = null;
//
//
//            while ((line = reader.readLine()) != null) {
//                respond.append(line);
//                //System.out.printf("got:%s\n", line);
//            }
//
//            return respond.toString();

        } catch (IOException ex) {
            //Logger.getLogger(NordnetConnector.class.getName()).log(Level.SEVERE, null, ex);
            System.err.printf("Connection failure ... : %s \n", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    
    void dumpObjects(Collection<Object> collection) {
        Iterator<Object> iter = collection.iterator();

        while (iter.hasNext()) {
            Object obj = iter.next();
            System.out.printf("Object: %s \n", obj.toString());
        }
    }

    
    public String authenticate(String url,
                               String loginToken,
                               String login,
                               String passToken,
                               String encryptedPassword) {

        try {
            HttpPost httpPost = new HttpPost(url);

            httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.15) Gecko/20110303 Ubuntu/10.04 (lucid) Firefox/3.6.15");
//            httpPost.addHeader("Referer", "");
            
            httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            httpPost.addHeader("Accept-Language", "en-us,en;q=0.5");
            httpPost.addHeader("Keep-Alive", "115");

            BasicClientCookie cookie1 = new BasicClientCookie("s_cc","true");
	    BasicClientCookie cookie2 = new BasicClientCookie("s_sq","%5B%5BB%5D%5D");

            cookie1.setDomain("www.nordnet.fi");
            cookie2.setDomain("www.nordnet.fi");

            httpclient.getCookieStore().addCookie(cookie1);
            httpclient.getCookieStore().addCookie(cookie2);

            
            
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("checksum", ""));
            nameValuePairs.add(new BasicNameValuePair("referer", "" /* URLEncoder.encode("/mux/login/startFI.html", "utf-8")*/));
            nameValuePairs.add(new BasicNameValuePair("encryption", "1"));

            nameValuePairs.add(new BasicNameValuePair(loginToken, login));

            nameValuePairs.add(new BasicNameValuePair(passToken, encryptedPassword /*URLEncoder.encode(encryptedPassword, "utf-8")*/));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
//            System.out.printf("Login:(%s)%s (%s)Pass:%s\n", loginToken, login, passToken, URLEncoder.encode(encryptedPassword, "utf-8"));
            


            return this.fetchPage(httpPost);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NordnetConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }


    public void close() {
        if (httpclient != null) {
            httpclient.getConnectionManager().closeExpiredConnections();
        }
    }
    
}
