package MailClient.ui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;

import java.io.File;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import MailClient.mailing.MailService;
import MailClient.mailing.MailAccount;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class WriteMailDialog extends Dialog {

    protected Object result;
    protected Shell shell;
    private Text textReceiveAddress;
    private Text textSubject;
    private Table tableAttachment;
    private Combo comboSendAccount;
    private StyledText styledTextPlainMessage;
    
    
    private MailService mailService;
    
    private MailAccount[] accounts;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public WriteMailDialog(Shell parent, int style, MailService mailService) {
        super(parent, style);
        setText("Write a Email");
        this.mailService = mailService;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(800, 600);
        shell.setText(getText());
        shell.setLayout(new GridLayout(3, false));
        
        Label lblFrom = new Label(shell, SWT.NONE);
        lblFrom.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblFrom.setText("From:");
        
        comboSendAccount = new Combo(shell, SWT.READ_ONLY);
        comboSendAccount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        accounts = mailService.getMailAccountSettings();
        String[] accountList = new String[accounts.length];
        for (int i = 0; i < mailService.getMailAccountCount(); i++) {
            accountList[i] = accounts[i].getDisplayName();
        }
        comboSendAccount.setItems(accountList);
        
        Button btnSend = new Button(shell, SWT.NONE);
        btnSend.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendMessage();
            }
        });
        btnSend.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 3));
        btnSend.setText("Send");
        
        Label lblTo = new Label(shell, SWT.NONE);
        lblTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTo.setText("To:");
        
        textReceiveAddress = new Text(shell, SWT.BORDER);
        textReceiveAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblSubject = new Label(shell, SWT.NONE);
        lblSubject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubject.setText("Subject:");
        
        textSubject = new Text(shell, SWT.BORDER);
        textSubject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        styledTextPlainMessage = new StyledText(shell, SWT.BORDER | SWT.WRAP);
        styledTextPlainMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        
        tableAttachment = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_tableAttachment = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_tableAttachment.heightHint = 10;
        tableAttachment.setLayoutData(gd_tableAttachment);
        tableAttachment.setHeaderVisible(true);
        tableAttachment.setLinesVisible(true);
        
        TableColumn tblclmnFileName = new TableColumn(tableAttachment, SWT.NONE);
        tblclmnFileName.setWidth(100);
        tblclmnFileName.setText("File Name");
        
        Button btnAttachment = new Button(shell, SWT.NONE);
        btnAttachment.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fDialog = new FileDialog(shell, SWT.MULTI);
                if (fDialog.open() != null) {
                    String filterPath = fDialog.getFilterPath();
                    String[] filenames = fDialog.getFileNames();
                    
                    for (String filename : filenames) {
                        TableItem item = new TableItem(tableAttachment, SWT.NONE);
                        item.setText(new String[] {new File(filterPath, filename).getAbsolutePath()});
                    }
                    
                }
            }
        });
        GridData gd_btnAttachment = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_btnAttachment.heightHint = 10;
        btnAttachment.setLayoutData(gd_btnAttachment);
        btnAttachment.setText("添加");

    }
    
    
    
    public void sendMessage() {
        MailAccount curSendAccount = null;
        
        for (MailAccount account : mailService.getMailAccountSettings()) {
            if (account.getDisplayName().equals(comboSendAccount.getText())) {
                curSendAccount = account;
                break;
            }
        }
        
        if (curSendAccount == null) {
            MessageDialog.openError(shell, "Account Not Found", "Account " + comboSendAccount.getText() + " Not Found.");
            return;
        }
        
        String sendInfo = "";
        MultiPartEmail email = curSendAccount.getMultiPartEmail();
        try {
            email.addTo(textReceiveAddress.getText());
            email.setSubject(textSubject.getText());
            email.setMsg(styledTextPlainMessage.getText());
            
            for (TableItem item : tableAttachment.getItems()) {
                String filePath = item.getText();
                File file = new File(filePath);
                System.out.println(file.exists());
                
                if (!file.canRead()) {
                    MessageDialog.openError(shell, "File Not Found", "File " + file + " no found on disk!");
                    return;
                }
                email.attach(file);
            }
            
            sendInfo = email.send();
            System.out.println(sendInfo);
            
            this.shell.close();
            
        } catch (EmailException e) {
            MessageDialog.openError(shell, "Error", e.getMessage());
            e.printStackTrace();
        }
    }
}
