import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Zookeeper Client main class.
 * Creates connection with ZooKeeper server & awaits events
 */
public class ZooKeeperClient implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 2000;
    private ZooKeeper zooKeeper;

    public static void main(String[] arg) throws IOException, InterruptedException, KeeperException {
        System.out.println("Starting application ....");
        ZooKeeperClient zooKeeperClient = new ZooKeeperClient();
        zooKeeperClient.connectToZookeeper();
        zooKeeperClient.run();
        zooKeeperClient.close();
        System.out.println("Exiting application");
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {

        System.out.println("Event type:" + event.getType() + " - " + event.getState());

        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to ZooKeeper");
                } else if (event.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from ZooKeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
