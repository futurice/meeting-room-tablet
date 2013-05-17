package com.futurice.android.reservator.model.platformcalendar;

import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.net.Uri;
import android.util.Log;
import android.accounts.AccountManager;
import android.accounts.Account;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class PlatformCalendarDataProxy extends DataProxy {
	private ContentResolver resolver;
	private AccountManager accountManager;
	
	private String roomAccountGlob;
	private final String DEFAULT_MEETING_NAME = "Reserved";
	private final String GOOGLE_ACCOUNT_TYPE = "com.google";
	private final String CALENDAR_SYNC_AUTHORITY = "com.android.calendar";
	// Preferred account, or null if any account is good
	String account = null;
	
	/**
	 * @param resolver From application context. Used to access the platform's Calendar Provider.
	 * @param accountManager From application context. Allows us to initiate a sync immediately after adding a reservation.
	 * @param accountGlob SQLite glob pattern that selects room calendar accounts.
	 */
	public PlatformCalendarDataProxy(ContentResolver resolver, AccountManager accountManager, String roomAccountGlob) {
		this.resolver = resolver;
		this.accountManager = accountManager;
		this.roomAccountGlob = roomAccountGlob;
	}
	
	// Non-ops
	@Override
	public void setCredentials(String user, String password) {}
	@Override
	public void setServer(String server) {}

	@Override
	public void reserve(Room r, TimeSpan timeSpan, String owner,
			String ownerEmail) throws ReservatorException {
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
		mNewValues.put(CalendarContract.Events.EVENT_TIMEZONE, java.util.Calendar.getInstance().getTimeZone().getID());
		mNewValues.put(CalendarContract.Events.EVENT_LOCATION, room.getLocation());
		mNewValues.put(CalendarContract.Events.TITLE, owner);
		
		Uri eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, mNewValues);
		if (eventUri == null) {
			throw new ReservatorException("Could not create event");
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
		
		syncGoogleCalendarAccount(accountName);
	}
	
	/**
	 * Resolves a Calendar's ACCOUNT_NAME.
	 * @throws ReservatorException If the account has been deleted. 
	 * @author vsin
	 */
	private String getAccountName(long calendarId) throws ReservatorException {
		String[] mProjection = { CalendarContract.Calendars.ACCOUNT_NAME };
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
			throw new ReservatorException("Room calendar has been deleted");
		}
		
		if (result.getCount() == 0) {
			result.close();
			throw new ReservatorException("Room calendar has been deleted");
		}
		
		result.moveToFirst();
		String accountName = result.getString(0);
		result.close();
		
		return accountName;
	}
	
	/**
	 * Initiate a sync on a Google Calendar account if possible.
	 */
	private void syncGoogleCalendarAccount(String accountName) {
		boolean success = false;
		for (Account account : accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE)) {
			if (account.name.equals(accountName)) {
				if (ContentResolver.getIsSyncable(account, CALENDAR_SYNC_AUTHORITY) > 0) {
					success = true;
					if (!ContentResolver.isSyncActive(account, CALENDAR_SYNC_AUTHORITY)) {
						ContentResolver.requestSync(account, CALENDAR_SYNC_AUTHORITY, new Bundle());
						Log.d("SYNC", "Calendar sync requested on " + accountName);
					} else {
						Log.d("SYNC", "Calendar sync was active on " + accountName);
					}
				} else {
					Log.d("SYNC", "Calendar is not syncable on " + accountName);
				}
			}
		}
		
		if (!success) {
			Log.w("SYNC", "Could not initiate sync on account " + accountName);
		}
	}
	
	@Override
	public Vector<Room> getRooms() throws ReservatorException {
		Vector<Room> rooms = new Vector<Room>(); 
		
		String[] mProjection = {
				CalendarContract.Calendars._ID,
				CalendarContract.Calendars.OWNER_ACCOUNT,
				CalendarContract.Calendars.NAME,
				CalendarContract.Calendars.CALENDAR_LOCATION
		};
		
		String mSelectionClause = CalendarContract.Calendars.OWNER_ACCOUNT + " GLOB ?";
		String[] mSelectionArgs = { roomAccountGlob };
		
		if (this.account != null) {
			mSelectionClause = mSelectionClause + " AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = ?";
			String[] mSelectionArgsAccount = { roomAccountGlob, account };
			mSelectionArgs = mSelectionArgsAccount;
		}
		
		String mSortOrder = null;
		
		Cursor result = resolver.query(
				CalendarContract.Calendars.CONTENT_URI,
				mProjection,
				mSelectionClause,
				mSelectionArgs,
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

	@Override
	public Vector<Reservation> getRoomReservations(Room r) throws ReservatorException {
		Vector<Reservation> reservations = new Vector<Reservation>();
		
		if (!(r instanceof PlatformCalendarRoom)) {
			return reservations;
		}
		
		PlatformCalendarRoom room = (PlatformCalendarRoom) r; 
		
		String[] mProjection = {
				CalendarContract.Events._ID,
				CalendarContract.Events.TITLE,
				CalendarContract.Events.DTSTART,
				CalendarContract.Events.DTEND
		};
		String mSelectionClause = 
				CalendarContract.Events.CALENDAR_ID + " = " + room.getId(); 
		String[] mSelectionArgs = {};
		String mSortOrder = null;
		
		Cursor result = resolver.query(
				CalendarContract.Events.CONTENT_URI,
				mProjection,
				mSelectionClause,
				mSelectionArgs,
				mSortOrder);
		
		if (result != null) {
			if (result.getCount() > 0) {
				result.moveToFirst();
				do {
					long eventId = result.getLong(0);
					reservations.add(new Reservation(
							Long.toString(eventId), 
							makeEventTitle(eventId, result.getString(1), DEFAULT_MEETING_NAME),
							new TimeSpan(
									new DateTime(result.getLong(2)),
									new DateTime(result.getLong(3)))));
				} while (result.moveToNext());
			}
			result.close();
		}
		
		return reservations;
	}
	
	/**
	 * Make a title. We first try to get some sort of an organizer, speaker or attendee name, 
	 * preferring those who have accepted the invitation to those who are unknown/tentative, 
	 * and those to those who have not declined. 
	 * If that fails (there are 0 attendees), we use the stored meeting title.
	 * As a last resort, a "default name" is returned.
	 *   
	 * @author vsin
	 */
	private String makeEventTitle(final long eventId, final String storedTitle, final String defaultTitle) {
		Vector<String> attendees = getAuthoritySortedAttendees(eventId);
		if (!attendees.isEmpty()) {
			return attendees.firstElement();
		}
		
		if (storedTitle != null && !storedTitle.isEmpty()) return storedTitle;
		return defaultTitle;
	}
	
	// Uses SQLite (http://www.sqlite.org/lang_select.html) 
	private final String TITLE_PREFERENCE_SORT_ORDER = 
		"CASE " + CalendarContract.Attendees.ATTENDEE_RELATIONSHIP + " " + 
			"WHEN " + CalendarContract.Attendees.RELATIONSHIP_ORGANIZER	+ " THEN 1 " + 
			"WHEN " + CalendarContract.Attendees.RELATIONSHIP_SPEAKER 	+ " THEN 2 " +
			"WHEN " + CalendarContract.Attendees.RELATIONSHIP_PERFORMER + " THEN 3 " +
			"ELSE 4 END, "+
		"CASE " + CalendarContract.Attendees.ATTENDEE_STATUS + " " +
			"WHEN " + CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED + " THEN 1 " +
			"WHEN " + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED + " THEN 3 " +
			"ELSE 2 END";
	
	private Vector<String> getAuthoritySortedAttendees(final long eventId) {
		Vector<String> attendees = new Vector<String>();
		
		String[] mProjection = { CalendarContract.Attendees.ATTENDEE_NAME };
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
				do {
					attendees.add(result.getString(0));
				} while (result.moveToNext());
			}
			result.close();
		}
		
		return attendees;
	}

	@Override
	public boolean hasFatalError() {
		// Make sure that we have the required room Calendars synced on some account 
		String[] mProjection = {};
		String mSelectionClause = 
				CalendarContract.Calendars.OWNER_ACCOUNT + " GLOB ? AND " + 
				CalendarContract.Calendars.SYNC_EVENTS + " = 1";
		String[] mSelectionArgs = { roomAccountGlob };
		String mSortOrder = null;
		
		Cursor result = resolver.query(
				CalendarContract.Calendars.CONTENT_URI,
				mProjection,
				mSelectionClause,
				mSelectionArgs,
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
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getAccount() {
		return this.account;
	}
}