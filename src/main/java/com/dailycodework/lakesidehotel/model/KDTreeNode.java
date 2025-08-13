package com.dailycodework.lakesidehotel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KDTreeNode {
    private Long hotelId;
    private Double latitude;
    private Double longitude;
    private KDTreeNode left;
    private KDTreeNode right;
    
    public KDTreeNode(Long hotelId, Double latitude, Double longitude) {
        this.hotelId = hotelId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.left = null;
        this.right = null;
    }
}
