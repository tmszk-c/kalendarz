package org.kalendarz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;

// Klasa narzędziowa do zapisu/odczytu XML przy użyciu biblioteki Jackson
public class ObslugaXML {

    public void zapiszOsoby(ArrayList<Osoba> listaOsob, String nazwaPliku) {
        try {
            XmlMapper xmlMapper = new XmlMapper();

            // Rejestracja modułu JavaTimeModule (obsługa nowych dat LocalDateTime)
            xmlMapper.registerModule(new JavaTimeModule());

            // Formatowanie: wyłączamy timestampy (czytelna data) i włączamy wcięcia (ładny XML)
            xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

            xmlMapper.writeValue(new File(nazwaPliku), listaOsob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Osoba> wczytajOsoby(String nazwaPliku) {
        ArrayList<Osoba> lista = new ArrayList<>();
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());

            // TypeReference jest konieczny, aby Java wiedziała, że wczytujemy listę konkretnie typu OSOBA
            // (rozwiązuje problem tzw. Type Erasure)
            lista = xmlMapper.readValue(new File(nazwaPliku), new TypeReference<ArrayList<Osoba>>() {});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

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