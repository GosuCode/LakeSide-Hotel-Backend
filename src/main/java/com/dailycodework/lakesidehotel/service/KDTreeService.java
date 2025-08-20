package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.Hotel;
import com.dailycodework.lakesidehotel.model.KDTreeNode;
import com.dailycodework.lakesidehotel.response.NearbyHotelResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class KDTreeService {

    private KDTreeNode root;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Long, Hotel> hotelCache = new HashMap<>();

    public void insertHotel(Long hotelId, Double latitude, Double longitude) {
        lock.writeLock().lock();
        try {
            root = insertRecursive(root, hotelId, latitude, longitude, 0);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private KDTreeNode insertRecursive(KDTreeNode node, Long hotelId, Double latitude, Double longitude, int depth) {
        if (node == null) {
            return new KDTreeNode(hotelId, latitude, longitude);
        }

        int cd = depth % 2; // 0 for latitude, 1 for longitude

        if (cd == 0) {
            if (latitude < node.getLatitude()) {
                node.setLeft(insertRecursive(node.getLeft(), hotelId, latitude, longitude, depth + 1));
            } else {
                node.setRight(insertRecursive(node.getRight(), hotelId, latitude, longitude, depth + 1));
            }
        } else {
            if (longitude < node.getLongitude()) {
                node.setLeft(insertRecursive(node.getLeft(), hotelId, latitude, longitude, depth + 1));
            } else {
                node.setRight(insertRecursive(node.getRight(), hotelId, latitude, longitude, depth + 1));
            }
        }

        return node;
    }

    public List<NearbyHotelResponse> findNearestHotels(double userLat, double userLon, int k) {
        lock.readLock().lock();
        try {
            PriorityQueue<NearbyHotelResponse> pq = new PriorityQueue<>(
                    (a, b) -> Double.compare(b.getDistance(), a.getDistance()));

            findNearestRecursive(root, userLat, userLon, k, pq, 0);

            List<NearbyHotelResponse> result = new ArrayList<>();
            while (!pq.isEmpty()) {
                result.add(0, pq.poll());
            }

            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void findNearestRecursive(KDTreeNode node, double userLat, double userLon, int k,
            PriorityQueue<NearbyHotelResponse> pq, int depth) {
        if (node == null)
            return;

        double distance = calculateDistance(userLat, userLon, node.getLatitude(), node.getLongitude());
        Hotel hotel = hotelCache.get(node.getHotelId());

        if (hotel != null) {
            NearbyHotelResponse response = new NearbyHotelResponse(
                    node.getHotelId(),
                    hotel.getName(),
                    hotel.getAddress(),
                    hotel.getContact(),
                    hotel.getEmail(),
                    hotel.getImageUrl(),
                    hotel.getDescription(),
                    hotel.getRoomsCount(),
                    node.getLatitude(),
                    node.getLongitude(),
                    distance);

            if (pq.size() < k) {
                pq.offer(response);
            } else if (distance < pq.peek().getDistance()) {
                pq.poll();
                pq.offer(response);
            }
        }

        int cd = depth % 2;
        KDTreeNode first, second;

        if (cd == 0) {
            if (userLat < node.getLatitude()) {
                first = node.getLeft();
                second = node.getRight();
            } else {
                first = node.getRight();
                second = node.getLeft();
            }
        } else {
            if (userLon < node.getLongitude()) {
                first = node.getLeft();
                second = node.getRight();
            } else {
                first = node.getRight();
                second = node.getLeft();
            }
        }

        findNearestRecursive(first, userLat, userLon, k, pq, depth + 1);

        // Check if we need to explore the other branch
        if (pq.size() < k || shouldExploreOtherBranch(node, userLat, userLon, depth, pq.peek().getDistance())) {
            findNearestRecursive(second, userLat, userLon, k, pq, depth + 1);
        }
    }

    private boolean shouldExploreOtherBranch(KDTreeNode node, double userLat, double userLon, int depth,
            double bestDistance) {
        int cd = depth % 2;
        double axisDistance;

        if (cd == 0) {
            axisDistance = Math.abs(userLat - node.getLatitude());
        } else {
            axisDistance = Math.abs(userLon - node.getLongitude());
        }

        return axisDistance < bestDistance;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for more accurate distance calculation
        final double R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public void updateHotelCache(Hotel hotel) {
        lock.writeLock().lock();
        try {
            hotelCache.put(hotel.getId(), hotel);
            if (hotel.getLatitude() != null && hotel.getLongitude() != null) {
                insertHotel(hotel.getId(), hotel.getLatitude(), hotel.getLongitude());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeHotel(Long hotelId) {
        lock.writeLock().lock();
        try {
            hotelCache.remove(hotelId);
            // Note: Removing from K-D tree is complex, so we'll rebuild when needed
            // For production, consider implementing proper removal or periodic rebuilds
        } finally {
            lock.writeLock().unlock();
        }
    }
}
