package aqua.blatt3.broker;

import aqua.blatt3.common.msgtypes.NameResolutionRequest;
import aqua.blatt3.common.msgtypes.NameResolutionResponse;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NameService {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, InetSocketAddress> mapping = new HashMap<>();

    public void add(String id, InetSocketAddress address) {
        lock.writeLock().lock();
        mapping.put(id, address);
        lock.writeLock().unlock();
    }

    public NameResolutionResponse get(InetSocketAddress sender, NameResolutionRequest request) {
        lock.readLock().lock();
        InetSocketAddress address = mapping.get(request.getTankID());
        if (address == null) {
            return new NameResolutionResponse(request.getRequestID(), sender, null);
        }
        lock.readLock().unlock();
        return new NameResolutionResponse(request.getRequestID(), sender, address);
    }
}
