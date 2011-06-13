package com.futurice.android.reservator.model.soap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

import com.futurice.android.reservator.model.soap.EWS.MicrosoftStyle;
import com.futurice.android.reservator.model.soap.SoapEWS.Envelope;

public class SoapDataProxy implements DataProxy{
	private String user = null;
	private String password = null;

	private String server;

	public SoapDataProxy(String server) {
		this.server = server;
	}

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

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
		schemeRegistry.register(new Scheme("https", UnsafeSSLSocketFactory.getUnsafeSocketFactory(), 443)); // XXX, Unsafe for debugging!
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
			throw new ReservatorException("Internal error - " + e.getMessage(), e);
		} catch (IOException e) {
			Log.w("SOAP", "Exception", e);
			throw new ReservatorException("Internal error - " + e.getMessage(), e);
		}

		Log.v("SOAP", result);

		return result;
	}

	protected void fetchRoomLists() throws ReservatorException {
		// fetch only once
		if (roomLists != null) return;

		String result = httpPost(getRoomListsXml);
		Log.d("fetchRoomLists", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			//StringWriter writer = new StringWriter();
			//Envelope env = new Envelope();
			//serializer.write(env, writer);
			// Log.v("SimpleXML", writer.toString());

			// why nonstrictness doesn't work with the attributes?
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.v("SimpleXML", envelope.toString());
			roomLists = envelope.getRoomLists();
		} catch (Exception e) {
			Log.e("SimpleXML", "fu-", e);
			throw new ReservatorException(e);
		}
	}

	protected List<Room> fetchRooms(String roomAddress) throws ReservatorException {
		Log.v("fetchRooms", roomAddress);

		String xml = getRoomsXmlTemplate.replace("{RoomListAddress}", roomAddress);
		String result = httpPost(xml);
		Log.d("fetchRooms", result);

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.v("SimpleXML", envelope.toString());
			return envelope.getRooms(this);
		} catch (Exception e) {
			Log.e("SimpleXML", "fu-", e);
			throw new ReservatorException(e);
		}
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

		Serializer serializer = new Persister(new Format(new MicrosoftStyle()));

		try {
			Envelope envelope = serializer.read(Envelope.class, result, false);
			Log.v("SimpleXML", envelope.toString());
			return envelope.getReservations(room);
		} catch (Exception e) {
			Log.e("SimpleXML", "fu-", e);
			throw new ReservatorException(e);
		}
	}
}
