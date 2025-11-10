package com.uade.comedor.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * Servicio para gestionar la subida y eliminación de imágenes en Azure Blob Storage.
 */
@Service
public class AzureBlobStorageService {
    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageService.class);

    @Autowired
    private BlobContainerClient blobContainerClient;

    /**
     * Sube una imagen al contenedor de Azure Blob Storage.
     * 
     * @param file Archivo de imagen a subir
     * @return URL pública de la imagen subida
     * @throws IOException Si hay error al leer el archivo
     */
    public String uploadImage(MultipartFile file) throws IOException {
        logger.info("🔵 Iniciando subida de imagen a Azure Blob Storage");
        
        // Validar que el archivo no esté vacío
        if (file.isEmpty()) {
            logger.error("❌ El archivo está vacío");
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        logger.info("📄 Content-Type recibido: {}", contentType);
        
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.error("❌ Tipo de archivo inválido: {}", contentType);
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String blobName = UUID.randomUUID().toString() + extension;
        logger.info("📝 Nombre del blob: {}", blobName);

        try {
            // Obtener el cliente del blob
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            logger.info("🔗 Cliente del blob obtenido");

            // Configurar headers HTTP para el blob
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(contentType);

            // Subir el archivo
            logger.info("⬆️ Subiendo archivo de {} bytes...", file.getSize());
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            // Retornar la URL pública del blob
            String blobUrl = blobClient.getBlobUrl();
            logger.info("✅ Imagen subida exitosamente: {}", blobUrl);
            return blobUrl;
            
        } catch (Exception e) {
            logger.error("❌ Error al subir imagen a Azure: {}", e.getMessage(), e);
            throw new IOException("Error al subir imagen a Azure: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una imagen del contenedor de Azure Blob Storage.
     * 
     * @param imageUrl URL completa de la imagen a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extraer el nombre del blob de la URL
            String blobName = extractBlobNameFromUrl(imageUrl);
            
            if (blobName == null || blobName.isEmpty()) {
                return false;
            }

            // Obtener el cliente del blob y eliminarlo
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            return blobClient.deleteIfExists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el nombre del blob de una URL completa.
     * 
     * @param url URL completa del blob
     * @return Nombre del blob
     */
    private String extractBlobNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // La URL tiene formato: https://comedorsa.blob.core.windows.net/comedorimages/nombre-archivo.jpg
        String[] parts = url.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    /**
     * Valida si un archivo es una imagen válida.
     * 
     * @param file Archivo a validar
     * @return true si es una imagen válida
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // Validar tipos MIME permitidos
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/webp");
    }

    /**
     * Sube una imagen enviada como data URL (base64) y retorna la URL pública del blob.
     * Formato esperado: data:<mime-type>;base64,<base64-data>
     */
    public String uploadImageFromBase64(String dataUrl) throws IOException {
        logger.info("🔵 Iniciando subida de imagen desde data URL");

        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            logger.error("❌ Data URL inválida");
            throw new IllegalArgumentException("Data URL inválida");
        }

        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex < 0) {
            logger.error("❌ Data URL sin sección base64");
            throw new IllegalArgumentException("Data URL sin sección base64");
        }

        String meta = dataUrl.substring(5, commaIndex); // skip "data:"
        String base64Data = dataUrl.substring(commaIndex + 1);

        // meta suele ser "image/jpeg;base64" o similar
        String[] metaParts = meta.split(";");
        String contentType = metaParts.length > 0 ? metaParts[0] : null;

        if (contentType == null || !contentType.startsWith("image/")) {
            logger.error("❌ Tipo de contenido inválido para data URL: {}", contentType);
            throw new IllegalArgumentException("El data URL debe contener un tipo de imagen válido");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al decodificar base64: {}", e.getMessage());
            throw new IOException("Error al decodificar base64: " + e.getMessage(), e);
        }

        // Determinar extensión a partir del contentType
        String extension = "";
        if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) extension = ".jpg";
        else if (contentType.equals("image/png")) extension = ".png";
        else if (contentType.equals("image/webp")) extension = ".webp";

        String blobName = java.util.UUID.randomUUID().toString() + extension;

        try (InputStream is = new ByteArrayInputStream(bytes)) {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

            // Subir los bytes
            blobClient.upload(is, bytes.length, true);
            blobClient.setHttpHeaders(headers);

            String blobUrl = blobClient.getBlobUrl();
            logger.info("✅ Imagen subida desde data URL exitosamente: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            logger.error("❌ Error al subir imagen desde data URL a Azure: {}", e.getMessage(), e);
            throw new IOException("Error al subir imagen a Azure: " + e.getMessage(), e);
        }
    }
}
