import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ZNodeService implements Watcher  {

    private static final String ELECTION_NAMESPACE = "/election";
    private static final String Z_NODE_NAMESPACE = "/znode_";
    private String currentZNodeName;
    private ZooKeeper zooKeeper;

    public void init(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createZNode(zooKeeper);
        determineLeader(zooKeeper);
        watchZNode();
    }

    private void createZNode(ZooKeeper zooKeeper) {

        try {
            String zNodeAffix = ELECTION_NAMESPACE + Z_NODE_NAMESPACE;
            String zNodePath = zooKeeper.create(zNodeAffix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            this.currentZNodeName = zNodePath.replace(ELECTION_NAMESPACE + "/", "");
        } catch (KeeperException | InterruptedException e) {
            System.out.println("Problem creating zNode due to:" +e.getMessage());
        }
    }

    private void determineLeader(ZooKeeper zooKeeper) {

        try {
            List<String> zNodes = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(zNodes);
            String smallestZNode = zNodes.get(0);

            if (smallestZNode.equals(currentZNodeName)) {
                System.out.println("I am the leader: [" + currentZNodeName +"]");
                return;
            }

            System.out.println("I am NOT the leader '" + currentZNodeName + "' the leader is: [" + smallestZNode +"]");

        } catch (KeeperException | InterruptedException e) {
            System.out.println("Failed to elect leader due to: " + e.getMessage());
        }

    }

    private void watchZNode() {

        try {
            String zNodePath = ELECTION_NAMESPACE + "/" + currentZNodeName;
            Stat stat = zooKeeper.exists(zNodePath, this);
            if (stat == null) {
              return;
            }

            byte[] data = zooKeeper.getData(zNodePath, this, stat);
            List<String> children = zooKeeper.getChildren(zNodePath, this);

            System.out.println(currentZNodeName + " [data=" + Arrays.toString(data) + ", children= "+ children.size()+"] ");

        } catch (KeeperException | InterruptedException e) {
            System.out.println("Unable to get determine if zNode exists due to:" + e.getMessage());
        }

    }

    @Override
    public void process(WatchedEvent event) {

        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to ZooKeeper server");
                } else if (event.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from ZooKeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                System.out.println("Node deleted: " + currentZNodeName);
                break;
            case NodeCreated:
                System.out.println("Node created:" + currentZNodeName);
                break;
            case NodeDataChanged:
                System.out.println("Node data changed:" + currentZNodeName);
                break;
            case NodeChildrenChanged:
                System.out.println("Node children changed" + currentZNodeName);
                break;
            default:
                System.out.println("Event type unhandled:" + event.getType());
        }

        // All events are one time triggers, to continuously watch events, re-register them
        watchZNode();
    }
}
