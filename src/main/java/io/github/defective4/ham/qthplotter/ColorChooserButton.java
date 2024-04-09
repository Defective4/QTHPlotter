package io.github.defective4.ham.qthplotter;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorChooserButton extends JButton {

    private Color color;

    public ColorChooserButton(Color initialColor) {
        this.color = initialColor;
        addActionListener(e -> {
            JColorChooser pane = new JColorChooser(color);
            JDialog dialog = JColorChooser.createDialog(null, "Color chooser", false, pane, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    color = pane.getColor();
                    repaint();
                }
            }, null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            dialog.pack();
            dialog.setResizable(false);
            dialog.setVisible(true);
        });
    }

    public Color getColor() {
        return color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.fillRect(5, 5, getWidth() - 10, getHeight() - 10);
    }
}
