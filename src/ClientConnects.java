import com.sun.xml.internal.ws.util.StringUtils;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.*;
import java.util.*;

public class ClientConnects {

    public static void main(String[] args) throws MessagingException {

        if (Proprietes.email_id.equals(Proprietes.email_id) && Proprietes.password.equals(Proprietes.password))
            System.out.println("You are logged in Email");
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        // testing
        props.setProperty("mail.imaps.usesocketchannels", "true");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", Proprietes.email_id, Proprietes.password);
            System.out.println("The name of the user's Gmail:" + "\n" + store);
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);
            BufferedReader optionReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Searching file from External User: " + "\n");

            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_WRITE);
            // we should display only unread messages
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message msg[] = inbox.search(ft);
            String myunreadMail = "";
            System.out.println("Number of unseen messages : " + msg.length);
            // inbox for messages
            Message[] messages = inbox.getMessages();
            for (int i = 0; i < messages.length; i++) {
                if (messages[i] != null && Proprietes.keyword.equals(messages[i].getSubject())) {
                    String myMail = "";
                    System.out.println("Message SUBJECT: " + messages[i].getSubject());
                    System.out.println("Message DATE: " + messages[i].getSentDate());
                    System.out.println("Message FROM: " + messages[i].getFrom()[0].toString());
                    System.out.println("CONTENT: " + messages[i].getContent().toString());
                    System.out.println(getTextFromMessage(messages[i]));
                }
                folderInbox.close(true);
                store.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        try {
            String result = "";
            if (message.isMimeType("text/plain")) {
                result = message.getContent().toString();
            } else if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                result = getTextFromMimeMultipart(mimeMultipart);
            }
            return result;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        try {
            String result = "";
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result = result + "\n" + bodyPart.getContent();
                    break; // without break same text appears twice in my tests
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result = result + "\n" + Jsoup.parse(html).text();
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                }
            }
            return result;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


