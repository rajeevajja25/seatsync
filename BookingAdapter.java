package com.transport;

/**
 * Delegation event model (Unit 3): the ADAPTER CLASS. Empty implementations of every listener method,
 * so a subclass overrides only the events it cares about (same idea as java.awt.event.KeyAdapter).
 */
public abstract class BookingAdapter implements BookingListener {
    @Override public void onReserved(BookingEvent e) { }
    @Override public void onBooked(BookingEvent e) { }
    @Override public void onReleased(BookingEvent e) { }
    @Override public void onExpired(BookingEvent e) { }
}
