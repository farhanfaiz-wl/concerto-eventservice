
package ai.concerto.event.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CertificateVerifierService {


  private static final Map<String, X509Certificate> CERTIFICATE_CACHE = new ConcurrentHashMap<>();
  private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
  private static final String VALID_SIGNING_CERT_CHAIN_PROTOCOL = "https";
  private static final String VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME = "s3.amazonaws.com";
  private static final String VALID_SIGNING_CERT_CHAING_URL_PATH_PREFIX = "/echo.api/";
  private static final String SIGNATURE_CERTIFICATE_TYPE = "X.509";
  private static final String SIGNATURE_KEY_TYPE = "RSA";

  public boolean verifyTimestamp(String timestampStr, long toleranceInMilliseconds) {
    if (timestampStr == null) {
      return false;
    }
    boolean isWithinTolerance = false;
    timestampStr = timestampStr.replace("\"", "");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    try {
      Date date = simpleDateFormat.parse(timestampStr);
      long delta = Math.abs(System.currentTimeMillis() - date.getTime());
      isWithinTolerance = delta <= toleranceInMilliseconds * Math.pow(10, 6);
    } catch (ParseException e) {
      log.error("Exception occured in parsing timestamp", e);
    }
    return isWithinTolerance;
  }

  /*
   * @param signingCertificateChainUrl the certificate chain URL provided in the request header
   */
  public void checkRequestSignature(final String requestBody, final String requestSignature,
      final String signingCertificateChainUrl) {
    if ((requestSignature == null) || (signingCertificateChainUrl == null)) {
      throw new SecurityException(
          "Missing signature/certificate for the provided speechlet request");
    }

    try {
      X509Certificate signingCertificate;
      if (CERTIFICATE_CACHE.containsKey(signingCertificateChainUrl)) {
        signingCertificate = CERTIFICATE_CACHE.get(signingCertificateChainUrl);
        /*
         * check the before/after dates on the certificate are still valid for the present time
         */
        signingCertificate.checkValidity();
      } else {
        signingCertificate = retrieveAndVerifyCertificateChain(signingCertificateChainUrl);
        CERTIFICATE_CACHE.put(signingCertificateChainUrl, signingCertificate);
      }

      // verify that the request was signed by the provided certificate
      Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
      signature.initVerify(signingCertificate.getPublicKey());
      signature.update(requestBody.getBytes(StandardCharsets.UTF_8));
      if (!signature
          .verify(Base64.decodeBase64(requestSignature.getBytes(StandardCharsets.UTF_8)))) {
        throw new SecurityException(
            "Failed to verify the signature/certificate for the provided speechlet request");
      }
    } catch (CertificateException | SignatureException | NoSuchAlgorithmException
        | InvalidKeyException ex) {
      throw new SecurityException(
          "Failed to verify the signature/certificate for the provided speechlet request", ex);
    }
  }

  /**
   * Verifies the signing certificate chain URL and returns a {@code URL} object.
   *
   * @param signingCertificateChainUrl the external form of the URL
   * @return the URL
   * @throws CertificateException if the URL is malformed or contains an invalid hostname, an
   *         unsupported protocol, or an invalid port (if specified)
   */
  private URL getAndVerifySigningCertificateChainUrl(final String signingCertificateChainUrl)
      throws CertificateException {
    try {
      URL url = new URI(signingCertificateChainUrl).normalize().toURL();
      // Validate the hostname
      if (!VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME.equalsIgnoreCase(url.getHost())) {
        throw new CertificateException(String.format(
            "SigningCertificateChainUrl [%s] does not contain the required hostname" + " of [%s]",
            signingCertificateChainUrl, VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME));
      }

      // Validate the path prefix
      String path = url.getPath();
      if (!path.startsWith(VALID_SIGNING_CERT_CHAING_URL_PATH_PREFIX)) {
        throw new CertificateException(String.format(
            "SigningCertificateChainUrl path [%s] is invalid. Expecting path to "
                + "start with [%s]",
            signingCertificateChainUrl, VALID_SIGNING_CERT_CHAING_URL_PATH_PREFIX));
      }

      // Validate the protocol
      String urlProtocol = url.getProtocol();
      if (!VALID_SIGNING_CERT_CHAIN_PROTOCOL.equalsIgnoreCase(urlProtocol)) {
        throw new CertificateException(
            String.format("SigningCertificateChainUrl [%s] contains an unsupported protocol [%s]",
                signingCertificateChainUrl, urlProtocol));
      }

      // Validate the port uses the default of 443 for HTTPS if explicitly defined in
      // the URL
      int urlPort = url.getPort();
      if (urlPort != -1 && urlPort != 443) {
        throw new CertificateException(
            String.format("SigningCertificateChainUrl [%s] contains an invalid port [%d]",
                signingCertificateChainUrl, urlPort));
      }

      return url;
    } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
      throw new CertificateException(
          String.format("SigningCertificateChainUrl [%s] is malformed", signingCertificateChainUrl),
          ex);
    }
  }

  /**
   * Retrieves the certificate from the specified URL and confirms that the certificate is valid.
   *
   * @param signingCertificateChainUrl the URL to retrieve the certificate chain from
   * @return the certificate at the specified URL, if the certificate is valid
   * @throws CertificateException if the certificate cannot be retrieve or is invalid
   */
  private X509Certificate retrieveAndVerifyCertificateChain(final String signingCertificateChainUrl)
      throws CertificateException {
    try (InputStream in =
        getAndVerifySigningCertificateChainUrl(signingCertificateChainUrl).openStream()) {
      CertificateFactory certificateFactory =
          CertificateFactory.getInstance(SIGNATURE_CERTIFICATE_TYPE);
      @SuppressWarnings("unchecked")
      Collection<X509Certificate> certificateChain =
          (Collection<X509Certificate>) certificateFactory.generateCertificates(in);
      /*
       * check the before/after dates on the certificate date to confirm that it is valid on the
       * current date
       */
      X509Certificate signingCertificate = certificateChain.iterator().next();
      signingCertificate.checkValidity();

      // check the certificate chain
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);

      X509TrustManager x509TrustManager = null;
      for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
        if (trustManager instanceof X509TrustManager tsManager) {
          x509TrustManager = tsManager;
        }
      }

      if (x509TrustManager == null) {
        throw new IllegalStateException(
            "No X509 TrustManager available. Unable to check certificate chain");
      } else {
        x509TrustManager.checkServerTrusted(
            certificateChain.toArray(new X509Certificate[certificateChain.size()]),
            SIGNATURE_KEY_TYPE);
      }

      /*
       * verify Echo API's hostname is specified as one of subject alternative names on the signing
       * certificate
       */
      if (!subjectAlernativeNameListContainsEchoSdkDomainName(
          signingCertificate.getSubjectAlternativeNames())) {
        throw new CertificateException("The provided certificate is not valid for the Echo SDK");
      }

      return signingCertificate;
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException ex) {
      throw new CertificateException(
          "Unable to verify certificate at URL: " + signingCertificateChainUrl, ex);
    }
  }

  private static boolean subjectAlernativeNameListContainsEchoSdkDomainName(
      Collection<List<?>> subjectAlternativeNames) {

    boolean containsAlexaKeyWord = false;
    for (List<?> subjectList : subjectAlternativeNames) {
      if (subjectList.contains("echo-api.amazon.com")) {
        containsAlexaKeyWord = true;
      }
    }
    return containsAlexaKeyWord;
  }

}
