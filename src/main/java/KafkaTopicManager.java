import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaTopicManager {

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092"; // Adresa Kafka
        String kafkaTopic = "news-topic"; // Topicul unde vom trimite datele
        String apiTopic = "technology"; // Subiect pentru API-ul de știri

        // Configurare AdminClient
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        AdminClient adminClient = AdminClient.create(adminProps);

        // Configurare Producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        try {
            // Creează topicul dacă nu există
            if (!topicExists(adminClient, kafkaTopic)) {
                System.out.println("Topic " + kafkaTopic + " nu există. Se creează...");
                createTopic(adminClient, apiTopic);
            }

            // Obține știri din API
            ApiDataRetriever apiDataRetriever = new ApiDataRetriever();
            String[] newsData = apiDataRetriever.returnNews(apiTopic);



            // Trimite datele la Kafka
            System.out.println("Trimit datele din API către Kafka...");

            for(String item: newsData) {
                producer.send(new ProducerRecord<>(kafkaTopic, null, item), (metadata, exception) -> {
                    if (exception != null) {
                        System.err.println("Eroare la trimiterea mesajului: " + exception.getMessage());
                    } else {
                        System.out.println("Mesaj trimis către topicul '" + kafkaTopic + "' la partiția " + metadata.partition());
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
            adminClient.close();
        }
    }

    // Verifică dacă un topic există
    private static boolean topicExists(AdminClient adminClient, String topicName) {
        try {
            return adminClient.listTopics().names().get().contains(topicName);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Creează un topic Kafka
    private static void createTopic(AdminClient adminClient, String topicName) {
        int numPartitions = 1; // Număr de partitii
        short replicationFactor = 1; // Factorul de replicare

        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            System.out.println("Topic " + topicName + " a fost creat cu succes.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.err.println("Eroare la crearea topicului " + topicName);
        }
    }
}