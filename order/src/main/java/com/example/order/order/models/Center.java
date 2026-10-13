package com.example.order.order.models;

import java.util.HashMap;
import java.util.Map;

public class Center {

    private Long centerId;
    private double x;
    private double y;
    private int grade = 1;
    private Map<Long, Integer> items = new HashMap<>();

    public Center() {
    }

    public Center(Long centerId, double x, double y, Map<Long, Integer> items) {
        this.centerId = centerId;
        this.x = x;
        this.y = y;
        this.items = items;
    }

    public Center(Long centerId, double x, double y, Map<Long, Integer> items, int grade) {
        this(centerId, x, y, items);
        setGrade(grade);
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        if (grade < 1 || grade > 5) {
            throw new IllegalArgumentException("Center grade must be between 1 and 5");
        }
        this.grade = grade;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Map<Long, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Long, Integer> items) {
        this.items = items;
    }
}
