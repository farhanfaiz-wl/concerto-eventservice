package ai.concerto.event.auth.filter;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class PreApiTokenHeaderFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final String headerName;

  public PreApiTokenHeaderFilter(String headerName) {
    this.headerName = headerName;
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    return request.getHeader(headerName);
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }
}
