package gui.utils;

import javax.swing.*;
import java.awt.*;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static void addFormRow(JPanel p, GridBagConstraints gbc, int row,
                                  String label, JComponent comp) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        p.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        p.add(comp, gbc);
    }

    public static void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    public static void showError(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
