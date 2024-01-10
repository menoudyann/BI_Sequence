package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ObjectNotFoundException, IOException, URISyntaxException {

        GoogleDataObjectImpl dataObject = new GoogleDataObjectImpl("GOOGLE_APPLICATION_CREDENTIALS");
        GoogleLabelDetectorImpl labelDetector = new GoogleLabelDetectorImpl("GOOGLE_APPLICATION_CREDENTIALS");

        URI remoteFullPath = URI.create("gs://java.gogle.cld.education/testSequence.png");
        URI localFullPath = URI.create("file:///Users/yannmenoud/Desktop/CPNV/BI/Sequence/src/main/java/org/example/datas/testLabelDetector.jpg");

        dataObject.callAPI(remoteFullPath, localFullPath);

        URL url = dataObject.publish(URI.create("gs://java.gogle.cld.education/voiturerue.jpg"), 90);

        String response = labelDetector.analyze(url, 10, 90);
        Gson gson = new Gson();
        Type labelListType = new TypeToken<List<Label>>() {
        }.getType();
        List<Label> labels = gson.fromJson(response, labelListType);

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyyHHmmss");
        String formattedDateTime = currentDateTime.format(formatter);

        String filePath = "/Users/yannmenoud/Desktop/CPNV/BI/Sequence/src/main/java/org/example/sql/" + formattedDateTime + ".sql";

        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            for (Label label : labels) {
                String sqlContent = "INSERT INTO `labels` (`name`, `value`) VALUES ('" + label.getName() + "', '" + label.getScore() + "');\n";
                writer.write(sqlContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataObject.upload(URI.create("file:///Users/yannmenoud/Desktop/CPNV/BI/Sequence/src/main/java/org/example/sql/" + formattedDateTime + ".sql"), URI.create("gs://java.gogle.cld.education/" + formattedDateTime + ".sql"));
    }
}