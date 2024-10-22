package MailClient;

import MailClient.ui.NewAccountConfigureWizard;
import MailClient.ui.WriteMailDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.mail.util.MimeMessageParser;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import MailClient.mailing.MailService;
import MailClient.mailing.MailAccount;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import MailClient.mailing.MailProtocol;

public class MailClient {

    private Shell shlMailClient;

    private Tree treeFolders;

    private Table tableMails;

    private Browser browserEmailViewer;

    private MailService mailService;
    private Text textSubject;
    private Text textFrom;
    private Text textTo;
    private Table tableAttachment;

    private Message browserCurrentShowMessage;

    public MailClient() {
        mailService = new MailService();
    }

    /**
     * Launch the application.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            MailClient window = new MailClient();
            window.open();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    public void open() {
        Display display = Display.getDefault();
        createContents();
        shlMailClient.open();
        shlMailClient.layout();
        while (!shlMailClient.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shlMailClient = new Shell();
        shlMailClient.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                doCloseClean();
            }
        });
        shlMailClient.setSize(960, 640);
        shlMailClient.setText("MailClient");
        shlMailClient.setLayout(new GridLayout(1, false));

        Menu menu = new Menu(shlMailClient, SWT.BAR);
        shlMailClient.setMenuBar(menu);

        MenuItem mntmMail = new MenuItem(menu, SWT.CASCADE);
        mntmMail.setText("Mail");

        Menu menu_1 = new Menu(mntmMail);
        mntmMail.setMenu(menu_1);

        MenuItem mntmNewAccount = new MenuItem(menu_1, SWT.NONE);

        mntmNewAccount.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                class NewAccountConfigureWizardDialog extends WizardDialog {
                    public NewAccountConfigureWizardDialog(Shell parentShell, IWizard wizard) {
                        super(parentShell, wizard);
                    }

                    public MailAccount getMailAccountSettings() {
                        return ((NewAccountConfigureWizard) getWizard()).getMailAccountSettings();
                    }
                }
                NewAccountConfigureWizardDialog dialog = new NewAccountConfigureWizardDialog(shlMailClient,
                        new NewAccountConfigureWizard());

                if (dialog.open() == 0) {

                    MailAccount settings = dialog.getMailAccountSettings();
                    mailService.addMailAccountSettings(settings);
                    addAccountToTree(settings);
                }
            }
        });
        mntmNewAccount.setText("New Account...");

        new MenuItem(menu_1, SWT.SEPARATOR);

        MenuItem mntmNewEmail = new MenuItem(menu_1, SWT.NONE);
        mntmNewEmail.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WriteMailDialog dialog = new WriteMailDialog(shlMailClient, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, mailService);
                dialog.open();
            }
        });
        mntmNewEmail.setText("New Email...");

        MenuItem mntmRefresh = new MenuItem(menu_1, SWT.NONE);
        mntmRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refresh();
            }
        });
        mntmRefresh.setText("Refresh");

        SashForm sashForm = new SashForm(shlMailClient, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Group grpAccounts = new Group(sashForm, SWT.NONE);
        grpAccounts.setText("Accounts");
        grpAccounts.setLayout(new GridLayout(1, false));

        treeFolders = new Tree(grpAccounts, SWT.BORDER);
        treeFolders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                Point point = new Point(e.x, e.y);
                TreeItem treeItem = treeFolders.getItem(point);
                if (treeItem != null && treeItem.getData("mail.folder") != null) {
                    try {
                        //System.out.println(treeItem.getData("mail.folder"));
                        loadMailFolderIncrementally((Folder) treeItem.getData("mail.folder"));
                    } catch (MessagingException e1) {
//                        MessageDialog.openError(shlMailClient, "Error", e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        treeFolders.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Group grpMails = new Group(sashForm, SWT.NONE);
        grpMails.setText("Mails");
        grpMails.setLayout(new GridLayout(1, false));

        MailAccount settings = new MailAccount("张岩森","2485940723@qq.com","ykogudgqlvhcdhhj","smtp.qq.com", MailProtocol.SMTP,"imap.qq.com",MailProtocol.IMAP);
        mailService.addMailAccountSettings(settings);
        addAccountToTree(settings);

        tableMails = new Table(grpMails, SWT.BORDER | SWT.FULL_SELECTION);
        tableMails.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Point point = new Point(e.x, e.y);
                TableItem item = tableMails.getItem(point);
                if (item != null && item.getData("mail.message") != null) {

                    Message message = (Message) item.getData("mail.message");
                    if (message instanceof MimeMessage) {
                        loadMessageView((MimeMessage) message);
                    } else {
                        MessageDialog.openError(shlMailClient, "Error while reading message", "Unknown type.");
                    }

                }
            }
        });
        tableMails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tableMails.setHeaderVisible(true);
        tableMails.setLinesVisible(true);

        TableColumn tblclmnSubject = new TableColumn(tableMails, SWT.NONE);
        tblclmnSubject.setWidth(100);
        tblclmnSubject.setText("Subject");

        TableColumn tblclmnFrom = new TableColumn(tableMails, SWT.NONE);
        tblclmnFrom.setWidth(100);
        tblclmnFrom.setText("From");

        TableColumn tblclmnTo = new TableColumn(tableMails, SWT.NONE);
        tblclmnTo.setWidth(100);
        tblclmnTo.setText("To");

        TableColumn tblclmnDate = new TableColumn(tableMails, SWT.NONE);
        tblclmnDate.setWidth(100);
        tblclmnDate.setText("Date");

        Group grpMailView = new Group(sashForm, SWT.NONE);
        grpMailView.setText("Mail Content");
        grpMailView.setLayout(new GridLayout(4, false));

        Label lblSubject = new Label(grpMailView, SWT.NONE);
        lblSubject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubject.setText("Subject:");

        textSubject = new Text(grpMailView, SWT.BORDER);
        textSubject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(grpMailView, SWT.NONE);

        Button btnDeleteMail = new Button(grpMailView, SWT.NONE);
        btnDeleteMail.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteCurrentShowed();
            }
        });
        btnDeleteMail.setText("Delete Mail");

        Label lblFrom = new Label(grpMailView, SWT.NONE);
        lblFrom.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblFrom.setText("From:");

        textFrom = new Text(grpMailView, SWT.BORDER);
        textFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblTo = new Label(grpMailView, SWT.NONE);
        lblTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTo.setText("To:");

        textTo = new Text(grpMailView, SWT.BORDER);
        textTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        browserEmailViewer = new Browser(grpMailView, SWT.NONE);
        browserEmailViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        tableAttachment = new Table(grpMailView, SWT.BORDER | SWT.FULL_SELECTION);
        tableAttachment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Point point = new Point(e.x, e.y);
                TableItem item = tableAttachment.getItem(point);
                if (item != null && item.getData("mail.attachment") != null) {
                    saveAttachment((DataSource) item.getData("mail.attachment"));
                }
            }
        });
        tableAttachment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
        tableAttachment.setHeaderVisible(true);
        tableAttachment.setLinesVisible(true);

        TableColumn tblclmnFilename = new TableColumn(tableAttachment, SWT.NONE);
        tblclmnFilename.setWidth(100);
        tblclmnFilename.setText("File Name");

        TableColumn tblclmnType = new TableColumn(tableAttachment, SWT.NONE);
        tblclmnType.setWidth(100);
        tblclmnType.setText("Type");
        sashForm.setWeights(new int[] { 1, 2, 6 });

    }

    public void refresh() {
        treeFolders.removeAll();
        for (MailAccount account : mailService.getMailAccountSettings()){
            addAccountToTree(account);
        }
    }

    public void addAccountToTree(MailAccount account) {
        if (account == null || account.getDisplayName() == null) {
            throw new IllegalArgumentException("Account or display name cannot be null");
        }

        TreeItem accountRoot = new TreeItem(treeFolders, 0);
        accountRoot.setText(account.getDisplayName());
        accountRoot.setData("mail.account", account);

        Folder[] folders = null;
        try {
            folders = account.getFolders();
        } catch (AuthenticationFailedException e) {
            MessageDialog.openError(shlMailClient, "Authentication Failed", e.getMessage());
            return;
        } catch (MessagingException e) {
            MessageDialog.openError(shlMailClient, "Something Went Mad", e.getMessage());
            e.printStackTrace();
            return;
        }

        for (Folder folder : folders) {
            TreeItem item = new TreeItem(accountRoot, SWT.NONE);
            item.setText(folder.getFullName());
            item.setData("mail.folder", folder);
        }

    }

    private AtomicInteger currentStart = new AtomicInteger(0);
    private final int PAGE_SIZE = 100;
    private Thread currentLoadThread;
    private final AtomicBoolean shouldContinue = new AtomicBoolean(true);

    private final ReentrantLock lock = new ReentrantLock();

    public synchronized void loadMailFolderIncrementally(Folder folder) throws MessagingException {
        lock.lock();
        try {
            if (currentLoadThread != null && currentLoadThread.isAlive()) {
                shouldContinue.set(false);
                currentLoadThread.join();
            }

            currentStart.set(0);
            shouldContinue.set(true);
            Display.getDefault().asyncExec(() -> {
                if (!tableMails.isDisposed()) {
                    tableMails.removeAll();
                }
            });

            currentLoadThread = new Thread(() -> {
                try {
                    while (shouldContinue.get() && currentStart.get() < folder.getMessageCount()) {
                        loadMailFolderPage(folder, currentStart.get(), PAGE_SIZE);
                        currentStart.addAndGet(PAGE_SIZE);
                        Thread.sleep(1);  // 加载完一页后暂停1毫秒，避免一次加载太多
                    }
                } catch (MessagingException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        System.out.println("Loading thread was interrupted");
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            currentLoadThread.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void loadMailFolderPage(Folder folder, int start, int count) throws MessagingException {
        if (!folder.isOpen()) {
            try {
                folder.open(Folder.READ_ONLY);
            } catch (MessagingException e) {
                e.printStackTrace();
                return;
            }
        }

        int totalMessages = folder.getMessageCount();
        int end = Math.min(start + count, totalMessages);

        if (start >= end) {
            System.out.println("No more messages to load");
            return;
        }

        Message[] messages = folder.getMessages(start + 1, end);
        java.util.List<Message> msgList = Arrays.asList(messages);
        Collections.reverse(msgList);
        messages = msgList.toArray(messages);

        for (Message message : messages) {
            if (!shouldContinue.get()) {
                System.out.println("Stopped loading due to folder switch");
                return;
            }

            String subject = message.getSubject();
            String fromString = "";
            String recString = "";
            String date = message.getReceivedDate().toString();

            Address[] fromAddresses = message.getFrom();
            Address[] recAddresses = message.getAllRecipients();
            for (Address address : fromAddresses) {
                try {
                    fromString += MimeUtility.decodeText(address.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            for (Address address : recAddresses) {
                try {
                    recString += MimeUtility.decodeText(address.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            String finalFromString = fromString;
            String finalRecString = recString;
            Display.getDefault().asyncExec(() -> {
                if (shouldContinue.get() && !tableMails.isDisposed()) {
                    TableItem tableItem = new TableItem(tableMails, SWT.NONE);
                    tableItem.setText(new String[]{subject, finalFromString, finalRecString, date});
                    tableItem.setData("mail.message", message);
                }
            });
        }
    }


    public void loadMessageView(MimeMessage message) {
        MimeMessageParser parser = new MimeMessageParser(message);
        try {
            parser.parse();

            textSubject.setText(parser.getSubject());

            textFrom.setText(parser.getFrom());

            String mailToString = "";
            for (Address address : parser.getTo()) {
                mailToString += MimeUtility.decodeText(address.toString());
            }
            textTo.setText(mailToString);

            if (parser.hasHtmlContent()) {
                browserEmailViewer.setText(parser.getHtmlContent());
            } else {
                browserEmailViewer.setText(parser.getPlainContent());
            }

            tableAttachment.removeAll();
            if (parser.hasAttachments()) {
                for (DataSource ds : parser.getAttachmentList()) {
                    TableItem tableItem = new TableItem(tableAttachment, SWT.NONE);
                    tableItem.setText(new String[] {ds.getName(), ds.getContentType()});

                    tableItem.setData("mail.attachment", ds);

                }
            }

            browserCurrentShowMessage = message;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            browserCurrentShowMessage = null;
        }
    }

    public void saveAttachment(DataSource ds) {
        FileDialog fDialog = new FileDialog(shlMailClient);
        fDialog.setFileName(ds.getName());
        if (fDialog.open() != null) {
            String filename = fDialog.getFileName();
            String filtrPath = fDialog.getFilterPath();
            File file = new File(filtrPath, filename);
            try {
                InputStream is = ds.getInputStream();
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buf)) != -1) {
                    fos.write(buf, 0, bytesRead);
                }
                fos.close();

            } catch (Exception e) {
                MessageDialog.openError(shlMailClient, "Save Error", e.getMessage());
            }
        }

    }

    public void deleteCurrentShowed() {
        try {
            browserCurrentShowMessage.setFlag(Flag.DELETED, true);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        browserEmailViewer.setText("");
        textFrom.setText("");
        textSubject.setText("");
        textTo.setText("");
    }

    public void doCloseClean() {
        for (TreeItem accounTreeItem : treeFolders.getItems()) {
            if (accounTreeItem.getData("mail.account") != null) {
                for (TreeItem folderItem : accounTreeItem.getItems()) {
                    if (folderItem.getData("mail.folder") != null) {
                        Folder folder = (Folder)folderItem.getData("mail.folder");
                        if (folder.isOpen()) {
                            try {
                                folder.close(true);
                                System.out.println("Try to close " + folder.getFullName());
                            } catch (MessagingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

}