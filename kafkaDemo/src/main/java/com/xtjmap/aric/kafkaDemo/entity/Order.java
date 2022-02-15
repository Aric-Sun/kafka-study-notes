package com.xtjmap.aric.kafkaDemo.entity;

/**
 * @author AricSun
 * @date 2022.01.25 17:14
 */
public class Order {
    private Long orderID;
    private int count;

    public Order() {
    }

    public Order(Long orderID, int count) {
        this.orderID = orderID;
        this.count = count;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
