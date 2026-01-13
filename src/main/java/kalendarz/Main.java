package kalendarz;

import javax.swing.SwingUtilities;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final int OPCJA_WYSWIETL_OSOBY = 1;
    private static final int OPCJA_WYSWIETL_ZDARZENIA = 2;
    private static final int OPCJA_ZDARZENIA_POPULARNOSC = 3;
    private static final int OPCJA_DODAJ_OSOBE = 4;
    private static final int OPCJA_DODAJ_ZDARZENIE = 5;
    private static final int OPCJA_USUN_OSOBE = 6;
    private static final int OPCJA_USUN_ZDARZENIE = 7;
    private static final int OPCJA_ZAPISZ_XML = 8;
    private static final int OPCJA_WCZYTAJ_XML = 9;
    private static final int OPCJA_WYCZYSC_RAM = 10;
    private static final int OPCJA_POLACZ_RELACJE = 11;
    private static final int OPCJA_WYJSCIE = 0;

    private static int licznikAkcji = 0;
    private static final int LIMIT_AKCJI_DO_ZAPISU = 10;

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equals("-txt")) {
            uruchomTekstowo();
        }
        else {
            SwingUtilities.invokeLater(() -> {
                new MainWindow().setVisible(true);
            });
        }
    }

    // ===================== TRYB TEKSTOWY =====================

    public static void uruchomTekstowo() {

        ObslugaXML xml = new ObslugaXML();
        ObslugaBazy baza = new ObslugaBazy();
        Scanner scanner = new Scanner(System.in);

        ArrayList<Osoba> listaOsob = new ArrayList<>();
        ArrayList<Zdarzenie> listaZdarzen = new ArrayList<>();

        System.out.println("Inicjalizacja danych...");
        boolean sukcesBaza = baza.wczytajDane(listaOsob, listaZdarzen);

        if (!sukcesBaza) {
            System.out.println("[INFO] Próba awaryjnego wczytania danych z plików XML...");

            ArrayList<Osoba> zXmlOsoby = xml.wczytajOsoby("kontakty.xml");
            ArrayList<Zdarzenie> zXmlZdarzenia = xml.wczytajZdarzenia("zdarzenia.xml");

            if (!zXmlOsoby.isEmpty() || !zXmlZdarzenia.isEmpty()) {
                listaOsob.addAll(zXmlOsoby);
                listaZdarzen.addAll(zXmlZdarzenia);
                System.out.println("[SUKCES] Wczytano dane z plików XML.");
            }
        }

        int wybor;
        while (true) {
            wyswietlMenu();

            if (scanner.hasNextInt()) {
                wybor = scanner.nextInt();
                scanner.nextLine();

                switch (wybor) {

                    case OPCJA_WYSWIETL_OSOBY -> {
                        listaOsob.sort(null);
                        wyswietlListe(listaOsob, "KONTAKTY");
                    }

                    case OPCJA_WYSWIETL_ZDARZENIA -> {
                        listaZdarzen.sort(null);
                        wyswietlListe(listaZdarzen, "ZDARZENIA");
                    }

                    case OPCJA_ZDARZENIA_POPULARNOSC -> {
                        listaZdarzen.sort(new SortowanieZdarzenieLiczbaOsob());
                        wyswietlListe(listaZdarzen, "ZDARZENIA (POPULARNOŚĆ)");
                    }

                    case OPCJA_DODAJ_OSOBE -> {
                        System.out.println("\n--- DODAJ OSOBĘ ---");
                        String nazwa = wczytajNiepustyTekst(scanner, "Imię i Nazwisko");
                        System.out.print("Telefon: ");
                        String telefon = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        try {
                            listaOsob.add(new Osoba(nazwa, telefon, email));
                            System.out.println("SUKCES: Dodano osobę.");
                            xml.zapiszOsoby(listaOsob, "kontakty.xml");
                            obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                        }
                        catch (IllegalArgumentException e) {
                            System.err.println("Błąd: " + e.getMessage());
                        }
                    }

                    case OPCJA_DODAJ_ZDARZENIE -> {
                        System.out.println("\n--- DODAJ ZDARZENIE ---");
                        String nazwa = wczytajNiepustyTekst(scanner, "Nazwa zdarzenia");
                        LocalDateTime dataCzas = wczytajDateICzas(scanner);
                        URL link = wczytajURL(scanner);

                        listaZdarzen.add(new Zdarzenie(nazwa, dataCzas, link));
                        System.out.println("SUKCES: Dodano zdarzenie.");

                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                    }

                    case OPCJA_USUN_OSOBE -> {
                        if (listaOsob.isEmpty()) {
                            System.out.println("Brak osób do usunięcia.");
                        }
                        else {
                            wyswietlListe(listaOsob, "Wybierz ID do usunięcia");
                            int idx = wczytajIndeks(scanner);

                            if (idx >= 0 && idx < listaOsob.size()) {
                                Osoba usuwana = listaOsob.get(idx);
                                for (Zdarzenie z : listaZdarzen) z.usunOsobe(usuwana);
                                listaOsob.remove(idx);
                                System.out.println("Usunięto: " + usuwana.getNazwa());

                                xml.zapiszOsoby(listaOsob, "kontakty.xml");
                                xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                                obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                            }
                        }
                    }

                    case OPCJA_USUN_ZDARZENIE -> {
                        if (listaZdarzen.isEmpty()) {
                            System.out.println("Brak zdarzeń do usunięcia.");
                        }
                        else {
                            wyswietlListe(listaZdarzen, "Wybierz ID do usunięcia");
                            int idx = wczytajIndeks(scanner);

                            if (idx >= 0 && idx < listaZdarzen.size()) {
                                Zdarzenie usuwane = listaZdarzen.get(idx);
                                for (Osoba o : listaOsob) o.usunZadanie(usuwane);
                                listaZdarzen.remove(idx);
                                System.out.println("Usunięto: " + usuwane.getTytul());

                                xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                                xml.zapiszOsoby(listaOsob, "kontakty.xml");
                                obsluzAutoZapis(baza, listaOsob, listaZdarzen);
                            }
                        }
                    }

                    case OPCJA_ZAPISZ_XML -> {
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        System.out.println("Zapisano XML.");
                    }

                    case OPCJA_WCZYTAJ_XML -> {
                        listaOsob = xml.wczytajOsoby("kontakty.xml");
                        listaZdarzen = xml.wczytajZdarzenia("zdarzenia.xml");
                        System.out.println("Wczytano XML.");
                    }

                    case OPCJA_WYCZYSC_RAM -> {
                        listaOsob.clear();
                        listaZdarzen.clear();
                        System.out.println("Wyczyszczono RAM.");
                    }

                    case OPCJA_POLACZ_RELACJE -> {
                        if (!listaOsob.isEmpty() && !listaZdarzen.isEmpty()) {
                            wyswietlListe(listaOsob, "Wybierz Osobę");
                            int io = wczytajIndeks(scanner);
                            wyswietlListe(listaZdarzen, "Wybierz Zdarzenie");
                            int iz = wczytajIndeks(scanner);

                            if (io >= 0 && io < listaOsob.size() && iz >= 0 && iz < listaZdarzen.size()) {
                                Osoba o = listaOsob.get(io);
                                Zdarzenie z = listaZdarzen.get(iz);
                                o.dodajZadanie(z);
                                z.dodajOsobe(o);
                                System.out.println("Połączono: " + o.getNazwa() + " <-> " + z.getTytul());
                            }
                        }
                    }

                    case OPCJA_WYJSCIE -> {
                        xml.zapiszOsoby(listaOsob, "kontakty.xml");
                        xml.zapiszZdarzenia(listaZdarzen, "zdarzenia.xml");
                        baza.zapiszDane(listaOsob, listaZdarzen);
                        scanner.close();
                        return;
                    }
                }
            }
            else {
                System.out.println("Wprowadź liczbę!");
                scanner.nextLine();
            }
        }
    }

    // ===================== METODY POMOCNICZE =====================

    public static void wyswietlMenu() {
        System.out.println("\n--- MENU GŁÓWNE KALENDARZA ---");
        System.out.println("1. Wyświetl Osoby");
        System.out.println("2. Wyświetl Zdarzenia");
        System.out.println("3. Zdarzenia wg popularności");
        System.out.println("4. Dodaj Osobę");
        System.out.println("5. Dodaj Zdarzenie");
        System.out.println("6. Usuń Osobę");
        System.out.println("7. Usuń Zdarzenie");
        System.out.println("8. Zapisz XML");
        System.out.println("9. Wczytaj XML");
        System.out.println("10. Wyczyść RAM");
        System.out.println("11. Połącz Osobę ze Zdarzeniem");
        System.out.println("0. Wyjdź");
        System.out.print("Wybór: ");
    }

    private static void obsluzAutoZapis(ObslugaBazy baza, ArrayList<Osoba> osoby, ArrayList<Zdarzenie> zdarzenia) {
        licznikAkcji++;
        if (licznikAkcji >= LIMIT_AKCJI_DO_ZAPISU) {
            baza.zapiszDane(osoby, zdarzenia);
            licznikAkcji = 0;
        }
    }

    private static void wyswietlListe(ArrayList<?> lista, String tytul) {
        System.out.println("\n--- " + tytul + " ---");
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("[" + i + "] " + lista.get(i));
        }
    }

    private static String wczytajNiepustyTekst(Scanner sc, String komunikat) {
        String tekst = "";
        while (tekst.trim().isEmpty()) {
            System.out.print(komunikat + ": ");
            tekst = sc.nextLine();
        }
        return tekst;
    }

    private static int wczytajIndeks(Scanner sc) {
        System.out.print("Podaj ID: ");
        if (sc.hasNextInt()) {
            int i = sc.nextInt();
            sc.nextLine();
            return i;
        }
        sc.nextLine();
        return -1;
    }

    private static LocalDateTime wczytajDateICzas(Scanner sc) {
        while (true) {
            try {
                System.out.print("Data (RRRR-MM-DD): ");
                LocalDate d = LocalDate.parse(sc.nextLine());
                System.out.print("Czas (HH:MM): ");
                LocalTime t = LocalTime.parse(sc.nextLine());
                return LocalDateTime.of(d, t);
            }
            catch (DateTimeParseException e) {
                System.out.println("Błąd formatu.");
            }
        }
    }

    private static URL wczytajURL(Scanner sc) {
        while (true) {
            try {
                System.out.print("Link (http...): ");
                return new URL(sc.nextLine());
            }
            catch (MalformedURLException e) {
                System.out.println("Zły format URL.");
            }
        }
    }
}
