package com.futurice.android.reservator.model.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
	
	private static final String getRoomListsXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
		"<soap:Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
		"xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
		"xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\" " +
		"xmlns:m=\"http://schemas.microsoft.com/exchange/services/2006/messages\"> " +
		"<soap:Header><t:RequestServerVersion Version =\"Exchange2010_SP1\"/></soap:Header>" +
		"<soap:Body><m:GetRoomLists /></soap:Body>" +
		"</soap:Envelope>";
	
	List<String> roomLists = null;
	List<Room> rooms = null;
	List<Reservation> reservations = null;
	
	@Override
	public void init() {
		Log.v("SOAP", "created SoapDataProxy");
		
		this.roomLists = null;
		this.rooms = null;
		this.reservations = new ArrayList<Reservation>();
	}
	
	@Override
	public void setCredentials(String user, String password) {
		this.user = user;
		this.password = password;		
	}
	
	private String httpPost(String entity) throws ReservatorException {
		String result = "";
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		DefaultHttpClient httpclient = new DefaultHttpClient(mgr, params);
		
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
		private boolean emailAddress = false;
		private List<String> roomLists = new ArrayList<String>();
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (emailAddress) {
				roomLists.add(new String(ch, start, length).trim());
			}
			
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("t:EmailAddress")) emailAddress = false;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			if (qName.equals("t:EmailAddress")) emailAddress = true;
		}
		
		public List<String> getRoomLists() {
			return roomLists;
		}
		
	}
	
	protected void fetchRoomLists() throws ReservatorException{
		// fetch only once
		if (roomLists != null) return;

		String result = httpPost(getRoomListsXml);
			
		GetRoomListsHandler handler = new GetRoomListsHandler();
		xmlParse(handler, result);
		
		roomLists = handler.getRoomLists();
	}
	
	@Override
	public void deinit() {
		this.rooms = null;
		this.reservations = null;
	}

	@Override
	public List<Room> getRooms() throws ReservatorException {
		// cache	
		if (rooms != null) return rooms;
		
		fetchRoomLists();
		
		for (String roomAddress : roomLists) {
			Log.v("Room", roomAddress);
		}
		
		return new ArrayList<Room>(); // TODO!
	}

	@Override
	public List<Reservation> getReservations() {
		return reservations;
	}

	@Override
	public boolean reserve(Reservation r) {
		reservations.add(r);
		r.setConfirmed(true);
		return true;
	}

	@Override
	public List<Reservation> getRoomReservations(Room room) throws ReservatorException {
		return new ArrayList<Reservation>();
	}
}
