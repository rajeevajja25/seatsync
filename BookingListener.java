package com.transport;

/** Delegation event model (Unit 3): the LISTENER INTERFACE. A class that implements it can be registered on the source. */
public interface BookingListener {
    void onReserved(BookingEvent e);
    void onBooked(BookingEvent e);
    void onReleased(BookingEvent e);   // user released the seat
    void onExpired(BookingEvent e);    // auto-release thread freed it
}
