package com.futurice.android.reservator.model.fum3;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.soap.UnsafeSSLSocketFactory;

/*
 * https://fum3.futurice.com/api/group/Futurice/?username=ogre
 * https://fum3.futurice.com/api/user/hnev/?username=ogre&include=cn
 * roomreservator:ako7Thar
 *
 */

public class FumAddressBook extends AddressBook {
	// ^\s*(.*)\((\S+)\)\s*$
	private Pattern namePattern = Pattern.compile("^\\s*(.*)\\((\\S+)\\)\\s*$");
	private Context context;

	public FumAddressBook(Context c) {
		context = c;
	}
	
	@Override
	protected Vector<AddressBookEntry> fetchEntries() throws ReservatorException {
		Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();

		String result = "";

		SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);

		String user = settings.getString(context.getString(R.string.PREFERENCES_FUM_USERNAME), "");
		String password = settings.getString(context.getString(R.string.PREFERENCES_FUM_PASSWORD), "");
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", UnsafeSSLSocketFactory.getUnsafeSocketFactory(), 443)); // XXX, Unsafe, only for debugging!
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		HttpParams params = new BasicHttpParams();

		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

		DefaultHttpClient httpclient = new DefaultHttpClient(mgr, params);

		// http://msdn.microsoft.com/en-us/library/bb856547(v=exchg.80).aspx
		// Authentication scheme ntlm not supported

		// Let's not log the user and password on verbose level
		// Log.v("httpGet","credentials "+user+":"+password);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

		HttpGet httpGet = new HttpGet("https://fum3.futurice.com/api/group/Futurice/?username=itteam&include=uniqueMember");

		//httpGet.setHeader("Content-Type","text/xml; charset=utf-8");

		try {
			HttpResponse response = httpclient.execute(httpGet);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ReservatorException("Http error -- " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}

			result = Helpers.readFromInputStream(response.getEntity().getContent(), (int) response.getEntity().getContentLength());
		} catch (ClientProtocolException e) {
			Log.w("FUM3", "Exception", e);
			throw new ReservatorException("Error fetching FUM addressbook -- " + e.getMessage(), e);
		} catch (IOException e) {
			Log.w("FUM3", "Exception", e);
			throw new ReservatorException("Error fetching FUM addressbook -- " + e.getMessage(), e);
		}

		Log.v("FUM3", result);

		try {
			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
			JSONArray locations = object.getJSONArray("uniqueMember");
			for (int i = 0; i < locations.length(); ++i) {
				JSONObject member = locations.getJSONObject(i);

				String email = member.getString("rdn_value")+"@futurice.com";

				Matcher m = namePattern.matcher(member.getString("display"));
				if (m.matches()) {
					entries.add(new AddressBookEntry(
							m.group(1).trim() + ", " + m.group(2),
							email));
				} else {
					entries.add(new AddressBookEntry(
							member.getString("display"),
							email));
				}
			}
			return entries;
		} catch (JSONException e) {
			Log.e("FUM3", "Json error", e);
			throw new ReservatorException(e);
		}

	}
}
