package com.futurice.android.reservator.model.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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

import android.util.Log;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
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
		
		this.rooms = new ArrayList<Room>();
		this.reservations = new ArrayList<Reservation>();
		
		fetch();
	}
	
	@Override
	public void setCredentials(String user, String password) {
		this.user = user;
		this.password = password;		
	}
	
	private String httpPost(String entity) {
		return "";
	}
	
	private void fetch() {
		String result = "";
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		DefaultHttpClient httpclient = new DefaultHttpClient(mgr, params);
		
		password = password == null ? "" : password; // debug
		
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("ogre@futurice.com", "k1taraS4nkari");
		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
		
		HttpPost httpPost = new HttpPost("https://mail.futurice.com/EWS/Exchange.asmx");
		
		try {
			StringEntity se = new StringEntity(getRoomListsXml,HTTP.UTF_8);
			se.setContentType("text/xml"); 
			httpPost.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			Log.w("SOAP", "Exception", e);
			return;
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
			return;
		} catch (IOException e) {
			Log.w("SOAP", "Exception", e);
			return;
		}
		
		Log.v("SOAP", result);
	}
	
	@Override
	public void deinit() {
		this.rooms = null;
		this.reservations = null;
	}

	@Override
	public List<Room> getRooms() {
		return rooms;
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
}
