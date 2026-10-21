package com.experiencefieldops.api.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstração de armazenamento de arquivos.
 * Implementação atual: disco local.
 * Implementação futura: trocar por S3StorageServiceImpl sem alterar nada mais.
 */
public interface StorageService {

    /**
     * Persiste o arquivo e retorna a fileKey gerada (nunca usa o nome original — RN-050).
     */
    String store(MultipartFile file);

    /**
     * Remove o arquivo pelo fileKey.
     */
    void delete(String fileKey);

    /**
     * Retorna a URL/caminho público ou interno do arquivo.
     */
    String getUrl(String fileKey);
}
