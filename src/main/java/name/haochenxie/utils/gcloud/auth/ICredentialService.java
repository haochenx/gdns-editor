package name.haochenxie.utils.gcloud.auth;

import java.io.IOException;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;

public interface ICredentialService {

  public Credential getCredentialFor(Set<String> scopes) throws IOException;

  public Credential getCachedCredentialFor(Set<String> scopes) throws IOException;

  public Credential authenticate(Set<String> scopes) throws IOException;

  public void deauthenticate(Set<String> scopes) throws IOException;

}
