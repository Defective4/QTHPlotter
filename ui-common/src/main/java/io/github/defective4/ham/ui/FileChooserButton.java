package io.github.defective4.ham.ui;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.io.File;

public class FileChooserButton extends JButton {

    public interface FileListener {
        void fileChoosen(File file);
    }

    private File file;
    private FileListener listener;

    public FileChooserButton(JLabel label) {
        label.setText("None");
        setText("Choose a file...");
        addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setApproveButtonText("Load");
            fc.setDialogTitle("Load a file");
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f != null && f.isFile()) {
                    String name = f.getName();
                    label.setText(name.substring(0, Math.min(16, name.length())));
                    file = f;
                } else {
                    label.setText("None");
                    file = null;
                }
                if (listener != null) listener.fileChoosen(f == null || !f.isFile() ? null : f);
            }
        });
    }

    public FileListener getListener() {
        return listener;
    }

    public void setListener(FileListener listener) {
        this.listener = listener;
    }

    public File getFile() {
        return file;
    }
}
