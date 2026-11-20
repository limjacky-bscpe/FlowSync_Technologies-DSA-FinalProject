package priorq;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Path dir = Path.of(System.getProperty("user.home"), ".priorq");
            try {
                Files.createDirectories(dir);
                FileChannel channel = FileChannel.open(dir.resolve("session.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    channel.close();
                    throw new IOException("PriorQ is already open for this user.");
                }
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try { lock.release(); channel.close(); } catch(IOException ignored) {}
                }));
                for (UIManager.LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels())
                    if ("Nimbus".equals(theme.getName())) UIManager.setLookAndFeel(theme.getClassName());
                UIManager.put("nimbusSelectionBackground", new Color(95,70,164));
                new Planner(new TaskStore(dir.resolve("tasks.xml"))).setVisible(true);
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(null,ex.getMessage(),"Could not open PriorQ",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
