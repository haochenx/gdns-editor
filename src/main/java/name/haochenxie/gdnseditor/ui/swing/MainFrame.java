package name.haochenxie.gdnseditor.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import name.haochenxie.gdnseditor.model.IGoogleDnsFactory;
import name.haochenxie.gdnseditor.ui.ICredentialProvider;
import name.haochenxie.lib.ui.swing.ISwingComponentProvider;
import name.haochenxie.utils.gcloud.auth.DefaultAuthModule;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ManagedZonesListResponse;
import com.google.api.services.dns.model.ResourceRecordSet;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;

// TODO
public class MainFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  protected interface xCredentialSelector
      extends ICredentialProvider, ISwingComponentProvider {};

  @Inject
  private xCredentialSelector credentialSelectorProvider;

  @Inject
  private IGoogleDnsFactory googleDnsFactory;

  private JTextField txtProject;

  private JComboBox<ManagedZone> cmbDomain;

  private JTextArea taEditor;

  @Inject
  public MainFrame(xCredentialSelector credentialSelector, IGoogleDnsFactory googleDnsFactory) throws HeadlessException {
    this.credentialSelectorProvider = credentialSelector;
    this.googleDnsFactory = googleDnsFactory;

    initUi();
  }

  protected void initUi() {
    Container contentPanel = this.getContentPane();
    contentPanel.setLayout(new BorderLayout());

    JPanel upperPanel = new JPanel();
    JPanel lowerPanel = new JPanel();
    JPanel centralPanel = new JPanel();

    contentPanel.add(upperPanel, BorderLayout.NORTH);
    contentPanel.add(lowerPanel, BorderLayout.SOUTH);
    contentPanel.add(centralPanel, BorderLayout.CENTER);

    { // Setup the upper panel
      JPanel zpanel = upperPanel;
      zpanel.setLayout(new BoxLayout(zpanel, BoxLayout.Y_AXIS));

      JComponent xxCredentialSelector = credentialSelectorProvider.getUiComponent();
      txtProject = new JTextField();
      cmbDomain = new JComboBox<>();
      JButton btnRefresh = new JButton("Refresh");

      { // btnRefresh
        btnRefresh.addActionListener(evt -> {
          try {
            Dns gdns = getGoogleDns();
            ManagedZonesListResponse result = gdns.managedZones().list(getGoogleProject()).execute();
            List<ManagedZone> mzs = result.getManagedZones();

            cmbDomain.setModel(new DefaultComboBoxModel<>(mzs.toArray(new ManagedZone[mzs.size()])));
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
      }

      { // cmbDomain
        cmbDomain.setRenderer(new ListCellRenderer<ManagedZone>() {

          private LoadingCache<ManagedZone, Component> cache;
          private Component nullLabel;

          {
            nullLabel = new JLabel("<press refresh to load managed zone list>");
          }

          {
            cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<ManagedZone, Component>() {

                  @Override
                  public Component load(ManagedZone mz) {
                    JLabel label = new JLabel(String.format("\"%s\" - %s",
                        mz.getName(),
                        mz.getDnsName()));
                    label.setToolTipText(mz.getDescription());

                    return label;
                  }

                });
          }

          @Override
          public Component getListCellRendererComponent(JList<? extends ManagedZone> list,
              ManagedZone value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
              return nullLabel;
            }

            try {
              return cache.get(value);
            } catch (ExecutionException ex) {
              throw new RuntimeException(ex);
            }
          }
        });

        cmbDomain.addActionListener(evt -> {
          @SuppressWarnings("unchecked")
          JComboBox<ManagedZone> cmb = (JComboBox<ManagedZone>) evt.getSource();
          ManagedZone mz = (ManagedZone) cmb.getSelectedItem();

          try {
            Dns gdns = getGoogleDns();
            List<ResourceRecordSet> rrsets = gdns.resourceRecordSets().list(getGoogleProject(), mz.getName()).execute().getRrsets();

            String strResult = Joiner.on("\n").join(rrsets);

            taEditor.setText(strResult);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        });
      }

      {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BorderLayout());

        rowPanel.add(txtProject, BorderLayout.CENTER);
        rowPanel.add(xxCredentialSelector, BorderLayout.EAST);

        zpanel.add(rowPanel);
      }

      {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BorderLayout());

        rowPanel.add(cmbDomain, BorderLayout.CENTER);
        rowPanel.add(btnRefresh, BorderLayout.EAST);

        zpanel.add(rowPanel);
      }
    }

    { // Setup the center panel
      JPanel zpanel = centralPanel;
      zpanel.setLayout(new BorderLayout());

      taEditor = new JTextArea();

      zpanel.add(taEditor, BorderLayout.CENTER);
    }

    { // Setup the lower panel
      JPanel zpanel = lowerPanel;
      zpanel.setLayout(new FlowLayout(FlowLayout.LEFT));

      JButton btnPreview = new JButton("Preview");
      JButton btnExecute = new JButton("Execute");

      zpanel.add(btnPreview);
      zpanel.add(btnExecute);
    }

    this.pack();
  }
private String getGoogleProject() {
  return txtProject.getText();
}

  private Dns getGoogleDns() {
    return googleDnsFactory.createGoogleDns(credentialSelectorProvider.getCredential());
  }


  public static void main(String[] args) {

    Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        install(new DefaultAuthModule());
      }

      @Provides
      public xCredentialSelector providesCredentialSelectorProvider(Injector injector) {
        return new xCredentialSelector() {

          private JCredentialSelector comp = injector.getInstance(JCredentialSelector.class);

          @Override
          public Credential getCredential() {
            return comp.getCredential();
          }

          @Override
          public JComponent getUiComponent() {
            return comp;
          }

        };
      }

      @Provides
      public IGoogleDnsFactory providesGoogleDnsProvider(HttpTransport transport, JsonFactory jsonFactory) {
        return new IGoogleDnsFactory() {

          @Override
          public Dns createGoogleDns(HttpRequestInitializer credential) {
            Dns dns = new Dns.Builder(transport, jsonFactory, credential).build();
            return dns;
          }
        };
      }

    });

    MainFrame win = injector.getInstance(MainFrame.class);

    win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    win.setVisible(true);
  }

}
