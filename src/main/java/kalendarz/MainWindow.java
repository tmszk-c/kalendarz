package kalendarz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private ArrayList<Osoba> listaOsob = new ArrayList<>();
    private ArrayList<Zdarzenie> listaZdarzen = new ArrayList<>();

    private JTable tableOsoby;
    private JTable tableZdarzenia;
    private JLabel statusBar;

    private DefaultTableModel modelOsoby;
    private DefaultTableModel modelZdarzenia;

    private JCalendarWidget kalendarzWidget;

    private int nextOsobaId = 1;
    private int nextZdarzenieId = 1;

    private final ObslugaBazy obslugaBazy = new ObslugaBazy();
    private final ObslugaXML obslugaXML = new ObslugaXML();

    public MainWindow() {
        setTitle("Kalendarz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1100, 700);
        getContentPane().setLayout(new BorderLayout());

        // ================== MENU GÓRNE ==================
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menuObsluga = new JMenu("Obsługa");
        menuBar.add(menuObsluga);

        JMenuItem miWczytajBaze = new JMenuItem("Pobierz z bazy");
        JMenuItem miZapiszBaze = new JMenuItem("Zapisz do bazy");
        JMenuItem miWczytajXML = new JMenuItem("Wczytaj XML");
        JMenuItem miZapiszXML = new JMenuItem("Zapisz XML");
        JMenuItem miWyczysc = new JMenuItem("Wyczyść RAM");

        menuObsluga.add(miWczytajBaze);
        menuObsluga.add(miZapiszBaze);
        menuObsluga.addSeparator();
        menuObsluga.add(miWczytajXML);
        menuObsluga.add(miZapiszXML);
        menuObsluga.addSeparator();
        menuObsluga.add(miWyczysc);

        miWczytajBaze.addActionListener(e -> {
            if(obslugaBazy.wczytajDane(listaOsob, listaZdarzen)) {
                odswiezTabele();
                kalendarzWidget.setListaZdarzen(listaZdarzen);
                ustawStatus("Pobrano dane z bazy.");
            } else {
                ustawStatus("Błąd wczytywania danych z bazy.");
            }
        });

        miZapiszBaze.addActionListener(e -> {
            obslugaBazy.zapiszDane(listaOsob, listaZdarzen);
            ustawStatus("Zapisano dane do bazy.");
        });

        miWczytajXML.addActionListener(e -> {
            listaOsob = obslugaXML.wczytajOsoby("osoby.xml");
            listaZdarzen = obslugaXML.wczytajZdarzenia("zdarzenia.xml");
            odswiezTabele();
            kalendarzWidget.setListaZdarzen(listaZdarzen);
            ustawStatus("Wczytano dane z XML.");
        });

        miZapiszXML.addActionListener(e -> {
            obslugaXML.zapiszOsoby(listaOsob, "osoby.xml");
            obslugaXML.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
            ustawStatus("Zapisano dane do XML.");
        });

        miWyczysc.addActionListener(e -> {
            listaOsob.clear();
            listaZdarzen.clear();
            odswiezTabele();
            kalendarzWidget.setListaZdarzen(listaZdarzen);
            ustawStatus("RAM wyczyszczony.");
        });

        // ================== TABY ==================
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

     // ---------- TAB OSOBY ----------
        JPanel panelOsoby = new JPanel(new BorderLayout());
        modelOsoby = new DefaultTableModel(new Object[]{"ID", "Imię i Nazwisko", "Numer", "Email", "Liczba wydarzeń"}, 0);
        tableOsoby = new JTable(modelOsoby) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // zablokowanie edycji wszystkich komórek
            }
        };
        panelOsoby.add(new JScrollPane(tableOsoby), BorderLayout.CENTER);

        JPanel panelOsobyPrzyciski = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDodajOsobe = new JButton("Dodaj");
        JButton btnEdytujOsobe = new JButton("Edytuj");
        JButton btnUsunOsobe = new JButton("Usuń");
        panelOsobyPrzyciski.add(btnDodajOsobe);
        panelOsobyPrzyciski.add(btnEdytujOsobe);
        panelOsobyPrzyciski.add(btnUsunOsobe);
        panelOsoby.add(panelOsobyPrzyciski, BorderLayout.SOUTH);

        tabbedPane.addTab("Osoby", panelOsoby);

        // ---------- TAB ZDARZENIA ----------
        JPanel panelZdarzenia = new JPanel(new BorderLayout());
        modelZdarzenia = new DefaultTableModel(new Object[]{"ID", "Tytuł", "Data", "Link", "Liczba osób"}, 0);
        tableZdarzenia = new JTable(modelZdarzenia) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // zablokowanie edycji wszystkich komórek
            }
        };
        panelZdarzenia.add(new JScrollPane(tableZdarzenia), BorderLayout.CENTER);

        JPanel panelZdarzeniaPrzyciski = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDodajZdarzenie = new JButton("Dodaj");
        JButton btnEdytujZdarzenie = new JButton("Edytuj");
        JButton btnUsunZdarzenie = new JButton("Usuń");
        JButton btnPolacz = new JButton("Przypisz Osobę");
        JButton btnSortPopularnosc = new JButton("Sortuj wg popularności");
        panelZdarzeniaPrzyciski.add(btnDodajZdarzenie);
        panelZdarzeniaPrzyciski.add(btnEdytujZdarzenie);
        panelZdarzeniaPrzyciski.add(btnUsunZdarzenie);
        panelZdarzeniaPrzyciski.add(btnPolacz);
        panelZdarzeniaPrzyciski.add(btnSortPopularnosc);
        panelZdarzenia.add(panelZdarzeniaPrzyciski, BorderLayout.SOUTH);

        tabbedPane.addTab("Zdarzenia", panelZdarzenia);


        // ---------- TAB KALENDARZ ----------
        JPanel panelKalendarz = new JPanel(new BorderLayout());
        kalendarzWidget = new JCalendarWidget(listaZdarzen, this::odswiezTabele);
        panelKalendarz.add(kalendarzWidget, BorderLayout.CENTER);
        tabbedPane.addTab("Kalendarz", panelKalendarz);

        // ================== STATUS BAR ==================
        statusBar = new JLabel("Gotowy");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // ================== AKCJE OSOBY ==================
        btnDodajOsobe.addActionListener(e -> dodajLubEdytujOsobe(null));
        btnEdytujOsobe.addActionListener(e -> {
            int idx = tableOsoby.getSelectedRow();
            if (idx >= 0) dodajLubEdytujOsobe(listaOsob.get(idx));
            else ustawStatus("Wybierz osobę do edycji.");
        });
        btnUsunOsobe.addActionListener(e -> {
            int idx = tableOsoby.getSelectedRow();
            if (idx >= 0) {
                Osoba o = listaOsob.remove(idx);
                for (Zdarzenie z : listaZdarzen) z.usunOsobe(o);
                odswiezTabele();
                kalendarzWidget.setListaZdarzen(listaZdarzen);
                zapiszAutomatycznie();
                ustawStatus("Usunięto osobę: " + o.getNazwa());
            } else ustawStatus("Wybierz osobę do usunięcia.");
        });

        // ================== AKCJE ZDARZENIA ==================
        btnDodajZdarzenie.addActionListener(e -> dodajLubEdytujZdarzenie(null));
        btnEdytujZdarzenie.addActionListener(e -> {
            int idx = tableZdarzenia.getSelectedRow();
            if (idx >= 0) dodajLubEdytujZdarzenie(listaZdarzen.get(idx));
            else ustawStatus("Wybierz zdarzenie do edycji.");
        });
        btnUsunZdarzenie.addActionListener(e -> {
            int idx = tableZdarzenia.getSelectedRow();
            if (idx >= 0) {
                Zdarzenie z = listaZdarzen.remove(idx);
                for (Osoba o : listaOsob) o.usunZadanie(z);
                odswiezTabele();
                kalendarzWidget.setListaZdarzen(listaZdarzen);
                zapiszAutomatycznie();
                ustawStatus("Usunięto zdarzenie: " + z.getTytul());
            } else ustawStatus("Wybierz zdarzenie do usunięcia.");
        });

        btnPolacz.addActionListener(e -> {
            polaczOsobeZeZdarzeniem();
            odswiezTabele();
            zapiszAutomatycznie();
        });

        btnSortPopularnosc.addActionListener(e -> {
            listaZdarzen.sort((a,b) -> b.getListaOsob().size() - a.getListaOsob().size());
            odswiezTabele();
            ustawStatus("Zdarzenia posortowane wg popularności.");
        });

        // ================== PIERWSZE ODŚWIEŻENIE I WCZYTANIE DANYCH Z BAZY ==================
        if(obslugaBazy.wczytajDane(listaOsob, listaZdarzen)) {
            // Ustawienie ID następnych elementów
            nextOsobaId = listaOsob.stream().mapToInt(Osoba::getId).max().orElse(0) + 1;
            nextZdarzenieId = listaZdarzen.stream().mapToInt(Zdarzenie::getId).max().orElse(0) + 1;
            odswiezTabele();
            kalendarzWidget.setListaZdarzen(listaZdarzen);
            ustawStatus("Dane z bazy załadowane przy starcie.");
        }

    }

    private void ustawStatus(String msg) {
        statusBar.setText(msg);
    }

    public void odswiezTabele() {
        modelOsoby.setRowCount(0);
        for (Osoba o : listaOsob) {
            modelOsoby.addRow(new Object[]{o.getId(), o.getNazwa(), o.getNumer(), o.getEmail(), o.getListaZadan().size()});
        }

        modelZdarzenia.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Zdarzenie z : listaZdarzen) {
            String dataStr = z.getData() != null ? z.getData().format(fmt) : "";
            modelZdarzenia.addRow(new Object[]{z.getId(), z.getTytul(), dataStr, z.getLink(), z.getListaOsob().size()});
        }
    }

    private void dodajLubEdytujOsobe(Osoba o) {
        JTextField txtNazwa = new JTextField();
        JTextField txtNumer = new JTextField();
        JTextField txtEmail = new JTextField();

        if (o != null) {
            txtNazwa.setText(o.getNazwa());
            txtNumer.setText(o.getNumer());
            txtEmail.setText(o.getEmail());
        }

        Object[] fields = {"Imię i Nazwisko:", txtNazwa, "Numer:", txtNumer, "Email:", txtEmail};
        int option = JOptionPane.showConfirmDialog(this, fields, o == null ? "Dodaj osobę" : "Edytuj osobę", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                if (o == null) {
                    Osoba nowa = new Osoba(txtNazwa.getText(), txtNumer.getText(), txtEmail.getText());
                    nowa.setId(nextOsobaId++);
                    listaOsob.add(nowa);
                    ustawStatus("Dodano osobę: " + nowa.getNazwa());
                } else {
                    o.setNazwa(txtNazwa.getText());
                    o.setNumer(txtNumer.getText());
                    o.setEmail(txtEmail.getText());
                    ustawStatus("Zaktualizowano osobę: " + o.getNazwa());
                }
                odswiezTabele();
                zapiszAutomatycznie();
            } catch (Exception e) {
                ustawStatus("Błąd: " + e.getMessage());
            }
        }
    }

    private void dodajLubEdytujZdarzenie(Zdarzenie z) {
        JTextField txtTytul = new JTextField();
        JTextField txtData = new JTextField("yyyy-MM-dd HH:mm");
        JTextField txtLink = new JTextField();

        if (z != null) {
            txtTytul.setText(z.getTytul());
            txtData.setText(z.getData().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            txtLink.setText(z.getLink() != null ? z.getLink().toString() : "");
        }

        Object[] fields = {"Tytuł:", txtTytul, "Data (yyyy-MM-dd HH:mm):", txtData, "Link:", txtLink};
        int option = JOptionPane.showConfirmDialog(this, fields, z == null ? "Dodaj zdarzenie" : "Edytuj zdarzenie", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                LocalDateTime dt = LocalDateTime.parse(txtData.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                URL url = txtLink.getText().isEmpty() ? null : new URL(txtLink.getText());

                if (z == null) {
                    Zdarzenie nowe = new Zdarzenie(txtTytul.getText(), dt, url);
                    nowe.setId(nextZdarzenieId++);
                    listaZdarzen.add(nowe);
                    ustawStatus("Dodano zdarzenie: " + nowe.getTytul());
                } else {
                    z.setTytul(txtTytul.getText());
                    z.setData(dt);
                    z.setLink(url);
                    ustawStatus("Zaktualizowano zdarzenie: " + z.getTytul());
                }
                odswiezTabele();
                kalendarzWidget.setListaZdarzen(listaZdarzen);
                zapiszAutomatycznie();
            } catch (Exception e) {
                ustawStatus("Błąd: " + e.getMessage());
            }
        }
    }

    private void polaczOsobeZeZdarzeniem() {
        if (listaOsob.isEmpty() || listaZdarzen.isEmpty()) {
            ustawStatus("Brak osób lub zdarzeń do połączenia.");
            return;
        }

        String[] osobyStr = listaOsob.stream().map(Osoba::getNazwa).toArray(String[]::new);
        String wybranaOsoba = (String) JOptionPane.showInputDialog(this, "Wybierz osobę:", "Połącz z wydarzeniem", JOptionPane.QUESTION_MESSAGE, null, osobyStr, osobyStr[0]);
        if (wybranaOsoba == null) return;
        Osoba o = listaOsob.stream().filter(x -> x.getNazwa().equals(wybranaOsoba)).findFirst().get();

        String[] zdarzeniaStr = listaZdarzen.stream().map(Zdarzenie::getTytul).toArray(String[]::new);
        String wybraneZdarzenie = (String) JOptionPane.showInputDialog(this, "Wybierz zdarzenie:", "Połącz z osobą", JOptionPane.QUESTION_MESSAGE, null, zdarzeniaStr, zdarzeniaStr[0]);
        if (wybraneZdarzenie == null) return;
        Zdarzenie z = listaZdarzen.stream().filter(x -> x.getTytul().equals(wybraneZdarzenie)).findFirst().get();

        o.dodajZadanie(z);
        z.dodajOsobe(o);
        odswiezTabele();
        kalendarzWidget.setListaZdarzen(listaZdarzen);
        ustawStatus("Połączono " + o.getNazwa() + " z " + z.getTytul());
        zapiszAutomatycznie();
    }

    // ================== AUTOMATYCZNY ZAPIS ==================
    private void zapiszAutomatycznie() {
        obslugaBazy.zapiszDane(listaOsob, listaZdarzen);
        obslugaXML.zapiszOsoby(listaOsob, "osoby.xml");
        obslugaXML.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}



