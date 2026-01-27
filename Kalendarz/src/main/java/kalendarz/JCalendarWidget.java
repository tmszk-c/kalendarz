package kalendarz;

import com.toedter.calendar.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
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
        setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        calendar = new JCalendar();
        calendar.setLocale(new Locale("pl", "PL"));
        calendar.getMonthChooser().setLocale(new Locale("pl", "PL"));

        add(calendar, BorderLayout.CENTER);

        poprawNazwyMiesiecy();

        // Nasłuchiwanie zmian daty, aby odświeżyć kropki/etykiety wydarzeń
        calendar.getMonthChooser().addPropertyChangeListener("month", e -> odswiez());
        calendar.getYearChooser().addPropertyChangeListener("year", e -> odswiez());

        initDayButtons();

        // Pierwsze odświeżenie po załadowaniu GUI
        SwingUtilities.invokeLater(this::odswiez);
    }

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

    private void initDayButtons() {
        JDayChooser chooser = calendar.getDayChooser();
        for (Component c : chooser.getDayPanel().getComponents()) {
            if (c instanceof JButton btn) {
                // ZMIANA: BorderLayout zamiast null layout zapobiega nakładaniu się tekstu
                btn.setLayout(new BorderLayout()); 
                btn.addActionListener(new DayListener());
                btn.addMouseListener(new HoverEffect(btn));
            }
        }
    }

    public void setListaZdarzen(ArrayList<Zdarzenie> listaZdarzen) {
        this.listaZdarzen = listaZdarzen;
        odswiez();
    }

    public void odswiez() {
        if (isRefreshing) return;
        isRefreshing = true;

        JDayChooser chooser = calendar.getDayChooser();
        int month = calendar.getMonthChooser().getMonth() + 1;
        int year = calendar.getYearChooser().getYear();

        for (Component c : chooser.getDayPanel().getComponents()) {
            if (!(c instanceof JButton btn)) continue;

            btn.removeAll();
            btn.setBackground(Color.WHITE);
            btn.setVerticalAlignment(SwingConstants.TOP); // Numer dnia na górze

            if (btn.getText().isEmpty()) {
                btn.revalidate();
                btn.repaint();
                continue;
            }

            int day;
            try {
                day = Integer.parseInt(btn.getText());
            } catch (NumberFormatException e) {
                continue;
            }

            // Filtrowanie wydarzeń dla konkretnego dnia
            long count = listaZdarzen.stream()
                    .filter(z -> z.getData() != null &&
                            z.getData().getYear() == year &&
                            z.getData().getMonthValue() == month &&
                            z.getData().getDayOfMonth() == day)
                    .count();

            if (count > 0) {
                btn.setBackground(new Color(210, 230, 255)); // Jasnoniebieskie tło dla dni z planem

                // Tworzenie etykiety "licznika" na dole przycisku
                JLabel lblCount = new JLabel(count + " wyd.", SwingConstants.CENTER);
                lblCount.setFont(new Font("Arial", Font.BOLD, 10));
                lblCount.setForeground(new Color(0, 102, 204));
                lblCount.setOpaque(true);
                lblCount.setBackground(new Color(255, 255, 255, 180)); // Półprzezroczyste tło
                
                // Dodanie do SOUTH, aby nie nachodziło na numer dnia w centrum/górze
                btn.add(lblCount, BorderLayout.SOUTH); 
            }

            btn.setToolTipText(count > 0 ? "Liczba wydarzeń: " + count : "Brak wydarzeń");
            btn.revalidate();
            btn.repaint();
        }

        isRefreshing = false;
    }

    private static class HoverEffect extends MouseAdapter {
        private final JButton btn;
        private Color original;

        public HoverEffect(JButton btn) {
            this.btn = btn;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            original = btn.getBackground();
            btn.setBackground(original.darker());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            btn.setBackground(original);
        }
    }

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

            String msg = events.isEmpty() ? "Brak wydarzeń tego dnia." : "Wybrano dzień: " + date.toLocalDate() + "\nLiczba wydarzeń: " + events.size();
            Object[] options = {"Dodaj nowe", "Zarządzaj istniejącymi", "Zamknij"};

            int choice = JOptionPane.showOptionDialog(JCalendarWidget.this, msg,
                    "Dzień: " + date.toLocalDate(), JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 0) addEvent(date);
            if (choice == 1 && !events.isEmpty()) editEvent(events);
        }
    }

    private void addEvent(LocalDateTime date) {
        JTextField title = new JTextField();
        JTextField link = new JTextField();

        Object[] form = {
            "Tytuł wydarzenia:", title,
            "Link URL (opcjonalnie):", link
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Dodaj nowe wydarzenie", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION && !title.getText().trim().isEmpty()) {
            try {
                URL u = link.getText().isEmpty() ? null : new URL(link.getText());
                listaZdarzen.add(new Zdarzenie(title.getText(), date, u));
                odswiez();
                if (callback != null) callback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd formatu: " + ex.getMessage());
            }
        }
    }

    private void editEvent(ArrayList<Zdarzenie> events) {
        Zdarzenie selected = (Zdarzenie) JOptionPane.showInputDialog(this, 
                "Wybierz wydarzenie do edycji:", "Zarządzaj wydarzeniami",
                JOptionPane.QUESTION_MESSAGE, null, events.toArray(), events.get(0));

        if (selected == null) return;

        JTextField title = new JTextField(selected.getTytul());
        JTextField dateField = new JTextField(selected.getData().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JTextField linkField = new JTextField(selected.getLink() == null ? "" : selected.getLink().toString());

        Object[] form = {
            "Tytuł:", title,
            "Data i godzina:", dateField,
            "Link:", linkField
        };

        if (JOptionPane.showConfirmDialog(this, form, "Edytuj", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                selected.setTytul(title.getText());
                selected.setData(LocalDateTime.parse(dateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                selected.setLink(linkField.getText().isEmpty() ? null : new URL(linkField.getText()));
                odswiez();
                if (callback != null) callback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage());
            }
        }
    }
}