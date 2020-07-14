package com.freewill.phayathaidetect.clients;

import android.content.Context;

import com.freewill.phayathaidetect.R;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class Unsafe_CA_SSL {
    public static OkHttpClient ConfigureSSLSocket() {
        try {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
//        final SSLContext sslContext = createCA();
//        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);
//            okHttpClient.interceptors().add(new AddCookiesInterceptor());
//            okHttpClient.interceptors().add(new ReceivedCookiesInterceptor());

//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.sslSocketFactory(sslSocketFactory, trustManager);

//            okHttpClient.sslSocketFactory(createCA(BeaconReferenceApplication.getAppContext(), getBASE_URL()).getSocketFactory(), trustManager);
            okHttpClient.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
//        OkHttpClient okHttpClient = okHttpClient.build();
        return okHttpClient.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Server Test
     *
     * @throws Exception
     */

    private static SSLContext createCA(Context context, String url) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // loading CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        int cer = R.raw.pmexcacer;
        if (url.contains("61.91.247.166")) {
            cer = R.raw.pmexcacertest;
        } else if (url.contains("hr.freewillgroup.com")) {
            cer = R.raw.pmexcacer;
        } else if (url.contains("192.168.101.24")){
            cer = R.raw.pme_19216810124;
        } else if (url.contains("52.163.82.249")){
            cer = R.raw.pmexcacer;
        }


        InputStream cert = context.getResources().openRawResource(cer);
        java.security.cert.Certificate ca;
        try {
            ca = cf.generateCertificate(cert);
        } finally {
            cert.close();
        }

        // creating a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // creating a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // creating an SSLSocketFactory that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // creating an OkHttpClient that uses our SSLSocketFactory
        return sslContext;

    }
}
