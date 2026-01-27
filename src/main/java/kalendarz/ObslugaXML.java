package kalendarz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.util.ArrayList;

/**
 * Klasa narzędziowa do obsługi zapisu i odczytu danych w formacie XML.
 * Wykorzystuje bibliotekę Jackson z modułem JavaTime do obsługi nowoczesnych typów dat.
 */
public class ObslugaXML {

    /**
     * Zapisuje listę osób do pliku XML z zachowaniem czytelnego formatowania.
     * @param listaOsob Lista obiektów Osoba do zapisania.
     * @param nazwaPliku Ścieżka do pliku wyjściowego.
     */
    public void zapiszOsoby(ArrayList<Osoba> listaOsob, String nazwaPliku) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.writeValue(new File(nazwaPliku), listaOsob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wczytuje listę osób z pliku XML.
     * @param nazwaPliku Ścieżka do pliku XML.
     * @return ArrayList wczytanych osób lub pusta lista w razie błędu.
     */
    public ArrayList<Osoba> wczytajOsoby(String nazwaPliku) {
        ArrayList<Osoba> lista = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            lista = xmlMapper.readValue(new File(nazwaPliku), new TypeReference<ArrayList<Osoba>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Zapisuje listę zdarzeń do pliku XML.
     * @param listaZdarzen Lista obiektów Zdarzenie.
     * @param nazwaPliku Ścieżka do pliku.
     */
    public void zapiszZdarzenia(ArrayList<Zdarzenie> listaZdarzen, String nazwaPliku) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.writeValue(new File(nazwaPliku), listaZdarzen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wczytuje listę zdarzeń z pliku XML.
     * @param nazwaPliku Ścieżka do pliku.
     * @return ArrayList zdarzeń.
     */
    public ArrayList<Zdarzenie> wczytajZdarzenia(String nazwaPliku) {
        ArrayList<Zdarzenie> lista = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            lista = xmlMapper.readValue(new File(nazwaPliku), new TypeReference<ArrayList<Zdarzenie>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}