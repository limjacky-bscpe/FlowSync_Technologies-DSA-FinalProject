package priorq;

import javax.swing.*;
import java.awt.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Arrays;

public class Main {
    static final Path HOME = Path.of(System.getProperty("user.home"), ".priorq");
    static final Color BLUE = new Color(65, 68, 197);
    static final Font BODY = new Font(
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()).contains("IBM Plex Serif")
                    ? "IBM Plex Serif" : "Serif", Font.PLAIN, 16);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Files.createDirectories(HOME);
                FileChannel channel = FileChannel.open(HOME.resolve("session.lock"),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    channel.close();
                    throw new IllegalStateException("PriorQ is already open.");
                }
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        lock.release();
                        channel.close();
                    } catch (Exception ignored) {}
                }));
                for (Object key : UIManager.getDefaults().keySet().toArray()) {
                    if (UIManager.get(key) instanceof Font) UIManager.put(key, BODY);
                }
                login();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "PriorQ",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static void login() {
        JFrame frame = new JFrame("PriorQ | Log in");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 240, 249));
        outer.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));

        JLabel brand = new JLabel("PriorQ");
        brand.setFont(BODY.deriveFont(Font.BOLD, 36f));
        brand.setForeground(BLUE);
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(BODY.deriveFont(Font.BOLD, 22f));
        JLabel nameLabel = new JLabel("Display name");
        JTextField name = new JTextField(23);
        JTextField email = new JTextField(23);
        JPasswordField password = new JPasswordField(23);
        JButton submit = new JButton("Log in");
        submit.setForeground(Color.WHITE);
        submit.setBackground(BLUE);
        JButton link = new JButton("New to PriorQ? Create account");
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setForeground(BLUE);

        for (JComponent item : new JComponent[]{brand, heading, nameLabel, name,
                new JLabel("Email"), email, new JLabel("Password"), password, submit, link}) {
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(item);
            form.add(Box.createVerticalStrut(5));
        }
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        for (JButton button : new JButton[]{submit, link})
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        nameLabel.setVisible(false);
        name.setVisible(false);
        link.addActionListener(e -> {
            boolean register = !name.isVisible();
            nameLabel.setVisible(register);
            name.setVisible(register);
            heading.setText(register ? "Create your student account" : "Welcome back");
            submit.setText(register ? "Create account" : "Log in");
            link.setText(register ? "Already have an account? Log in" : "New to PriorQ? Create account");
            frame.setTitle("PriorQ | " + submit.getText());
            password.setText("");
            frame.pack();
            (register ? name : email).requestFocusInWindow();
        });

        submit.addActionListener(e -> {
            char[] secret = password.getPassword();
            try {
                Account account = name.isVisible()
                        ? Account.register(name.getText(), email.getText(), secret)
                        : Account.signIn(email.getText(), secret);
                TaskStore store = new TaskStore(account.directory.resolve("tasks.xml"));
                new Planner(account, store).setVisible(true);
                frame.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "PriorQ",
                        JOptionPane.WARNING_MESSAGE);
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