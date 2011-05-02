/*
 *
 *
 * http://www.jscape.com/articles/pop_java_ssl_gmail.html
 * http://commons.apache.org/email/
 * http://pipoltek.blogspot.com/2008/02/sending-mail-using-gmail-smtp-server.html
 */
package org.jtotus.gui.mail;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// Common mail example:
//    public boolean pushMail(String login,
//                            String password,
//                            String subject,
//                            String message) {
//        try {
//            Email email = new SimpleEmail();
//            email.setHostName("smtp.gmail.com");
//            email.setSmtpPort(587);
//            email.setAuthenticator(new DefaultAuthenticator(login, password));
//            email.setTLS(true);
//            email.setFrom(login+"@gmail.com");
//            email.setSubject("TestMail");
//            email.setMsg("This is a test mail ... :-)");
//            email.addTo(login+"@gmail.com");
//            email.send();
//        } catch (EmailException ex) {
//            Logger.getLogger(JtotusGmailClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return true;
//    }


public class JtotusGmailClient implements Callable {

    private int port = 465;
    private String host = "smtp.gmail.com";
    private String defaultDomain = "gmail.com";
    private String defaultLogin = null;
    private String defaultPassword = null;
    private String subject = "Jtotus stock report";
    private StringBuffer mailBuffer = null;
    private boolean enableTimestamp = true;

    public JtotusGmailClient() {
        mailBuffer = new StringBuffer();
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(getDefaultLogin(), getDefaultPassword());
        }
        public void setAuthInfo(String name, String password) {
            setDefaultLogin(name);
            setDefaultPassword(password);
        }
    }

    public void clean() {
        mailBuffer = new StringBuffer();
    }

    public void pushText(String mesg) {
        if (mailBuffer == null) {
            mailBuffer = new StringBuffer();
        }
        mailBuffer.append(mesg);
    }

    
    public boolean sendMail(String login,
                            String password,
                            String subject,
                            String mesg) {

        if (login == null
                || password == null) {
            return false;
        }

        if (login.lastIndexOf("@") == -1) {
            login = login + "@" + getDefaultDomain();
        }

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Properties props = new Properties();
        props.put("mail.smtp.user", login);
        props.put("mail.smtp.host", this.getHost());
        props.put("mail.smtp.port", this.getPort());
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.socketFactory.port", this.getPort());
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl", "true");
        props.put("mail.transport.protocol", "smtp");

        SecurityManager security = System.getSecurityManager();
        SMTPAuthenticator auth = new SMTPAuthenticator();
        auth.setAuthInfo(login, password);
        Session session = Session.getInstance(props, auth);
        session.setDebug(true);


        try {
            //Compose message

            if (enableTimestamp) {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
                subject += " (" + format.format(Calendar.getInstance().getTime()) + ")";
            }

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(login));
            message.setRecipients(Message.RecipientType.TO,
                                  InternetAddress.parse(login));
            message.setSentDate(new Date());
            message.setSubject(subject);
            message.setText(mesg);

            Transport transport = session.getTransport("smtp");
            transport.connect(getHost(), getPort(),login, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();


            System.out.println("Sent mail to: " + login + " Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
            //TODO: send notification of the failure to GUI
        }

        return true;
    }

    public void sendThreaded() {
        Thread thread = new Thread() {
            public void run() {
                sendMail(getDefaultLogin(),
                         getDefaultPassword(),
                         getSubject(),
                         getMessage());
            }
        };
        thread.start();
    }


    public Object call() throws Exception {

        boolean result = false;

        //TODO: If login/pass is not provided,
        //1. load GUIConfig.
        //2. if login not found ask for Gui (ask for perserv)
        //3. login and send


        result = this.sendMail(this.getDefaultLogin(),
                               this.getDefaultPassword(),
                               this.getSubject(),
                               this.getMessage());

        return result;

    }

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public String getDefaultLogin() {
        return defaultLogin;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSubject() {
        return subject;
    }

    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

    public void setDefaultLogin(String defaultLogin) {
        this.defaultLogin = defaultLogin;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getMessage() {

        if (mailBuffer != null) {
            return mailBuffer.toString();
        }

        return null;
    }
}
