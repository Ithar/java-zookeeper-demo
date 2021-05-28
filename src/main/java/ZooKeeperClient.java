import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Zookeeper Client main class.
 * Creates connection with ZooKeeper server & awaits events
 */
public class ZooKeeperClient {

    private static final ZNodeService zNodeService = new ZNodeService();

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 2000;

    private ZooKeeper zooKeeper;

    public static void main(String[] arg) throws IOException, InterruptedException {

        ZooKeeperClient zooKeeperClient = new ZooKeeperClient();
        zooKeeperClient.connectToZookeeper();

        zNodeService.init(zooKeeperClient.getZooKeeper());

        zooKeeperClient.run();
        zooKeeperClient.close();
        System.out.println("Exiting application");
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, zNodeService);
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    private ZooKeeper getZooKeeper() {
        return this.zooKeeper;
    }

}
