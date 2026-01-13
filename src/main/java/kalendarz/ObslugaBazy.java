package kalendarz;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObslugaBazy {

    private static final String URL_DB = "jdbc:sqlserver://localhost:2222;databaseName=MojKalendarz;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "TwojeSilneHaslo123!";

    public Connection polacz() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL_DB, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Błąd połączenia z MSSQL: " + e.getMessage());
        }
        return conn;
    }

    // 2. ZAPIS DANYCH (RAM -> BAZA)
    public void zapiszDane(ArrayList<Osoba> osoby, ArrayList<Zdarzenie> zdarzenia) {
        try (Connection conn = DriverManager.getConnection(URL_DB, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();

            // Czyścimy tabele (Kolejność ważna ze względu na klucze obce!)
            stmt.execute("DELETE FROM Uczestnicy_Zdarzenia");
            stmt.execute("DELETE FROM Osoba");
            stmt.execute("DELETE FROM Zdarzenie");

            // --- ZAPIS OSÓB ---
            String insertOsoba = "INSERT INTO Osoba (nazwa, numer, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertOsoba, Statement.RETURN_GENERATED_KEYS)) {
                for (Osoba o : osoby) {
                    ps.setString(1, o.getNazwa());
                    ps.setString(2, o.getNumer());
                    ps.setString(3, o.getEmail());
                    ps.executeUpdate();

                    // Aktualizujemy ID w obiekcie Java (baza nadała nowe)
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) o.setId(rs.getInt(1));
                }
            }

            // --- ZAPIS ZDARZEŃ ---
            String insertZdarzenie = "INSERT INTO Zdarzenie (tytul, data, link) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertZdarzenie, Statement.RETURN_GENERATED_KEYS)) {
                for (Zdarzenie z : zdarzenia) {
                    ps.setString(1, z.getTytul());
                    // Konwersja LocalDateTime -> Timestamp (format zrozumiały dla SQL Servera)
                    ps.setTimestamp(2, Timestamp.valueOf(z.getData()));
                    ps.setString(3, z.getLink() != null ? z.getLink().toString() : "");
                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) z.setId(rs.getInt(1));
                }
            }

            // --- ZAPIS RELACJI (TABELA ŁĄCZNIKOWA) ---
            String insertRelacja = "INSERT INTO Uczestnicy_Zdarzenia (osoba_id, zdarzenie_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRelacja)) {
                for (Zdarzenie z : zdarzenia) {
                    for (Osoba o : z.getListaOsob()) {
                        ps.setInt(1, o.getId()); // Uwaga: w Twoim SQL najpierw jest osoba_id
                        ps.setInt(2, z.getId()); // potem zdarzenie_id
                        ps.executeUpdate();
                    }
                }
            }

            conn.commit(); // Zatwierdzenie zmian
            System.out.println("[DB] Dane zostały pomyślnie zsynchronizowane z bazą.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. ODCZYT DANYCH (BAZA -> RAM)
 // W ObslugaBazy.java zmieniamy sygnaturę metody:
    public boolean wczytajDane(ArrayList<Osoba> listaOsob, ArrayList<Zdarzenie> listaZdarzen) {
        listaOsob.clear();
        listaZdarzen.clear();

        Map<Integer, Osoba> mapaOsob = new HashMap<>();
        Map<Integer, Zdarzenie> mapaZdarzen = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(URL_DB, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();

            // A. Wczytaj Osoby
            ResultSet rsOsoby = stmt.executeQuery("SELECT * FROM Osoba");
            while (rsOsoby.next()) {
                Osoba o = new Osoba(rsOsoby.getString("nazwa"), rsOsoby.getString("numer"), rsOsoby.getString("email"));
                o.setId(rsOsoby.getInt("id"));
                listaOsob.add(o);
                mapaOsob.put(o.getId(), o);
            }

            // B. Wczytaj Zdarzenia
            ResultSet rsZdarzenia = stmt.executeQuery("SELECT * FROM Zdarzenie");
            while (rsZdarzenia.next()) {
                try {
                    Timestamp ts = rsZdarzenia.getTimestamp("data");
                    LocalDateTime dataCzas = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();
                    String linkStr = rsZdarzenia.getString("link");
                    URL url = null;
                    if (linkStr != null && !linkStr.isBlank()) {  // <- sprawdzamy też pusty string
                        try {
                            url = new URL(linkStr);
                        } catch (Exception e) {
                            System.err.println("Niepoprawny link: " + linkStr + ", ustawiam null");
                        }
                    }

                    Zdarzenie z = new Zdarzenie(rsZdarzenia.getString("tytul"), dataCzas, url);
                    z.setId(rsZdarzenia.getInt("id"));
                    listaZdarzen.add(z);
                    mapaZdarzen.put(z.getId(), z);
                } catch (Exception e) {
                    System.err.println("Błąd odczytu zdarzenia: " + e.getMessage());
                }
            }

            // C. Odtwórz Relacje
            ResultSet rsRelacje = stmt.executeQuery("SELECT * FROM Uczestnicy_Zdarzenia");
            while (rsRelacje.next()) {
                Osoba o = mapaOsob.get(rsRelacje.getInt("osoba_id"));
                Zdarzenie z = mapaZdarzen.get(rsRelacje.getInt("zdarzenie_id"));
                if (z != null && o != null) {
                    z.dodajOsobe(o);
                    o.dodajZadanie(z);
                }
            }

            System.out.println("[DB] Wczytano dane z bazy.");
            return true; // Sukces

        } catch (SQLException e) {
            System.err.println("[DB BŁĄD] Nie udało się wczytać z bazy: " + e.getMessage());
            return false; // Porażka - baza nie odpowiedziała
        }
    }
}