package MailClient.mailing;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class MailAccount {
    private String senderName;
    
    private String address;

    private String password;

    private Authenticator authenticator;

    private Properties sendProperties;

    private Properties receiveProperties;

    public MailAccount(String displayName, String address, String password, Properties sendProperties,
            Properties receiveProperties) {
        this.senderName = displayName;
        this.address = address;
        this.password = password;
        this.authenticator = new SimplePasswordAuthenticator(address, password);
        this.sendProperties = sendProperties;
        this.receiveProperties = receiveProperties;
    }

    public MailAccount(String displayName, String username, String password, String sendServer, MailProtocol sendProtocol,
            String receiveServer, MailProtocol receiProtocol) {
        this.senderName = displayName;
        this.address = username;
        this.password = password;
        this.authenticator = new SimplePasswordAuthenticator(username, password);

        this.sendProperties = new Properties();
        if (sendProtocol == MailProtocol.SMTP) {
            this.sendProperties.setProperty("mail.transport.protocol", "smtp");
            this.sendProperties.setProperty("mail.smtp.auth", "true");
            this.sendProperties.setProperty("mail.smtp.host", sendServer);
        }

        this.receiveProperties = new Properties();
        if (receiProtocol == MailProtocol.IMAP) {
            this.receiveProperties.setProperty("mail.transport.protocol", "imaps");
            this.receiveProperties.setProperty("mail.imap.auth", "true");
            this.receiveProperties.setProperty("mail.imap.host", receiveServer);
        } else if (receiProtocol == MailProtocol.POP3) {
            this.receiveProperties.setProperty("mail.transport.protocol", "pop3");
            this.receiveProperties.setProperty("mail.pop3.auth", "true");
            this.receiveProperties.setProperty("mail.pop3.host", receiveServer);
        }
        this.receiveProperties.setProperty("mail.host", receiveServer);
    }

    public boolean testConnection() {
        try {
            Session session = Session.getInstance(sendProperties, authenticator);
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

        try {
            Session session = Session.getInstance(receiveProperties, authenticator);
            String provider = receiveProperties.getProperty("mail.transport.protocol");
            Store store = session.getStore(provider);
            store.connect();
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String getDisplayName() {
        String res = "";
        if (address.contains("@")) {
            res = address;
        } else {
            String domain = this.sendProperties.getProperty("mail.smtp.host");
            res = address + "@" + domain.substring(domain.indexOf(".") + 1);
        }
        return res;
    }
    
    public String getSendHostName() {
        return this.sendProperties.getProperty("mail.smtp.host");
    }
    
    public MultiPartEmail getMultiPartEmail() {
        MultiPartEmail email = new MultiPartEmail();
        email.setAuthentication(address, password);
        email.setHostName(this.sendProperties.getProperty("mail.smtp.host"));
        try {
            email.setFrom(getDisplayName(), senderName);
        } catch (EmailException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return email;
    }

    public String[] getFolderNames() throws MessagingException {
        Folder[] folders = getFolders();
        
        String[] res = new String[folders.length];
        for (int i = 0; i < folders.length; i++) {
            res[i] = folders[i].getFullName();
        }
        return res;
    }
    
    public Folder[] getFolders() throws MessagingException {
        Session session = Session.getInstance(receiveProperties, authenticator);
        Store store;
        
        store = session.getStore(this.receiveProperties.getProperty("mail.transport.protocol"));
        store.connect(this.receiveProperties.getProperty("mail.host"), address, password);
        return store.getDefaultFolder().list();
    }
    
    public Authenticator getAuthenticator() {
        return authenticator;
    }
}
