package com.devpro.devlearningroadmapmanager.cloud.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    // Cloudinary est injecté automatiquement depuis CloudinaryConfig
    private final Cloudinary cloudinary;

    /**
     * Upload une image vers Cloudinary et retourne son URL publique.
     * L'image est stockée dans le dossier "educationpriorite" sur Cloudinary.
     *
     * @param file le fichier image envoyé depuis le formulaire Angular
     * @return l'URL HTTPS publique de l'image stockée sur Cloudinary
     */
    public String uploadImage(MultipartFile file) {
        try {
            // file.getBytes() convertit le fichier en tableau d'octets
            // car Cloudinary attend des bytes et non un MultipartFile directement
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            // "folder" organise les images dans un dossier sur Cloudinary
                            // Toutes les images seront dans : educationpriorite/nom_image
                            "folder", "educationpriorite"
                    )
            );

            // Cloudinary retourne un Map avec plusieurs infos sur l'image uploadée :
            // - "secure_url" : URL HTTPS publique → ex: https://res.cloudinary.com/divmsihdv/image/upload/v123/educationpriorite/image.jpg
            // - "public_id"  : identifiant unique pour supprimer l'image plus tard
            // - "width"      : largeur en pixels
            // - "height"     : hauteur en pixels
            // On retourne uniquement l'URL qui sera stockée en base de données
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Erreur upload Cloudinary : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime une image de Cloudinary à partir de son URL publique.
     * Appelé lors de la suppression d'un article ou lors d'une mise à jour d'image.
     *
     * @param imageUrl l'URL complète de l'image stockée en base de données
     */
    public void deleteImage(String imageUrl) {
        try {
            // Sécurité : ne rien faire si l'URL est null ou vide
            if (imageUrl == null || imageUrl.isEmpty()) return;

            // Cloudinary identifie les images par leur "public_id" et non par leur URL complète
            // Il faut donc extraire le public_id depuis l'URL avant de supprimer
            String publicId = extractPublicId(imageUrl);

            // Supprime l'image sur Cloudinary via son public_id
            // ObjectUtils.emptyMap() = pas d'options supplémentaires
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            throw new RuntimeException("Erreur suppression Cloudinary : " + e.getMessage(), e);
        }
    }

    /**
     * Extrait le public_id depuis une URL Cloudinary complète.
     *
     * Exemple d'URL complète :
     * https://res.cloudinary.com/divmsihdv/image/upload/v1234567890/educationpriorite/mon_image.jpg
     *
     * Public_id attendu par Cloudinary pour la suppression :
     * educationpriorite/mon_image
     *
     * @param imageUrl l'URL complète de l'image
     * @return le public_id extrait sans l'extension
     */
    private String extractPublicId(String imageUrl) {
        // Étape 1 : coupe l'URL en 2 parties au niveau de "/upload/"
        // parts[0] = "https://res.cloudinary.com/divmsihdv/image"
        // parts[1] = "v1234567890/educationpriorite/mon_image.jpg"
        String[] parts = imageUrl.split("/upload/");
        String afterUpload = parts[1];

        // Étape 2 : supprime la version Cloudinary si présente (commence par "v" + chiffres)
        // "v1234567890/educationpriorite/mon_image.jpg" → "educationpriorite/mon_image.jpg"
        if (afterUpload.startsWith("v")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }

        // Étape 3 : supprime l'extension du fichier (.jpg, .png, .webp...)
        // "educationpriorite/mon_image.jpg" → "educationpriorite/mon_image"
        return afterUpload.substring(0, afterUpload.lastIndexOf("."));
    }

    // Upload PDF vers Cloudinary
    public String uploadPdf(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "educationpriorite/pdfs",
                            "resource_type", "raw",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Erreur upload PDF Cloudinary : " + e.getMessage(), e);
        }
    }

    // Upload image de couverture
    public String uploadCover(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "educationpriorite/covers",
                            "resource_type", "image"
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Erreur upload couverture Cloudinary : " + e.getMessage(), e);
        }
    }

    // Supprime un fichier raw (PDF)
    public void deletePdf(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) return;
            String publicId = extractPublicIdRaw(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (IOException e) {
            throw new RuntimeException("Erreur suppression PDF Cloudinary : " + e.getMessage(), e);
        }
    }

    // Extrait le public_id pour les fichiers raw (pas d'extension à supprimer)
    private String extractPublicIdRaw(String fileUrl) {
        String[] parts = fileUrl.split("/upload/");
        String afterUpload = parts[1];
        if (afterUpload.startsWith("v")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        return afterUpload; // ← pas de suppression d'extension pour les raw
    }
}