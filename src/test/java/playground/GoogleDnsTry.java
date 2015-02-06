package playground;

import java.util.List;

import name.haochenxie.utils.gcloud.auth.DefaultAuthModule;
import name.haochenxie.utils.gcloud.auth.ICredentialService;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.DnsScopes;
import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ResourceRecordSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class GoogleDnsTry {

  @Inject
  ICredentialService credentialService;

  @Inject
  HttpTransport transport;

  @Inject
  JsonFactory jsonFactory;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new DefaultAuthModule());
    injector.injectMembers(this);
  }

  @Test
  public void tryListDomains() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();
    for (ManagedZone mz : mzs) {
      System.out.printf("%s: %s\n",
          mz.getDnsName(), mz);
    }
  }

  @Test
  public void tryListRRs() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();
    ManagedZone mz = mzs.get(0);
    System.out.println("For " + mz.getDnsName());
    List<ResourceRecordSet> rrsets = dns.resourceRecordSets().list("hx-network", mz.getName()).execute().getRrsets();
    for (ResourceRecordSet rrs : rrsets) {
      System.out.printf("%s\n", rrs);
    }
  }

}
