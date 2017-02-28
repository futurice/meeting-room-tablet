package com.futurice.android.reservator.model.platformcalendar;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the DataProxy for getting meeting room info through
 * the platform's Content Provider API.
 *
 * @author vsin
 */
public class PlatformCalendarDataProxy extends DataProxy {
    private Context context;
    private static final Pattern idPattern = Pattern.compile("^(\\d+)(-.*)?");
    private final String DEFAULT_MEETING_NAME = "Reserved";
    private final String CALENDAR_SYNC_AUTHORITY = "com.android.calendar";
    private final String RESOURCE_CALENDAR_TYPE = "resource.calendar.google.com";
    // Event fetch window (if we try to query all events it's very, very slow)
    private final long EVENT_SELECTION_PERIOD_BACKWARD = 24 * 60 * 60 * 1000; // One day
    private final long EVENT_SELECTION_PERIOD_FORWARD = 11 * 24 * 60 * 60 * 1000; // 11 days
    // Uses SQLite (http://www.sqlite.org/lang_select.html)
    private final String TITLE_PREFERENCE_SORT_ORDER =
            "CASE " + CalendarContract.Attendees.ATTENDEE_RELATIONSHIP + " " +
                    "WHEN " + CalendarContract.Attendees.RELATIONSHIP_ORGANIZER + " THEN 1 " +
                    "WHEN " + CalendarContract.Attendees.RELATIONSHIP_SPEAKER + " THEN 2 " +
                    "WHEN " + CalendarContract.Attendees.RELATIONSHIP_PERFORMER + " THEN 3 " +
                    "WHEN " + CalendarContract.Attendees.RELATIONSHIP_ATTENDEE + " THEN 4 " +
                    "ELSE 5 END, " +
                    "CASE " + CalendarContract.Attendees.ATTENDEE_STATUS + " " +
                    "WHEN " + CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED + " THEN 1 " +
                    "WHEN " + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED + " THEN 3 " +
                    "ELSE 2 END";
    // Preferred account, or null if any account is good
    String account = null;
    TimeZone SYSTEM_TZ = java.util.Calendar.getInstance().getTimeZone();
    private ContentResolver resolver;
    private AccountManager accountManager;
    private Mode calendarMode;
    // Two-level data structure that stores locally created reservations
    // for each room until they get synced. Access but altering the Set objects
    // or modifying the structure of the Map must be within MakeReservationTask synchronized block.
    private Map<Room, HashSet<Reservation>> locallyCreatedReservationCaches =
            Collections.synchronizedMap(new HashMap<Room, HashSet<Reservation>>());

    private boolean designationMeetingName;

    /**
     * @param resolver       From application context. Used to access the platform's Calendar Provider.
     * @param accountManager From application context. Allows us to initiate MakeReservationTask sync immediately after adding MakeReservationTask reservation.
     * @param calendarMode   Enum to select if a resource or non resource calendar is used.
     */
    public PlatformCalendarDataProxy(ContentResolver resolver, AccountManager accountManager, Mode calendarMode, Context context) {
        this.resolver = resolver;
        this.accountManager = accountManager;
        this.calendarMode = calendarMode;
        this.context = context;
        setDesignationMeetingName(context);
    }

    public enum Mode {
        // Filters Google Resources, only available for App users
        RESOURCES("*@resource.calendar.google.com"),
        // Shows all calendars by default
        CALENDARS(null);

        private String resourcesGlob = null;
        private Mode(String resourcesGlob)
        {
            this.resourcesGlob = resourcesGlob;
        }

    }

    private void setDesignationMeetingName(Context context) {
        String settingMeetingDesignation = context.getSharedPreferences(context.getString(R.string.PREFERENCES_NAME), context.MODE_PRIVATE).getString("meetingDesignation", "");
        this.designationMeetingName = settingMeetingDesignation.equals(context.getString(R.string.meetingTitleMeetingName));
    }

    // Non-ops
    @Override
    public void setCredentials(String user, String password) {
    }

    @Override
    public void setServer(String server) {
    }

    @Override
    public void reserve(Room r, TimeSpan timeSpan, String owner, String ownerEmail, String meetingName) throws ReservatorException {
        if (!(r instanceof PlatformCalendarRoom)) {
            throw new ReservatorException("Data type error (expecting PlatformCalendarRoom)");
        }

        PlatformCalendarRoom room = (PlatformCalendarRoom) r;

        // This also ensures that the calendar has not been deleted
        String accountName = getAccountName(room.getId());

        ContentValues mNewValues = new ContentValues();
        mNewValues.put(CalendarContract.Events.CALENDAR_ID, room.getId());
        mNewValues.put(CalendarContract.Events.ORGANIZER, ownerEmail);
        mNewValues.put(CalendarContract.Events.DTSTART, timeSpan.getStart().getTimeInMillis());
        mNewValues.put(CalendarContract.Events.DTEND, timeSpan.getEnd().getTimeInMillis());
        mNewValues.put(CalendarContract.Events.EVENT_TIMEZONE, SYSTEM_TZ.getID());
        mNewValues.put(CalendarContract.Events.EVENT_LOCATION, room.getLocation());
        mNewValues.put(CalendarContract.Events.TITLE, meetingName);

        Uri eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, mNewValues);
        if (eventUri == null) {
            throw new ReservatorException("Could not create event");
        }
        if(owner.isEmpty()){
            owner = ownerEmail;
        }

        long eventId = Long.parseLong(eventUri.getLastPathSegment());
        mNewValues = new ContentValues();
        mNewValues.put(CalendarContract.Attendees.EVENT_ID, eventId);
        mNewValues.put(CalendarContract.Attendees.ATTENDEE_NAME, owner);
        mNewValues.put(CalendarContract.Attendees.ATTENDEE_EMAIL, ownerEmail);
        mNewValues.put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.RELATIONSHIP_ORGANIZER);
        mNewValues.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_OPTIONAL);
        mNewValues.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_NONE);

        Uri attendeeUri = resolver.insert(CalendarContract.Attendees.CONTENT_URI, mNewValues);
        if (attendeeUri == null) {
            Log.w("reserve", "Could not add an attendeee");
        }

        syncCalendarAccount(accountName);

        Reservation createdReservation = new Reservation(
                Long.toString(eventId) + "-" + Long.toString(timeSpan.getStart().getTimeInMillis()),
                meetingName,
                timeSpan);
        createdReservation.setIsCancellable(true);
        putToLocalCache(room, createdReservation);
    }

    /**
     * Resolves MakeReservationTask Calendar's ACCOUNT_NAME.
     *
     * @throws ReservatorException If the account has been deleted.
     * @author vsin
     */
    private String getAccountName(long calendarId) throws ReservatorException {
        String[] mProjection = {CalendarContract.Calendars.ACCOUNT_NAME};
        String mSelectionClause = CalendarContract.Calendars._ID + " = " + Long.toString(calendarId);
        String[] mSelectionArgs = {};
        String mSortOrder = null;

        Cursor result = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result == null) {
            throw new ReservatorException(context.getString(R.string.roomDeleted));
        }

        if (result.getCount() == 0) {
            result.close();
            throw new ReservatorException(context.getString(R.string.roomDeleted));
        }

        result.moveToFirst();
        String accountName = result.getString(0);
        result.close();

        return accountName;
    }

    private long getEventIdFromReservation(final Reservation r) throws ReservatorException {
        Matcher idMatcher = idPattern.matcher(r.getId());
        if (!idMatcher.matches()) {
            throw new ReservatorException(context.getString(R.string.parsID));
        }

        return Long.parseLong(idMatcher.group(1));
    }

    @Override
    public void cancelReservation(Reservation reservation) throws ReservatorException {
        if (!reservation.isCancellable()) return;
        long eventId = getEventIdFromReservation(reservation);

        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);

        // Get calendar ID
        String[] mProjection = {CalendarContract.Events.CALENDAR_ID};
        String mSelectionClause = "DELETED=0";
        String[] mSelectionArgs = {};
        String mSortOrder = null;
        Cursor result = resolver.query(
                eventUri,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result == null) {
            return; // Event has already been deleted!
        }

        if (result.getCount() == 0) {
            result.close();
            return; // Event has already been deleted!
        }

        result.moveToFirst();
        long calendarId = result.getLong(0);
        result.close();

        // Remove from platform calendar
        int nRows = resolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null);
        // Remove from local caches
        synchronized (locallyCreatedReservationCaches) {
            for (Map.Entry<Room, HashSet<Reservation>> entry : locallyCreatedReservationCaches.entrySet()) {
                if (entry.getValue().contains(reservation)) {
                    HashSet<Reservation> filtered = new HashSet<Reservation>(entry.getValue());
                    filtered.remove(reservation);
                    entry.setValue(filtered);
                }
            }
        }

        if (nRows > 0) {
            try {
                syncCalendarAccount(getAccountName(calendarId));
            } catch (ReservatorException e) {
                ; // Calendar has been deleted by user, can't sync. "Not MakeReservationTask biggie"
            }
        }
    }

    /**
     * Initiate MakeReservationTask sync on MakeReservationTask Calendar account if possible.
     */
    private void syncCalendarAccount(String accountName) {
        boolean success = false;
        for (Account account : accountManager.getAccounts()) {
                if (account.name.equals(accountName)) {
                    if (ContentResolver.getIsSyncable(account, CALENDAR_SYNC_AUTHORITY) > 0) {
                        success = true;
                        if (!ContentResolver.isSyncActive(account, CALENDAR_SYNC_AUTHORITY)) {
                            ContentResolver.requestSync(account, CALENDAR_SYNC_AUTHORITY, new Bundle());
                            Log.d("SYNC", String.format("%s %s", context.getString(R.string.syncGoogleCalRequest), accountName));
                        } else {
                            Log.d("SYNC", String.format("%s %s", context.getString(R.string.syncGoogleCalActiv), accountName));
                        }
                    } else {
                        Log.d("SYNC", String.format("%s %s", context.getString(R.string.syncGoogleCalNotSync), accountName));
                    }
                }
        }

        if (!success) {
            Log.w("SYNC", String.format("%s %s", context.getString(R.string.initiateNot), accountName));
        }
    }

    public void synchronize(Room r) {
        PlatformCalendarRoom room = (PlatformCalendarRoom) r;
        try {
            syncCalendarAccount(getAccountName(room.getId()));
        } catch (ReservatorException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Vector<Room> getRooms() throws ReservatorException {
        setSyncOn();

        Vector<Room> rooms = new Vector<Room>();

        String[] mProjection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.OWNER_ACCOUNT,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.CALENDAR_LOCATION};

        List<String> mSelectionClauses = new ArrayList<String>();
        List<String> mSelectionArgs = new ArrayList<String>();

        if (this.calendarMode == Mode.RESOURCES) {
            mSelectionClauses.add(CalendarContract.Calendars.OWNER_ACCOUNT + " GLOB ?");
            mSelectionArgs.add(Mode.RESOURCES.resourcesGlob);
        } else {
            mSelectionClauses.add(CalendarContract.Calendars.OWNER_ACCOUNT + " LIKE '%" + context.getString(R.string.accountType) + "'");
        }

        if (this.account != null) {
            mSelectionClauses.add(CalendarContract.Calendars.ACCOUNT_NAME + " = ?");
            mSelectionArgs.add(account);
        }

        String mSortOrder = null;

        Cursor result = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                mProjection,
                TextUtils.join(" AND ", mSelectionClauses),
                mSelectionArgs.toArray(new String[0]),
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    String name = result.getString(2);

                    String location = result.getString(3);
                    if (location == null || location.isEmpty()) {
                        location = name;
                    }
                    rooms.add(new PlatformCalendarRoom(
                            name,
                            result.getString(1),
                            result.getLong(0),
                            location));
                } while (result.moveToNext());
            }
            result.close();
        }

        return rooms;
    }

    private void putToLocalCache(Room room, Reservation reservation) {
        synchronized (locallyCreatedReservationCaches) {
            HashSet<Reservation> roomCache;

            if (locallyCreatedReservationCaches.get(room) != null) {
                roomCache = locallyCreatedReservationCaches.get(room);
            } else {
                roomCache = new HashSet<Reservation>();
            }

            roomCache.add(reservation);
            locallyCreatedReservationCaches.put(room, roomCache);
        }
    }

    @Override
    public Vector<Reservation> getRoomReservations(Room r) throws ReservatorException {
        if (!(r instanceof PlatformCalendarRoom)) {
            return new Vector<Reservation>();
        }

        PlatformCalendarRoom room = (PlatformCalendarRoom) r;
        String calendarOwnerAccount = room.getEmail();

        long now = new Date().getTime();
        long minTime = now - EVENT_SELECTION_PERIOD_BACKWARD;
        long maxTime = now + EVENT_SELECTION_PERIOD_FORWARD;

        HashSet<Reservation> roomCache = null;
        if (locallyCreatedReservationCaches.containsKey(room)) {
            roomCache = locallyCreatedReservationCaches.get(room);
        }

        // Remove old locally cached reservations
        // NB it's crucial that we do not alter the structure of any instance data here (it's not synchronized)
        if (roomCache != null && roomCache.size() > 0) {
            TimeSpan interest = new TimeSpan(new DateTime(minTime), new DateTime(maxTime));
            HashSet<Reservation> filteredRoomCache = new HashSet<Reservation>();

            for (Reservation cachedReservation : roomCache) {
                if (cachedReservation.getTimeSpan().intersects(interest)) {
                    filteredRoomCache.add(cachedReservation);
                }
            }

            if (filteredRoomCache.size() != roomCache.size()) {
                synchronized (locallyCreatedReservationCaches) {
                    locallyCreatedReservationCaches.put(room, filteredRoomCache);
                }
                roomCache = filteredRoomCache;
            }
        }

        HashSet<Reservation> reservations = getInstancesTableReservations(room, minTime, maxTime, calendarOwnerAccount);

        // Remove those that have now been synced
        if (roomCache != null && roomCache.size() > 0) {
            HashSet<Reservation> filteredRoomCache = new HashSet<Reservation>(roomCache);

            filteredRoomCache.removeAll(reservations);

            if (filteredRoomCache.size() != roomCache.size()) {
                synchronized (locallyCreatedReservationCaches) {
                    locallyCreatedReservationCaches.put(room, filteredRoomCache);
                }
                roomCache = filteredRoomCache;
            }

            // Finally, add the cached reservations to the result set
            reservations.addAll(roomCache);
        }

        return new Vector<Reservation>(reservations);
    }

    private HashSet<Reservation> getInstancesTableReservations(
            PlatformCalendarRoom room, long minTime, long maxTime, String calendarAccount) {
        HashSet<Reservation> reservations = new HashSet<Reservation>();

        String[] mProjection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ORGANIZER
        };
        String mSelectionClause =
                CalendarContract.Instances.CALENDAR_ID + " = " + room.getId() + " AND " +
                        CalendarContract.Instances.STATUS + " != " + CalendarContract.Instances.STATUS_CANCELED + " AND " +
                        CalendarContract.Instances.SELF_ATTENDEE_STATUS + " != " + CalendarContract.Attendees.STATUS_CANCELED;
        String[] mSelectionArgs = {};
        String mSortOrder = null;

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, minTime);
        ContentUris.appendId(builder, maxTime);

        Cursor result = resolver.query(
                builder.build(),
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    long eventId = result.getLong(0);
                    String title = result.getString(1);
                    long start = result.getLong(2);
                    long end = Math.max(start, result.getLong(3));
                    String eventOrganizerAccount = result.getString(4);
                    //  Log.d("ReservationDetails", "id=" + eventId + ", title=" + title + ", organizer=" + eventOrganizerAccount);

                    Reservation res = new Reservation(
                            Long.toString(eventId) + "-" + Long.toString(start),
                            makeEventTitle(room.getName(), eventId, title, eventOrganizerAccount, DEFAULT_MEETING_NAME),
                            new TimeSpan(new DateTime(start), new DateTime(end)), getAuthoritySortedAttendees(eventId));
                    if (eventOrganizerAccount != null && calendarAccount.equals(eventOrganizerAccount.toLowerCase())) {
                        res.setIsCancellable(true);
                    }
                    reservations.add(res);

                } while (result.moveToNext());
            }
            result.close();
        }

        return reservations;
    }

    /**
     * Make MakeReservationTask title. We first try to get some sort of an organizer, speaker or attendee name,
     * ignoring empty names and the name of this room and preferring those who have accepted the
     * invitation to those who are unknown/tentative, and those to those who have not declined.
     * If the name is empty but the email address is not we use the email address
     * <p/>
     * If that fails to yield MakeReservationTask name we use the event owner, unless it ends with "resource.calendar.google.com"
     * <p/>
     * As MakeReservationTask last resort, MakeReservationTask "default name" is returned.
     *
     * @author vsin
     */
    private String makeEventTitle(final String roomName, final long eventId, final String storedTitle, final String organizer,
                                  final String defaultTitle) {

        if (storedTitle != null && !storedTitle.isEmpty() && designationMeetingName) {
            return storedTitle;
        }

        for (String attendee : getAuthoritySortedAttendees(eventId)) {
            if (attendee != null && !attendee.isEmpty() && !attendee.equals(roomName)) {
                return attendee;
            }
        }

        if (organizer != null && !organizer.isEmpty()) {
            final int atIndex = organizer.indexOf("@");
            final String domain = organizer.substring(atIndex + 1);

            // Do not show resource calendar as an organizer.
            if (!domain.equals(RESOURCE_CALENDAR_TYPE)) {
                return organizer;
            }
        }

        /// if (storedTitle != null && !storedTitle.isEmpty()) return storedTitle;
        return defaultTitle;
    }

    private Vector<String> getAuthoritySortedAttendees(final long eventId) {
        Vector<String> attendees = new Vector<String>();

        String[] mProjection = {
                CalendarContract.Attendees.ATTENDEE_NAME,
                CalendarContract.Attendees.ATTENDEE_EMAIL,
                CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
                CalendarContract.Attendees.ATTENDEE_STATUS};
        String mSelectionClause = CalendarContract.Attendees.EVENT_ID + " = " + eventId;
        String[] mSelectionArgs = {};
        String mSortOrder = TITLE_PREFERENCE_SORT_ORDER;

        Cursor result = resolver.query(
                CalendarContract.Attendees.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                // Log.d("SortedAttendees", "eventId=" + eventId + ", attendeeName=" + result.getString(0) + ", attendeeEmail=" +
                //         result.getString(1)+ ", " + result.getCount());
                do {
                    if (result.getString(0) != null && !result.getString(0).isEmpty()) {
                        attendees.add(result.getString(0));
                    } else {
                        attendees.add(result.getString(1));
                    }
                } while (result.moveToNext());
            }
            result.close();
        }

        return attendees;
    }

    @Override
    public boolean hasFatalError() {
        // Make sure that we have the required room Calendars synced on some account
        setSyncOn();

        String[] mProjection = {};

        String mSelectionClause = "";
        ArrayList<String> mSelectionArgs = new ArrayList<String>();

        if (this.calendarMode == Mode.RESOURCES) {
            mSelectionClause += CalendarContract.Calendars.OWNER_ACCOUNT + " GLOB ?";
            mSelectionArgs.add(Mode.RESOURCES.resourcesGlob);
        }
        mSelectionClause += CalendarContract.Calendars.SYNC_EVENTS + " = 1";

        String mSortOrder = null;

        Cursor result = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs.toArray(new String[0]),
                mSortOrder);

        if (result == null) {
            return true;
        }

        if (result.getCount() == 0) {
            result.close();
            return true;
        }

        result.close();
        return false;
    }

    /**
     * Sets sync flag on for all calendars that match the room account glob pattern.
     */
    private void setSyncOn() {
        ContentValues mUpdateValues = new ContentValues();
        String mSelectionClause = "";
        ArrayList<String> mSelectionArgs = new ArrayList<String>();

        if (this.calendarMode == Mode.RESOURCES) {
            mSelectionClause += CalendarContract.Calendars.OWNER_ACCOUNT + " GLOB ?";
            mSelectionArgs.add(Mode.RESOURCES.resourcesGlob);
        } else {
            mSelectionClause += CalendarContract.Calendars.OWNER_ACCOUNT + " LIKE '%" + context.getString(R.string.accountType) + "'";
        }

        mUpdateValues.put("SYNC_EVENTS", 1);
        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

        }
        resolver.update(
                CalendarContract.Calendars.CONTENT_URI,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs.toArray(new String[0]));
    }

    private int checkSelfPermission(String writeCalendar) {
        return 0;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}