package br.net.convertix.dinix.service;

import br.net.convertix.dinix.config.StorageProperties;
import br.net.convertix.dinix.exception.BusinessException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfilePhotoStorage {

    private static final String AVATARS = "avatars";
    private static final String URL_PREFIX = "/uploads/avatars/";
    private static final Set<String> TIPOS = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private final StorageProperties properties;

    public ProfilePhotoStorage(StorageProperties properties) {
        this.properties = properties;
    }

    public String store(UUID userId, MultipartFile file) {
        validar(file);
        String ext = extensao(file);
        String filename = userId + "-" + UUID.randomUUID() + ext;
        Path destino = diretorioAvatares().resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Não foi possível salvar a foto");
        }
        return URL_PREFIX + filename;
    }

    public void deleteByUrl(String url) {
        if (url == null || url.isBlank() || !url.startsWith(URL_PREFIX)) {
            return;
        }
        String filename = url.substring(URL_PREFIX.length());
        Path arquivo = resolve(filename);
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
            // arquivo órfão não bloqueia a operação
        }
    }

    public Resource load(String filename) {
        Path arquivo = resolve(filename);
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            return null;
        }
        return new FileSystemResource(arquivo);
    }

    public MediaType mediaType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    private Path resolve(String filename) {
        if (filename == null || !filename.matches("[a-zA-Z0-9._-]+")) {
            return null;
        }
        Path base = diretorioAvatares().normalize();
        Path arquivo = base.resolve(filename).normalize();
        if (!arquivo.startsWith(base)) {
            return null;
        }
        return arquivo;
    }

    private Path diretorioAvatares() {
        try {
            Path dir = Paths.get(properties.path(), AVATARS).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new BusinessException("Não foi possível preparar o armazenamento de fotos");
        }
    }

    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Envie uma foto");
        }
        if (file.getSize() > properties.maxBytes()) {
            throw new BusinessException("A foto deve ter no máximo 5 MB");
        }
        String content = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        String nome = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        boolean tipoOk = TIPOS.contains(content)
                || nome.endsWith(".jpg")
                || nome.endsWith(".jpeg")
                || nome.endsWith(".png")
                || nome.endsWith(".webp");
        if (!tipoOk) {
            throw new BusinessException("Envie uma imagem JPG, PNG ou WEBP");
        }
    }

    private String extensao(MultipartFile file) {
        String nome = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        String content = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (content.contains("png") || nome.endsWith(".png")) {
            return ".png";
        }
        if (content.contains("webp") || nome.endsWith(".webp")) {
            return ".webp";
        }
        return ".jpg";
    }
}
