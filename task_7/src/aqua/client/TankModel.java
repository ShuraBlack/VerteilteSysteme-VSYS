package aqua.client;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import aqua.common.Direction;
import aqua.common.FishModel;
import aqua.common.msgtypes.*;

import javax.swing.*;

public class TankModel extends Observable implements Iterable<FishModel> {

	private boolean registered = false;

	public static final int WIDTH = 600;
	public static final int HEIGHT = 350;
	protected static final int MAX_FISHIES = 5;
	protected static final Random rand = new Random();
	protected volatile String id;
	protected final Set<FishModel> fishies;
	protected int fishCounter = 0;
	protected final ClientCommunicator.ClientForwarder forwarder;

	protected InetSocketAddress leftNeighbor;
	protected InetSocketAddress rightNeighbor;

	protected boolean token;
	protected Timer timer;

	protected Snapshot snapshot;
	protected ChannelMode mode = ChannelMode.IDLE;

	protected SnapshotToken snapshotToken;
	protected boolean tokenSender = false;

	private Thread snapshotTokenHandOff;

	// protected Map<String, Reference> forwardReference = new HashMap<>();
	protected Map<String, InetSocketAddress> homeAgent = new HashMap<>();

	public TankModel(ClientCommunicator.ClientForwarder forwarder) {
		this.fishies = Collections.newSetFromMap(new ConcurrentHashMap<>());
		this.forwarder = forwarder;
		this.token = false;
		this.timer = new Timer();
	}

	public synchronized void onToken() {
		this.token = true;
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				token = false;
				forwarder.handOffToken(leftNeighbor);
			}
		},2000);
	}

	public synchronized boolean hasToken() {
		return this.token;
	}

	public void initiateSnapshot() {
		// Speicher lokalen Zustand
		this.snapshot = new Snapshot(this.fishCounter);
		// Starte Aufzeichnung
		this.mode = ChannelMode.BOTH;
		// Sende Marker
		forwarder.send(this.leftNeighbor, new SnapshotMarker());
		forwarder.send(this.rightNeighbor, new SnapshotMarker());

		// Starte Snapshot token
		this.tokenSender = true;
		this.snapshotToken = new SnapshotToken();
	}

	synchronized void onRegistration(String id, int leasing) {
		this.id = id;
		if (!registered) {
			newFish(WIDTH - FishModel.getXSize(), rand.nextInt(HEIGHT - FishModel.getYSize()));
			registered = true;
		}
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				forwarder.register();
			}
		},(leasing-1) * 1000L);
	}

	synchronized void onNeighborUpdate(InetSocketAddress left, InetSocketAddress right) {
		this.leftNeighbor = left;
		this.rightNeighbor = right;
	}

	/*
	synchronized void onLocationRequest(String id) {
		locateFishGlobally(id);
	}
	 */

	public synchronized void onSnapshotMarker(InetSocketAddress sender) {
		if (this.mode.equals(ChannelMode.IDLE)) {
			// Speicher lokalen Zustand
			this.snapshot = new Snapshot(this.fishCounter);

			// Starte Aufzeichnung auf andere Kanäle
			if (this.leftNeighbor.equals(sender)) {
				this.mode = ChannelMode.RIGHT;
			} else if (this.rightNeighbor.equals(sender)) {
				this.mode = ChannelMode.LEFT;
			}

			// Sende Marker (Alle Ausgänge)
			forwarder.send(this.leftNeighbor, new SnapshotMarker());
			forwarder.send(this.rightNeighbor, new SnapshotMarker());
		} else {
			switch (this.mode) {
				case BOTH:
					if (sender.equals(this.leftNeighbor)) {
						this.mode = ChannelMode.RIGHT;
					} else if (sender.equals(this.rightNeighbor)) {
						this.mode = ChannelMode.LEFT;
					}
					break;
				case LEFT:
					if (sender.equals(this.leftNeighbor)) {
						this.mode = ChannelMode.IDLE;
					}
					break;
				case RIGHT:
					if (sender.equals(this.rightNeighbor)) {
						this.mode = ChannelMode.IDLE;
					}
					break;
			}

			if (this.mode.equals(ChannelMode.IDLE)) {
				this.snapshot.setFinished();
			}

			if (this.snapshot.isFinished() && this.snapshotToken != null) {
				System.out.println("Initiator send snapshot token");
				this.snapshotToken.add(this.snapshot.getValue());
				forwarder.send(this.leftNeighbor, this.snapshotToken);

				this.snapshot = null;
				this.snapshotToken = null;
			}
		}
	}

	public void onNameResolution(NameResolutionResponse response) {
		forwarder.locationUpdate(response);
	}

	public void onUpdateLocation(String id, InetSocketAddress location) {
		homeAgent.put(id, location);
	}

	public synchronized void onSnapshotToken(SnapshotToken token) {
		if (this.tokenSender) {
			new Thread(() -> JOptionPane.showMessageDialog(null, "Global Snapshot: " + token.getValue())).start();
			this.tokenSender = false;
		} else {

			if (this.snapshotTokenHandOff != null) {
				return;
			}

			this.snapshotTokenHandOff = new Thread(() -> {
				while(!this.snapshot.isFinished()) {
					Thread.onSpinWait();
				}

				System.out.println("Client updated snapshot and handoff token");
				token.add(this.snapshot.getValue());
				forwarder.send(this.leftNeighbor, token);

				this.snapshot = null;
				this.snapshotTokenHandOff = null;
			});
			this.snapshotTokenHandOff.start();
		}
		this.snapshotToken = null;
	}

	public synchronized void newFish(int x, int y) {
		if (fishies.size() < MAX_FISHIES) {
			x = Math.min(x, WIDTH - FishModel.getXSize() - 1);
			y = Math.min(y, HEIGHT - FishModel.getYSize());

			FishModel fish = new FishModel("fish" + (++fishCounter) + "@" + getId(), x, y,
					rand.nextBoolean() ? Direction.LEFT : Direction.RIGHT);
			fishies.add(fish);
			homeAgent.put(fish.getId(), null);
			//forwardReference.put(fish.getId(), Reference.HERE);
		}
	}

	synchronized void receiveFish(FishModel fish) {
		if (!this.mode.equals(ChannelMode.IDLE)) {
			this.snapshot.update(1);
		}
		fish.setToStart();
		fishies.add(fish);
		// Eigener Fisch
		if (homeAgent.containsKey(fish.getId())) {
			homeAgent.put(fish.getId(), null);
			return;
		}

		// Fremder Fisch
		forwarder.nameResolution(fish);

		//forwardReference.put(fish.getId(), Reference.HERE);
	}

	public String getId() {
		return id;
	}

	public synchronized int getFishCounter() {
		return fishCounter;
	}

	public synchronized Iterator<FishModel> iterator() {
		return fishies.iterator();
	}

	private synchronized void updateFishies() {
		for (Iterator<FishModel> it = iterator(); it.hasNext();) {
			FishModel fish = it.next();

			fish.update();

			if (fish.hitsEdge()) {
				if (!hasToken()) {
					fish.reverse();
					return;
				}
				if (this.snapshot != null) {
					this.snapshot.update(-1);
				}
				if (fish.getDirection().getVector() == 1) {
					forwarder.handOff(fish, rightNeighbor);
					//forwardReference.put(fish.getId(), Reference.RIGHT);
				} else {
					forwarder.handOff(fish, leftNeighbor);
					//forwardReference.put(fish.getId(), Reference.LEFT);
				}
			}

			if (fish.disappears())
				it.remove();
		}
	}

	public void locateFishGlobally(String id) {
		// Lokale Suche
		InetSocketAddress address = locateFishLocally(id);
		// Weitergabe an Heimatsaquarium
		if (address == null) {
			return;
		}
		forwarder.send(address, new LocationRequest(id));
	}

	public InetSocketAddress locateFishLocally(String id) {
		InetSocketAddress address = homeAgent.get(id);
		if (address == null) {
			for (FishModel fish : fishies) {
				if (!fish.getId().equals(id)) {
					continue;
				}
				fish.toggle();
			}
		}
		return address;
	}

	/*
	public void locateFishGlobally(String id) {
		Reference ref = forwardReference.get(id);
		if (ref == null) {
			return;
		}
		// Lokale Suche
		if (ref.equals(Reference.HERE)) {
			for (FishModel fish : fishies) {
				if (!fish.getId().equals(id)) {
					continue;
				}
				fish.toggle();
				return;
			}
		}
		// Msg für suche
		if (ref.equals(Reference.RIGHT)) {
			forwarder.send(rightNeighbor, new LocationRequest(id));
		} else {
			forwarder.send(leftNeighbor, new LocationRequest(id));
		}
	}
	 */

	private synchronized void update() {
		updateFishies();
		setChanged();
		notifyObservers();
	}

	protected void run() {
		forwarder.register();

		try {
			while (!Thread.currentThread().isInterrupted()) {
				update();
				TimeUnit.MILLISECONDS.sleep(10);
			}
		} catch (InterruptedException consumed) {
			// allow method to terminate
		}
	}

	public synchronized void finish() {
		forwarder.deregister(id);
	}
}