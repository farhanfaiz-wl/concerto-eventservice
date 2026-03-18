package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class CertificateVerifierServiceTest {
  @InjectMocks
  @Resource
  CertificateVerifierService certificateVerifierService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void verifyTimestampWithNoTimestampStr() {
    assertFalse(certificateVerifierService.verifyTimestamp(null, 23L));
  }

  @Test
  void verifyTimestamp() {
    assertTrue(certificateVerifierService.verifyTimestamp("2013-09-29T18:46:19Z", 23000000));
  }

  @Test
  void checkRequestSignatureException() {
    assertThrows(SecurityException.class, () -> {
      certificateVerifierService.checkRequestSignature("requestBody", null,
          "signingCertificateChainUrl");
    });
  }
}
