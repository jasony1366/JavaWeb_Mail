package MailClient.mailing;

import java.util.ArrayList;

import MailClient.mailing.MailAccount;

public class MailService {
    private ArrayList<MailAccount> accounts = new ArrayList<>();
    
    
    public MailAccount[] getMailAccountSettings() {
        MailAccount[] arr = new MailAccount[accounts.size()];
        return accounts.toArray(arr);
    }
    
    public void addMailAccountSettings(MailAccount settings) {
        accounts.add(settings);
    }
    
    public int getMailAccountCount() {
        return accounts.size();
    }
    
}
