package ai.concerto.event.exchange;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import lombok.Data;

@Data
public class InboxSocketUser implements Authentication {

  private static final long serialVersionUID = 4329593784590354946L;
  private String userName;
  private boolean authenticated;

  public InboxSocketUser(String userName, Boolean authenticated) {
    this.userName = userName;
    this.authenticated = authenticated;
  }

  @Override
  public String getName() {
    return this.userName;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>();
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getDetails() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return this.userName;
  }

  @Override
  public boolean isAuthenticated() {
    return authenticated;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    this.authenticated = isAuthenticated;
  }

}
