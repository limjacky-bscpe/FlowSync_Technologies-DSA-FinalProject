package priorq;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
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

    private void go(String destination) {
        page = destination;
        render();
    }

    private void edit(Task task) {
        editing = task;
        go("Editor");
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text, "PriorQ", JOptionPane.INFORMATION_MESSAGE);
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
                new EmptyBorder(9, 18, 9, 14)));

        JPanel title = clear(new BorderLayout(8, 0));
        title.add(account.dark() ? label("PriorQ", 25, true) : brand(), BorderLayout.WEST);
        JLabel fullName = label("— Queue-Based Smart Task Scheduling and Deadline Prioritization System",
                17, false);
        fullName.setToolTipText(fullName.getText());
        title.add(fullName);
        header.add(title);

        JPanel controls = clear(new FlowLayout(FlowLayout.RIGHT, 6, 0));
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

        JButton theme = action(account.dark() ? "☾   ●" : "☼   ☾",
                account.dark() ? accent : tint, account.dark() ? Color.WHITE : ink, () -> {
                    if (page.equals("Editor")) {
                        message("Save or cancel this task before switching themes.");
                        return;
                    }
                    saveProfile(account.name(), !account.dark());
                });
        theme.setToolTipText("Switch appearance");
        controls.add(theme);

        String name = account.name();
        String initial = name.substring(0, name.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
        JButton avatar = primary(initial, () -> go("Profile"));
        avatar.setToolTipText("Open profile");
        controls.add(avatar);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel sidebar() {
        JPanel rail = column();
        rail.setOpaque(true);
        rail.setBackground(railColor);
        rail.setPreferredSize(new Dimension(Math.max(165, Math.min(245, getWidth() / 6)), 1));
        rail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, line),
                new EmptyBorder(18, 10, 15, 10)));

        if (account.dark()) {
            JPanel logo = brand();
            logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
            stack(rail, logo);
            rail.add(Box.createVerticalStrut(22));
        }

        String[] names = {"Dashboard", "My Tasks", "Schedule", "Completed"};
        String[] icons = {"⌂", "☑", "▦", "✓"};
        for (int i = 0; i < names.length; i++) {
            String n = names[i];
            boolean selected = n.equals(page) || (page.equals("Editor") && n.equals("My Tasks"));
            Color fill = selected ? (account.dark() ? accent : tint) : null;
            Color text = selected && account.dark() ? Color.WHITE : ink;
            JButton nav = action(icons[i] + "    " + n, fill, text, () -> go(n));
            nav.setFont(Main.BODY.deriveFont(selected ? Font.BOLD : Font.PLAIN, 19f));
            nav.setHorizontalAlignment(SwingConstants.LEFT);
            nav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            stack(rail, nav);
        }
        rail.add(Box.createVerticalGlue());
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

        if (account.dark()) {
            shell.add(sidebar(), BorderLayout.WEST);
            content.add(header(), BorderLayout.NORTH);
        } else {
            shell.add(header(), BorderLayout.NORTH);
            content.add(sidebar(), BorderLayout.WEST);
        }
        shell.add(content);
        setContentPane(shell);
        revalidate();
        repaint();
    }

    private void heading(String title, String subtitle, boolean create) {
        JPanel top = clear(new BorderLayout(8, 0));
        top.add(label(title, getWidth() >= 1300 ? 40 : 33, true));
        if (create) top.add(primary("＋  Create Task", () -> edit(null)), BorderLayout.EAST);
        stack(body, top);
        if (!subtitle.isBlank()) {
            JLabel description = label(subtitle, 17, false);
            description.setForeground(muted);
            stack(body, description);
        }
        body.add(Box.createVerticalStrut(5));
    }

    private JPanel section(String title, JComponent contents) {
        JPanel panel = card(surface, 15);
        panel.add(label(title, 22, true), BorderLayout.NORTH);
        panel.add(contents);
        return panel;
    }

    private JPanel stat(String symbol, long value, String caption, int colorType) {
        int[] lightTiles = {0xE4E8FC, 0xFCEBCC, 0xFBDDE2, 0xDEF2E3};
        int[] darkTiles = {0x303A6C, 0x51432C, 0x52323A, 0x284B3B};
        int[] lightInk = {0x3744B6, 0x9D6410, 0xAD2940, 0x177440};
        int[] darkInk = {0xC0CAFF, 0xFFD075, 0xFF9CA6, 0xA3E6AE};
        JPanel panel = card(surface, 18);
        JPanel tile = card(tone(lightTiles[colorType], darkTiles[colorType]), 5);
        tile.setPreferredSize(new Dimension(57, 57));
        JLabel icon = label(symbol, 25, true);
        icon.setForeground(tone(lightInk[colorType], darkInk[colorType]));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        tile.add(icon);
        JPanel words = column();
        words.add(label(String.valueOf(value), 34, true));
        words.add(label(caption, 16, false));
        panel.add(tile, BorderLayout.WEST);
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

    private long weeklyCount() {
        LocalDate start = monday();
        return store.completed().stream().map(Task::completedOn).filter(Objects::nonNull)
                .filter(d -> !d.isBefore(start) && d.isBefore(start.plusWeeks(1))).count();
    }

    private JCheckBox completeBox(Task task) {
        JCheckBox box = new JCheckBox();
        box.setOpaque(false);
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

    private void dashboard() {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good Morning" : hour < 18 ? "Good Afternoon" : "Good Evening";
        heading(greeting + ", " + account.name(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), true);
        stack(body, label("Your task queue", 35, true));
        stack(body, label("Stay on top of deadlines, one task at a time.", 17, false));

        List<Task> active = store.active();
        stats(stat("☷", active.size(), "Active tasks", 0),
                stat("▦", active.stream().filter(t -> t.due().equals(LocalDate.now())).count(),
                        "Due today", 1),
                stat("✓", weeklyCount(), "Completed this week", 3));

        JPanel queue = column();
        int rank = 1;
        for (Task t : active.stream().limit(5).toList()) {
            JPanel row = card(bg, 9);
            row.add(label(String.valueOf(rank++), 20, true), BorderLayout.WEST);
            JPanel words = column();
            words.add(taskLink(t));
            words.add(label(t.subject() + "   ·   " + t.priority() + "   ·   " + due(t), 13, false));
            row.add(words);
            row.add(completeBox(t), BorderLayout.EAST);
            stack(queue, row);
        }
        if (active.isEmpty()) stack(queue, label("Your queue is clear.", 16, false));

        JPanel todayPanel = column();
        List<Task> today = store.schedule().stream()
                .filter(t -> t.study().toLocalDate().equals(LocalDate.now())).toList();
        for (Task t : today)
            stack(todayPanel, plain(t.study().format(CLOCK) + "   " + t.title(), () -> edit(t)));
        if (today.isEmpty()) stack(todayPanel, label("No study sessions today.", 15, false));

        JPanel right = column();
        stack(right, section("Today’s Schedule", todayPanel));
        JPanel plan = column();
        stack(plan, label(today.stream().mapToInt(Task::minutes).sum()
                + " study minutes planned today.", 15, false));
        stack(plan, primary("View schedule  →", () -> go("Schedule")));
        stack(right, section("✦  Smart scheduling", plan));

        JPanel split = clear(new BorderLayout(15, 0));
        split.add(section("Priority Queue", queue));
        split.add(right, BorderLayout.EAST);
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

    private JPanel field(String caption, JComponent control) {
        JPanel panel = clear(new BorderLayout(0, 5));
        JLabel name = label(caption, 15, true);
        name.setLabelFor(control);
        panel.add(name, BorderLayout.NORTH);
        panel.add(control);
        return panel;
    }

    private JPanel badge(String text, Color fill, Color foreground) {
        JPanel wrap = clear(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel chip = card(fill, 4);
        chip.setBorder(new EmptyBorder(4, 9, 4, 9));
        JLabel words = label(text, 14, true);
        words.setForeground(foreground);
        chip.add(words);
        wrap.add(chip);
        return wrap;
    }

    private JPanel priorityBadge(Task.Priority value) {
        return switch (value) {
            case High -> badge("High", tone(0xFFD8DB, 0x573138), tone(0x981622, 0xFFADB0));
            case Medium -> badge("Medium", tone(0xFFEBC1, 0x54472D), tone(0x885006, 0xFFDA86));
            case Low -> badge("Low", tone(0xD7EFDD, 0x24543A), tone(0x186339, 0xB6ECC1));
        };
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

    private void tasks() {
        heading("My Tasks", account.dark()
                ? "Welcome back to PriorQ! Here are your tasks, organized by priority and deadline."
                : "", true);
        List<Task> active = store.active();

        JTextField search = input(new JTextField(query, 12));
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

        JPanel filters = clear(new GridLayout(1, 4, 12, 0));
        filters.add(field("Search tasks", search));
        filters.add(field("Subject", subjects));
        filters.add(field("Priority", levels));
        filters.add(field("Deadline", dates));
        stack(body, filters);

        LocalDate today = LocalDate.now();
        long soon = active.stream().filter(t -> !t.due().isBefore(today)
                && !t.due().isAfter(today.plusDays(3))).count();
        long high = active.stream().filter(t -> t.priority() == Task.Priority.High).count();
        stats(stat("☷", active.size(), "Total Tasks", 0), stat("▦", soon, "Due Soon", 1),
                stat("⚑", high, "High Priority", 2));

        JPanel rows = column();
        stack(body, tableBox(rows));
        Runnable filter = () -> {
            query = search.getText();
            subject = String.valueOf(subjects.getSelectedItem());
            priority = String.valueOf(levels.getSelectedItem());
            deadline = String.valueOf(dates.getSelectedItem());
            rows.removeAll();

            double[] widths = {.39, .23, .15, .23};
            rows.add(tableRow(true, widths, label("     Task", 16, true),
                    label("Subject", 16, true), label("Priority", 16, true),
                    label("Deadline", 16, true)));
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
                    case "Overdue" -> LocalDateTime.of(t.due(), t.dueTime()).isBefore(LocalDateTime.now());
                    default -> true;
                };
                if (!include) continue;

                JPanel title = clear(new BorderLayout(6, 0));
                title.add(completeBox(t), BorderLayout.WEST);
                title.add(taskLink(t));
                JComponent course = account.dark()
                        ? badge(t.subject(), new Color(49, 59, 80), ink) : label(t.subject(), 16, false);
                JComponent when = label(t.due().format(DAY), 15, false);
                if (account.dark()) {
                    boolean now = t.due().equals(today);
                    boolean next = t.due().equals(today.plusDays(1));
                    String text = now ? "Due today" : next ? "Due tomorrow" : "Due " + t.due().format(SHORT_DAY);
                    when = badge(text, now ? new Color(82, 49, 57) : next
                            ? new Color(81, 68, 44) : new Color(49, 59, 80), ink);
                }
                when.setToolTipText(due(t));
                rows.add(tableRow(false, widths, title, course, priorityBadge(t.priority()), when));
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
        heading("Completed Tasks", account.dark() ? "A record of the tasks you’ve finished."
                : "A record of your finished work and achievements.", false);
        List<Task> done = store.completed();
        stats(stat("✓", weeklyCount(), "Completed this week", 3),
                stat("◈", done.size(), "Total completed", 0));

        JPanel rows = column();
        double[] widths = {.38, .24, .23, .15};
        rows.add(tableRow(true, widths, label("Task", 16, true), label("Subject", 16, true),
                label("Completion Date", 16, true), label("Status", 16, true)));
        for (Task t : done) {
            JPanel title = clear(new BorderLayout(7, 0));
            JLabel check = label("✓", 19, true);
            check.setForeground(tone(0x218348, 0x87DE9A));
            title.add(check, BorderLayout.WEST);
            title.add(taskLink(t));
            String date = t.completedOn() == null ? "Unrecorded" : t.completedOn().format(DAY);
            rows.add(tableRow(false, widths, title, label(t.subject(), 16, false),
                    label(date, 15, false), badge("Completed",
                            tone(0xD4EFDB, 0x285238), tone(0x166333, 0xCEF1D6))));
        }
        if (done.isEmpty()) rows.add(label("   No completed tasks yet.", 15, false));
        stack(body, account.dark() ? tableBox(rows) : section("Completed Tasks", rows));
    }

    private void schedule() {
        heading("Schedule", "Plan and organize your tasks across the week.", true);

        JPanel left = column();
        JPanel nav = card(surface, 5);
        JPanel arrows = clear(new FlowLayout(FlowLayout.LEFT, 5, 0));
        arrows.add(plain("‹", () -> { week = week.minusWeeks(1); render(); }));
        arrows.add(label(week.format(SHORT_DAY) + " – " + week.plusDays(6).format(DAY), 17, true));
        arrows.add(plain("›", () -> { week = week.plusWeeks(1); render(); }));
        nav.add(arrows);
        nav.add(plain("This Week", () -> { week = monday(); render(); }), BorderLayout.EAST);
        stack(left, nav);
        stack(left, calendar());

        JPanel list = column();
        for (Task t : store.active().stream().limit(5).toList()) {
            JPanel entry = card(tone(0xF8F8FB, 0x202A3C), 8);
            entry.add(taskLink(t));
            entry.add(priorityBadge(t.priority()), BorderLayout.EAST);
            entry.add(label(due(t), 13, false), BorderLayout.SOUTH);
            stack(list, entry);
        }
        if (store.active().isEmpty()) stack(list, label("No upcoming deadlines.", 15, false));
        JPanel aside = section("Upcoming deadlines", list);
        aside.setPreferredSize(new Dimension(Math.max(210, Math.min(315, getWidth() / 5)),
                aside.getPreferredSize().height));
        JPanel holder = clear(new BorderLayout());
        holder.add(aside, BorderLayout.NORTH);
        JPanel split = clear(new BorderLayout(14, 0));
        split.add(left);
        split.add(holder, BorderLayout.EAST);
        stack(body, split);
    }

    private record Block(Task task, int day, int start, int end, int lane, int lanes) {}

    private List<Block> blocks() {
        List<Block> result = new ArrayList<>();
        List<Task> sessions = store.schedule();
        for (int d = 0; d < 7; d++) {
            LocalDateTime midnight = week.plusDays(d).atStartOfDay();
            List<Integer> ends = new ArrayList<>();
            List<Block> group = new ArrayList<>();
            for (Task t : sessions) {
                LocalDateTime finish = t.study().plusMinutes(t.minutes());
                if (!finish.isAfter(midnight) || !t.study().isBefore(midnight.plusDays(1))) continue;
                int start = (int)Math.max(0L, Duration.between(midnight, t.study()).toMinutes());
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
                group.add(new Block(t, d, start, end, lane, 1));
            }
            finishGroup(result, group, ends.size());
        }
        return result;
    }

    private void finishGroup(List<Block> result, List<Block> group, int count) {
        for (Block b : group)
            result.add(new Block(b.task(), b.day(), b.start(), b.end(), b.lane(), count));
    }

    private JPanel calendar() {
        List<Block> blocks = blocks();
        int first = Math.min(account.dark() ? 8 : 7,
                blocks.stream().mapToInt(b -> b.start() / 60).min().orElse(24));
        int end = Math.max(account.dark() ? 22 : 21,
                blocks.stream().mapToInt(b -> (b.end() + 59) / 60).max().orElse(0));
        final int row = 34, head = 48, gutter = 65;

        JPanel grid = new JPanel(null) {
            private int edge(int day) {
                return gutter + (getWidth() - gutter) * day / 7;
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(tone(0xEFF0F6, 0x222C40));
                g.fillRect(0, 0, getWidth(), head);
                g.setColor(ink);
                g.setFont(Main.BODY.deriveFont(Font.BOLD, 14f));
                g.drawString("Time", 9, 29);
                for (int d = 0; d < 7; d++) {
                    LocalDate day = week.plusDays(d);
                    int x = edge(d) + 5;
                    g.drawString(day.format(DateTimeFormatter.ofPattern("EEE")), x, 18);
                    g.drawString(day.format(SHORT_DAY), x, 37);
                }
                g.setFont(Main.BODY.deriveFont(12f));
                for (int h = first; h < end; h++) {
                    int y = head + (h - first) * row;
                    g.setColor(line);
                    g.drawLine(0, y, getWidth(), y);
                    g.setColor(ink);
                    g.drawString(LocalTime.of(h, 0).format(CLOCK), 4, y + 21);
                }
                g.setColor(line);
                for (int d = 0; d <= 7; d++) g.drawLine(edge(d), 0, edge(d), getHeight());
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
                    button.setBounds(x + 1, head + (b.start() - first * 60) * row / 60,
                            Math.max(1, right - x - 2), Math.max(3, (b.end() - b.start()) * row / 60 - 1));
                }
            }
        };
        grid.setBackground(surface);
        grid.setBorder(BorderFactory.createLineBorder(line));
        grid.setPreferredSize(new Dimension(495, head + (end - first) * row));

        int[] lights = {0xD2EDD8, 0xD8E8FC, 0xE2DAF8, 0xFAE7B9};
        int[] darks = {0x74BA94, 0x8C9CE4, 0xAE94D3, 0xE7BD79};
        for (Block b : blocks) {
            Task t = b.task();
            String times = t.study().format(CLOCK) + " – " + t.study().plusMinutes(t.minutes()).format(CLOCK);
            String safe = t.title().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            JButton button = new JButton("<html><b>" + safe + "</b><br>" + times + "</html>");
            int color = Math.floorMod(t.subject().hashCode(), lights.length);
            button.setBackground(tone(lights[color], darks[color]));
            button.setForeground(new Color(22, 27, 47));
            button.setFont(Main.BODY.deriveFont(11f));
            button.setOpaque(true);
            button.setBorder(new EmptyBorder(2, 3, 1, 2));
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

    private JPanel editorField(String caption, JComponent control) {
        if (!account.dark()) return field(caption, control);
        JPanel row = clear(new BorderLayout(12, 0));
        JLabel title = label(caption, 16, true);
        title.setPreferredSize(new Dimension(155, 30));
        title.setLabelFor(control);
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
        JTextField title = input(new JTextField(t == null ? "" : t.title()));
        stack(form, editorField("Task title *", title));

        JComboBox<String> course = input(new JComboBox<>());
        course.setEditable(true);
        java.util.stream.Stream.concat(store.active().stream(), store.completed().stream())
                .map(Task::subject).distinct().sorted().forEach(course::addItem);
        course.setSelectedItem(t == null ? "" : t.subject());
        if (course.getEditor().getEditorComponent() instanceof JTextField text) input(text);
        stack(form, editorField("Subject *", course));

        JTextArea notes = input(new JTextArea(t == null ? "" : t.description(), 3, 22));
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        stack(form, editorField("Description", new JScrollPane(notes)));

        ButtonGroup group = new ButtonGroup();
        JPanel choices = clear(new GridLayout(1, 3, 10, 0));
        for (Task.Priority p : new Task.Priority[]{Task.Priority.Low, Task.Priority.Medium, Task.Priority.High}) {
            JRadioButton choice = new JRadioButton(p.name());
            choice.setSelected(p == (t == null ? Task.Priority.Medium : t.priority()));
            choice.setActionCommand(p.name());
            choice.setOpaque(false);
            choice.setForeground(ink);
            choice.setFont(Main.BODY.deriveFont(Font.BOLD, 15f));
            choice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            group.add(choice);
            Color fill = switch (p) {
                case High -> tone(0xF8DBDF, 0x4D303A);
                case Medium -> tone(0xFAE9C8, 0x4B422D);
                case Low -> tone(0xDDEEE0, 0x294737);
            };
            JPanel option = card(fill, 4);
            option.add(choice);
            choices.add(option);
        }
        stack(form, editorField("Priority *", choices));

        JTextField date = input(new JTextField((t == null ? LocalDate.now() : t.due()).toString()));
        JTextField time = input(new JTextField(t == null ? "23:59" : t.dueTime().toString()));
        date.setToolTipText("YYYY-MM-DD");
        time.setToolTipText("24-hour time, for example 17:30");
        JSpinner minutes = input(new JSpinner(new SpinnerNumberModel(
                t == null ? 60 : t.minutes(), 1, 1440, 1)));
        if (minutes.getEditor() instanceof JSpinner.DefaultEditor e) input(e.getTextField());
        if (account.dark()) {
            stack(form, editorField("Due date *", date));
            stack(form, editorField("Due time *", time));
            stack(form, editorField("Duration (min) *", minutes));
        } else {
            JPanel dates = clear(new GridLayout(1, 3, 12, 0));
            dates.add(field("Due date *", date));
            dates.add(field("Due time *", time));
            dates.add(field("Estimated duration (min) *", minutes));
            stack(form, dates);
        }

        JTextField study = input(new JTextField(t == null || t.study() == null
                ? "" : t.study().format(SESSION)));
        JComboBox<Task.Kind> kind = input(new JComboBox<>(Task.Kind.values()));
        JComboBox<Task.Status> status = input(new JComboBox<>(Task.Status.values()));
        if (t != null) {
            kind.setSelectedItem(t.kind());
            status.setSelectedItem(t.status());
        }

        JPanel extra = column();
        stack(extra, editorField("Study session", field("YYYY-MM-DD HH:mm (optional)", study)));
        stack(extra, editorField("Task type", kind));
        stack(extra, editorField("Progress", status));
        extra.setVisible(false);
        extra.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(extra);

        JPanel actions = clear(new FlowLayout(FlowLayout.LEFT, 7, 0));
        actions.add(primary(t == null ? "＋  Add to queue" : "Save changes", () -> {
            try {
                minutes.commitEdit();
                Task.Status progress = (Task.Status)status.getSelectedItem();
                Task saved = new Task(t == null ? store.nextId() : t.id(), title.getText(),
                        Objects.toString(course.getEditor().getItem(), ""),
                        (Task.Kind)kind.getSelectedItem(), LocalDate.parse(date.getText().strip()),
                        Task.Priority.valueOf(group.getSelection().getActionCommand()),
                        t == null ? Task.Status.TODO : t.status(),
                        study.getText().isBlank() ? null : LocalDateTime.parse(study.getText().strip(), SESSION),
                        notes.getText(), LocalTime.parse(time.getText().strip()),
                        (int)minutes.getValue(), t == null ? null : t.completedOn());
                store.save(saved.withStatus(progress));
                query = "";
                subject = "All subjects";
                priority = "All priorities";
                deadline = "All deadlines";
                go(progress == Task.Status.DONE ? "Completed" : "My Tasks");
            } catch (DateTimeParseException ex) {
                message("Use a date like 2026-11-24 and a time like 14:30. "
                        + "For a study session, enter both, for example 2026-11-23 09:00.");
            } catch (Exception ex) {
                message(ex.getMessage());
            }
        }));
        actions.add(plain("Cancel", () -> go("My Tasks")));
        actions.add(plain("Study options  ▾", () -> {
            extra.setVisible(!extra.isVisible());
            body.revalidate();
            body.repaint();
        }));
        if (t != null) actions.add(plain("Delete task", () -> {
            if (JOptionPane.showConfirmDialog(this, "Delete this task?", "PriorQ",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
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
        stack(form, field("Display name", name));
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