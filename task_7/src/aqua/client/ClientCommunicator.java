package aqua.client;

import java.io.Serializable;
import java.net.InetSocketAddress;

import aqua.common.endpoint.SecureEndpointAsymmetric;
import aqua.common.msgtypes.*;
import messaging.Endpoint;
import messaging.Message;
import aqua.common.FishModel;
import aqua.common.Properties;

public class ClientCommunicator {

	private final Endpoint endpoint;

	public ClientCommunicator() {
		endpoint = new SecureEndpointAsymmetric();
	}

	public class ClientForwarder {
		private final InetSocketAddress broker;

		private ClientForwarder() {
			this.broker = new InetSocketAddress(Properties.HOST, Properties.PORT);
		}

		public void register() {
			endpoint.send(broker, new RegisterRequest());
		}

		public void deregister(String id) {
			endpoint.send(broker, new DeregisterRequest(id));
		}

		public void handOff(FishModel fish, InetSocketAddress receiver) {
			endpoint.send(receiver, new HandoffRequest(fish));
		}

		public void nameResolution(FishModel fish) {
			endpoint.send(broker, new NameResolutionRequest(fish.getTankId(),fish.getId()));
		}

		public void locationUpdate(NameResolutionResponse response) {
			endpoint.send(response.getTargetAddress(), new LocationUpdate(response.getRequestId(), response.getSourceAddress()));
		}

		public void handOffToken(InetSocketAddress receiver) {
			endpoint.send(receiver, new Token());
		}
		public void send(InetSocketAddress receiver, Serializable payload) {
			endpoint.send(receiver, payload);
		}
	}

	public class ClientReceiver extends Thread {
		private final TankModel tankModel;

		private ClientReceiver(TankModel tankModel) {
			this.tankModel = tankModel;
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				Message msg = endpoint.blockingReceive();

				if (msg == null) {
					return;
				}

				if (msg.getPayload() instanceof RegisterResponse)
					tankModel.onRegistration(((RegisterResponse) msg.getPayload()).getId()
							, ((RegisterResponse) msg.getPayload()).getLeasing());

				if (msg.getPayload() instanceof HandoffRequest)
					tankModel.receiveFish(((HandoffRequest) msg.getPayload()).getFish());

				if (msg.getPayload() instanceof NeighborUpdate) {
					NeighborUpdate payload = (NeighborUpdate) msg.getPayload();
					tankModel.onNeighborUpdate(payload.getLeft(), payload.getRight());
				}

				if (msg.getPayload() instanceof SnapshotMarker)
					tankModel.onSnapshotMarker(msg.getSender());

				if (msg.getPayload() instanceof SnapshotToken)
					tankModel.onSnapshotToken((SnapshotToken) msg.getPayload());

				if (msg.getPayload() instanceof Token)
					tankModel.onToken();

				if (msg.getPayload() instanceof NameResolutionResponse)
					tankModel.onNameResolution((NameResolutionResponse) msg.getPayload());

				if (msg.getPayload() instanceof LocationRequest)
					tankModel.locateFishLocally(((LocationRequest) msg.getPayload()).getId());

				if (msg.getPayload() instanceof LocationUpdate) {
					LocationUpdate payload = (LocationUpdate) msg.getPayload();
					tankModel.onUpdateLocation(payload.getId(), payload.getAddress());
				}
				/*
				if (msg.getPayload() instanceof LocationRequest)
					tankModel.onLocationRequest(((LocationRequest) msg.getPayload()).getId());
				 */
			}
			System.out.println("Receiver stopped.");
		}
	}

	public ClientForwarder newClientForwarder() {
		return new ClientForwarder();
	}

	public ClientReceiver newClientReceiver(TankModel tankModel) {
		return new ClientReceiver(tankModel);
	}

}