package University.BQ;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.BlockingQueue;

public class MessageProducer implements Runnable {
    private BlockingQueue<Message> messageQueue;
    private Message[] messages;

    public MessageProducer(BlockingQueue<Message> messageQueue, Message[] messages) {
        this.messageQueue = messageQueue;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            for (Message message : messages) {
                messageQueue.put(message);
            }

            for (int i = 0; i < 9; i++) {
                messageQueue.put(new MimeMessage((Session) null));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
