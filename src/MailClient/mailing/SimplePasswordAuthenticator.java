package MailClient.mailing;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SimplePasswordAuthenticator extends Authenticator {
    private String username;
    private String password;
    
    public SimplePasswordAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
