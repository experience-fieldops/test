package com.experiencefieldops.api.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.experiencefieldops.api.services.StorageService;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private final Path uploadDir;

    public LocalStorageServiceImpl(@Value("${storage.local.path:uploads}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload: " + uploadDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        // Gera fileKey único — nunca usa nome original (RN-050)
        String extension = getExtension(file.getOriginalFilename());
        String fileKey = UUID.randomUUID().toString() + extension;

        try {
            Path target = uploadDir.resolve(fileKey);
            file.transferTo(target);
            return fileKey;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar arquivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            Path target = uploadDir.resolve(fileKey);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao remover arquivo: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // Em local, retorna o caminho relativo. Com S3, aqui virá a URL pública do bucket.
        return "/uploads/" + fileKey;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
