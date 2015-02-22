package name.haochenxie.gdnseditor.model;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.dns.Dns;

public interface IGoogleDnsFactory {

  public Dns createGoogleDns(HttpRequestInitializer credential);

}
