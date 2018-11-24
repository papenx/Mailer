package University.BQ;


import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.BlockingQueue;

public class ProducerChecker implements  Runnable {
    private BlockingQueue<Message> queue;

    public ProducerChecker(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true){
            if(queue.size() == 0)
                break;
        }
        System.out.println(System.currentTimeMillis());
    }
}
