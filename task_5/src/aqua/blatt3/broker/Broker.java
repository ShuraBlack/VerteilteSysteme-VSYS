package aqua.blatt3.broker;

import aqua.blatt3.common.Direction;
import aqua.blatt3.common.msgtypes.*;
import messaging.Endpoint;
import messaging.Message;

import javax.swing.*;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Broker für Aufgabenblatt 2
 * @since 22.03.2023
 * @author ShuraBlack
 */
public class Broker {

    private final NameService nameService = new NameService();

    private final ExecutorService service;
    private final Endpoint endpoint;
    private final ClientCollection<InetSocketAddress> connections;
    private int counterID = 0;
    private volatile boolean stopRequested = false;

    private final ReadWriteLock lock;

    public Broker() {
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.endpoint = new Endpoint(4711);
        this.connections = new ClientCollection<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void broker() {
        Thread backgroundThread = new Thread(() -> {
            JOptionPane.showMessageDialog(null,"Mit betätigen des OK Buttons wird der Server aufgefordert herunterzufahren!");
            this.stopRequested = true;
        });
        backgroundThread.start();
        System.out.println("Waiting for Connection...");
        while (!stopRequested) {
            final Message msg = this.endpoint.blockingReceive();
            if (msg.getPayload() instanceof PoisonPill) {
                System.out.println("Server got poisend and will shutdown soon!");
                break;
            }
            this.service.execute(new BrokerTask(msg));
        }
        if (!this.service.isShutdown()) {
            this.service.shutdownNow();
        }
        System.exit(0);
    }

    private void register(InetSocketAddress sender) {
        this.lock.writeLock().lock();
        final int idx = this.connections.indexOf(sender);
        if (idx != -1) {
            System.out.println("Client already got registered!");
            return;
        }
        final String id = "tank_" + (counterID++);
        this.connections.add(id,sender);
        this.nameService.add(id, sender);
        endpoint.send(sender,new RegisterResponse(id));
        System.out.println("New Client registered. ClientID: " + id + " - Activ Clients: " + this.connections.size());

        final int callerIdx = this.connections.indexOf(sender);
        switch (this.connections.size()) {
            case 1:
                endpoint.send(sender, new NeighborUpdate(sender,sender));
                break;
            case 2:
                final InetSocketAddress neighbor = this.connections.getLeftNeighorOf(callerIdx);

                endpoint.send(sender, new NeighborUpdate(neighbor,neighbor));
                endpoint.send(neighbor, new NeighborUpdate(sender,sender));
                break;
            default:
                Pair<InetSocketAddress, InetSocketAddress> neighbors = getLeftRightNeighbors(sender);

                sendNeighborUpdate(sender, neighbors);
                sendNeighborUpdate(neighbors.getLeft(), getLeftRightNeighbors(neighbors.getLeft()));
                sendNeighborUpdate(neighbors.getRight(), getLeftRightNeighbors(neighbors.getRight()));
        }
        System.out.println("Neighbors informed about new client");

        if (counterID == 1) {
            endpoint.send(sender, new Token());
        }
        this.lock.writeLock().unlock();
    }

    private void deregister(InetSocketAddress sender) {
        this.lock.writeLock().lock();
        final int idx = this.connections.indexOf(sender);
        if (idx == -1) {
            return;
        }
        Pair<InetSocketAddress, InetSocketAddress> neighbors = getLeftRightNeighbors(sender);
        this.connections.remove(idx);


        if (this.connections.size() != 0) {
            sendNeighborUpdate(neighbors.getLeft(), getLeftRightNeighbors(neighbors.getLeft()));
            sendNeighborUpdate(neighbors.getRight(), getLeftRightNeighbors(neighbors.getRight()));
        }
        System.out.println("Client deregistered and neighbors informed");
        this.lock.writeLock().unlock();
    }

    private void resolution(InetSocketAddress sender, NameResolutionRequest payload) {
        final NameResolutionResponse response = this.nameService.get(sender, payload);
        this.endpoint.send(sender, response);
    }

    private void handoffFish(InetSocketAddress sender, HandoffRequest payload) {
        this.lock.readLock().lock();
        final Direction dir = payload.getFish().getDirection();
        final int idx = this.connections.indexOf(sender);

        if (idx == -1) {
            System.out.println("Client tried to handoff without register!");
            return;
        }
        // Rechts(1), Links(-1)
        if (dir.getVector() == 1) {
            this.endpoint.send(this.connections.getRightNeighorOf(idx), payload);
        } else {
            this.endpoint.send(this.connections.getLeftNeighorOf(idx), payload);
        }
        System.out.println("Hand off: " + payload.getFish().getId());
        this.lock.readLock().unlock();
    }

    private void sendNeighborUpdate(InetSocketAddress receiver, Pair<InetSocketAddress, InetSocketAddress> neighbors) {
        this.endpoint.send(receiver, new NeighborUpdate(neighbors.getLeft(), neighbors.getRight()));
    }

    private Pair<InetSocketAddress, InetSocketAddress> getLeftRightNeighbors(InetSocketAddress address) {
        final int index = this.connections.indexOf(address);
        final InetSocketAddress leftNeighbor = this.connections.getLeftNeighorOf(index);
        final InetSocketAddress rightNeighbor = this.connections.getRightNeighorOf(index);
        return new Pair<>(leftNeighbor, rightNeighbor);
    }

    private class BrokerTask implements Runnable {

        private final Message msg;

        public BrokerTask(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            System.out.printf("Request worker(%d) runs\n",System.identityHashCode(this));
            final Serializable payload = msg.getPayload();
            final InetSocketAddress sender = msg.getSender();

            if (payload instanceof RegisterRequest)
                register(sender);

            if (payload instanceof HandoffRequest)
                handoffFish(sender, (HandoffRequest) payload);

            if (payload instanceof DeregisterRequest)
                deregister(sender);

            if (payload instanceof NameResolutionRequest)
                resolution(sender, (NameResolutionRequest) payload);
        }

    }

    private static class Pair<L,R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }
    }

    public static void main(String[] args) {
        Broker broker = new Broker();
        broker.broker();
    }
}
