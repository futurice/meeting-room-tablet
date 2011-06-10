package com.futurice.android.reservator.model.soap;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.futurice.android.reservator.model.ReservatorException;

public class UnsafeSSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public UnsafeSSLSocketFactory(KeyStore trustStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {

        super(trustStore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

    public static UnsafeSSLSocketFactory getUnsafeSocketFactory() throws ReservatorException {
    	KeyStore trustStore;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);
	        return new UnsafeSSLSocketFactory(trustStore);
		} catch (KeyStoreException e) {
			throw new ReservatorException(e);
		} catch (KeyManagementException e) {
			throw new ReservatorException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new ReservatorException(e);
		} catch (UnrecoverableKeyException e) {
			throw new ReservatorException(e);
		} catch (CertificateException e) {
			throw new ReservatorException(e);
		} catch (IOException e) {
			throw new ReservatorException(e);
		}

    }
}