# Custom Algorithms in LakeSide Hotel Demo

This document describes the custom algorithms implemented in this project for spatial search and dynamic pricing.

---

## 1. KD-Tree Nearest Neighbor Search Algorithm

**Location**: `src/main/java/com/dailycodework/lakesidehotel/service/KDTreeService.java`

**Purpose**: Efficiently find the K nearest hotels to a user's location (latitude, longitude) using a 2-dimensional KD-Tree data structure.

### Algorithm Overview

The KD-Tree (K-Dimensional Tree) is a space-partitioning data structure used for organizing points in k-dimensional space. For this hotel search use case, we use a 2D tree (k=2) where dimensions are latitude and longitude.

### Data Structure

```
KDTreeNode {
    hotelId: Long
    latitude: Double
    longitude: Double
    left: KDTreeNode
    right: KDTreeNode
}
```

### Insertion Algorithm

**Time Complexity**: O(log n) average case, O(n) worst case (unbalanced tree)

```
insertRecursive(node, hotelId, latitude, longitude, depth):
    1. If node is null:
       - Create new KDTreeNode with hotelId, latitude, longitude
       - Return the new node

    2. Determine current dimension (cd = depth % 2):
       - cd == 0: Compare by latitude
       - cd == 1: Compare by longitude

    3. If cd == 0 (latitude dimension):
       - If latitude < node.latitude:
           - node.left = insertRecursive(node.left, hotelId, lat, lon, depth + 1)
       - Else:
           - node.right = insertRecursive(node.right, hotelId, lat, lon, depth + 1)

    4. If cd == 1 (longitude dimension):
       - If longitude < node.longitude:
           - node.left = insertRecursive(node.left, hotelId, lat, lon, depth + 1)
       - Else:
           - node.right = insertRecursive(node.right, hotelId, lat, lon, depth + 1)

    5. Return node
```

**Key Points**:

- Alternates between latitude (depth % 2 == 0) and longitude (depth % 2 == 1) at each level
- Creates a balanced partitioning of the 2D space
- Maintains tree structure through recursive insertion

### Nearest Neighbor Search Algorithm

**Time Complexity**: O(log n) average case for balanced tree

```
findNearestHotels(userLat, userLon, k):
    1. Initialize PriorityQueue (max-heap) with capacity k
       - Comparator: Sort by distance (descending)

    2. Call findNearestRecursive(root, userLat, userLon, k, priorityQueue, 0)

    3. Convert PriorityQueue to List (in ascending distance order)

    4. Return list of K nearest hotels
```

```
findNearestRecursive(node, userLat, userLon, k, priorityQueue, depth):
    1. If node is null, return

    2. Calculate distance from user to current node using Haversine formula

    3. If hotel exists in cache:
       - Create NearbyHotelResponse with distance
       - If priorityQueue.size() < k:
           - Add to priorityQueue
       - Else if distance < priorityQueue.peek().distance:
           - Remove farthest (top of max-heap)
           - Add current node

    4. Determine current dimension (cd = depth % 2)

    5. Choose which branch to explore first:
       - If cd == 0 (latitude):
           - If userLat < node.latitude:
               - first = node.left
               - second = node.right
           - Else:
               - first = node.right
               - second = node.left

       - If cd == 1 (longitude):
           - If userLon < node.longitude:
               - first = node.left
               - second = node.right
           - Else:
               - first = node.right
               - second = node.left

    6. Recursively search first branch:
       - findNearestRecursive(first, userLat, userLon, k, priorityQueue, depth + 1)

    7. Check if second branch needs exploration:
       - If priorityQueue.size() < k OR shouldExploreOtherBranch(node, userLat, userLon, depth, bestDistance):
           - findNearestRecursive(second, userLat, userLon, k, priorityQueue, depth + 1)
```

### Pruning Optimization

```
shouldExploreOtherBranch(node, userLat, userLon, depth, bestDistance):
    1. Determine current dimension (cd = depth % 2)

    2. Calculate axis distance:
       - If cd == 0: axisDistance = |userLat - node.latitude|
       - If cd == 1: axisDistance = |userLon - node.longitude|

    3. Return true if axisDistance < bestDistance
       - This means the hyperplane might contain closer points
```

**Key Optimization**:

- Prunes branches that cannot possibly contain a closer point than the current k-th nearest
- Reduces unnecessary recursive calls
- Improves average case performance significantly

---

## 2. Haversine Distance Formula

**Location**: `src/main/java/com/dailycodework/lakesidehotel/service/KDTreeService.java` (calculateDistance method)

**Purpose**: Calculate the great-circle distance between two points on Earth's surface given their latitude and longitude coordinates.

### Algorithm

**Formula**:

```
a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
c = 2 × atan2(√a, √(1-a))
distance = R × c
```

Where:

- φ (phi) = latitude in radians
- λ (lambda) = longitude in radians
- R = Earth's radius (6371 km)
- Δφ = difference in latitude
- Δλ = difference in longitude

### Implementation Steps

```
calculateDistance(lat1, lon1, lat2, lon2):
    1. Convert latitude and longitude differences to radians:
       - latDistance = toRadians(lat2 - lat1)
       - lonDistance = toRadians(lon2 - lon1)

    2. Calculate 'a' using Haversine formula:
       a = sin²(latDistance/2) + cos(lat1) × cos(lat2) × sin²(lonDistance/2)

    3. Calculate central angle 'c':
       c = 2 × atan2(√a, √(1-a))

    4. Calculate distance:
       distance = R × c  (where R = 6371 km)

    5. Return distance in kilometers
```

### Complexity

- **Time Complexity**: O(1) - Constant time calculation
- **Space Complexity**: O(1) - Constant space

### Accuracy

- Accounts for Earth's spherical shape
- More accurate than simple Euclidean distance for geographic coordinates
- Suitable for distances up to a few hundred kilometers

---

## 3. Dynamic Pricing Algorithm

**Location**: `src/main/java/com/dailycodework/lakesidehotel/service/DynamicPricingService.java`

**Purpose**: Calculate room pricing with multiple adjustment factors based on dates, seasons, and booking timing.

### Pricing Adjustment Rules

| Adjustment Type         | Condition                | Percentage | Applied To           |
| ----------------------- | ------------------------ | ---------- | -------------------- |
| **Weekend Surcharge**   | Friday or Saturday night | +15%       | Base price per night |
| **Seasonal Multiplier** | Dec 15 - Jan 5           | +20%       | Base price per night |
| **Last-Minute Booking** | Booking within 3 days    | +25%       | Base price (total)   |
| **Early-Bird Discount** | Booking 30+ days ahead   | -10%       | Base price (total)   |

### Algorithm Overview

```
calculatePrice(roomId, checkIn, checkOut):
    1. Fetch room base price from database

    2. Initialize:
       - finalPrice = basePrice
       - adjustments = []

    3. For each night in stay (from checkIn to checkOut):
       a. Calculate nightly adjustments:
          - If Friday or Saturday: Add 15% weekend surcharge
          - If Dec 15 - Jan 5: Add 20% seasonal multiplier

       b. Apply nightly adjustments to finalPrice
       c. Add adjustments to list

    4. Calculate total adjustment amount:
       - totalAdjustment = finalPrice - basePrice

    5. Apply timing-based adjustments:
       - If booking within 3 days: Add 25% last-minute surcharge
       - If booking 30+ days ahead: Subtract 10% early-bird discount

    6. Round final price to 2 decimal places

    7. Return DynamicPricingResponse with:
       - basePrice
       - finalPrice
       - adjustments (detailed list)
       - totalAdjustmentAmount
```

### Nightly Adjustment Calculation

```
calculateNightlyAdjustments(date, basePrice):
    1. Initialize empty adjustments list

    2. Check if weekend (Friday or Saturday):
       - If true:
           - amount = basePrice × 0.15
           - Add "Weekend Surcharge" adjustment (15%, amount)

    3. Check if seasonal date:
       - If month == 12 AND day >= 15: seasonal = true
       - If month == 1 AND day <= 5: seasonal = true
       - If seasonal:
           - amount = basePrice × 0.20
           - Add "Seasonal Multiplier" adjustment (20%, amount)

    4. Return adjustments list
```

### Seasonal Date Detection

```
isSeasonalDate(date):
    1. Extract month and day from date

    2. Check December 15-31:
       - If month == 12 AND day >= 15: return true

    3. Check January 1-5:
       - If month == 1 AND day <= 5: return true

    4. Return false (not seasonal)
```

### Timing-Based Adjustments

```
calculateLastMinuteAdjustment(checkIn):
    1. Calculate days until check-in:
       - daysUntil = daysBetween(now, checkIn)

    2. If 0 <= daysUntil <= 3:
       - Return 0.25 (25% surcharge)

    3. Else:
       - Return 0 (no adjustment)
```

```
calculateEarlyBirdDiscount(checkIn):
    1. Calculate days until check-in:
       - daysUntil = daysBetween(now, checkIn)

    2. If daysUntil >= 30:
       - Return 0.10 (10% discount)

    3. Else:
       - Return 0 (no discount)
```

### Final Price Calculation

```
applyTimingAdjustments(response, checkIn):
    1. Get last-minute adjustment percentage
    2. Get early-bird discount percentage

    3. Initialize adjustedPrice = response.finalPrice

    4. Apply last-minute surcharge (if applicable):
       - If lastMinute > 0:
           - amount = basePrice × lastMinute
           - adjustedPrice += amount
           - Add adjustment to list

    5. Apply early-bird discount (if applicable):
       - If earlyBird > 0:
           - amount = basePrice × earlyBird
           - adjustedPrice -= amount (subtract discount)
           - Add adjustment to list

    6. Recalculate total adjustment:
       - totalAdjustment = adjustedPrice - basePrice

    7. Round to 2 decimal places

    8. Return updated DynamicPricingResponse
```

### Complexity

- **Time Complexity**: O(n) where n = number of nights
  - Must iterate through each night to apply nightly adjustments
- **Space Complexity**: O(n) for storing adjustments per night

### Example Calculation

```
Base Price: Rs. 1000/night
Check-in: Friday, Dec 20, 2024
Check-out: Sunday, Dec 22, 2024 (2 nights)
Booking Date: Dec 19, 2024 (1 day in advance)

Night 1 (Dec 20 - Friday):
  - Base: 1000
  - Weekend surcharge (15%): +150
  - Seasonal multiplier (20%): +200
  - Subtotal: 1350

Night 2 (Dec 21 - Saturday):
  - Base: 1000
  - Weekend surcharge (15%): +150
  - Seasonal multiplier (20%): +200
  - Subtotal: 1350

Nightly Total: 2700

Timing Adjustments:
  - Last-minute booking (25%): +500 (on base 2000)
  - Final Price: 3200

Total Adjustment: 1200 (60% increase)
```

---

## Summary

These three custom algorithms work together to provide:

1. **Fast Spatial Search**: KD-Tree enables O(log n) nearest hotel search
2. **Accurate Distance Calculation**: Haversine formula provides precise geographic distances
3. **Dynamic Pricing**: Per-night and timing-based adjustments maximize revenue optimization

All algorithms are implemented from scratch and optimized for the hotel booking domain.
