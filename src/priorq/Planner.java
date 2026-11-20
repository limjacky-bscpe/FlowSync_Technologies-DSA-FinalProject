package priorq;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.*;
import java.time.format.*;
import java.util.List;

final class Planner extends JFrame {
    private static final DateTimeFormatter SESSION = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private final TaskStore store;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTable table = new JTable();
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel next = new JLabel(), progress = new JLabel(), hint = new JLabel();
    private final JButton advance = new JButton(), edit = new JButton("Edit task"), delete = new JButton("Delete task");
    private List<Task> visible = List.of();

    Planner(TaskStore store) {
        super("PriorQ | Academic task planner");
        this.store=store;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(0,12));
        root.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
        setContentPane(root);
        JPanel header=new JPanel(new BorderLayout());
        JLabel brand=new JLabel("PriorQ   /   Academic planner");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD,24f));
        header.add(brand);
        JButton add=new JButton("+ Add task");
        add.addActionListener(e -> openEditor(null)); header.add(add,BorderLayout.EAST);
        JPanel hero=new JPanel(new BorderLayout(12,10));
        hero.setBackground(new Color(231,223,249));
        hero.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
        hero.add(new JLabel("NEXT IN QUEUE"),BorderLayout.NORTH);
        next.setFont(next.getFont().deriveFont(Font.BOLD,19f));
        hero.add(next); hero.add(advance,BorderLayout.EAST);
        JPanel top=new JPanel(new BorderLayout(0,14)); top.add(header,BorderLayout.NORTH); top.add(hero);
        root.add(top,BorderLayout.NORTH);
        for(String name:new String[]{"Task queue","Study schedule","Completed"}) tabs.addTab(name,new JPanel(new BorderLayout()));
        tabs.addChangeListener(e -> refresh());
        table.setRowHeight(32); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class,null);
        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().addListSelectionListener(e -> selectionChanged());
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2 && selected()!=null) openEditor(selected()); }
        });
        root.add(tabs);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        buttons.add(edit); buttons.add(delete);
        edit.addActionListener(e -> openEditor(selected()));
        delete.addActionListener(e -> {
            Task t=selected();
            if(t!=null && JOptionPane.showConfirmDialog(this,"Delete this task?","Delete task",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
                try {store.delete(t.id()); refresh();} catch(IOException ex){error(ex);}
        });
        advance.addActionListener(e -> {
            Task t=store.next();
            if(t!=null) try {
                store.save(t.withStatus(t.status()==Task.Status.TODO?Task.Status.ACTIVE:Task.Status.DONE)); refresh();
            } catch(IOException ex){error(ex);}
        });
        JPanel footer=new JPanel(new BorderLayout(8,8));
        footer.add(hint,BorderLayout.NORTH);footer.add(buttons,BorderLayout.WEST);footer.add(progress,BorderLayout.EAST);
        root.add(footer,BorderLayout.SOUTH);
        setSize(1020,600);setMinimumSize(new Dimension(720,430));setLocationRelativeTo(null);
        refresh();
        addWindowFocusListener(new WindowAdapter(){public void windowGainedFocus(WindowEvent e){ if (isShowing()) refresh(); }});
    }

    private Task selected(){
        int i=table.getSelectedRow();
        return i<0 || i>=visible.size()?null:visible.get(i);
    }
    private void selectionChanged(){ boolean enabled=selected()!=null;edit.setEnabled(enabled);delete.setEnabled(enabled); }
    private void error(Exception ex){ JOptionPane.showMessageDialog(this,ex.getMessage(),"PriorQ",JOptionPane.ERROR_MESSAGE); }
    private void refresh(){
        int tab=tabs.getSelectedIndex();
        List<Task> active=store.active(), done=store.completed();
        visible=switch(tab){case 1 -> store.schedule(); case 2 -> done; default -> active;};
        DefaultTableModel model=new DefaultTableModel(new String[]{tab==1?"Study session":"Order","Task","Subject","Type","Deadline","Priority","Status"},0);
        for(Task t:visible){
            String due=t.due().toString();
            if(t.status()!=Task.Status.DONE){
                if(t.due().isBefore(LocalDate.now())) due+=" (overdue)";
                else if(t.due().equals(LocalDate.now())) due+=" (today)";
            }
            model.addRow(new Object[]{tab==1?t.study().format(SESSION):model.getRowCount()+1,t.title(),t.subject(),t.kind(),due,t.priority(),t.status()});
        }
        table.setModel(model); table.setDefaultEditor(Object.class,null);
        int[] widths={tab==1?150:48,230,155,95,155,70,100};
        for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JPanel panel=(JPanel)tabs.getSelectedComponent(); panel.add(scroll);panel.revalidate();panel.repaint();
        Task front=store.next();
        next.setText(front==null?"Your queue is clear":front.title()+"   ·   "+front.due());
        advance.setEnabled(front!=null);advance.setText(front!=null && front.status()==Task.Status.ACTIVE?"Mark complete":"Start task");
        int total=active.size()+done.size();
        progress.setText(done.size()+" of "+total+" complete   •   "+active.size()+" active");
        hint.setText(switch(tab){case 1 -> "Set a study date and time on a task to plan a session.";case 2 -> "Edit a completed task to reopen it.";default -> "Earliest deadline first. Ties use priority, then original entry order.";});
        selectionChanged();
    }

    private void openEditor(Task t){
        JTextField title=new JTextField(t==null?"":t.title(),24), subject=new JTextField(t==null?"":t.subject());
        JTextField due=new JTextField(t==null?LocalDate.now().toString():t.due().toString());
        JTextField study=new JTextField(t==null || t.study()==null?"":t.study().format(SESSION));
        JComboBox<Task.Kind> kind=new JComboBox<>(Task.Kind.values());
        JComboBox<Task.Priority> priority=new JComboBox<>(Task.Priority.values());
        JComboBox<Task.Status> status=new JComboBox<>(Task.Status.values());
        priority.setSelectedItem(t==null?Task.Priority.Medium:t.priority());
        if(t!=null){kind.setSelectedItem(t.kind());status.setSelectedItem(t.status());}
        JPanel form=new JPanel(new GridLayout(0,1,0,3));
        String[] labels={"Task name","Subject","Task type","Priority","Deadline (YYYY-MM-DD)","Study session (YYYY-MM-DD HH:mm, optional)","Status"};
        JComponent[] inputs={title,subject,kind,priority,due,study,status};
        for(int i=0;i<labels.length;i++){JLabel label=new JLabel(labels[i]);label.setLabelFor(inputs[i]);form.add(label);form.add(inputs[i]);}
        while(JOptionPane.showConfirmDialog(this,form,t==null?"Add task":"Edit task",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try {
                Task value=new Task(t==null?store.nextId():t.id(),title.getText(),subject.getText(),
                    (Task.Kind)kind.getSelectedItem(),LocalDate.parse(due.getText().strip()),
                    (Task.Priority)priority.getSelectedItem(),(Task.Status)status.getSelectedItem(),
                    study.getText().isBlank()?null:LocalDateTime.parse(study.getText().strip(),SESSION));
                store.save(value);refresh();return;
            } catch(DateTimeParseException ex){error(new IllegalArgumentException("Use a valid date such as 2026-11-24 and a study time such as 2026-11-23 14:30."));}
            catch(IllegalArgumentException | IOException ex){error(ex);}
        }
    }
}
