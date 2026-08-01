package com.church.churchapp.controller;

import com.church.churchapp.entity.Document;
import com.church.churchapp.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('DOCUMENTS', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    // ==========================================
    // ✅ UPLOAD DOCUMENT
    // ==========================================
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "memberName", required = false) String memberName,
            @RequestParam("uploadDate") String uploadDate) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty.");
            }

            Document doc = new Document();
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            doc.setFileSize(file.getSize());
            doc.setCategory(category);
            doc.setUploadDate(uploadDate);
            doc.setNotes(notes);
            doc.setMemberId(memberId);
            doc.setMemberName(memberName);
            doc.setFileData(file.getBytes());

            Document saved = documentRepository.save(doc);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    // ==========================================
    // ✅ GET ALL DOCUMENTS (Jackson automatically ignores fileData via @JsonIgnore)
    // ==========================================
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        try {
            List<Document> docs = documentRepository.findAll();
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // ✅ DOWNLOAD / PREVIEW DOCUMENT
    // ==========================================
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            Document doc = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            if (doc.getFileData() == null) {
                return ResponseEntity.notFound().build();
            }

            // Setting Content-Disposition inline allows browser-native PDF & image renders
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, doc.getFileType())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .body(doc.getFileData());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // ✅ DELETE DOCUMENT
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            if (!documentRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            documentRepository.deleteById(id);
            return ResponseEntity.ok("Document deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Delete failed: " + e.getMessage());
        }
    }
}
