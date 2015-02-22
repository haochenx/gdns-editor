package name.haochenxie.gdnseditor.ui.swing;

import java.io.IOException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;

import name.haochenxie.gdnseditor.ui.ICredentialProvider;
import name.haochenxie.lib.ui.swing.ISwingComponentProvider;
import name.haochenxie.utils.gcloud.auth.ICredentialService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.dns.DnsScopes;
import com.google.inject.Inject;

public class JCredentialSelector extends JButton
    implements ICredentialProvider,
               ISwingComponentProvider {

  private static final long serialVersionUID = 1L;

  private static final Set<String> CREDENTIAL_SCOPES = DnsScopes.all();

  @Inject
  private ICredentialService credentialService;

  private Credential credential;

  @Inject
  public JCredentialSelector(ICredentialService credentialService) throws IOException {
    this.credentialService = credentialService;

    initModel();
    initUi();
  }

  protected void initModel() throws IOException {
    credential = credentialService.getCachedCredentialFor(CREDENTIAL_SCOPES);
  }

  protected void initUi() {
    this.addActionListener(event -> {
      try {
        if (credential != null) {
          credentialService.deauthenticate(CREDENTIAL_SCOPES);
          credential = null;
        }
        credential = credentialService.authenticate(CREDENTIAL_SCOPES);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });

    refreshUi();
  }

  protected void refreshUi() {
    if (credential == null) {
      this.setText("Authenticate");
    } else {
      this.setText("Re-authenticate");
    }
  }

  @Override
  public JComponent getUiComponent() {
    return this;
  }

  @Override
  public Credential getCredential() {
    return credential;
  }

}
