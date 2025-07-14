
package com.perforce.team.ui.swarmreview;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
/**
 * Class to disable SSL validation on self signed certificates
 * 
 *
 */
public class SSLValidator {
   private static final P4HostnameVerifier P4_HOSTNAME_VERIFIER = new P4HostnameVerifier();
   private static SSLSocketFactory factory;

   
   public static void disableSSL(HttpURLConnection conn)
       throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

      if (conn instanceof HttpsURLConnection) {
         HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
         SSLSocketFactory factory = prepFactory(httpsConnection);
         httpsConnection.setSSLSocketFactory(factory);
         httpsConnection.setHostnameVerifier(P4_HOSTNAME_VERIFIER);
      }
   }

   static synchronized SSLSocketFactory
            prepFactory(HttpsURLConnection httpsConnection)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

      if (factory == null) {
         SSLContext ctx = SSLContext.getInstance("TLS");
         ctx.init(null, new TrustManager[]{ new P4EclipseTrustManager() }, null);
         factory = ctx.getSocketFactory();
      }
      return factory;
   }

   private static final class P4HostnameVerifier implements HostnameVerifier {
      public boolean verify(String hostname, SSLSession session) {
         return true;
      }
   }

   private static class P4EclipseTrustManager implements X509TrustManager {
      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
      public X509Certificate[] getAcceptedIssuers() { return null; }
   }

}