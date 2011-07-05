package com.futurice.android.reservator.model.soap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import android.util.Log;

import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import com.futurice.android.reservator.model.soap.EWS.MicrosoftStyle;
import com.futurice.android.reservator.model.soap.SoapEWS.Envelope;

public class SoapDataProxy extends DataProxy{
	private String user = null;
	private String password = null;

	private String server;

	private class Bucket {
		private final long millis;
		private final Room room;
		private final Reservation reservation;

		public Bucket(Room room, Reservation reservation) {
			this.millis = System.currentTimeMillis();
			this.room = room;
			this.reservation = reservation;
		}

		public long getMillis() {
			return millis;
		}

		public Room getRoom() {
			return room;
		}

		public Reservation getReservation() {
			return reservation;
		}

	}

	private final long RECENLY_ADDED_EXPIRE_TIME = 10*1000; // 10 seconds

	private Vector<Bucket> recentlyAdded = new Vector<Bucket>();

	public SoapDataProxy(String server) {
		this.server = server;
	}

	private static String getResourceAsString(String resource) {
		return Helpers.readFromInputStream(SoapDataProxy.class.getResourceAsStream(resource), -1);
	}

	@Override
	public void setServer(String server) {
		this.server = server;
	}

	private static final String getRoomListsXml = getResourceAsString("GetRoomLists.xml");
	private static final String getRoomsXmlTemplate = getResourceAsString("GetRooms.xml");
	// private static final String getUserAvailabilityXmlTemplate = getResourceAsString("GetUserAvailability.xml");
	private static final String createItemCalendarXmlTemplate = getResourceAsString("CreateItemCalendar.xml");
	private static final String findItemCalendarXmlTemplate = getResourceAsString("FindItemCalendar.xml");

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static final SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
	}

	private String httpPost(String entity) throws ReservatorException {
		long start = System.currentTimeMillis();
		Log.v("httpPost", entity);
		String result = "";

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", UnsafeSSLSocketFactory.getUnsafeSocketFactory(), 443)); // XXX, Unsafe, only for debugging!
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		DefaultHttpClient httpclient = new DefaultHttpClient(mgr, params);

		// http://msdn.microsoft.com/en-us/library/bb856547(v=exchg.80).aspx
		// Authentication scheme ntlm not supported

		Log.v("httpPost","credentials "+user+":"+password);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

		HttpPost httpPost = new HttpPost("https://" + server + "/EWS/Exchange.asmx");

		try {
			StringEntity se = new StringEntity(entity,HTTP.UTF_8);
			se.setContentType("text/xml");
			httpPost.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			Log.w("SOAP", "Exception", e);
			throw new ReservatorException("Internal error - " + e.getMessage(), e);
		}

		httpPost.setHeader("Content-Type","text/xml; charset=utf-8");

		try {
			HttpResponse response = httpclient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ReservatorException("Http error -- " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}

			result = Helpers.readFromInputStream(response.getEntity().getContent(), (int) response.getEntity().getContentLength());
		} catch (ClientProtocolException e) {
			Log.e("SOAP", "httpPost", e);
			throw new ReservatorException("Internal error - " + e.getMessage(), e);
		} catch (IOException e) {
			Log.e("SOAP", "httpPost", e);
			throw new ReservatorException("Internal error - " + e.getMessage(), e);
		}

		Log.v("SOAP", result);
		Log.d("Performance", "HTTP post done in "+ (System.currentTimeMillis() - start) + "ms");
		return result;
	}

	protected Vector<String> fetchRoomLists() throws ReservatorException {
		// fetch only once
		String result = httpPost(getRoomListsXml);
		//Log.d("fetchRoomLists", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			// why non/strictness doesn't work with the attributes?
			long start = System.currentTimeMillis();
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.d("Performance", "Read roomlist envelope in " + (System.currentTimeMillis() - start) + "ms");
			//Log.d("SOAP", envelope.toString());
			return envelope.getRoomLists();
		} catch (ReservatorException e) {
			throw e;
		} catch (Exception e) {
			Log.e("SOAP", "fetchRoomLists XML-parsing failed", e);
			throw new ReservatorException(e);
		}
	}

	protected Vector<Room> fetchRooms(String roomAddress) throws ReservatorException {
		Log.v("fetchRooms", roomAddress);

		String xml = getRoomsXmlTemplate.replace("{RoomListAddress}", roomAddress);
		String result = httpPost(xml);
		//Log.d("fetchRooms", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			long start = System.currentTimeMillis();
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.d("Performance", "Read room envelope in " + (System.currentTimeMillis() - start) + "ms");
			//Log.d("SOAP", envelope.toString());
			return envelope.getRooms(this);
		} catch (ReservatorException e) {
			throw e;
		} catch (Exception e) {
			Log.e("SOAP", "fetchRooms XML-parsing failed", e);
			throw new ReservatorException(e);
		}
	}

	@Override
	public Vector<Room> getRooms() throws ReservatorException {
		fetchRoomLists();

		Vector<Room> rooms = new Vector<Room>();

		for (String roomAddress : fetchRoomLists()) {
			rooms.addAll(fetchRooms(roomAddress));
		}

		Collections.sort(rooms, new Comparator<Room>() {
			@Override
			public int compare(Room room1, Room room2) {
				return room1.getEmail().compareTo(room2.getEmail());
			}
		});

		return rooms;
	}

	@Override
	public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException {
		Log.v("SOAP", "reserve");

		String xml = createItemCalendarXmlTemplate;
		xml = xml.replace("{RoomName}", room.getName());
		xml = xml.replace("{RoomAddress}", room.getEmail());
		xml = xml.replace("{UserAddress}", ownerEmail);
		synchronized (dateFormatUTC) {
			xml = xml.replace("{StartTime}", dateFormat.format(timeSpan.getStart().getTime()));
			xml = xml.replace("{EndTime}", dateFormat.format(timeSpan.getEnd().getTime()));
		}
		xml = xml.replace("{Subject}", "Reserved with FutuReservator5000");

		String result = httpPost(xml);
		Log.v("SOAP", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.d("SOAP", envelope.toString());
			String id = envelope.checkCreateItemSuccessful();
			synchronized (recentlyAdded) {
				recentlyAdded.add(new Bucket(room, new Reservation(id, "FutuReservator5000", timeSpan)));
			}
		} catch (ReservatorException e) {
			throw e;
		} catch (Exception e) {
			Log.e("SOAP", "reserve XML-parsing failed", e);
			throw new ReservatorException(e);
		}
	}

	@Override
	public Vector<Reservation> getRoomReservations(Room room) throws ReservatorException {
		Log.v("SOAP", "getRoomReservations: "+  room.toString());

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		Calendar fromNow = (Calendar) now.clone();
		fromNow.add(Calendar.MONTH, 1);

		String xml = findItemCalendarXmlTemplate;
		xml = xml.replace("{RoomAddress}", room.getEmail());
		synchronized (dateFormatUTC) {
			xml = xml.replace("{StartTime}", dateFormatUTC.format(now.getTime()));
			xml = xml.replace("{EndTime}", dateFormatUTC.format(fromNow.getTime()));
		}
		String result = httpPost(xml);
		//Log.v("SOAP", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			long start = System.currentTimeMillis();
			Envelope envelope = serializer.read(Envelope.class, result, false);
			// Log.d("SOAP", envelope.toString());
			Vector<Reservation> reservations = envelope.getReservations(room);

			expireRecentlyAdded();

			synchronized (recentlyAdded) {
				for (Bucket bucket : recentlyAdded) {
					// not our room
					if (!bucket.getRoom().equals(room)) {
						continue;
					}

					boolean isThere = false;

					// see if reservation is there
					for (Reservation r : reservations) {
						if (bucket.getReservation().equals(r)) {
							isThere = true;
							break;
						}
					}

					// add if not there
					if (!isThere) {
						reservations.add(bucket.getReservation());
					}
				}
			}

			Log.d("Performance", "Parsed room reservations envelope in " + (System.currentTimeMillis() - start) + "ms");
			return reservations;
		} catch (ReservatorException e) {
			throw e;
		} catch (Exception e) {
			Log.e("SOAP", "getRoomReservations XML-parsing failed", e);
			throw new ReservatorException(e);
		}
	}

	private void expireRecentlyAdded() {
		long stamp = System.currentTimeMillis() - RECENLY_ADDED_EXPIRE_TIME;
		for (Iterator<Bucket> iter = recentlyAdded.iterator(); iter.hasNext(); ) {
			Bucket buck = iter.next();
			if (buck.getMillis() < stamp) {
				iter.remove();
			}
		}

	}
}
