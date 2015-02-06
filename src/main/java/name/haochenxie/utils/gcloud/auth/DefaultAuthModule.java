package name.haochenxie.utils.gcloud.auth;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

import name.haochenxie.utils.gcloud.auth.impl.PersistedCredentialService;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

public class DefaultAuthModule extends AbstractModule {

  protected Module bootstrapModule = new AbstractModule() {
    @Override
    protected void configure() {
      try {
        bind(HttpTransport.class).toInstance(GoogleNetHttpTransport.newTrustedTransport());
        bind(JsonFactory.class).toInstance(JacksonFactory.getDefaultInstance());
        bind(File.class).annotatedWith(Names.named("credentialStorageFile")).toInstance(new File("credentials.local/CredentialsStorage"));
        bind(Reader.class).annotatedWith(Names.named("clientId")).toInstance(new FileReader("credentials.local/client-id.json"));
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new ConfigurationException(Collections.singleton(
            new Message(Collections.emptyList(),
            ex.getMessage(), ex)));
      }
    }
  };

  protected Injector bootstrapInjector = Guice.createInjector(bootstrapModule);

  @Override
  protected void configure() {
    install(bootstrapModule);
  }

  @Provides @Singleton
  public ICredentialService providesICredentialService() throws IOException {
    return new PersistedCredentialService(
        bootstrapInjector.getInstance(Key.get(File.class, Names.named("credentialStorageFile"))),
        bootstrapInjector.getInstance(Key.get(Reader.class, Names.named("clientId"))),
        bootstrapInjector.getInstance(HttpTransport.class),
        bootstrapInjector.getInstance(JsonFactory.class));
  }

}
