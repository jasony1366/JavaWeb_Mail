package MailClient.ui;

import org.eclipse.jface.wizard.Wizard;

import javax.mail.NoSuchProviderException;
import MailClient.mailing.MailAccount;

public class NewAccountConfigureWizard extends Wizard {
    
    protected MailAccount accountSettings;

    protected NewAccountConfigureWizardPage nConfigPage;
    
    public NewAccountConfigureWizard() {
        setWindowTitle("New Email Account Wizard");
    }

    @Override
    public void addPages() {
        nConfigPage = new NewAccountConfigureWizardPage();
        addPage(nConfigPage);
    }
    
    @Override
    public boolean canFinish() {
        return nConfigPage.isConfigurationValid() && super.canFinish();
    }
    
    @Override
    public boolean performFinish() {
        try {
            accountSettings = nConfigPage.getMailAccountSettings();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return true;
    }

    public MailAccount getMailAccountSettings() {
        return accountSettings;
    }
}
