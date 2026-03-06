package com.devpro.devlearningroadmapmanager.email.service.impl;

import com.devpro.devlearningroadmapmanager.email.dto.MessageContact;
import com.devpro.devlearningroadmapmanager.email.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    // Spring injecte automatiquement le JavaMailSender configuré dans application.properties
    private final JavaMailSender mailSender;

    // Récupère l'adresse Gmail configurée dans spring.mail.username
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendContactMessage(MessageContact message) {
        try {
            // Horodatage automatique au moment de l'envoi
            message.setDateEnvoi(Instant.now());

            // SimpleMailMessage = email basique sans HTML ni pièce jointe
            SimpleMailMessage mail = new SimpleMailMessage();

            // Destinataire : ton adresse Gmail — c'est toi qui reçois le message
            mail.setTo(fromEmail);

            // Expéditeur : obligatoirement ton adresse Gmail (Gmail refuse les autres)
            mail.setFrom(fromEmail);

            // Reply-To : quand tu cliques "Répondre", la réponse ira au visiteur
            // et non à ton propre Gmail
            mail.setReplyTo(message.getEmail());

            // Sujet préfixé pour identifier facilement les messages de contact
            mail.setSubject("[EducationPriorité] " + message.getSujet());

            // Corps du mail en texte brut avec les infos du visiteur
            mail.setText(
                    "Nom : " + message.getNom() + "\n" +
                            "Email : " + message.getEmail() + "\n" +
                            "Date : " + message.getDateEnvoi() + "\n\n" +
                            "Message : \n" + message.getMessage()
            );

            // Déclenche l'envoi via le serveur SMTP de Gmail
            mailSender.send(mail);

        } catch (Exception e) {
            // Remonte l'erreur proprement si Gmail est injoignable ou si les credentials sont invalides
            throw new RuntimeException("Erreur envoi email : " + e.getMessage(), e);
        }
    }
}
