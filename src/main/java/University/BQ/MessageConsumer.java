package University.BQ;

import com.google.common.io.ByteStreams;
import com.sun.mail.imap.IMAPMessage;
import org.mortbay.util.ajax.JSON;
import sun.misc.IOUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.concurrent.BlockingQueue;

public class MessageConsumer implements Runnable {
    private BlockingQueue<Message> messageQueue;

    public MessageConsumer(BlockingQueue<Message> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = messageQueue.take();
                OutputStream out = new FileOutputStream("C:/Rodion/msg/" + message.getMessageNumber() + ".txt");
                message.writeTo(out);
                out.close();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (MessagingException | IOException e) {
                return;
            }
        }
    }
}
