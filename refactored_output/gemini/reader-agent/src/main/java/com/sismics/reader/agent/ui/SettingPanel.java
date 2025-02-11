```java
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.base.Strings;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.sismics.util.MessageUtil;
import com.sismics.util.validator.PortNumberValidator;

/**
 * Settings panel.
 */
public class SettingPanel extends JPanel {

    private static final PortNumberValidator PORT_NUMBER_VALIDATOR = new PortNumberValidator();

    private final Setting setting;

    private DefaultFormBuilder builder;

    public SettingPanel(Setting setting) {
        this.setting = setting;
        this.builder = new DefaultFormBuilder(new FormLayout("d, 6dlu, d, max(d;30dlu):grow"));
    }

    /**
     * Initialize UI components.
     */
    public void initComponent() {
        initPortSetting();
        initContextPathSetting();
        initAutoStartSetting();
        initSecureSetting();

        JButton defaultButton = new JButton(MessageUtil.getMessage("agent.setting.default"));
        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Arrays.asList("port", "context_path")
                        .forEach(key -> setValue(key, getDefaultValue(key)));
                Arrays.asList("auto_start", "secure")
                        .forEach(key -> setChecked(key, getDefaultValue(key)));
            }
        });

        JButton saveButton = new JButton(MessageUtil.getMessage("agent.setting.save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    saveSetting();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SettingPanel.this,
                            e.getMessage(),
                            MessageUtil.getMessage("agent.setting.save.error.title"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        builder.appendSeparator(saveButton, MessageUtil.getMessage("agent.setting.save"));
        builder.append(defaultButton, saveButton);

        setBorder(Borders.DIALOG_BORDER);

        setLayout(new BorderLayout(12, 12));
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    private void initPortSetting() {
        JFormattedTextField portField = new JFormattedTextField();
        builder.append(MessageUtil.getMessage("agent.setting.port"), portField);
        portField.setValue(setting.getPort());
    }

    private void initContextPathSetting() {
        JFormattedTextField contextPathField = new JFormattedTextField();
        builder.append(MessageUtil.getMessage("agent.setting.context_path"), contextPathField);
        contextPathField.setValue(setting.getContextPath());
    }

    private void initAutoStartSetting() {
        JCheckBox autoStartCheckBox = new JCheckBox();
        builder.append(MessageUtil.getMessage("agent.setting.auto_start"), autoStartCheckBox);
        autoStartCheckBox.setSelected(setting.isAutoStart());
    }

    private void initSecureSetting() {
        JCheckBox secureCheckBox = new JCheckBox();
        builder.append(MessageUtil.getMessage("agent.setting.secure"), secureCheckBox);
        secureCheckBox.setSelected(setting.isSecure());
    }

    private void saveSetting() throws Exception {
        for (Map.Entry<String, JFormattedTextField> entry : keyValuePairs.entrySet()) {
            updateSettingValue(entry.getKey(), entry.getValue());
        }
        setting.save();

        JOptionPane.showMessageDialog(this,
                MessageUtil.getMessage("agent.setting.save.ok.message"),
                MessageUtil.getMessage("agent.setting.save.ok.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSettingValue(String key, JFormattedTextField field) throws Exception {
        switch (key) {
            case "port":
                setting.setPort(getPort(field));
                break;
            case "context_path":
                setting.setContextPath(getContextPath(field));
                break;
            case "auto_start":
                setting.setAutoStart(getChecked((JCheckBox) field));
                break;
            case "secure":
                setting.setSecure(getChecked((JCheckBox) field));
                break;
        }
    }

    private int getPort(JFormattedTextField field) throws Exception {
        int port;
        try {
            port = ((Number) field.getValue()).intValue();
            PORT_NUMBER_VALIDATOR.validate(port);
        } catch (Exception e) {
            throw new Exception(MessageUtil.getMessage("agent.setting.save.error.port"), e);
        }
        return port;
    }

    private String getContextPath(JFormattedTextField field) throws Exception {
        String contextPath = field.getText().trim();
        if (contextPath.contains(" ") || !contextPath.startsWith("/")) {
            throw new Exception(MessageUtil.getMessage("agent.setting.save.error.context_path"));
        }
        return contextPath;
    }

    private boolean getChecked(JCheckBox field) {
        return field.isSelected();
    }

    private Object getDefaultValue(String key) {
        switch (key) {
            case "port":
                return Setting.DEFAULT_PORT;
            case "context_path":
                return Setting.DEFAULT_CONTEXT_PATH;
            case "auto_start":
                return Setting.DEFAULT_AUTO_START;
            case "secure":
                return Setting.DEFAULT_SECURE;
        }
        return null;
    }

    private void setValue(String key, Object value) {
        keyValuePairs.get(key).setValue(value);
    }

    private void setChecked(String key, boolean checked) {
        ((JCheckBox) keyValuePairs.get(key)).setSelected(checked);
    }
}
```