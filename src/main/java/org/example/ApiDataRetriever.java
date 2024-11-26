package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiDataRetriever {

    // Define the API endpoint
    private static final String API_URL = "https://newsapi.org/v2/everything?q=%s&from=2024-10-26&sortBy=popularity&apiKey=a65aa431ff2442bb8b8d16709eb70962";


    public String returnNews(String topic){



        try {

            String finalUrl=String.format(API_URL,topic);
            // Fetch data from the API
            String jsonData = fetchDataFromApi(finalUrl);

            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonObject.put("topic",topic);
            JSONArray jsonArray = jsonObject.getJSONArray("articles");

// Create a new array to store valid articles
            JSONArray filteredArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject article = jsonArray.getJSONObject(i);

                // Check if any of the specified keys are null
                if (!article.optString("description", null).toString().equals("[Removed]")  &&
                        !article.optString("title", null).toString().equals("[Removed]") &&
                        !article.optString("content", null).toString().equals("[Removed]")) {

                    // If none of the keys are null, add the article to the filtered array
                    removeKeys(article, "status", "totalResults", "urlToImage", "source", "url", "publishedAt");
                    filteredArray.put(article);
                }
            }

// Replace the original array with the filtered array
            jsonObject.put("articles", filteredArray);


            // Example: Access specific data from the JSON object
            return ("Response JSON: " + jsonObject.toString(4)); // Pretty print JSON
        } catch (Exception e) {
            return ("Error retrieving data: " + e.getMessage());

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
        // Create an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET() // Specify the HTTP method
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the response code is 200 (OK)
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch data. HTTP status code: " + response.statusCode());
        }


        return response.body(); // Return the response body

    }

    private static void removeKeys(JSONObject jsonObject, String... keys) {
        for (String key : keys) {
            jsonObject.remove(key);
        }
    }
}
