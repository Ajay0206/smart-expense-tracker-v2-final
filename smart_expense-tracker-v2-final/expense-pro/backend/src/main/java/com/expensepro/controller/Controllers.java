package com.expensepro.controller;

import com.expensepro.model.*;
import com.expensepro.repository.CategoryRepository;
import com.expensepro.repository.UserRepository;
import com.expensepro.service.ExpenseService;
import com.expensepro.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

abstract class BaseController {
    @Autowired UserRepository users;

    protected Long uid(Authentication auth) {
        return users.findByUsername(auth.getName()).orElseThrow().getId();
    }
}

@RestController
@RequestMapping("/expenses")
class ExpenseController extends BaseController {

    @Autowired ExpenseService   svc;
    @Autowired CategoryRepository catRepo;

    @GetMapping
    public ResponseEntity<?> list(Authentication auth,
            @RequestParam(defaultValue="0")  int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) String q) {

        Page<Expense> pg = svc.search(uid(auth), categoryId, from, to, q, page, size);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content",       pg.getContent().stream().map(svc::toMap).toList());
        result.put("totalElements", pg.getTotalElements());
        result.put("totalPages",    pg.getTotalPages());
        result.put("number",        pg.getNumber());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth,
            @RequestBody ExpenseService.ExpenseReq req) {
        return ResponseEntity.ok(svc.toMap(svc.create(uid(auth), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id,
            @RequestBody ExpenseService.ExpenseReq req) {
        return ResponseEntity.ok(svc.toMap(svc.update(id, uid(auth), req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        svc.delete(id, uid(auth));
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        return ResponseEntity.ok(svc.dashboard(uid(auth)));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories(Authentication auth) {
        return ResponseEntity.ok(catRepo.findForUser(uid(auth)));
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth,
            @RequestBody Map<String, Object> body) {
        var user = users.findByUsername(auth.getName()).orElseThrow();
        if (body.containsKey("monthlyLimit"))
            user.setMonthlyLimit(new java.math.BigDecimal(body.get("monthlyLimit").toString()));
        if (body.containsKey("currency"))
            user.setCurrency((String) body.get("currency"));
        if (body.containsKey("fullName"))
            user.setFullName((String) body.get("fullName"));
        users.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }
}

@RestController
@RequestMapping("/reports")
class ReportController extends BaseController {

    @Autowired PdfService pdfService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(Authentication auth,
            @RequestParam(required=false) Integer year,
            @RequestParam(required=false) Integer month) throws Exception {
        LocalDate now = LocalDate.now();
        int y = year  != null ? year  : now.getYear();
        int m = month != null ? month : now.getMonthValue();

        byte[] pdf = pdfService.monthlyReport(uid(auth), y, m);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"expense-report-" + y + "-" +
                    String.format("%02d", m) + ".pdf\"")
                .body(pdf);
    }
}
