package dansplugins.currencies.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dansplugins.currencies.Currencies;
import dansplugins.currencies.data.PersistentData;
import dansplugins.currencies.objects.Coinpurse;
import dansplugins.currencies.objects.Currency;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel McCoy Stephenson
 */
public class StorageService {
    private final ConfigService configService;
    private final Currencies currencies;
    private final PersistentData persistentData;

    private final static String FILE_PATH = "./plugins/Currencies/";
    private final static String ACTIVE_CURRENCIES_FILE_NAME = "activeCurrencies.json";
    private final static String RETIRED_CURRENCIES_FILE_NAME = "retiredCurrencies.json";
    private final static String COINPURSES_FILE_NAME = "coinpurses.json";

    private final static Type LIST_MAP_TYPE = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();;

    public StorageService(ConfigService configService, Currencies currencies, PersistentData persistentData) {
        this.configService = configService;
        this.currencies = currencies;
        this.persistentData = persistentData;
    }

    public void save() {
        saveActiveCurrencies();
        saveRetiredCurrencies();
        saveCoinpurses();
        if (configService.hasBeenAltered()) {
            currencies.saveConfig();
        }
    }

    public void load() {
        loadActiveCurrencies();
        loadRetiredCurrencies();
        loadCoinpurses();
    }

    private void saveActiveCurrencies() {
        // save each currency object individually
        List<Map<String, String>> currencies = new ArrayList<>();
        for (Currency currency : persistentData.getActiveCurrencies()){
            currencies.add(currency.save());
        }

        writeOutFiles(currencies, ACTIVE_CURRENCIES_FILE_NAME);
    }

    private void saveRetiredCurrencies() {
        // save each currency object individually
        List<Map<String, String>> currencies = new ArrayList<>();
        for (Currency currency : persistentData.getRetiredCurrencies()){
            currencies.add(currency.save());
        }

        writeOutFiles(currencies, RETIRED_CURRENCIES_FILE_NAME);
    }

    private void saveCoinpurses() {
        // save each coinpurse object individually
        List<Map<String, String>> coinpurses = new ArrayList<>();
        for (Coinpurse coinpurse : persistentData.getCoinpurses()){
            coinpurses.add(coinpurse.save());
        }

        writeOutFiles(coinpurses, COINPURSES_FILE_NAME);
    }

    private void writeOutFiles(List<Map<String, String>> saveData, String fileName) {
        try {
            File parentFolder = new File(FILE_PATH);
            parentFolder.mkdir();
            File file = new File(FILE_PATH, fileName);
            file.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            outputStreamWriter.write(gson.toJson(saveData));
            outputStreamWriter.close();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.toString());
        }
    }

    private void loadActiveCurrencies() {
        persistentData.getActiveCurrencies().clear();

        ArrayList<HashMap<String, String>> data = loadDataFromFilename(FILE_PATH + ACTIVE_CURRENCIES_FILE_NAME);

        for (Map<String, String> currencyData : data){
            Currency currency = new Currency(currencyData);
            persistentData.addActiveCurrency(currency);
        }
    }

    private void loadRetiredCurrencies() {
        persistentData.getRetiredCurrencies().clear();

        ArrayList<HashMap<String, String>> data = loadDataFromFilename(FILE_PATH + RETIRED_CURRENCIES_FILE_NAME);

        for (Map<String, String> currencyData : data){
            Currency currency = new Currency(currencyData);
            persistentData.addRetiredCurrency(currency);
        }
    }

    private void loadCoinpurses() {
        persistentData.getCoinpurses().clear();

        ArrayList<HashMap<String, String>> data = loadDataFromFilename(FILE_PATH + COINPURSES_FILE_NAME);

        for (Map<String, String> coinpurseData : data){
            Coinpurse coinpurse = new Coinpurse(coinpurseData, persistentData, currencies);
            persistentData.addCoinpurse(coinpurse);
        }
    }

    private ArrayList<HashMap<String, String>> loadDataFromFilename(String filename) {
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();;
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            return gson.fromJson(reader, LIST_MAP_TYPE);
        } catch (FileNotFoundException e) {
            // Fail silently because this can actually happen in normal use
        }
        return new ArrayList<>();
    }

}