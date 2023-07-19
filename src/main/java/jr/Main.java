package jr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import telegram.TelegramNotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String BASE_URL = "https://javarush.com/api/1.0/rest/tasks/taskcom.javarush.games.snake.";
    private static final String SESSION_COOKIE = "JSESSIONID=777fd713-2910-46d2-8c07-de7dda0bbd02";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    private static final int NUMBER_OF_TASKS = 20;
    private static final boolean IS_LOGGING = false;
    private static final StringBuilder messageBuilder = new StringBuilder();

    public static void main(String[] args) {
        String resetUrl = BASE_URL + "part" + NUMBER_OF_TASKS + "/reset";
        TelegramNotifier notifier = new TelegramNotifier();
        messageBuilder.append(executeResetRequest(resetUrl));

        for (int i = 1; i <= NUMBER_OF_TASKS; i++) {
            String url = BASE_URL + "part" + String.format("%02d", i) + "/solution";
            try {
                String json = getJsonFromUrl(url);
                List<FileInfo> files = parseFilesFromJson(json);
                String requestBody = generateRequestBody(files);

                String runUrl = url.replace("/solution", "/run");
                int responseCode = makePostRequest(runUrl, requestBody);

                if (responseCode == 200) {
                    messageBuilder.append("\nTask ").append(i).append(": Ok");
                } else {
                    messageBuilder.append("\nTask ").append(i).append(": Failed");
                    break;
                }
            } catch (IOException e) {
                messageBuilder.append("\nTask ").append(i).append(": Failed");
                messageBuilder.append("\nAn error occurred: ").append(e.getMessage());
                break;
            }
        }

        messageBuilder.append(executeResetRequest(resetUrl));

        try {
            if (getAchievements().contains("Мегатрон")) {
                messageBuilder.append("\nМегатрон получен");
            }
        } catch (IOException e) {
            messageBuilder.append("\nAn error occurred: ").append(e.getMessage());
        }

        notifier.sendNotification(messageBuilder.toString());
    }

    private static String getJsonFromUrl(String urlString) throws IOException {
        if (IS_LOGGING) {
            messageBuilder.append("\nMaking GET request to: ").append(urlString);
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", SESSION_COOKIE);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static List<FileInfo> parseFilesFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode filesNode = rootNode.path("files");

        List<FileInfo> files = new ArrayList<>();
        for (JsonNode fileNode : filesNode) {
            String path = fileNode.path("path").asText();
            String content = fileNode.path("content").asText();
            files.add(new FileInfo(path, content));
        }

        return files;
    }

    private static String generateRequestBody(List<FileInfo> files) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode()
                .putPOJO("files", files)
                .put("inputData", "")
                .putPOJO("jobTypes", List.of("COMPILE", "VALIDATE"));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    private static String getAchievements() throws IOException {
        URL url = new URL("https://javarush.com/api/1.0/rest/achievements/users/current");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", SESSION_COOKIE);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static int makePostRequest(String urlString, String requestBody) throws IOException {
        if (IS_LOGGING) {
            messageBuilder.append("Making POST request to: ").append(urlString);
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Cookie", SESSION_COOKIE);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);

        try (var outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        return connection.getResponseCode();
    }

    private static String executeResetRequest(String resetUrl) {
        try {
            int responseCode = makePostRequest(resetUrl, "");
            if (responseCode == 200) {
                return "\nReset: Ok";
            } else {
                return "\nReset: Failed";
            }
        } catch (IOException e) {
            return "\nReset: Failed" + "\nAn error occurred: " + e.getMessage();
        }
    }
}