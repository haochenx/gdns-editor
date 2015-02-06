package name.haochenxie.utils.gcloud.auth.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

import name.haochenxie.utils.gcloud.auth.ICredentialService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.base.Preconditions;

public class PersistedCredentialService implements ICredentialService {


  private File credentialStorageFile;
  private DataStore<StoredCredential> credentialDataStore;
  private HttpTransport transport;
  private JsonFactory jsonFactory;
  private GoogleClientSecrets clientSecrets;

  public PersistedCredentialService(File credentialStorageFile, Reader clientSecretsSource, HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    this.credentialStorageFile = credentialStorageFile;
    this.transport = transport;
    this.jsonFactory = jsonFactory;

    Preconditions.checkNotNull(credentialStorageFile);
    Preconditions.checkNotNull(clientSecretsSource);

    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(credentialStorageFile.getParentFile());
    credentialDataStore = dataStoreFactory.<StoredCredential>getDataStore(credentialStorageFile.getName());
    clientSecrets = GoogleClientSecrets.load(jsonFactory,
        new FileReader("credentials.local/client-id.json"));
  }

  @Override
  public Credential getCredentialFor(Set<String> scopes) throws IOException {
    String userid = getSetCanonicalString(scopes);

    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
            transport, jsonFactory, clientSecrets, scopes)
      .setCredentialDataStore(credentialDataStore)
      .build();
    Credential credential = flow.loadCredential(userid);
    if (credential == null) {
      credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(userid);
    }
    return credential;
  }

  private String getSetCanonicalString(Set<String> scopes) {
    return Joiner.on('_').join(new TreeSet<>(scopes));
  }

  public File getCredentialStorageFile() {
    return credentialStorageFile;
  }

}
