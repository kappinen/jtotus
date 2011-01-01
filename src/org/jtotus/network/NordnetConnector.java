/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jtotus.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
public class NordnetConnector {

    DefaultHttpClient httpclient = null;

    private DefaultHttpClient getClient() {

        if (httpclient != null) {
            return httpclient;
        }

        httpclient = new DefaultHttpClient();
        
        Properties prop = System.getProperties();
        if (prop.getProperty("https.proxyHost") != null
                && prop.getProperty("https.proxyPort") != null) {

            httpclient.getCredentialsProvider().setCredentials(
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

    public HttpGet getMethod(String url) {
        HttpGet httpget = new HttpGet(url);

        
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

        System.out.printf("fetching: %s:%s?%s\n",
                url.getURI().getHost(),
                url.getURI().getRawPath(),
                url.getURI().getQuery());

        StringBuffer respond = new StringBuffer();
        HttpResponse response = null;


        try {
            response = this.getClient().execute(url);
            HttpEntity entity = response.getEntity();

            
            InputStream instream = entity.getContent();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(instream));

            // do something useful with the response
          //  System.out.printf("done:%s : %s\n", url, response.getStatusLine().toString());
            String line = null;

            while ((line = reader.readLine()) != null) {
                respond.append(line);
            }

            return respond.toString();

        } catch (IOException ex) {
            Logger.getLogger(NordnetConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public String authenticate(String url,
                               String loginToken,
                               String login,
                               String passToken,
                               String pass) {
        try {
            HttpPost httpPost = new HttpPost(url);


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("checksum", ""));
            nameValuePairs.add(new BasicNameValuePair("referer", "%2Fmux%2Fweb%2Fnordnet%2Findex.html"));
            nameValuePairs.add(new BasicNameValuePair("usePhrase", "0"));
            nameValuePairs.add(new BasicNameValuePair(loginToken, login));
            nameValuePairs.add(new BasicNameValuePair(passToken, pass));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
           
            return this.fetchPage(httpPost);
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NordnetConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    
}
