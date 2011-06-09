package com.futurice.android.reservator.model.soap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class SoapDataProxy implements DataProxy{
	private String user = null;
	private String password = null;

	private static String readFromInputStream(InputStream is) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte buffer[] = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return os.toString("UTF-8");
		} catch (IOException e) {
			Log.e("SOAP", "readFromInputStream", e);
			return "";
		}
	}

	private static String getResourceAsString(String resource) {
		return readFromInputStream(SoapDataProxy.class.getResourceAsStream(resource));
	}

	private static final String getRoomListsXml = getResourceAsString("GetRoomLists.xml");
	private static final String getRoomsXmlTemplate = getResourceAsString("GetRooms.xml");
	private static final String getUserAvailabilityXmlTemplate = getResourceAsString("GetUserAvailability.xml");

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	List<String> roomLists = null;
	List<Room> rooms = null;

	@Override
	public void init() {
		Log.v("SOAP", "created SoapDataProxy");

		this.roomLists = null;
		this.rooms = null;
	}

	@Override
	public void setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
	}

	private String httpPost(String entity) throws ReservatorException {
		Log.v("httpPost", entity);
		String result = "";

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		DefaultHttpClient httpclient = new DefaultHttpClient(mgr, params);

		Log.v("httpPost","credentials "+user+":"+password);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

		HttpPost httpPost = new HttpPost("https://mail.futurice.com/EWS/Exchange.asmx");

		try {
			StringEntity se = new StringEntity(entity,HTTP.UTF_8);
			se.setContentType("text/xml");
			httpPost.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			Log.w("SOAP", "Exception", e);
			throw new ReservatorException("Internal error", e);
		}

		httpPost.setHeader("Content-Type","text/xml; charset=utf=8'");

		try {
			HttpResponse response = httpclient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ReservatorException("Http error, probably wrong credentials");
			}

			BufferedReader reader = new BufferedReader(
			    new InputStreamReader(
			      response.getEntity().getContent()
			    )
			  );

			String line = null;
			while ((line = reader.readLine()) != null){
			  result += line + "\n";
			}
		} catch (ClientProtocolException e) {
			Log.w("SOAP", "Exception", e);
			throw new ReservatorException("Internal error", e);
		} catch (IOException e) {
			Log.w("SOAP", "Exception", e);
			throw new ReservatorException("Internal error", e);
		}

		Log.v("SOAP", result);

		return result;
	}

	private void xmlParse(ContentHandler handler, String xml) throws ReservatorException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			/** Create handler to handle XML Tags */
			xr.setContentHandler(handler);
			xr.parse(new InputSource(new StringReader(xml)));
		} catch (ParserConfigurationException e) {
			throw new ReservatorException(e);
		} catch (SAXException e) {
			throw new ReservatorException(e);
		} catch (IOException e) {
			throw new ReservatorException(e);
		}

	}


	protected class GetRoomListsHandler extends DefaultHandler  {
		private boolean inEmailAddress = false;
		private List<String> roomLists = new ArrayList<String>();

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inEmailAddress) {
				roomLists.add(new String(ch, start, length).trim());
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("t:EmailAddress")) inEmailAddress = false;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equals("t:EmailAddress")) inEmailAddress = true;
		}

		public List<String> getRoomLists() {
			return roomLists;
		}

	}

	protected class GetRoomsHandler extends DefaultHandler  {
		private DataProxy dataProxy = null;

		private boolean inName = false;
		private boolean inEmail= false;
		private String currentName;
		private String currentEmail;

		private List<Room> rooms = new ArrayList<Room>();

		public GetRoomsHandler(DataProxy dataProxy) {
			this.dataProxy = dataProxy;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inName) {
				currentName = new String(ch, start, length).trim();
			}
			else if (inEmail) {
				currentEmail = new String(ch, start, length).trim();
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("t:Room")) {
				if (currentName != null && currentEmail != null) {
					rooms.add(new Room(currentName, currentEmail, dataProxy));
				} else {
					Log.e("GetRoomsHandler", "malformed xml");
				}
			}
			else if (qName.equals("t:Name")) inName = false;
			else if (qName.equals("t:EmailAddress")) inEmail = false;

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// Log.d("GetRoomsHandler", qName);
			if (qName.equals("t:Room")) {
				currentName = null;
				currentEmail = null;
				inName = false;
				inEmail= false;
			}
			else if (qName.equals("t:Name")) inName = true;
			else if (qName.equals("t:EmailAddress")) inEmail = true;
		}

		public List<Room> getRooms() {
			return rooms;
		}

	}

	protected void fetchRoomLists() throws ReservatorException{
		// fetch only once
		if (roomLists != null) return;

		String result = httpPost(getRoomListsXml);
		Log.d("fetchRoomLists", result);

		GetRoomListsHandler handler = new GetRoomListsHandler();
		xmlParse(handler, result);

		roomLists = handler.getRoomLists();
	}

	protected List<Room> fetchRooms(String roomAddress) throws ReservatorException {
		Log.v("fetchRooms", roomAddress);

		String xml = getRoomsXmlTemplate.replace("{RoomListAddress}", roomAddress);
		String result = httpPost(xml);
		Log.d("fetchRooms", result);

		GetRoomsHandler handler = new GetRoomsHandler(this);
		xmlParse(handler, result);

		return handler.getRooms();
	}

	@Override
	public void deinit() {
		this.rooms = null;
	}

	@Override
	public List<Room> getRooms() throws ReservatorException {
		// cache
		if (rooms != null) return rooms;

		fetchRoomLists();

		rooms = new ArrayList<Room>();

		for (String roomAddress : roomLists) {
			rooms.addAll(fetchRooms(roomAddress));
		}

		return rooms;
	}

	@Override
	public List<Reservation> getReservations() throws ReservatorException {
		throw new ReservatorException("not implemented");
	}

	@Override
	public boolean reserve(Reservation r) throws ReservatorException {
		throw new ReservatorException("not implemented");
	}

	protected class GetUserAvailabilityHandler extends DefaultHandler  {
		private boolean inSubject = false;
		private boolean inStartTime = false;
		private boolean inEndTime = false;

		private String currentSubject;
		private String currentStartTime;
		private String currentEndTime;

		private Room room;
		private ArrayList<Reservation> reservations = new ArrayList<Reservation>();

		public GetUserAvailabilityHandler(Room room) {
			this.room = room;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inSubject) {
				currentSubject = new String(ch, start, length).trim();
			}
			else if (inStartTime) {
				currentStartTime = new String(ch, start, length).trim();
			}
			else if (inEndTime) {
				currentEndTime = new String(ch, start, length).trim();
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("CalendarEvent")) {
				if (currentSubject != null && currentStartTime != null && currentEndTime != null) {
					Log.i("GetUserAvailabilty", currentSubject + currentStartTime + currentEndTime);

					Calendar startTime = Calendar.getInstance();
					Calendar endTime = Calendar.getInstance();
					try {
						startTime.setTime(dateFormat.parse(currentStartTime));
						endTime.setTime(dateFormat.parse(currentEndTime));
					} catch (ParseException e) {
						Log.e("GetUserAvailabilityHandler", "malformed xml - parse error", e);
						return;
					}

					reservations.add(new Reservation(room, startTime, endTime));
				} else {
					Log.e("GetUserAvailabilityHandler", "malformed xml");
				}
			}
			else if (qName.equals("Subject")) inSubject = false;
			else if (qName.equals("StartTime")) inStartTime = false;
			else if (qName.equals("EndTime")) inEndTime = false;

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// Log.d("GetRoomsHandler", qName);
			if (qName.equals("CalendarEvent")) {
				currentSubject = null;
				currentStartTime = null;
				currentEndTime = null;
				inSubject = false;
				inStartTime = false;
				inEndTime = false;
			}
			else if (qName.equals("Subject")) inSubject = true;
			else if (qName.equals("StartTime")) inStartTime = true;
			else if (qName.equals("EndTime")) inEndTime = true;
		}

		public List<Reservation> getReservations() {
			return reservations;
		}
	}


	@Override
	public List<Reservation> getRoomReservations(Room room) throws ReservatorException {
		Log.v("getRoomReservations", room.toString());

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		Calendar fromNow = (Calendar) now.clone();
		fromNow.add(Calendar.MONTH, 1);

		String xml = getUserAvailabilityXmlTemplate;
		xml = xml.replace("{UserAddress}", room.getEmail());
		xml = xml.replace("{StartTime}", dateFormat.format(now.getTime()));
		xml = xml.replace("{EndTime}", dateFormat.format(fromNow.getTime()));

		String result = httpPost(xml);
		Log.v("getRoomReservations", result);

		GetUserAvailabilityHandler handler = new GetUserAvailabilityHandler(room);
		xmlParse(handler, result);

		return handler.getReservations();
	}
}
