import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class ZNodeService implements Watcher  {

    private static final String ELECTION_NAMESPACE = "/election";
    private static final String Z_NODE_NAMESPACE = "/z_";
    private String currentZNodeName;
    private ZooKeeper zooKeeper;

    public void init(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createZNode(zooKeeper);
        determineLeader(zooKeeper);
        pingAliveMsg();
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
            Stat predecessorStat = null; // To avoid race condition

            while(predecessorStat == null) {
                List<String> zNodes = zooKeeper.getChildren(ELECTION_NAMESPACE, false);

                if (zNodes.isEmpty()) {
                    System.out.println("All nodes in the cluster are dead; may the Lord have mercy on our souls.");
                    return;
                }

                Collections.sort(zNodes);
                String smallestZNode = zNodes.get(0);

                if (smallestZNode.equals(currentZNodeName)) {
                    System.out.println("I am the leader: [" + currentZNodeName + "]");
                    return;
                }

                int predecessorIndex = Collections.binarySearch(zNodes, currentZNodeName) - 1;
                String predecessorZNode = zNodes.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZNode, this);

                System.out.println("I am NOT the leader '" + currentZNodeName + "' I am watching >>> " + predecessorZNode);
            }

        } catch (KeeperException | InterruptedException e) {
            System.out.println("Failed to elect leader due to: " + e.getMessage());
        }

    }

    private void pingAliveMsg() {
       Timer timer = new Timer();
       timer.schedule(new PingTask(currentZNodeName), 0 , 2000);
    }

    @Override
    public void process(WatchedEvent event) {

        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("===========================================");
                    System.out.println("Successfully connected to ZooKeeper server");
                    System.out.println("===========================================\n");
                } else if (event.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from ZooKeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                System.out.println("\n-- NODE DELETED --\n");
                // All events are one time triggers, to continuously watch events, re-register them
                determineLeader(zooKeeper);
                break;
            default:
                System.out.println("Event type unhandled:" + event.getType());
        }
    }
}
