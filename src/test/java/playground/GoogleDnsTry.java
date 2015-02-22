package playground;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import name.haochenxie.utils.gcloud.auth.DefaultAuthModule;
import name.haochenxie.utils.gcloud.auth.ICredentialService;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.DnsScopes;
import com.google.api.services.dns.model.Change;
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

    for (ManagedZone mz : mzs) {
      System.out.println("For " + mz.getDnsName());
      List<ResourceRecordSet> rrsets = dns.resourceRecordSets().list("hx-network", mz.getName()).execute().getRrsets();
      rrsets.stream()
          .forEach(x -> System.out.println(x));
    }
  }

  //@Test
  public void doMoveMainServerToKyoOnHaochenxieName() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();

    List<ManagedZone> filtered = mzs.stream()
        .filter(mz -> mz.getDnsName().equals("haochenxie.name."))
        .collect(Collectors.toList());
    assertFalse(filtered.isEmpty());

    ManagedZone mz = filtered.get(0);
    System.out.println("For " + mz.getDnsName());
    List<ResourceRecordSet> rrsets = dns.resourceRecordSets().list("hx-network", mz.getName()).execute().getRrsets();

    List<ResourceRecordSet> frrset = rrsets.stream()
        .filter(rr ->
                  rr.getName().equals("haochenxie.name.") &&
                  rr.getType().equals("A"))
        .collect(Collectors.toList());

    assertFalse(frrset.isEmpty());

    ResourceRecordSet oldrr = frrset.get(0);
    ResourceRecordSet newrr = oldrr.clone();

    assertFalse(newrr == oldrr);
    assertTrue(newrr.equals(oldrr));

    {
      List<String> rrdata = newrr.getRrdatas();
      assertEquals(1, rrdata.size());
      System.out.println(rrdata);
      newrr.setRrdatas(Arrays.asList("54.199.199.90"));
    }

    Change dchange = new Change();

    dchange.setDeletions(Arrays.asList(oldrr));
    dchange.setAdditions(Arrays.asList(newrr));

    System.out.println(dchange);

    Change xchange = dns.changes().create("hx-network", mz.getName(), dchange).execute();
    assertNotNull(xchange);

    System.out.println(xchange.getStatus());

  }

  //@Test
  public void doMoveMainServerToKyoOnHxieCc() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();

    List<ManagedZone> filtered = mzs.stream()
        .filter(mz -> mz.getDnsName().equals("hxie.cc."))
        .collect(Collectors.toList());
    assertFalse(filtered.isEmpty());

    ManagedZone mz = filtered.get(0);
    System.out.println("For " + mz.getDnsName());
    List<ResourceRecordSet> rrsets = dns.resourceRecordSets().list("hx-network", mz.getName()).execute().getRrsets();

    List<ResourceRecordSet> frrset = rrsets.stream()
        .filter(rr ->
                  rr.getName().equals("hxie.cc.") &&
                  rr.getType().equals("A"))
        .collect(Collectors.toList());

    assertEquals(1, frrset.size());

    ResourceRecordSet oldrr = frrset.get(0);
    ResourceRecordSet newrr = oldrr.clone();

    assertFalse(newrr == oldrr);
    assertTrue(newrr.equals(oldrr));

    {
      List<String> rrdata = newrr.getRrdatas();
      assertEquals(1, rrdata.size());
      System.out.println(rrdata);
      newrr.setRrdatas(Arrays.asList("54.199.199.90"));
    }

    System.out.println(oldrr);
    System.out.println("->");
    System.out.println(newrr);

    Change dchange = new Change();

    dchange.setDeletions(Arrays.asList(oldrr));
    dchange.setAdditions(Arrays.asList(newrr));

    System.out.println(dchange);

    Change xchange = dns.changes().create("hx-network", mz.getName(), dchange).execute();
    assertNotNull(xchange);

    System.out.println(xchange.getStatus());

  }

  //@Test
  public void doMoveMainServerToKyoOnHaochenXieName() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();

    List<ManagedZone> filtered = mzs.stream()
        .filter(mz -> mz.getDnsName().equals("haochen.xie.name."))
        .collect(Collectors.toList());
    assertFalse(filtered.isEmpty());

    ManagedZone mz = filtered.get(0);
    System.out.println("For " + mz.getDnsName());
    List<ResourceRecordSet> rrsets = dns.resourceRecordSets().list("hx-network", mz.getName()).execute().getRrsets();

    List<ResourceRecordSet> frrset = rrsets.stream()
        .filter(rr ->
                  rr.getName().equals("haochen.xie.name.") &&
                  rr.getType().equals("A"))
        .collect(Collectors.toList());

    assertEquals(1, frrset.size());

    ResourceRecordSet oldrr = frrset.get(0);
    ResourceRecordSet newrr = oldrr.clone();

    assertFalse(newrr == oldrr);
    assertTrue(newrr.equals(oldrr));

    {
      List<String> rrdata = newrr.getRrdatas();
      assertEquals(1, rrdata.size());
      System.out.println(rrdata);
      newrr.setRrdatas(Arrays.asList("54.199.199.90"));
    }

    System.out.println(oldrr);
    System.out.println("->");
    System.out.println(newrr);

    Change dchange = new Change();

    dchange.setDeletions(Arrays.asList(oldrr));
    dchange.setAdditions(Arrays.asList(newrr));

    System.out.println(dchange);

    Change xchange = dns.changes().create("hx-network", mz.getName(), dchange).execute();
    assertNotNull(xchange);

    System.out.println(xchange.getStatus());

  }

  @Test
  public void tryListChanges() throws Exception {
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
    List<ManagedZone> mzs = dns.managedZones().list("hx-network").execute().getManagedZones();
    ManagedZone mz = mzs.get(0);

    List<Change> changeList = dns.changes().list("hx-network", mz.getName()).execute().getChanges();
    for (Change change : changeList) {
      System.out.println(change);
    }
  }

  @Test
  public void tryUploadTextRecord() throws Exception {
    // TODO
    Credential credential = credentialService.getCredentialFor(DnsScopes.all());
    Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();


    ResourceRecordSet record = new ResourceRecordSet();
    record.setType("TXT");
    //record.setRrdatas(rrdatas); //TODO continue from here

    Change change = new Change();

  }

}
