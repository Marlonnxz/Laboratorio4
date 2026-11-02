package co.edu.uptc.students.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AzureBlobStorageService {

    private static final Logger log = LoggerFactory.getLogger(AzureBlobStorageService.class);

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.container-name:academic-files}")
    private String containerName;

    @Value("${azure.storage.sas-expiry-minutes:15}")
    private int sasExpiryMinutes;

    private BlobContainerClient containerClient;
    private boolean isConfigured = false;

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            try {
                BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

                containerClient = serviceClient.getBlobContainerClient(containerName);
                if (!containerClient.exists()) {
                    containerClient.create();
                    log.info("Created private Azure Blob container: {}", containerName);
                }
                isConfigured = true;
                log.info("Azure Blob Storage initialized successfully with container: {}", containerName);
            } catch (Exception e) {
                log.warn("Could not connect to Azure Blob Storage ({}), continuing in mock/fallback mode", e.getMessage());
            }
        } else {
            log.info("No Azure Storage connection string provided. AzureBlobStorageService running in mock mode.");
        }
    }

    public String uploadFile(String folder, String originalFilename, String contentType, byte[] data) {
        String cleanName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
        String blobName = folder + "/" + UUID.randomUUID() + "-" + cleanName;

        if (isConfigured && containerClient != null) {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.upload(new ByteArrayInputStream(data), data.length, true);
            blobClient.setHttpHeaders(headers);
            log.info("Uploaded blob: {} ({} bytes)", blobName, data.length);
        } else {
            log.info("[MOCK] Uploaded file: {} ({} bytes)", blobName, data.length);
        }

        return blobName;
    }

    public String generateReadSasUrl(String blobName) {
        if (blobName == null || blobName.isBlank()) {
            return null;
        }

        if (isConfigured && containerClient != null) {
            try {
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                if (blobClient.exists()) {
                    BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
                    OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(sasExpiryMinutes);
                    OffsetDateTime startTime = OffsetDateTime.now().minusMinutes(2); // Clock skew protection

                    BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
                            .setStartTime(startTime);

                    String sasToken = blobClient.generateSas(values);
                    return blobClient.getBlobUrl() + "?" + sasToken;
                }
            } catch (Exception e) {
                log.warn("Error generating SAS token for {}: {}", blobName, e.getMessage());
            }
        }

        return "https://mockstorage.blob.core.windows.net/" + containerName + "/" + blobName + "?mockSas=valid";
    }

    public void deleteFile(String blobName) {
        if (blobName == null || blobName.isBlank()) return;

        if (isConfigured && containerClient != null) {
            try {
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                blobClient.deleteIfExists();
                log.info("Deleted blob: {}", blobName);
            } catch (Exception e) {
                log.warn("Could not delete blob {}: {}", blobName, e.getMessage());
            }
        }
    }
}
