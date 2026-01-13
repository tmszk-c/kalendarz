package kalendarz;

import com.toedter.calendar.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class JCalendarWidget extends JPanel {

    private JCalendar calendar;
    private ArrayList<Zdarzenie> listaZdarzen;
    private Runnable callback;
    private boolean isRefreshing = false;

    public JCalendarWidget(ArrayList<Zdarzenie> listaZdarzen, Runnable callback) {
        this.listaZdarzen = listaZdarzen;
        this.callback = callback;

        setLayout(new BorderLayout());

        calendar = new JCalendar();
        calendar.setLocale(new Locale("pl", "PL"));
        calendar.getMonthChooser().setLocale(new Locale("pl", "PL"));

        add(calendar, BorderLayout.CENTER);

        poprawNazwyMiesiecy();

        calendar.getMonthChooser().addPropertyChangeListener("month", e -> odswiez());
        calendar.getYearChooser().addPropertyChangeListener("year", e -> odswiez());

        initDayButtons();

        // WAŻNE: odśwież od razu, żeby w pierwszym widoku były pokazane wydarzenia
        SwingUtilities.invokeLater(this::odswiez);
    }

    // --- Poprawione nazwy miesięcy ---
    private void poprawNazwyMiesiecy() {
        String[] months = {
                "styczeń", "luty", "marzec", "kwiecień",
                "maj", "czerwiec", "lipiec", "sierpień",
                "wrzesień", "październik", "listopad", "grudzień"
        };
        @SuppressWarnings("unchecked")
        JComboBox<String> box = (JComboBox<String>) calendar.getMonthChooser().getComboBox();
        box.removeAllItems();
        for (String m : months) box.addItem(m);
    }

    // --- Inicjalizacja kafelków dni ---
    private void initDayButtons() {
        JDayChooser chooser = calendar.getDayChooser();
        for (Component c : chooser.getDayPanel().getComponents()) {
            if (c instanceof JButton btn) {
                btn.setLayout(null);
                btn.addActionListener(new DayListener());
                btn.addMouseListener(new HoverEffect(btn));
            }
        }
    }

    public void setListaZdarzen(ArrayList<Zdarzenie> listaZdarzen) {
        this.listaZdarzen = listaZdarzen;
        odswiez();
    }

    // --- Odświeżanie kafelków z wydarzeniami i labelami ---
    public void odswiez() {
        if (isRefreshing) return;
        isRefreshing = true;

        JDayChooser chooser = calendar.getDayChooser();
        int month = calendar.getMonthChooser().getMonth() + 1;
        int year = calendar.getYearChooser().getYear();

        for (Component c : chooser.getDayPanel().getComponents()) {
            if (!(c instanceof JButton btn)) continue;

            btn.removeAll();
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBackground(Color.WHITE);

            if (btn.getText().isEmpty()) continue;

            int day;
            try {
                day = Integer.parseInt(btn.getText());
            } catch (NumberFormatException e) {
                continue;
            }

            long count = listaZdarzen.stream()
                    .filter(z -> z.getData() != null &&
                            z.getData().getYear() == year &&
                            z.getData().getMonthValue() == month &&
                            z.getData().getDayOfMonth() == day)
                    .count();

            if (count > 0) {
                btn.setBackground(new Color(170, 210, 255)); // kolor dla dni z wydarzeniami

                JLabel lblCount = new JLabel("Wydarzenia: " + count, SwingConstants.CENTER);
                lblCount.setFont(new Font("Arial", Font.PLAIN, 9));
                lblCount.setForeground(Color.BLACK);
                lblCount.setBounds(0, btn.getHeight() - 18, btn.getWidth(), 14);
                btn.add(lblCount);
            }

            btn.setToolTipText(count > 0 ? "Wydarzenia: " + count : null);
        }

        revalidate();
        repaint();
        isRefreshing = false;
    }

    // --- Hover na każdym kafelku ---
    private static class HoverEffect extends MouseAdapter {
        private final JButton btn;
        private Color original;

        public HoverEffect(JButton btn) {
            this.btn = btn;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            original = btn.getBackground();
            btn.setBackground(original.brighter());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            btn.setBackground(original);
        }
    }

    // --- Listener kliknięcia w dzień ---
    private class DayListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton btn = (JButton) e.getSource();
            if (btn.getText().isEmpty()) return;

            int day = Integer.parseInt(btn.getText());
            int month = calendar.getMonthChooser().getMonth() + 1;
            int year = calendar.getYearChooser().getYear();

            LocalDateTime date = LocalDateTime.of(year, month, day, 12, 0);

            ArrayList<Zdarzenie> events = new ArrayList<>();
            for (Zdarzenie z : listaZdarzen) {
                if (z.getData() != null && z.getData().toLocalDate().equals(date.toLocalDate()))
                    events.add(z);
            }

            String msg = events.isEmpty() ? "Brak wydarzeń" : "Wydarzenia tego dnia: " + events.size();
            String[] options = {"Dodaj", "Edytuj", "Anuluj"};

            int choice = JOptionPane.showOptionDialog(JCalendarWidget.this, msg,
                    date.toLocalDate().toString(), JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 0) addEvent(date);
            if (choice == 1 && !events.isEmpty()) editEvent(events);
        }
    }

    private void addEvent(LocalDateTime date) {
        JTextField title = new JTextField();
        JTextField link = new JTextField();

        Object[] form = {"Tytuł:", title, "Link (opcjonalnie):", link};
        if (JOptionPane.showConfirmDialog(this, form, "Nowe wydarzenie",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                URL u = link.getText().isEmpty() ? null : new URL(link.getText());
                listaZdarzen.add(new Zdarzenie(title.getText(), date, u));
                odswiez();
                if (callback != null) callback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void editEvent(ArrayList<Zdarzenie> events) {
        String[] names = events.stream().map(Zdarzenie::getTytul).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this, "Wybierz wydarzenie:",
                "Edytuj", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);

        if (selected == null) return;

        Zdarzenie z = events.stream().filter(ev -> ev.getTytul().equals(selected)).findFirst().get();

        JTextField title = new JTextField(z.getTytul());
        JTextField date = new JTextField(z.getData().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JTextField link = new JTextField(z.getLink() == null ? "" : z.getLink().toString());

        Object[] form = {"Tytuł:", title, "Data (yyyy-MM-dd HH:mm):", date, "Link (opcjonalnie):", link};

        if (JOptionPane.showConfirmDialog(this, form, "Edytuj wydarzenie",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                z.setTytul(title.getText());
                z.setData(LocalDateTime.parse(date.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                z.setLink(link.getText().isEmpty() ? null : new URL(link.getText()));
                odswiez();
                if (callback != null) callback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    public JCalendar getCalendar() {
        return calendar;
    }
}
