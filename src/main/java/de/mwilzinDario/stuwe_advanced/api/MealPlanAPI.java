package de.mwilzinDario.stuwe_advanced.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mwilzinDario.stuwe_advanced.models.MealItem;

public class MealPlanAPI {
    private static final String BASE_URL = "https://www.my-stuwe.de/wp-json/mealplans/v1/canteens/621";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Fetches the meal plan for the current day
    public static List<MealItem> fetchMealPlan() {
        try {
            String apiUrl = buildApiUrl();
            System.out.println("Fetching: " + apiUrl);
            
            HttpURLConnection conn = createConnection(apiUrl);
            String json = readResponse(conn);
            
            return parseMealItems(json);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Builds the API URL for the current day
    private static String buildApiUrl() {
        long epochMillis = Instant.now().toEpochMilli();
        return BASE_URL + "?lang=de&v=" + epochMillis;
    }

    // Creates a connection to the API
    private static HttpURLConnection createConnection(String apiUrl) throws Exception {
        URL url = new URI(apiUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    // Reads the response from the API and returns the content as a string
    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        
        StringBuilder content;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        conn.disconnect();
        
        return content.toString();
    }

    // Parses the meal items from the JSON response
    private static List<MealItem> parseMealItems(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        List<MealItem> mealItems = new ArrayList<>();
        JsonNode menus = root.path("621").path("menus");
        
        if (menus.isArray()) {
            for (JsonNode menuNode : menus) {
                MealItem item = new MealItem();
                item.id = menuNode.path("id").asText();
                item.menuLine = menuNode.path("menuLine").asText();
                item.studentPrice = menuNode.path("studentPrice").asText();
                item.menuDate = menuNode.path("menuDate").asText();
                
                parseMenuArray(menuNode, item);
                parseIconsArray(menuNode, item);
                
                mealItems.add(item);
            }
        }
        
        return mealItems;
    }

    // Parses the menu array from the JSON response
    private static void parseMenuArray(JsonNode menuNode, MealItem item) {
        JsonNode menuArr = menuNode.path("menu");
        if (menuArr.isArray()) {
            item.menu = new String[menuArr.size()];
            for (int i = 0; i < menuArr.size(); i++) {
                item.menu[i] = menuArr.get(i).asText();
            }
        }
    }

    // Parses the icons array from the JSON response for filtering by dietary preferences
    private static void parseIconsArray(JsonNode menuNode, MealItem item) {
        JsonNode iconsArr = menuNode.path("icons");
        if (iconsArr.isArray()) {
            item.icons = new String[iconsArr.size()];
            for (int i = 0; i < iconsArr.size(); i++) {
                item.icons[i] = iconsArr.get(i).asText();
            }
        }
    }
} 