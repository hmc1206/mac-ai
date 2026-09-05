package com.example.mac_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ObsidianService {

    private final Path vaultPath;

    public ObsidianService(
            @Value("${obsidian.vault-path}") String vaultPath
    ) {
        this.vaultPath = Path.of(vaultPath);

        System.out.println("========== Obsidian ==========");
        System.out.println("Vault Path      : " + this.vaultPath);
        System.out.println("Vault Exists    : " + Files.exists(this.vaultPath));
        System.out.println("Is Directory    : " + Files.isDirectory(this.vaultPath));
        System.out.println("Is Writable     : " + Files.isWritable(this.vaultPath));
        System.out.println("==============================");
    }

    public void createDailyNote(String date, String content) {
        try {
            Path dailyDirectory = vaultPath.resolve("03_Daily");

            Files.createDirectories(dailyDirectory);

            Path dailyNote = dailyDirectory.resolve(date + ".md");

            Files.writeString(
                    dailyNote,
                    content,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Obsidian Daily Note 생성 완료: " + dailyNote
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Obsidian Daily Note 생성 실패",
                    e
            );
        }
    }
}