import java.util.TimerTask;

public class PingTask  extends TimerTask  {

    private final String nodeName;

    public PingTask(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public void run() {
        System.out.println("I am " + nodeName + " still alive and going some work !");
    }
}
