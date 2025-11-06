package com.uade.comedor.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Servicio para gestionar la subida y eliminación de imágenes en Azure Blob Storage.
 */
@Service
public class AzureBlobStorageService {

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
        // Validar que el archivo no esté vacío
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String blobName = UUID.randomUUID().toString() + extension;

        // Obtener el cliente del blob
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        // Configurar headers HTTP para el blob
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(contentType);

        // Subir el archivo
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        // Retornar la URL pública del blob
        return blobClient.getBlobUrl();
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
}
