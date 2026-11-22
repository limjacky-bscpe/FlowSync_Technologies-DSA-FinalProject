package priorq;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

final class Planner extends JFrame {
    private final Account account;
    private final TaskStore store;

    private String page = "Dashboard";
    private Task editing;
    private LocalDate week = monday();

    private String query = "";
    private String subject = "All subjects";
    private String priority = "All priorities";
    private String deadline = "All deadlines";

    private JPanel body;
    private Color bg, surface, railColor, ink, muted, line, accent, tint;

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DAY =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter CLOCK =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter SESSION =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    Planner(Account account, TaskStore store) {
        super("PriorQ | Academic planner");
        this.account = account;
        this.store = store;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(1500, screen.width), Math.min(940, screen.height));
        setLocationRelativeTo(null);
        render();
    }

    private static LocalDate monday() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    private Color tone(int light, int dark) {
        return new Color(account.dark() ? dark : light);
    }

    private void palette() {
        bg = tone(0xFAF7F5, 0x0E1321);
        surface = tone(0xFCFBFC, 0x1B2436);
        railColor = tone(0xFAF9FB, 0x0A101C);
        ink = tone(0x101739, 0xF5F4FA);
        muted = tone(0x545A75, 0xBEC8DE);
        line = tone(0xD5D6E2, 0x43516B);
        accent = tone(0x393CC3, 0x5658DF);
        tint = tone(0xDEDFFA, 0x343F64);
    }

    private JLabel label(String text, int size, boolean bold) {
        JLabel result = new JLabel(text);
        result.putClientProperty("html.disable", true);
        result.setFont(Main.BODY.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size));
        result.setForeground(ink);
        return result;
    }

    private JPanel clear(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel column() {
        JPanel panel = clear(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private void stack(JPanel parent, JComponent item) {
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(item);
        parent.add(Box.createVerticalStrut(12));
    }

    private JPanel card(Color fill, int padding) {
        JPanel panel = new JPanel(new BorderLayout(10, 8)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(line);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    // rounded filled box with no outline (used for chips and small tiles)
    private JPanel pill(Color fill, int radius) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JButton action(String text, Color fill, Color foreground, Runnable run) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                int inset = hover ? 0 : 2;
                if (fill != null || hover) {
                    g.setColor(fill == null ? tint : fill);
                    g.fillRoundRect(inset, inset, getWidth() - 2 * inset,
                            getHeight() - 2 * inset, 14, 14);
                }
                if (hasFocus()) {
                    g.setColor(foreground);
                    g.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 12, 12);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.putClientProperty("html.disable", true);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setForeground(foreground);
        button.setFont(Main.BODY.deriveFont(Font.BOLD, 16f));
        button.setBorder(new EmptyBorder(9, 11, 9, 11));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> run.run());
        return button;
    }

    private JButton primary(String text, Runnable run) {
        return action(text, accent, Color.WHITE, run);
    }

    private JButton plain(String text, Runnable run) {
        return action(text, null, ink, run);
    }

    // button with a thin outline (used for Cancel and the week arrows)
    private JButton outline(String text, Icon icon, Runnable run) {
        JButton button = action(text, null, ink, run);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(10);
        }
        button.setBorder(new RoundBorder(line, 14, new Insets(8, 14, 8, 14)));
        return button;
    }

    private void go(String destination) {
        page = destination;
        render();
    }

    private void edit(Task task) {
        editing = task;
        go("Editor");
    }

    private void message(String text) {
        Main.showMessage(this, text, JOptionPane.INFORMATION_MESSAGE);
    }

    // thin outline with really rounded corners
    private static final class RoundBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        private final Insets pad;

        RoundBorder(Color color, int radius, Insets pad) {
            this.color = color;
            this.radius = radius;
            this.pad = pad;
        }

        @Override
        public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(pad.top, pad.left, pad.bottom, pad.right);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(pad.top, pad.left, pad.bottom, pad.right);
            return insets;
        }
    }

    // dropdown inside a rounded box (same look as the search box)
    private JPanel comboBox(JComboBox<?> combo) {
        Color fill = account.dark() ? surface : Color.WHITE;
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton(icon("drop", 18, muted));
                arrow.setBorder(new EmptyBorder(0, 6, 0, 12));
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                arrow.setOpaque(false);
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                super.paintCurrentValue(g, bounds, false);
            }
        });
        combo.setOpaque(false);
        combo.setBorder(new EmptyBorder(8, 8, 8, 0));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(line);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.dispose();
            }
        };
        box.setOpaque(false);
        box.add(combo);
        return box;
    }

    // ---------- drawn icons (simple line art on a 24 x 24 grid) ----------

    private Icon icon(String kind, int size, Color color) {
        return new Icon() {
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }

            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.scale(size / 24.0, size / 24.0);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_PURE);
                g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(color);
                switch (kind) {
                    case "home" -> {
                        g.drawPolyline(new int[]{3, 12, 21}, new int[]{11, 3, 11}, 3);
                        g.drawPolyline(new int[]{5, 5, 19, 19}, new int[]{10, 21, 21, 10}, 4);
                        g.drawPolyline(new int[]{10, 10, 14, 14}, new int[]{21, 15, 15, 21}, 4);
                    }
                    case "tasks" -> {
                        g.drawRoundRect(3, 3, 18, 18, 5, 5);
                        g.drawPolyline(new int[]{8, 11, 16}, new int[]{12, 15, 9}, 3);
                    }
                    case "calendar" -> {
                        g.drawRoundRect(3, 5, 18, 16, 4, 4);
                        g.drawLine(3, 10, 21, 10);
                        g.drawLine(8, 3, 8, 7);
                        g.drawLine(16, 3, 16, 7);
                    }
                    case "done" -> {
                        g.drawOval(3, 3, 18, 18);
                        g.drawPolyline(new int[]{8, 11, 16}, new int[]{12, 15, 9}, 3);
                    }
                    case "check" -> g.drawPolyline(new int[]{5, 10, 19}, new int[]{12, 17, 7}, 3);
                    case "list" -> {
                        for (int i = 0; i < 3; i++) {
                            int row = 6 + i * 6;
                            g.fill(new Ellipse2D.Double(3, row - 1.6, 3.2, 3.2));
                            g.drawLine(9, row, 21, row);
                        }
                    }
                    case "flag" -> {
                        g.drawLine(5, 3, 5, 21);
                        g.drawPolyline(new int[]{5, 19, 15, 19, 5}, new int[]{4, 4, 9, 14, 14}, 5);
                    }
                    case "bars" -> {
                        g.fill(new RoundRectangle2D.Double(4, 13, 4, 8, 2, 2));
                        g.fill(new RoundRectangle2D.Double(10, 8, 4, 13, 2, 2));
                        g.fill(new RoundRectangle2D.Double(16, 3, 4, 18, 2, 2));
                    }
                    case "down" -> {
                        g.drawLine(12, 4, 12, 19);
                        g.drawPolyline(new int[]{6, 12, 18}, new int[]{14, 20, 14}, 3);
                    }
                    case "layers" -> {
                        g.drawPolygon(new int[]{12, 21, 12, 3}, new int[]{3, 8, 13, 8}, 4);
                        g.drawPolyline(new int[]{3, 12, 21}, new int[]{12, 17, 12}, 3);
                        g.drawPolyline(new int[]{3, 12, 21}, new int[]{16, 21, 16}, 3);
                    }
                    case "alert" -> {
                        g.drawPolygon(new int[]{12, 22, 2}, new int[]{3, 20, 20}, 3);
                        g.drawLine(12, 9, 12, 14);
                        g.fill(new Ellipse2D.Double(11, 16, 2, 2));
                    }
                    case "clock" -> {
                        g.drawOval(3, 3, 18, 18);
                        g.drawPolyline(new int[]{12, 12, 16}, new int[]{7, 12, 14}, 3);
                    }
                    case "search" -> {
                        g.drawOval(4, 4, 12, 12);
                        g.drawLine(14, 14, 20, 20);
                    }
                    case "left" -> g.drawPolyline(new int[]{15, 8, 15}, new int[]{5, 12, 19}, 3);
                    case "right" -> g.drawPolyline(new int[]{9, 16, 9}, new int[]{5, 12, 19}, 3);
                    case "drop" -> g.drawPolyline(new int[]{6, 12, 18}, new int[]{9, 15, 9}, 3);
                    case "up" -> g.drawPolyline(new int[]{6, 12, 18}, new int[]{15, 9, 15}, 3);
                    case "plus" -> {
                        g.drawLine(12, 5, 12, 19);
                        g.drawLine(5, 12, 19, 12);
                    }
                    case "spark" -> {
                        Path2D star = new Path2D.Double();
                        star.moveTo(12, 2);
                        star.quadTo(12, 12, 22, 12);
                        star.quadTo(12, 12, 12, 22);
                        star.quadTo(12, 12, 2, 12);
                        star.quadTo(12, 12, 12, 2);
                        g.fill(star);
                    }
                    case "sun" -> {
                        g.drawOval(8, 8, 8, 8);
                        for (int i = 0; i < 8; i++) {
                            double a = Math.PI / 4 * i;
                            g.draw(new Line2D.Double(12 + Math.cos(a) * 7, 12 + Math.sin(a) * 7,
                                    12 + Math.cos(a) * 10, 12 + Math.sin(a) * 10));
                        }
                    }
                    case "moon" -> {
                        Area moon = new Area(new Ellipse2D.Double(4, 3, 17, 17));
                        moon.subtract(new Area(new Ellipse2D.Double(9, 0, 15, 15)));
                        g.fill(moon);
                    }
                    default -> { }
                }
                g.dispose();
            }
        };
    }

    // big empty / checked rounded checkbox
    private Icon squareIcon(int size, boolean checked) {
        return new Icon() {
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }

            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.scale(size / 30.0, size / 30.0);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(ink);
                g.drawRoundRect(2, 2, 25, 25, 9, 9);
                if (checked) {
                    g.drawLine(8, 15, 13, 20);
                    g.drawLine(13, 20, 22, 9);
                }
                g.dispose();
            }
        };
    }

    // green box with a white check (used for finished tasks)
    private Icon filledCheck(int size) {
        Color fill = tone(0x1F9A52, 0x3DB46D);
        return new Icon() {
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }

            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.scale(size / 24.0, size / 24.0);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(2, 2, 20, 20, 6, 6);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawPolyline(new int[]{7, 10, 17}, new int[]{12, 16, 8}, 3);
                g.dispose();
            }
        };
    }

    private String initial() {
        String name = account.name();
        return name.substring(0, name.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
    }

    private JButton avatarButton(int size, Runnable run) {
        String letter = initial();
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(getModel().isRollover() ? accent.brighter() : accent);
                g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g.setColor(Color.WHITE);
                g.setFont(Main.BODY.deriveFont(Font.BOLD, size * 0.45f));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(letter, (getWidth() - fm.stringWidth(letter)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(size, size));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> run.run());
        return button;
    }

    // sliding light / dark switch for the top bar
    private JButton themeToggle() {
        JButton toggle = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dark = account.dark();
                int w = 56, h = 30;
                int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
                g.setColor(dark ? accent : tint);
                g.fillRoundRect(x, y, w, h, h, h);
                int knob = h - 6;
                int kx = dark ? x + w - knob - 3 : x + 3;
                g.setColor(dark ? Color.WHITE : accent);
                g.fillOval(kx, y + 3, knob, knob);
                Icon glyph = icon(dark ? "moon" : "sun", 14, dark ? accent : Color.WHITE);
                glyph.paintIcon(this, g, kx + (knob - 14) / 2, y + 3 + (knob - 14) / 2);
                g.dispose();
            }
        };
        toggle.setOpaque(false);
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setPreferredSize(new Dimension(64, 40));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setToolTipText("Switch appearance");
        toggle.addActionListener(e -> {
            if (page.equals("Editor")) {
                message("Save or cancel this task before switching themes.");
                return;
            }
            saveProfile(account.name(), !account.dark());
        });
        return toggle;
    }

    private JPanel brand() {
        JPanel panel = clear(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JComponent logo = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int[] colors = {0x9CA4F1, 0x717AE6, 0x474CDD};
                for (int i = 0; i < colors.length; i++) {
                    int y = 24 - i * 7;
                    g.setColor(new Color(colors[i]));
                    g.fillPolygon(new int[]{1, 19, 37, 19},
                            new int[]{y, y - 8, y, y + 8}, 4);
                }
                g.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(39, 34));
        panel.add(logo);
        panel.add(label("PriorQ", 30, true));
        return panel;
    }

    private Icon bell() {
        return new Icon() {
            public int getIconWidth() { return 26; }
            public int getIconHeight() { return 26; }

            public void paintIcon(Component c, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.translate(x, y);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(1.7f));
                g.setColor(ink);
                Path2D shape = new Path2D.Double();
                shape.moveTo(3, 20);
                shape.quadTo(6, 17, 6, 11);
                shape.curveTo(6, 1, 20, 1, 20, 11);
                shape.quadTo(20, 17, 23, 20);
                shape.closePath();
                g.draw(shape);
                g.drawArc(10, 18, 7, 6, 190, 160);
                if (store.next() != null) {
                    g.setColor(tone(0xD64144, 0x6B70FC));
                    g.fillOval(20, 0, 6, 6);
                }
                g.dispose();
            }
        };
    }

    private JPanel header() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(account.dark() ? new Color(22, 28, 42) : railColor);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, line),
                new EmptyBorder(11, 22, 11, 16)));

        JPanel title = clear(new FlowLayout(FlowLayout.LEFT, 8, 0));
        title.add(label("PriorQ", 19, true));
        JLabel fullName = label("— Queue-Based Smart Task Scheduling and Deadline Prioritization System",
                17, false);
        fullName.setToolTipText(fullName.getText());
        title.add(fullName);
        header.add(title);

        JPanel controls = clear(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton notices = plain("", () -> {
            StringBuilder text = new StringBuilder();
            for (Task t : store.active().stream().limit(5).toList())
                text.append(t.title()).append("  ·  ").append(due(t)).append('\n');
            message(text.isEmpty() ? "Your queue is clear." : text.toString());
        });
        notices.setIcon(bell());
        notices.setToolTipText("Upcoming deadlines");
        notices.getAccessibleContext().setAccessibleName("Upcoming deadlines");
        controls.add(notices);

        JPanel divider = new JPanel();
        divider.setBackground(line);
        divider.setPreferredSize(new Dimension(1, 28));
        controls.add(divider);

        controls.add(themeToggle());

        JButton avatar = avatarButton(40, () -> go("Profile"));
        avatar.setToolTipText("Open profile");
        controls.add(avatar);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel sidebar() {
        JPanel rail = column();
        rail.setOpaque(true);
        rail.setBackground(railColor);
        rail.setPreferredSize(new Dimension(Math.max(190, Math.min(260, getWidth() / 6)), 1));
        rail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, line),
                new EmptyBorder(18, 12, 12, 12)));

        JPanel logo = brand();
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
        stack(rail, logo);
        rail.add(Box.createVerticalStrut(18));

        String[] names = {"Dashboard", "My Tasks", "Schedule", "Completed"};
        String[] kinds = {"home", "tasks", "calendar", "done"};
        for (int i = 0; i < names.length; i++) {
            String n = names[i];
            boolean selected = n.equals(page) || (page.equals("Editor") && n.equals("My Tasks"));
            Color text = selected ? Color.WHITE : ink;
            JButton nav = action(n, selected ? accent : null, text, () -> go(n));
            nav.setIcon(icon(kinds[i], 24, text));
            nav.setIconTextGap(14);
            nav.setFont(Main.BODY.deriveFont(selected ? Font.BOLD : Font.PLAIN, 18f));
            nav.setHorizontalAlignment(SwingConstants.LEFT);
            nav.setBorder(new EmptyBorder(11, 14, 11, 12));
            nav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            stack(rail, nav);
        }
        rail.add(Box.createVerticalGlue());

        JPanel divider = new JPanel();
        divider.setBackground(line);
        divider.setPreferredSize(new Dimension(1, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        stack(rail, divider);

        JPanel me = clear(new BorderLayout(10, 0));
        me.setBorder(new EmptyBorder(6, 6, 4, 6));
        me.add(avatarButton(42, () -> go("Profile")), BorderLayout.WEST);
        JPanel who = column();
        JLabel name = label(account.name(), 16, true);
        JLabel role = label("Student", 14, false);
        role.setForeground(muted);
        who.add(name);
        who.add(role);
        me.add(who);
        me.add(new JLabel(icon("drop", 20, ink)), BorderLayout.EAST);
        me.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        me.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        me.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                go("Profile");
            }
        });
        stack(rail, me);
        return rail;
    }

    private void render() {
        palette();
        JPanel shell = new JPanel(new BorderLayout());
        JPanel content = new JPanel(new BorderLayout());
        body = column();
        int margin = getWidth() >= 1300 ? 32 : 20;
        body.setBorder(new EmptyBorder(24, margin, 26, margin));

        switch (page) {
            case "My Tasks" -> tasks();
            case "Completed" -> completed();
            case "Schedule" -> schedule();
            case "Editor" -> editor();
            case "Profile" -> profile();
            default -> dashboard();
        }

        JPanel canvas = new JPanel(new BorderLayout());
        canvas.setBackground(bg);
        canvas.add(body, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        content.add(scroll);

        shell.add(sidebar(), BorderLayout.WEST);
        content.add(header(), BorderLayout.NORTH);
        shell.add(content);
        setContentPane(shell);
        revalidate();
        repaint();
    }

    private void heading(String title, String subtitle, boolean create) {
        JPanel top = clear(new BorderLayout(8, 0));
        top.add(label(title, getWidth() >= 1300 ? 40 : 33, true));
        if (create) {
            JButton add = primary("Create Task", () -> edit(null));
            add.setIcon(icon("plus", 18, Color.WHITE));
            add.setIconTextGap(8);
            top.add(add, BorderLayout.EAST);
        }
        stack(body, top);
        if (!subtitle.isBlank()) {
            JLabel description = label(subtitle, 17, false);
            description.setForeground(muted);
            stack(body, description);
        }
        body.add(Box.createVerticalStrut(5));
    }

    private JPanel section(String title, JComponent contents) {
        return section(title, null, contents);
    }

    private JPanel section(String title, String iconKind, JComponent contents) {
        JPanel panel = card(surface, 16);
        JLabel top = label(title, 22, true);
        if (iconKind != null) {
            top.setIcon(icon(iconKind, 26, accent));
            top.setIconTextGap(10);
        }
        top.setBorder(new EmptyBorder(0, 0, 6, 0));
        panel.add(top, BorderLayout.NORTH);
        panel.add(contents);
        return panel;
    }

    private JPanel stat(String iconKind, long value, String caption, int colorType) {
        int[] lightTiles = {0xE4E8FC, 0xFCEBCC, 0xFBDDE2, 0xDEF2E3};
        int[] darkTiles = {0x303A6C, 0x51432C, 0x52323A, 0x284B3B};
        int[] lightInk = {0x3744B6, 0x9D6410, 0xAD2940, 0x177440};
        int[] darkInk = {0xC0CAFF, 0xFFD075, 0xFF9CA6, 0xA3E6AE};
        JPanel panel = card(surface, 16);
        JPanel tile = pill(tone(lightTiles[colorType], darkTiles[colorType]), 14);
        tile.setPreferredSize(new Dimension(58, 58));
        JLabel mark = new JLabel(icon(iconKind, 28, tone(lightInk[colorType], darkInk[colorType])));
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        tile.add(mark);
        JPanel words = column();
        words.add(label(String.valueOf(value), 34, true));
        words.add(label(caption, 16, false));
        JPanel tileWrap = clear(new GridBagLayout());
        tileWrap.add(tile);
        panel.add(tileWrap, BorderLayout.WEST);
        panel.add(words);
        return panel;
    }

    private void stats(JPanel... cards) {
        JPanel row = clear(new GridLayout(1, cards.length, 16, 0));
        for (JPanel panel : cards) row.add(panel);
        stack(body, row);
        body.add(Box.createVerticalStrut(6));
    }

    private String due(Task t) {
        LocalDate today = LocalDate.now();
        String day = t.due().equals(today) ? "today"
                : t.due().equals(today.plusDays(1)) ? "tomorrow" : t.due().format(SHORT_DAY);
        return "Due " + day + ", " + t.dueTime().format(CLOCK);
    }

    private boolean isOverdue(Task t) {
        return LocalDateTime.of(t.due(), t.dueTime()).isBefore(LocalDateTime.now());
    }

    private long weeklyCount() {
        LocalDate start = monday();
        return store.completed().stream().map(Task::completedOn).filter(Objects::nonNull)
                .filter(d -> !d.isBefore(start) && d.isBefore(start.plusWeeks(1))).count();
    }

    private JCheckBox completeBox(Task task, int size) {
        JCheckBox box = new JCheckBox();
        box.setOpaque(false);
        box.setIcon(squareIcon(size, false));
        box.setSelectedIcon(squareIcon(size, true));
        box.setToolTipText("Mark complete");
        box.getAccessibleContext().setAccessibleName("Complete " + task.title());
        box.addActionListener(e -> {
            try {
                store.save(task.withStatus(Task.Status.DONE));
                render();
            } catch (Exception ex) {
                box.setSelected(false);
                message(ex.getMessage());
            }
        });
        return box;
    }

    private JButton taskLink(Task t) {
        JButton link = plain(t.title(), () -> edit(t));
        link.setToolTipText(t.title());
        link.setHorizontalAlignment(SwingConstants.LEFT);
        link.setBorder(new EmptyBorder(5, 2, 5, 2));
        return link;
    }

    private JPanel badge(String text, Color fill, Color foreground) {
        return badge(text, null, fill, foreground);
    }

    private JPanel badge(String text, Icon icon, Color fill, Color foreground) {
        JPanel wrap = clear(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel chip = pill(fill, 16);
        chip.setBorder(new EmptyBorder(4, 10, 4, 11));
        JLabel words = label(text, 14, true);
        words.setForeground(foreground);
        if (icon != null) {
            words.setIcon(icon);
            words.setIconTextGap(6);
        }
        chip.add(words);
        wrap.add(chip);
        return wrap;
    }

    // subject chip: same color as that subject's blocks in the schedule
    private JPanel subjectChip(String name) {
        int[][] colors = {
                {0xD7EFDD, 0x24543A, 0x186339, 0xB6ECC1},
                {0xDDE8FC, 0x2A3A63, 0x1E3FA8, 0xB6C8FF},
                {0xE6DCF8, 0x3D3260, 0x5A33A3, 0xD2C2FF},
                {0xFAE7B9, 0x54472D, 0x885006, 0xFFDA86}};
        int[] c = colors[Math.floorMod(name.hashCode(), colors.length)];
        return badge(name, tone(c[0], c[1]), tone(c[2], c[3]));
    }

    private JPanel priorityBadge(Task.Priority value, boolean withIcon) {
        Color fill = switch (value) {
            case High -> tone(0xFFD8DB, 0x573138);
            case Medium -> tone(0xFFEBC1, 0x54472D);
            case Low -> tone(0xD7EFDD, 0x24543A);
        };
        Color text = switch (value) {
            case High -> tone(0x981622, 0xFFADB0);
            case Medium -> tone(0x885006, 0xFFDA86);
            case Low -> tone(0x186339, 0xB6ECC1);
        };
        String kind = switch (value) {
            case High -> "flag";
            case Medium -> "bars";
            case Low -> "down";
        };
        return badge(value.name(), withIcon ? icon(kind, 16, text) : null, fill, text);
    }

    private JLabel dueLabel(String text, int size) {
        JLabel result = label(text, size, false);
        result.setIcon(icon("calendar", size + 4, ink));
        result.setIconTextGap(8);
        return result;
    }

    // one task box on the dashboard: number, color bar, title, chips, checkbox
    private JPanel queueRow(int rank, Task t) {
        Color barColor = switch (t.priority()) {
            case High -> tone(0xEF4444, 0xFF6B6B);
            case Medium -> tone(0xF5A623, 0xFFC247);
            case Low -> tone(0x3B6FF0, 0x6C93FF);
        };

        JPanel number = pill(tone(0xE9E8F8, 0x343F64), 12);
        number.setPreferredSize(new Dimension(44, 44));
        JLabel numberText = label(String.valueOf(rank), 20, true);
        numberText.setHorizontalAlignment(SwingConstants.CENTER);
        number.add(numberText);
        JPanel numberWrap = clear(new GridBagLayout());
        numberWrap.add(number);

        JComponent bar = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(barColor);
                g.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 6, 6);
                g.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(6, 1));

        JPanel left = clear(new BorderLayout(12, 0));
        left.add(numberWrap, BorderLayout.WEST);
        left.add(bar);

        JButton title = taskLink(t);
        title.setFont(Main.BODY.deriveFont(Font.BOLD, 18f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chips = clear(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(subjectChip(t.subject()));
        chips.add(priorityBadge(t.priority(), true));
        JLabel divider = label("|", 18, false);
        divider.setForeground(muted);
        chips.add(divider);
        JLabel when = dueLabel(due(t), 15);
        if (isOverdue(t)) when.setForeground(tone(0xC62828, 0xFF8A8A));
        chips.add(when);

        JPanel words = column();
        words.add(title);
        words.add(Box.createVerticalStrut(4));
        words.add(chips);

        JPanel boxWrap = clear(new GridBagLayout());
        boxWrap.add(completeBox(t, 30));

        JPanel row = card(surface, 10);
        row.add(left, BorderLayout.WEST);
        row.add(words);
        row.add(boxWrap, BorderLayout.EAST);
        return row;
    }

    // one study session in "Today's Schedule"
    private JPanel sessionRow(Task t) {
        JPanel row = clear(new BorderLayout(10, 0));
        JPanel time = pill(tone(0xE9E8F8, 0x343F64), 12);
        time.setBorder(new EmptyBorder(6, 9, 6, 9));
        time.add(label(t.study().format(CLOCK), 14, true));
        JPanel timeWrap = clear(new GridBagLayout());
        timeWrap.add(time);

        JPanel session = pill(tone(0xE6E7FB, 0x2B3658), 14);
        session.setBorder(new EmptyBorder(8, 12, 8, 12));
        JPanel words = column();
        JButton title = taskLink(t);
        title.setFont(Main.BODY.deriveFont(Font.BOLD, 15f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 2, 0));
        JLabel sub = label(t.subject(), 13, false);
        sub.setForeground(muted);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        words.add(title);
        words.add(sub);
        session.add(words);

        row.add(timeWrap, BorderLayout.WEST);
        row.add(session);
        return row;
    }

    private void dashboard() {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good Morning" : hour < 18 ? "Good Afternoon" : "Good Evening";
        heading(greeting + ", " + account.name(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), true);
        stack(body, label("Your task queue", 35, true));
        stack(body, label("Stay on top of deadlines, one task at a time.", 17, false));

        List<Task> active = store.active();
        long overdue = active.stream().filter(this::isOverdue).count();
        long dueToday = active.stream().filter(t -> t.due().equals(LocalDate.now())).count();
        stats(stat("list", active.size(), "Active tasks", 0),
                stat("calendar", dueToday, "Due today", 1),
                stat("alert", overdue, "Overdue", 2),
                stat("done", weeklyCount(), "Completed this week", 3));

        JPanel queue = column();
        int rank = 1;
        for (Task t : active.stream().limit(5).toList()) stack(queue, queueRow(rank++, t));
        if (active.isEmpty()) stack(queue, label("Your queue is clear.", 16, false));
        JPanel queueTop = clear(new BorderLayout());
        queueTop.add(queue, BorderLayout.NORTH);

        JPanel todayPanel = column();
        List<Task> today = store.schedule().stream()
                .filter(t -> t.study().toLocalDate().equals(LocalDate.now())).toList();
        for (Task t : today) stack(todayPanel, sessionRow(t));
        if (today.isEmpty()) stack(todayPanel, label("No study sessions today.", 15, false));

        JPanel right = column();
        stack(right, section("Today’s Schedule", "calendar", todayPanel));
        JPanel plan = column();
        stack(plan, label(today.stream().mapToInt(Task::minutes).sum()
                + " study minutes planned today.", 15, false));
        JButton view = primary("View schedule  →", () -> go("Schedule"));
        view.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        stack(plan, view);
        stack(right, section("Smart scheduling", "spark", plan));
        JPanel rightHolder = clear(new BorderLayout());
        rightHolder.add(right, BorderLayout.NORTH);
        rightHolder.setPreferredSize(new Dimension(getWidth() >= 1300 ? 390 : 300, 1));

        JPanel split = clear(new BorderLayout(15, 0));
        split.add(section("TASK", "layers", queueTop));
        split.add(rightHolder, BorderLayout.EAST);
        stack(body, split);
    }

    private <T extends JComponent> T input(T field) {
        field.setFont(Main.BODY);
        field.setBackground(account.dark() ? surface : Color.WHITE);
        field.setForeground(ink);
        if (field instanceof javax.swing.text.JTextComponent text) {
            text.setCaretColor(ink);
            text.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(line, 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
        }
        return field;
    }

    // text field that shows grey hint text while it is empty
    private JTextField hintField(String text, String hint) {
        JTextField field = new JTextField(text, 12) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (!getText().isEmpty()) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(muted);
                g.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g.getFontMetrics();
                int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(hint, in.left, y);
                g.dispose();
            }
        };
        return input(field);
    }

    // text field inside a rounded box with an icon on the left or right
    private JPanel iconField(JTextField field, Icon icon, boolean left) {
        Color fill = account.dark() ? surface : Color.WHITE;
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(line);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.dispose();
            }
        };
        box.setOpaque(false);
        field.setBorder(new EmptyBorder(8, icon == null ? 12 : left ? 4 : 10, 8,
                icon == null ? 12 : left ? 10 : 4));
        field.setOpaque(false);
        box.add(field);
        if (icon != null) {
            JLabel mark = new JLabel(icon);
            mark.setBorder(new EmptyBorder(0, left ? 12 : 0, 0, left ? 0 : 12));
            box.add(mark, left ? BorderLayout.WEST : BorderLayout.EAST);
        }
        return box;
    }

    // any component inside a rounded box (same look as the search box)
    private JPanel roundBox(JComponent inner) {
        Color fill = account.dark() ? surface : Color.WHITE;
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(line);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.dispose();
            }
        };
        box.setOpaque(false);
        box.add(inner);
        return box;
    }

    // caption text; a trailing " *" turns into a red star
    private JComponent caption(String text) {
        if (!text.endsWith(" *")) return label(text, 15, true);
        JPanel row = clear(new FlowLayout(FlowLayout.LEFT, 3, 0));
        row.add(label(text.substring(0, text.length() - 2), 15, true));
        JLabel star = label("*", 15, true);
        star.setForeground(tone(0xD64144, 0xFF6B6B));
        row.add(star);
        return row;
    }

    private JPanel field(String name, JComponent control) {
        JPanel panel = clear(new BorderLayout(0, 5));
        panel.add(caption(name), BorderLayout.NORTH);
        panel.add(control);
        return panel;
    }

    private JPanel tableRow(boolean header, double[] weights, JComponent... cells) {
        JPanel panel = clear(new GridBagLayout());
        if (header) {
            panel.setOpaque(true);
            panel.setBackground(tone(0xEDEEF5, 0x222D41));
        }
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, line),
                new EmptyBorder(10, 9, 10, 9)));
        for (int i = 0; i < cells.length; i++) {
            JComponent cell = cells[i];
            int height = Math.max(25, cell.getPreferredSize().height);
            cell.setMinimumSize(new Dimension(0, height));
            cell.setPreferredSize(new Dimension(0, height));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.gridy = 0;
            c.weightx = weights[i];
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 4, 0, 4);
            panel.add(cell, c);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel tableBox(JPanel rows) {
        JPanel panel = card(surface, 2);
        panel.add(rows, BorderLayout.NORTH);
        return panel;
    }

    private JLabel headerCell(String text, int indent) {
        JLabel cell = label(text, 16, true);
        cell.setBorder(new EmptyBorder(0, indent, 0, 0));
        return cell;
    }

    private void tasks() {
        heading("My Tasks", "Here are your queued tasks and deadlines.", true);
        List<Task> active = store.active();

        JTextField search = hintField(query, "Search tasks…");
        JPanel searchBox = iconField(search, icon("search", 20, muted), true);
        JComboBox<String> subjects = input(new JComboBox<>());
        subjects.addItem("All subjects");
        active.stream().map(Task::subject).distinct().sorted().forEach(subjects::addItem);
        subjects.setSelectedItem(subject);

        JComboBox<String> levels = input(new JComboBox<>(
                new String[]{"All priorities", "High", "Medium", "Low"}));
        levels.setSelectedItem(priority);
        JComboBox<String> dates = input(new JComboBox<>(
                new String[]{"All deadlines", "Due today", "This week", "Overdue"}));
        dates.setSelectedItem(deadline);

        JPanel combos = clear(new GridLayout(1, 3, 12, 0));
        combos.add(comboBox(subjects));
        combos.add(comboBox(levels));
        combos.add(comboBox(dates));
        combos.setPreferredSize(new Dimension(600, 44));
        JPanel filters = clear(new BorderLayout(12, 0));
        filters.add(searchBox);
        filters.add(combos, BorderLayout.EAST);
        stack(body, filters);
        body.add(Box.createVerticalStrut(6));

        LocalDate today = LocalDate.now();
        long soon = active.stream().filter(t -> !t.due().isBefore(today)
                && !t.due().isAfter(today.plusDays(3))).count();
        long high = active.stream().filter(t -> t.priority() == Task.Priority.High).count();
        stats(stat("list", active.size(), "Total Tasks", 0), stat("clock", soon, "Due Soon", 1),
                stat("flag", high, "High Priority", 2));

        JPanel rows = column();
        stack(body, tableBox(rows));
        Runnable filter = () -> {
            query = search.getText();
            subject = String.valueOf(subjects.getSelectedItem());
            priority = String.valueOf(levels.getSelectedItem());
            deadline = String.valueOf(dates.getSelectedItem());
            rows.removeAll();

            double[] widths = {.39, .23, .15, .23};
            rows.add(tableRow(true, widths, headerCell("Task", 34),
                    headerCell("Subject", 0), headerCell("Priority", 0),
                    headerCell("Deadline", 0)));
            int matches = 0;
            for (Task t : active) {
                String words = (t.title() + " " + t.subject()).toLowerCase(Locale.ROOT);
                if (!words.contains(query.strip().toLowerCase(Locale.ROOT))) continue;
                if (!subject.equals("All subjects") && !subject.equals(t.subject())) continue;
                if (!priority.equals("All priorities") && !priority.equals(t.priority().name())) continue;

                boolean include = switch (deadline) {
                    case "Due today" -> t.due().equals(today);
                    case "This week" -> !t.due().isBefore(monday())
                            && t.due().isBefore(monday().plusWeeks(1));
                    case "Overdue" -> isOverdue(t);
                    default -> true;
                };
                if (!include) continue;

                JPanel title = clear(new BorderLayout(6, 0));
                title.add(completeBox(t, 24), BorderLayout.WEST);
                title.add(taskLink(t));
                JComponent when;
                if (account.dark()) {
                    boolean now = t.due().equals(today);
                    boolean next = t.due().equals(today.plusDays(1));
                    String text = now ? "Due today" : next ? "Due tomorrow" : "Due " + t.due().format(SHORT_DAY);
                    when = badge(text, icon("calendar", 16, ink), now ? new Color(82, 49, 57) : next
                            ? new Color(81, 68, 44) : new Color(49, 59, 80), ink);
                } else {
                    when = dueLabel(t.due().format(DAY), 15);
                    if (isOverdue(t)) when.setForeground(tone(0xC62828, 0xFF8A8A));
                }
                when.setToolTipText(due(t));
                rows.add(tableRow(false, widths, title, subjectChip(t.subject()),
                        priorityBadge(t.priority(), true), when));
                matches++;
            }
            if (matches == 0) rows.add(label("   No tasks match these filters.", 15, false));
            rows.revalidate();
            rows.repaint();
        };

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter.run(); }
            public void removeUpdate(DocumentEvent e) { filter.run(); }
            public void changedUpdate(DocumentEvent e) { filter.run(); }
        });
        subjects.addActionListener(e -> filter.run());
        levels.addActionListener(e -> filter.run());
        dates.addActionListener(e -> filter.run());
        filter.run();
    }

    private void completed() {
        heading("Completed Tasks", "A record of the tasks you’ve finished.", false);
        List<Task> done = store.completed();
        stats(stat("done", weeklyCount(), "Completed this week", 3),
                stat("layers", done.size(), "Total completed", 0));

        JPanel rows = column();
        double[] widths = {.38, .24, .23, .15};
        rows.add(tableRow(true, widths, headerCell("Task", 34), headerCell("Subject", 0),
                headerCell("Completion Date", 0), headerCell("Status", 0)));
        Color doneText = tone(0x166333, 0xCEF1D6);
        for (Task t : done) {
            JPanel title = clear(new BorderLayout(8, 0));
            title.add(new JLabel(filledCheck(26)), BorderLayout.WEST);
            title.add(taskLink(t));
            JComponent date = t.completedOn() == null ? label("Unrecorded", 15, false)
                    : dueLabel(t.completedOn().format(DAY), 15);
            rows.add(tableRow(false, widths, title, subjectChip(t.subject()), date,
                    badge("Completed", icon("check", 16, doneText),
                            tone(0xD4EFDB, 0x285238), doneText)));
        }
        if (done.isEmpty()) rows.add(label("   No completed tasks yet.", 15, false));
        stack(body, tableBox(rows));
    }

    private void schedule() {
        heading("Schedule",
                "Plan and view your tasks on a weekly calendar with a prioritized, time-blocked schedule.",
                true);

        JPanel nav = clear(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton back = outline("", icon("left", 18, ink), () -> { week = week.minusWeeks(1); render(); });
        back.setToolTipText("Previous week");
        JButton thisWeek = outline(week.format(SHORT_DAY) + " – " + week.plusDays(6).format(DAY),
                icon("calendar", 20, ink), () -> { week = monday(); render(); });
        thisWeek.setToolTipText("Back to this week");
        JButton forward = outline("", icon("right", 18, ink), () -> { week = week.plusWeeks(1); render(); });
        forward.setToolTipText("Next week");
        nav.add(back);
        nav.add(thisWeek);
        nav.add(forward);
        stack(body, nav);

        JPanel list = column();
        JLabel hint = label("Nearest deadlines first.", 14, false);
        hint.setForeground(muted);
        stack(list, hint);
        for (Task t : store.active().stream().limit(5).toList()) {
            JPanel entry = card(tone(0xF8F8FB, 0x202A3C), 10);
            JPanel top = clear(new BorderLayout(10, 0));
            top.add(priorityBadge(t.priority(), false), BorderLayout.WEST);
            top.add(taskLink(t));
            entry.add(top);
            JLabel when = dueLabel(due(t), 13);
            if (isOverdue(t)) when.setForeground(tone(0xC62828, 0xFF8A8A));
            entry.add(when, BorderLayout.SOUTH);
            stack(list, entry);
        }
        if (store.active().isEmpty()) stack(list, label("No upcoming deadlines.", 15, false));
        JPanel aside = section("Upcoming deadlines", list);
        aside.setPreferredSize(new Dimension(Math.max(230, Math.min(330, getWidth() / 5)),
                aside.getPreferredSize().height));
        JPanel holder = clear(new BorderLayout());
        holder.add(aside, BorderLayout.NORTH);
        JPanel split = clear(new BorderLayout(14, 0));
        split.add(calendar());
        split.add(holder, BorderLayout.EAST);
        stack(body, split);
    }

    private record Block(Task task, int day, int start, int end, int lane, int lanes, boolean due) {}

    // something drawn on the calendar: a study session or a deadline marker
    private record Item(Task task, LocalDateTime start, int minutes, boolean due) {}

    private List<Block> blocks() {
        List<Item> items = new ArrayList<>();
        for (Task t : store.schedule()) items.add(new Item(t, t.study(), t.minutes(), false));
        // every unfinished task also gets a 30 minute marker that ends at its due time
        for (Task t : store.active())
            items.add(new Item(t, LocalDateTime.of(t.due(), t.dueTime()).minusMinutes(30), 30, true));
        items.sort(Comparator.comparing(Item::start));

        List<Block> result = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            LocalDateTime midnight = week.plusDays(d).atStartOfDay();
            List<Integer> ends = new ArrayList<>();
            List<Block> group = new ArrayList<>();
            for (Item item : items) {
                LocalDateTime finish = item.start().plusMinutes(item.minutes());
                if (!finish.isAfter(midnight) || !item.start().isBefore(midnight.plusDays(1))) continue;
                int start = (int)Math.max(0L, Duration.between(midnight, item.start()).toMinutes());
                int end = (int)Math.min(1440L, Duration.between(midnight, finish).toMinutes());
                if (!ends.isEmpty() && Collections.max(ends) <= start) {
                    finishGroup(result, group, ends.size());
                    ends.clear();
                    group.clear();
                }
                int lane = 0;
                while (lane < ends.size() && ends.get(lane) > start) lane++;
                if (lane == ends.size()) ends.add(end);
                else ends.set(lane, end);
                group.add(new Block(item.task(), d, start, end, lane, 1, item.due()));
            }
            finishGroup(result, group, ends.size());
        }
        return result;
    }

    private void finishGroup(List<Block> result, List<Block> group, int count) {
        for (Block b : group)
            result.add(new Block(b.task(), b.day(), b.start(), b.end(), b.lane(), count, b.due()));
    }

    // "9 AM" or "9:30 AM" (short, so it fits inside a schedule block)
    private String compactTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern(
                time.getMinute() == 0 ? "h a" : "h:mm a", Locale.ENGLISH));
    }

    private JPanel calendar() {
        List<Block> blocks = blocks();
        int first = Math.min(6, blocks.stream().mapToInt(b -> b.start() / 60).min().orElse(24));
        int end = Math.max(24, blocks.stream().mapToInt(b -> (b.end() + 59) / 60).max().orElse(0));
        final int row = 38, head = 52, gutter = 80;

        JPanel grid = new JPanel(null) {
            private int edge(int day) {
                return gutter + (getWidth() - gutter) * day / 7;
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Shape frame = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(surface);
                g.fill(frame);
                g.clip(frame);
                g.setColor(tone(0xEFF0F6, 0x222C40));
                g.fillRect(0, 0, getWidth(), head);
                g.setColor(ink);
                g.setFont(Main.BODY.deriveFont(Font.BOLD, 15f));
                FontMetrics bold = g.getFontMetrics();
                g.drawString("Time", (gutter - bold.stringWidth("Time")) / 2, 31);
                for (int d = 0; d < 7; d++) {
                    LocalDate day = week.plusDays(d);
                    int mid = (edge(d) + edge(d + 1)) / 2;
                    g.setFont(Main.BODY.deriveFont(Font.BOLD, 15f));
                    String name = day.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH));
                    g.drawString(name, mid - g.getFontMetrics().stringWidth(name) / 2, 21);
                    g.setFont(Main.BODY.deriveFont(13f));
                    String date = day.format(SHORT_DAY);
                    g.drawString(date, mid - g.getFontMetrics().stringWidth(date) / 2, 40);
                }
                g.setFont(Main.BODY.deriveFont(13f));
                FontMetrics small = g.getFontMetrics();
                for (int h = first; h < end; h++) {
                    int y = head + (h - first) * row;
                    g.setColor(line);
                    g.drawLine(0, y, getWidth(), y);
                    g.setColor(ink);
                    String hour = LocalTime.of(h, 0).format(CLOCK);
                    g.drawString(hour, (gutter - small.stringWidth(hour)) / 2, y + row / 2 + 5);
                }
                g.setColor(line);
                for (int d = 0; d <= 7; d++) g.drawLine(edge(d), 0, edge(d), getHeight());
                g.setClip(null);
                g.setColor(line);
                g.draw(frame);
                g.dispose();
            }

            @Override
            public void doLayout() {
                for (Component child : getComponents()) {
                    JButton button = (JButton)child;
                    Block b = (Block)button.getClientProperty("block");
                    int width = edge(b.day() + 1) - edge(b.day());
                    int x = edge(b.day()) + width * b.lane() / b.lanes();
                    int right = edge(b.day()) + width * (b.lane() + 1) / b.lanes();
                    button.setBounds(x + 2, head + (b.start() - first * 60) * row / 60 + 1,
                            Math.max(1, right - x - 4), Math.max(26, (b.end() - b.start()) * row / 60 - 2));
                }
            }
        };
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(495, head + (end - first) * row + 10));

        int[] lights = {0xD2EDD8, 0xD8E8FC, 0xE2DAF8, 0xFAE7B9};
        int[] darks = {0x74BA94, 0x8C9CE4, 0xAE94D3, 0xE7BD79};
        for (Block b : blocks) {
            Task t = b.task();
            String times = b.due() ? "Deadline " + t.dueTime().format(CLOCK)
                    : t.study().format(CLOCK) + " – " + t.study().plusMinutes(t.minutes()).format(CLOCK);
            String safe = t.title().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            String compact = b.due() ? "" : compactTime(t.study().toLocalTime()) + " – "
                    + compactTime(t.study().plusMinutes(t.minutes()).toLocalTime());
            boolean tall = !b.due() && b.end() - b.start() >= 75;
            // short blocks use one plain line (it ends with "..." instead of being cut off)
            String words = tall ? "<html><b>" + safe + "</b><br>" + compact + "</html>"
                    : b.due() ? "Due: " + t.title() : t.title();
            JButton button = new JButton(words) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(getBackground());
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    if (b.due()) {
                        g.setColor(getBackground().darker());
                        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    }
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            int color = Math.floorMod(t.subject().hashCode(), lights.length);
            if (b.due()) {
                // deadline markers use the priority color: red = High, amber = Medium, green = Low
                button.setBackground(switch (t.priority()) {
                    case High -> tone(0xFAD0D5, 0xE98A93);
                    case Medium -> tone(0xFAE7B9, 0xE7BD79);
                    case Low -> tone(0xD2EDD8, 0x74BA94);
                });
            } else {
                button.setBackground(tone(lights[color], darks[color]));
            }
            button.setForeground(new Color(22, 27, 47));
            button.setFont(Main.BODY.deriveFont(tall ? Font.PLAIN : Font.BOLD, 11f));
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(3, 6, 2, 3));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setVerticalAlignment(SwingConstants.TOP);
            button.setToolTipText(t.title() + " · " + times);
            button.putClientProperty("block", b);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> edit(t));
            grid.add(button);
        }
        return grid;
    }

    private JPanel editorField(String name, JComponent control) {
        if (!account.dark()) return field(name, control);
        JPanel row = clear(new BorderLayout(12, 0));
        JComponent title = caption(name);
        title.setPreferredSize(new Dimension(170, 34));
        row.add(title, BorderLayout.WEST);
        row.add(control);
        return row;
    }

    private void editor() {
        Task t = editing;
        if (account.dark()) stack(body, label(t == null ? "CREATE TASK" : "EDIT TASK", 14, true));
        heading(t == null ? "Create a new task" : "Edit task",
                "Add a task to your queue with a subject, priority, and deadline.", false);

        JPanel form = column();
        JTextField title = hintField(t == null ? "" : t.title(), "e.g., Review Nutrition in the Life Stages");
        stack(form, editorField("Task title *", iconField(title, null, false)));

        JComboBox<String> course = input(new JComboBox<>());
        course.setEditable(true);
        java.util.stream.Stream.concat(store.active().stream(), store.completed().stream())
                .map(Task::subject).distinct().sorted().forEach(course::addItem);
        course.setSelectedItem(t == null ? "" : t.subject());
        JPanel courseBox = comboBox(course);
        if (course.getEditor().getEditorComponent() instanceof JTextField text) {
            input(text);
            text.setBorder(new EmptyBorder(0, 4, 0, 4));
            text.setOpaque(false);
        }

        JTextArea notes = input(new JTextArea(t == null ? "" : t.description(), 3, 22));
        notes.setOpaque(false);
        notes.setBorder(new EmptyBorder(8, 12, 8, 12));
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notes);
        notesScroll.setBorder(null);
        notesScroll.setOpaque(false);
        notesScroll.getViewport().setOpaque(false);
        JPanel notesBox = roundBox(notesScroll);

        ButtonGroup group = new ButtonGroup();
        JPanel choices = clear(new GridLayout(1, 3, 10, 0));
        for (Task.Priority p : new Task.Priority[]{Task.Priority.Low, Task.Priority.Medium, Task.Priority.High}) {
            JRadioButton choice = new JRadioButton();
            choice.setSelected(p == (t == null ? Task.Priority.Medium : t.priority()));
            choice.setActionCommand(p.name());
            choice.setOpaque(false);
            choice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            choice.getAccessibleContext().setAccessibleName(p.name());
            group.add(choice);
            JPanel option = card(surface, 6);
            option.add(choice, BorderLayout.WEST);
            option.add(priorityBadge(p, false));
            option.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            option.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    choice.doClick();
                }
            });
            choices.add(option);
        }

        if (account.dark()) {
            stack(form, editorField("Subject *", courseBox));
            stack(form, editorField("Description", notesBox));
            stack(form, editorField("Priority *", choices));
        } else {
            JPanel pair = clear(new GridLayout(1, 2, 16, 0));
            pair.add(field("Subject *", courseBox));
            pair.add(field("Priority *", choices));
            stack(form, pair);
            stack(form, field("Description", notesBox));
        }

        JTextField date = input(new JTextField((t == null ? LocalDate.now() : t.due()).toString()));
        // due time dropdown: every 30 minutes, plus 11:59 PM
        List<LocalTime> slots = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            slots.add(LocalTime.of(h, 0));
            slots.add(LocalTime.of(h, 30));
        }
        slots.add(LocalTime.of(23, 59));
        LocalTime current = t == null ? LocalTime.of(23, 59) : t.dueTime();
        if (!slots.contains(current)) {
            slots.add(current);
            Collections.sort(slots);
        }
        JComboBox<String> time = input(new JComboBox<>());
        for (LocalTime slot : slots) time.addItem(slot.format(CLOCK));
        time.setSelectedItem(current.format(CLOCK));
        date.setToolTipText("YYYY-MM-DD");
        JPanel dateBox = iconField(date, icon("calendar", 20, muted), false);
        JPanel timeBox = comboBox(time);
        if (account.dark()) {
            stack(form, editorField("Due date *", dateBox));
            stack(form, editorField("Due time *", timeBox));
        } else {
            JPanel dates = clear(new GridLayout(1, 2, 12, 0));
            dates.add(field("Due date *", dateBox));
            dates.add(field("Due time *", timeBox));
            stack(form, dates);
        }

        JTextField study = hintField(t == null || t.study() == null
                ? "" : t.study().format(SESSION), "YYYY-MM-DD HH:mm, for example 2026-11-23 09:00");
        JComboBox<Task.Kind> kind = input(new JComboBox<>(Task.Kind.values()));
        JComboBox<Task.Status> status = input(new JComboBox<>(Task.Status.values()));
        if (t != null) {
            kind.setSelectedItem(t.kind());
            status.setSelectedItem(t.status());
        }

        if (account.dark()) {
            stack(form, editorField("Task type", comboBox(kind)));
            stack(form, editorField("Progress", comboBox(status)));
            stack(form, editorField("Study session",
                    iconField(study, icon("calendar", 20, muted), false)));
        } else {
            JPanel more = clear(new GridLayout(1, 2, 12, 0));
            more.add(field("Task type", comboBox(kind)));
            more.add(field("Progress", comboBox(status)));
            stack(form, more);
            stack(form, field("Study session (optional)",
                    iconField(study, icon("calendar", 20, muted), false)));
        }

        JPanel actions = clear(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton save = primary(t == null ? "Add to queue" : "Save changes", () -> {
            try {
                Task.Status progress = (Task.Status)status.getSelectedItem();
                Task saved = new Task(t == null ? store.nextId() : t.id(), title.getText(),
                        Objects.toString(course.getEditor().getItem(), ""),
                        (Task.Kind)kind.getSelectedItem(), LocalDate.parse(date.getText().strip()),
                        Task.Priority.valueOf(group.getSelection().getActionCommand()),
                        t == null ? Task.Status.TODO : t.status(),
                        study.getText().isBlank() ? null : LocalDateTime.parse(study.getText().strip(), SESSION),
                        notes.getText(), LocalTime.parse(String.valueOf(time.getSelectedItem()), CLOCK),
                        t == null ? 60 : t.minutes(), t == null ? null : t.completedOn());
                store.save(saved.withStatus(progress));
                query = "";
                subject = "All subjects";
                priority = "All priorities";
                deadline = "All deadlines";
                go(progress == Task.Status.DONE ? "Completed" : "My Tasks");
            } catch (DateTimeParseException ex) {
                message("Use a date like 2026-11-24. "
                        + "For a study session, enter both date and time, for example 2026-11-23 09:00.");
            } catch (Exception ex) {
                message(ex.getMessage());
            }
        });
        if (t == null) {
            save.setIcon(icon("plus", 18, Color.WHITE));
            save.setIconTextGap(8);
        }
        actions.add(save);
        actions.add(outline("Cancel", null, () -> go("My Tasks")));
        if (t != null) actions.add(plain("Delete task", () -> {
            if (!Main.confirm(this, "Delete this task?")) return;
            try {
                store.delete(t.id());
                go("My Tasks");
            } catch (Exception ex) {
                message(ex.getMessage());
            }
        }));
        stack(form, actions);
        JPanel panel = card(surface, 23);
        panel.add(form);
        stack(body, panel);
    }

    private void profile() {
        heading("Your profile", account.email, false);
        JPanel form = column();
        JTextField name = input(new JTextField(account.name()));
        stack(form, field("Display name", iconField(name, null, false)));
        JCheckBox dark = new JCheckBox("Dark mode", account.dark());
        dark.setOpaque(false);
        dark.setForeground(ink);
        stack(form, dark);
        stack(form, primary("Save settings", () -> saveProfile(name.getText(), dark.isSelected())));
        stack(form, plain("Log out", () -> {
            Main.login();
            dispose();
        }));
        stack(body, section("Profile settings", form));
    }

    private void saveProfile(String name, boolean dark) {
        try {
            account.saveProfile(name, dark);
            render();
        } catch (Exception ex) {
            message(ex.getMessage());
        }
    }
}