package aqua.blatt2.broker;

import aqua.blatt2.common.Direction;
import aqua.blatt2.common.msgtypes.DeregisterRequest;
import aqua.blatt2.common.msgtypes.HandoffRequest;
import aqua.blatt2.common.msgtypes.RegisterRequest;
import aqua.blatt2.common.msgtypes.RegisterResponse;
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
        endpoint.send(sender,new RegisterResponse(id));
        System.out.println("New Client registered. ClientID: " + id + " - Activ Clients: " + this.connections.size());
        this.lock.writeLock().unlock();
    }

    private void deregister(InetSocketAddress sender) {
        this.lock.writeLock().lock();
        final int idx = this.connections.indexOf(sender);
        if (idx == -1) {
            return;
        }
        this.connections.remove(idx);
        System.out.println("Client deregistered");
        this.lock.writeLock().unlock();
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
        this.lock.readLock().unlock();
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

            if (payload instanceof RegisterRequest) {
                register(sender);
            } else if (payload instanceof HandoffRequest) {
                handoffFish(sender, (HandoffRequest) payload);
            } else if (payload instanceof DeregisterRequest) {
                deregister(sender);
            }
        }

    }

    public static void main(String[] args) {
        Broker broker = new Broker();
        broker.broker();
    }
}
