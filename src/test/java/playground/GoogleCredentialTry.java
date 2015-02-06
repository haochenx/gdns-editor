package playground;

import java.io.File;
import java.io.FileReader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.dns.DnsScopes;

public class GoogleCredentialTry {

  private static JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  public static void main(String[] args) throws Exception {
    NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    File dsDir = new File("credentials.local/" + GoogleCredentialTry.class.getName());
    dsDir.mkdirs();
    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dsDir);
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new FileReader("credentials.local/client-id.json"));
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, DnsScopes.all())
      .setDataStoreFactory(dataStoreFactory)
      .build();

    Credential loadedCredential = flow.loadCredential("haochen");
    if (loadedCredential != null) {
      System.out.println("loaded:");
      System.out.println(loadedCredential.getAccessToken());
    }
    if (loadedCredential == null) {
      Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("haochen2");
      System.out.println("requested:");
      System.out.println(credential.getAccessToken());
    }
  }

}
