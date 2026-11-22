package priorq;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.Arrays;

public class Main {
    static final Path HOME = Path.of(System.getProperty("user.home"), ".priorq");
    static final Color BLUE = new Color(65, 68, 197);
    static final Color RED = new Color(214, 69, 80);
    static final Font BODY = new Font(
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()).contains("Sansation")
                    ? "Sansation" : "SansSerif",
            Font.PLAIN, 16
    );

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Files.createDirectories(HOME);

                FileChannel channel = FileChannel.open(
                        HOME.resolve("session.lock"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE
                );

                FileLock lock = channel.tryLock();

                if (lock == null) {
                    channel.close();
                    throw new IllegalStateException("PriorQ is already open.");
                }

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        lock.release();
                        channel.close();
                    } catch (Exception ignored) {
                    }
                }));

                for (Object key : UIManager.getDefaults().keySet().toArray()) {
                    if (UIManager.get(key) instanceof Font) {
                        UIManager.put(key, BODY);
                    }
                }

                login();
            } catch (Exception ex) {
                showMessage(null, ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color outline;

        RoundedPanel(LayoutManager layout, Color fill, Color outline, int cornerRadius) {
            super(layout);
            this.cornerRadius = cornerRadius;
            this.outline = outline;
            setBackground(fill);
            setOpaque(false);
        }

        void setOutline(Color color) {
            outline = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth() - 1;
            int height = getHeight() - 1;

            g.setColor(getBackground());
            g.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            g.setColor(outline);
            g.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            g.dispose();
        }
    }

    private static class RoundedButton extends JButton {
        private final boolean secondary;

        RoundedButton(String text) {
            this(text, false);
        }

        // secondary = light button (used for "No" / "Cancel")
        RoundedButton(String text, boolean secondary) {
            super(text);
            this.secondary = secondary;
            setForeground(secondary ? BLUE : Color.WHITE);
            setFont(BODY.deriveFont(Font.BOLD, 16f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill = secondary ? new Color(235, 235, 250) : BLUE;
            if (getModel().isPressed()) {
                fill = secondary ? new Color(212, 212, 240) : new Color(48, 50, 160);
            } else if (getModel().isRollover()) {
                fill = secondary ? new Color(224, 224, 247) : new Color(81, 84, 211);
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();

            super.paintComponent(graphics);
        }
    }

    // round icon with a symbol inside (!, i, ?)
    private static class IconBadge extends JComponent {
        private final String symbol;
        private final Color color;

        IconBadge(String symbol, Color color) {
            this.symbol = symbol;
            this.color = color;
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setColor(color);
            g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

            g.setColor(Color.WHITE);
            g.setFont(BODY.deriveFont(Font.BOLD, 22f));
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(symbol)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(symbol, x, y);

            g.dispose();
        }
    }

    // shows a styled popup. If yesNo is true, it shows Yes / No buttons.
    // returns true if the user clicked OK or Yes
    private static boolean showDialog(Component parent, String message, String symbol,
                                      Color iconColor, boolean yesNo) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "PriorQ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        boolean[] answer = {false};

        // make the message safe to show inside html (so it can wrap)
        String safe = String.valueOf(message)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        JLabel text = new JLabel("<html><div style='width:260px'>" + safe + "</div></html>");
        text.setFont(BODY.deriveFont(Font.PLAIN, 15f));
        text.setForeground(new Color(40, 39, 61));

        JPanel badgeHolder = new JPanel(new BorderLayout());
        badgeHolder.setOpaque(false);
        badgeHolder.add(new IconBadge(symbol, iconColor), BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.add(badgeHolder, BorderLayout.WEST);
        top.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        RoundedButton ok = new RoundedButton(yesNo ? "Yes" : "OK");
        ok.setPreferredSize(new Dimension(96, 42));
        ok.addActionListener(event -> {
            answer[0] = true;
            dialog.dispose();
        });

        if (yesNo) {
            RoundedButton no = new RoundedButton("No", true);
            no.setPreferredSize(new Dimension(96, 42));
            no.addActionListener(event -> dialog.dispose());
            buttons.add(no);
        }
        buttons.add(ok);

        RoundedPanel card = new RoundedPanel(
                new BorderLayout(0, 20),
                Color.WHITE,
                new Color(220, 219, 237),
                22
        );
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));
        card.add(top, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 240, 249));
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        outer.add(card);

        // pressing Esc closes the popup
        dialog.getRootPane().registerKeyboardAction(
                (ActionEvent event) -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setContentPane(outer);
        dialog.getRootPane().setDefaultButton(ok);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return answer[0];
    }

    // type = JOptionPane.ERROR_MESSAGE, WARNING_MESSAGE, or INFORMATION_MESSAGE
    static void showMessage(Component parent, String message, int type) {
        if (type == JOptionPane.INFORMATION_MESSAGE) {
            showDialog(parent, message, "i", BLUE, false);
        } else {
            showDialog(parent, message, "!", RED, false);
        }
    }

    // Yes / No popup, returns true if the user clicked Yes
    static boolean confirm(Component parent, String message) {
        return showDialog(parent, message, "?", BLUE, true);
    }

    private static JPanel inputRow(String caption, JTextField input) {
        Color normal = new Color(250, 250, 255);
        Color focused = new Color(241, 241, 255);
        Color outline = new Color(192, 193, 216);

        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel label = new JLabel(caption);
        label.setFont(BODY.deriveFont(Font.PLAIN, 15f));
        label.setForeground(new Color(40, 39, 61));
        label.setLabelFor(input);

        RoundedPanel box = new RoundedPanel(
                new BorderLayout(),
                normal,
                outline,
                13
        );

        input.setOpaque(false);
        input.setFont(BODY.deriveFont(15f));
        input.setBorder(BorderFactory.createEmptyBorder(8, 11, 8, 11));
        input.setCaretColor(BLUE);

        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                box.setBackground(focused);
                box.setOutline(BLUE);
            }

            @Override
            public void focusLost(FocusEvent event) {
                box.setBackground(normal);
                box.setOutline(outline);
            }
        });

        box.add(input, BorderLayout.CENTER);
        row.add(label, BorderLayout.NORTH);
        row.add(box, BorderLayout.CENTER);

        return row;
    }

    static void login() {
        JFrame frame = new JFrame("PriorQ | Log in");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 240, 249));
        outer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        RoundedPanel form = new RoundedPanel(
                null,
                Color.WHITE,
                new Color(220, 219, 237),
                22
        );
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel brand = new JLabel("PriorQ");
        brand.setFont(BODY.deriveFont(Font.BOLD, 36f));
        brand.setForeground(BLUE);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(BODY.deriveFont(Font.BOLD, 22f));
        heading.setForeground(new Color(37, 36, 60));
        heading.setBorder(BorderFactory.createEmptyBorder(3, 0, 17, 0));

        JTextField name = new JTextField(23);
        JTextField email = new JTextField(23);
        JPasswordField password = new JPasswordField(23);

        JPanel nameRow = inputRow("Display name", name);
        JPanel emailRow = inputRow("Email", email);
        JPanel passwordRow = inputRow("Password", password);
        nameRow.setVisible(false);

        RoundedButton submit = new RoundedButton("Log in");
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton link = new JButton("New to PriorQ? Create account");
        link.setFont(BODY.deriveFont(15f));
        link.setForeground(BLUE);
        link.setOpaque(false);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (JComponent item : new JComponent[]{
                brand, heading, nameRow, emailRow, passwordRow, submit, link}) {
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(item);
        }

        link.addActionListener(event -> {
            boolean registering = !nameRow.isVisible();

            nameRow.setVisible(registering);

            if (registering) {
                heading.setText("Create your student account");
                submit.setText("Create account");
                link.setText("Already have an account? Log in");
            } else {
                heading.setText("Welcome back");
                submit.setText("Log in");
                link.setText("New to PriorQ? Create account");
            }

            frame.setTitle("PriorQ | " + submit.getText());
            password.setText("");
            frame.pack();

            if (registering) {
                name.requestFocusInWindow();
            } else {
                email.requestFocusInWindow();
            }
        });

        submit.addActionListener(event -> {
            char[] secret = password.getPassword();

            try {
                Account account;

                if (nameRow.isVisible()) {
                    account = Account.register(name.getText(), email.getText(), secret);
                } else {
                    account = Account.signIn(email.getText(), secret);
                }

                TaskStore store = new TaskStore(
                        account.directory.resolve("tasks.xml")
                );

                new Planner(account, store).setVisible(true);
                frame.dispose();
            } catch (Exception ex) {
                showMessage(frame, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
            } finally {
                Arrays.fill(secret, '\0');
                password.setText("");
            }
        });

        outer.add(form);
        frame.setContentPane(outer);
        frame.getRootPane().setDefaultButton(submit);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}