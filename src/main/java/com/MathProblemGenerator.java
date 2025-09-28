package com;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;


    

public class MathProblemGenerator {

    // Your Gemini API Key should be set as an environment variable
    
     // specify the directory where your .env file is located
    
     private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";

    /**
     * Generates a math problem using the Gemini API.
     * @param gradeLevel The target grade level (e.g., "5th grade").
     * @param problemType The type of math problem (e.g., "fractions word problem").
     * @return The generated math problem as a string.
     * @throws IOException If there is a network error.
     * @throws InterruptedException If the connection is interrupted.
     */
    public static String generateMathProblem(String gradeLevel, String problemType) throws IOException, InterruptedException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: GEMINI_API_KEY environment variable not set.";
        }

        // 1. Construct the prompt for the AI model
        String prompt = String.format(
             "Generate one math problem of the type '%s' for a '%s' student. " +
            "Create a JSON object with three keys: " +
            "1. 'problem': A string containing the question. " +
            "2. 'answers': A JSON array of 4 string options. One is the correct answer, and the other three are plausible incorrect answers. " +
            "3. 'correctIndex': An integer from 0 to 3 indicating the index of the correct answer in the 'answers' array. " +
            "Do not include any text outside of the JSON object.",
            problemType, gradeLevel
        );

        // 2. Create the JSON payload for the API request
        String jsonPayload = new JSONObject()
            .put("contents", new JSONArray()
                .put(new JSONObject()
                    .put("parts", new JSONArray()
                        .put(new JSONObject().put("text", prompt)))))
            .toString();

        // 3. Set up the HTTP client and request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + "?key=" + API_KEY))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
            
        // 4. Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. Parse the response to extract the generated text
        if (response.statusCode() == 200) {
            JSONObject responseBody = new JSONObject(response.body());
            return responseBody.getJSONArray("candidates")
                               .getJSONObject(0)
                               .getJSONObject("content")
                               .getJSONArray("parts")
                               .getJSONObject(0)
                               .getString("text");
        } else {
            return "Error: Failed to generate problem. Status code: " + response.statusCode() + "\nResponse: " + response.body();
        }
    }

    public static void main(String[] args) {
        // --- Define the desired math problem ---
        String gradeLevel = "4th Grade";
        String problemType = "Division with remainders";

        System.out.println("Generating a " + problemType + " problem for a " + gradeLevel + " student...");
        System.out.println("--------------------------------------------------\n");

        try {
            String mathProblem = generateMathProblem(gradeLevel, problemType);
            System.out.println(mathProblem);
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred while calling the API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

