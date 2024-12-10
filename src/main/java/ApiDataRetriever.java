import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiDataRetriever {

    // Define the API endpoint
    private static final String API_URL = "https://newsapi.org/v2/everything?q=%s&from=2024-11-10&sortBy=publishedAt&apiKey=%s";

    /**
     * Retrieves news articles for a given topic.
     * @param topic The topic to search for.
     * @return A simple array of strings, with each string representing an article formatted by newlines.
     */
    public String[] returnNews(String topic) {
        try {
            // Load API key from .env file
            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("APITOKEN");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("API token is missing or invalid.");
            }

            // Construct the final URL
            String finalUrl = String.format(API_URL, topic, apiKey);

            // Fetch data from the API
            String jsonData = fetchDataFromApi(finalUrl);

            // Parse and filter the JSON response, then return the formatted array of strings
            return processJsonResponse(jsonData);

        } catch (Exception e) {
            // Return error message if an exception occurs
            System.err.println("Error retrieving data: " + e.getMessage());
            return new String[]{"Error retrieving data: " + e.getMessage()};  // Return error message as an array
        }
    }

    /**
     * Fetch data from the given API endpoint.
     * @param apiUrl The API URL.
     * @return The JSON response as a String.
     * @throws IOException If there's an I/O error.
     * @throws InterruptedException If the request is interrupted.
     */
    private static String fetchDataFromApi(String apiUrl) throws IOException, InterruptedException {
        // Create an HTTP client configured for HTTP/2
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the response code is 200 (OK)
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch data. HTTP status code: " + response.statusCode());
        }

        return response.body(); // Return the response body
    }

    /**
     * Processes the JSON response from the API and formats the articles into strings.
     * @param jsonData The raw JSON response as a string.
     * @return An array of strings where each string is an article formatted by newlines.
     */
    private String[] processJsonResponse(String jsonData) {
        JSONObject jsonObject = new JSONObject(jsonData);

        // Retrieve the articles array
        JSONArray articles = jsonObject.getJSONArray("articles");

        // Create a list to store the formatted strings for each article
        List<String> formattedArticles = new ArrayList<>();

        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);

            // Check if any critical fields are "[Removed]" and exclude those
            if (!article.optString("description", "[Removed]").equals("[Removed]") &&
                    !article.optString("content", "[Removed]").equals("[Removed]")) {

                // Build a formatted string for the article
                StringBuilder formattedArticle = new StringBuilder();
                formattedArticle.append("Title: ").append(article.optString("title", "N/A")).append("\n");
                formattedArticle.append("Description: ").append(article.optString("description", "N/A")).append("\n");
                formattedArticle.append("Content: ").append(article.optString("content", "N/A")).append("\n");

                // Add the formatted article to the list
                formattedArticles.add(formattedArticle.toString());
            }
        }

        // Return the list as an array of strings
        return formattedArticles.toArray(new String[0]);
    }

    public static void main(String[] args) {
        ApiDataRetriever retriever = new ApiDataRetriever();
        // Example usage with topic "technology"
        String[] result = retriever.returnNews("technology");

        // Print each article from the result array
        for (String article : result) {
            System.out.println(article);
            System.out.println("---");
        }
    }
}
