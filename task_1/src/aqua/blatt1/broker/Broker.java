package aqua.blatt1.broker;

import aqua.blatt1.common.Direction;
import aqua.blatt1.common.msgtypes.DeregisterRequest;
import aqua.blatt1.common.msgtypes.HandoffRequest;
import aqua.blatt1.common.msgtypes.RegisterRequest;
import aqua.blatt1.common.msgtypes.RegisterResponse;
import messaging.Endpoint;
import messaging.Message;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Broker für Aufgabenblatt 1
 * @since 18.03.2023
 * @author ShuraBlack
 */
public class Broker {

    private final Endpoint endpoint;
    private final ClientCollection<InetSocketAddress> connections;
    private int counterID = 0;
    private boolean run = true;

    public Broker() {
        this.endpoint = new Endpoint(4711);
        this.connections = new ClientCollection<>();
    }

    /**
     * In der broker-Methode soll in einer Endlosschleife blockierend auf Nachrichten
     * gewartet werden. Ankommende Nachrichten muessen dekodiert und die im folgenden beschriebenen Methoden aufgerufen werden.
     */
    public void broker() {
        System.out.println("Waiting for Connection...");
        while (run) {
            final Message msg = this.endpoint.blockingReceive();

            final Serializable payload = msg.getPayload();
            final InetSocketAddress sender = msg.getSender();
            System.out.println("Message Received - Address: " + sender.getHostString());

            if (payload instanceof RegisterRequest) {
                register(sender);
            } else if (payload instanceof HandoffRequest) {
                handoffFish(sender, (HandoffRequest) payload);
            } else if (payload instanceof DeregisterRequest) {
                deregister(sender);
            }
        }
    }

    public void close() {
        this.run = false;
    }

    /**
     * Die register-Methode wird aufgerufen bei einem {@link RegisterRequest}. Der
     * Broker vergibt eine neue ID, beispielweise "tank1" fuer den ersten Client, "tank2"
     * fuer den zweiten, usw., traegt den neuen Client in die Client-Liste ein und antwortet
     * ihm mit einer {@link RegisterResponse}-Nachricht.
     * @param sender {@link InetSocketAddress} des Senders
     */
    private void register(InetSocketAddress sender) {
        final int idx = this.connections.indexOf(sender);
        if (idx != -1) {
            System.out.println("Client already got registered!");
            return;
        }
        final String id = "tank_" + (counterID++);
        this.connections.add(id,sender);
        endpoint.send(sender,new RegisterResponse(id));
        System.out.println("New Client registered. ClientID: " + id + " - Activ Clients: " + this.connections.size());
    }

    /**
     * Die deregister-Methode wird aufgerufen bei einem {@link DeregisterRequest}. Der
     * Broker entfernt den Client aus der Client-Liste.
     * @param sender {@link InetSocketAddress} des Senders
     */
    private void deregister(InetSocketAddress sender) {
        final int idx = this.connections.indexOf(sender);
        if (idx == -1) {
            return;
        }
        this.connections.remove(idx);
        System.out.println("Client deregistered");
    }

    /**
     * Die Methode handoffFish wird aufgerufen bei einem {@link HandoffRequest}. Der
     * Broker ermittelt den betroffenen Nachbarn und gibt den {@link HandoffRequest} an
     * diesen weiter.
     * @param sender {@link InetSocketAddress} des Senders
     * @param payload {@link Serializable} Request welches als Payload dran hängt
     */
    private void handoffFish(InetSocketAddress sender, HandoffRequest payload) {
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
    }

    public static void main(String[] args) {
        Broker broker = new Broker();
        broker.broker();
    }
}
