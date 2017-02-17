package com.futurice.android.reservator.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Vector;

public class RoomTest {
    private long oneHour = 3600000;
    private long thirtyMinutes = 1800000;
    private long fifteenMinutes = 900000;
    private Date currentTime = new Date();
    private long currentTimeMS = currentTime.getTime();
    private TimeSpan timeSpanOne = new TimeSpan(new DateTime(currentTimeMS-thirtyMinutes),new DateTime(currentTimeMS+fifteenMinutes + 60000));
    private TimeSpan timeSpanTwo =  new TimeSpan(new DateTime(currentTimeMS-thirtyMinutes),new DateTime(currentTimeMS+(2*oneHour)));
    private Reservation res;
    private Room room;

    @Before
    public void setUp() {
        Vector<String> attendees = new Vector<>();
        attendees.add("Leo Neu");
        attendees.add("room");
        res = new Reservation("xxx","meeting",timeSpanOne, attendees);
        room = new Room("room","email");
        
        Vector<Reservation> reservationVector = new Vector<>();
        reservationVector.add(res);
        room.setReservations(reservationVector);
    }

    @Test
    public void testGetTimeDifferenceHour() throws Exception {
        long hour = room.getTimeDifferenceHour(currentTime);
        Assert.assertTrue(hour <= 0);
        Assert.assertFalse(hour > 0);

        res.setTimeSpan(timeSpanTwo);
        hour = room.getTimeDifferenceHour(currentTime);
        Assert.assertTrue(hour > 0);
        Assert.assertFalse(hour <= 0);
    }

    @Test
    public void testGetTimeDifferenceMinute() throws Exception {
        long minutes = room.getTimeDifferenceMinute(currentTime);
        Assert.assertTrue( minutes <= 15);
        Assert.assertFalse(minutes > 15);
    }

    @Test
    public void testGetShownName(){
        Assert.assertTrue(room.getShownRoomName().equals("room"));
        Assert.assertFalse(room.getShownRoomName().equals("Leo Neu"));
    }
}