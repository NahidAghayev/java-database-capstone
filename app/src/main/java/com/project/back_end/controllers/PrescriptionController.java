package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private CommonService service;

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody Prescription prescription,
            @PathVariable String token) {
        if (!service.validateToken(token, "doctor")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {
        if (!service.validateToken(token, "doctor")) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}
