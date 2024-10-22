package MailClient.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import javax.mail.NoSuchProviderException;
import MailClient.mailing.MailAccount;
import MailClient.mailing.MailProtocol;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class NewAccountConfigureWizardPage extends WizardPage {
    private Text textSenderName;
    private Text textEmailAddr;
    private Text textPassword;
    private Text textSendHost;
    private Text textReceiveHost;
    private Combo comboSendProtocol;
    private Combo comboReceiveProtocol;

    public NewAccountConfigureWizardPage() {
        super("New Account Page");
        setTitle("New Email Account Wizard");
        setDescription("Connect an Email account to MailClient.");
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        setControl(container);
        container.setLayout(new GridLayout(6, false));

        Label lblSenderName = new Label(container, SWT.NONE);
        lblSenderName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSenderName.setText("Your Name:");

        textSenderName = new Text(container, SWT.BORDER);
        textSenderName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        textSenderName.setText(System.getProperty("user.name"));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        Label lblEmailAddress = new Label(container, SWT.NONE);
        lblEmailAddress.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblEmailAddress.setText("Address:");

        textEmailAddr = new Text(container, SWT.BORDER);
        textEmailAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        Label lblPassword = new Label(container, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText("Password:");

        textPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
        textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        Label lblSendBy = new Label(container, SWT.NONE);
        lblSendBy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSendBy.setText("Send by:");

        comboSendProtocol = new Combo(container, SWT.READ_ONLY);
        comboSendProtocol.setItems(new String[] { "SMTP" });
        comboSendProtocol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSendProtocol.select(0);

        Label lblSendHost = new Label(container, SWT.NONE);
        lblSendHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSendHost.setText("Host:");

        textSendHost = new Text(container, SWT.BORDER);
        textSendHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblSendPort = new Label(container, SWT.NONE);
        lblSendPort.setText("Port:");

        Spinner spinner = new Spinner(container, SWT.BORDER);
        spinner.setMaximum(65535);
        spinner.setSelection(587);

        Label lblReceiveBy = new Label(container, SWT.NONE);
        lblReceiveBy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblReceiveBy.setText("Receive by:");

        comboReceiveProtocol = new Combo(container, SWT.READ_ONLY);
        comboReceiveProtocol.setItems(new String[] { "IMAP", "POP3" });
        comboReceiveProtocol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboReceiveProtocol.select(0);

        Label lblReceiveHost = new Label(container, SWT.NONE);
        lblReceiveHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblReceiveHost.setText("Host:");

        textReceiveHost = new Text(container, SWT.BORDER);
        textReceiveHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblReceivePort = new Label(container, SWT.NONE);
        lblReceivePort.setText("Port");

        Spinner spinner_1 = new Spinner(container, SWT.BORDER);
        spinner_1.setMaximum(65535);
        spinner_1.setSelection(993);

        Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
                Button btnTestConnection = new Button(container, SWT.NONE);
                btnTestConnection.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (isConfigurationValid()) {
                            try {
                                MailAccount settings = getMailAccountSettings();
                                boolean ok = settings.testConnection();
                                if (ok) {
                                    setMessage("Connection succeed!");
                                }
                                else {
                                    setErrorMessage("Configuration invalid.");
                                }
                            } catch (NoSuchProviderException e1) {
                                // TODO Auto-generated catch block
                                MessageDialog.openError(getShell(), "Something Went Mad.", e1.getMessage());
                            }
                        }
                        else {
                            MessageDialog.openError(getShell(), "Invalid Configure", "Invalid configure in some field. Check it and retest.");
                            return;
                        }
                        
                    }
                });
                btnTestConnection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
                btnTestConnection.setText("Test Connection!");
        
        textSenderName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                callToUpdateButton();
            }
        });
        textEmailAddr.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                callToUpdateButton();
            }
        });
        textPassword.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                callToUpdateButton();
            }
        });
        textSendHost.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                callToUpdateButton();
            }
        });
        textReceiveHost.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                callToUpdateButton();
            }
        });
        
    }

    public boolean isConfigurationValid() {
        if (textSenderName.getText().isBlank() || textEmailAddr.getText().isBlank() || textPassword.getText().isBlank()
                || textSendHost.getText().isBlank() || textReceiveHost.getText().isBlank()) {
            return false;
        }
        return true;
    }

    public MailAccount getMailAccountSettings() throws NoSuchProviderException {
        if (isConfigurationValid()) {
            MailProtocol sendProtocol;
            String sendProtocolString = comboSendProtocol.getText();
            if (sendProtocolString.equals("SMTP")) {
                sendProtocol = MailProtocol.SMTP;
            } else {
                throw new NoSuchProviderException("No provider for " + sendProtocolString);
            }

            MailProtocol receiveProtocol;
            String receiveProtocolString = comboReceiveProtocol.getText();
            if (receiveProtocolString.equals("IMAP")) {
                receiveProtocol = MailProtocol.IMAP;
            } else if (receiveProtocolString.equals("POP3")) {
                receiveProtocol = MailProtocol.POP3;
            } else {
                throw new NoSuchProviderException("No provider for " + receiveProtocolString);
            }

            return new MailAccount(textSenderName.getText(), textEmailAddr.getText(), textPassword.getText(), textSendHost.getText(),
                    sendProtocol, textReceiveHost.getText(), receiveProtocol);
        }
        return null;
    }
    
    private void callToUpdateButton() {
        super.getWizard().getContainer().updateButtons();
    }

}
