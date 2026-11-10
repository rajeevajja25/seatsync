package com.transport;

import java.util.EventObject;

/**
 * Delegation event model (Unit 3): the EVENT OBJECT. Describes what happened to a seat,
 * exactly like java.awt.event.KeyEvent describes a key press.
 */
public class BookingEvent extends EventObject {

    public enum Type { RESERVED, BOOKED, RELEASED, EXPIRED }

    private final Type type;
    private final String seatId;
    private final String passenger;
    private final long time = System.currentTimeMillis();

    public BookingEvent(Object source, Type type, String seatId, String passenger) {
        super(source);
        this.type = type;
        this.seatId = seatId;
        this.passenger = passenger;
    }

    public Type getType() { return type; }
    public String getSeatId() { return seatId; }
    public String getPassenger() { return passenger; }
    public long getTime() { return time; }
}
