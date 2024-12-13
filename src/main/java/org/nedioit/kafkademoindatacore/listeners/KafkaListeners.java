package org.nedioit.kafkademoindatacore.listeners;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    // Définition du composant Listener(Ecouteur) qui écoute les connexions entrantes dans le broker (producer & consumer)
    // On a choisi de grouper toutes les connexions entrantes vers le topic indatacore en groupId
    // Pour vérifier que le hôte fonctionne bien


    @KafkaListener(
            topics = "RQV5001-request-topic",
            groupId = "groupId"
    )
    void kafkaListener(String data){
        System.out.println("Listener received :" + data);
    }
}