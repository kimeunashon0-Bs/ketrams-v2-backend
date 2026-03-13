package com.ketrams.v2.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String level;
    private String category;

    private boolean enabled = true; // NEW: soft delete flag

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(columnDefinition = "TEXT")
    private String documentUrls; // JSON array of file paths

    @JsonProperty("documentUrls")
    public List<String> getDocumentUrlsList() {
        if (this.documentUrls == null || this.documentUrls.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> list = mapper.readValue(this.documentUrls, new TypeReference<List<String>>() {});
            return list.stream()
                    .map(s -> s.contains(File.separator) ? s.substring(s.lastIndexOf(File.separator) + 1) : s)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setDocumentUrlsList(List<String> urls) {
        if (urls == null) {
            this.documentUrls = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.documentUrls = mapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize document URLs", e);
        }
    }
}