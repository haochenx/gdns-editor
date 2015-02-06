package name.haochenxie.utils.gcloud.auth.impl;

import static org.junit.Assert.assertNotNull;
import name.haochenxie.utils.gcloud.auth.DefaultAuthModule;
import name.haochenxie.utils.gcloud.auth.ICredentialService;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.dns.DnsScopes;
import com.google.api.services.gmail.GmailScopes;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class PersistedCredentialServiceExample {

  @Inject
  ICredentialService credentialService;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new DefaultAuthModule());
    injector.injectMembers(this);
  }

  @Test
  public void tryGetDnsCredentials() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    assertNotNull(credential);

    System.out.println(credential.getAccessToken());
  }

  @Test
  public void tryGetGmailCredentials() throws Exception {
    Credential credential = credentialService.getCredentialFor(GmailScopes.all());
    assertNotNull(credential);

    System.out.println(credential.getAccessToken());
  }

}
