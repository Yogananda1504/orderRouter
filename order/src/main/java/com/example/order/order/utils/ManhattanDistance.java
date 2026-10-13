package com.example.order.order.utils;

public class ManhattanDistance {

    // Coordinates and the returned distance are measured in kilometers.
    public double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
