package com.minihealth.patient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Patient records kept in memory (sample data only), so the service needs no database. */
@RestController
public class PatientController {

    public record Patient(Long id, String name, int age) {
    }

    private final Map<Long, Patient> patients = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public PatientController() {
        add("Asha Rao", 34);
        add("Ravi Kumar", 52);
    }

    @GetMapping("/patients")
    public Collection<Patient> list() {
        return patients.values();
    }

    @GetMapping("/patients/{id}")
    public Patient get(@PathVariable Long id) {
        Patient patient = patients.get(id);
        if (patient == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient " + id + " not found");
        }
        return patient;
    }

    @PostMapping("/patients")
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@RequestBody Patient request) {
        if (request.name() == null || request.name().isBlank() || request.age() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required and age must be >= 0");
        }
        return add(request.name(), request.age());
    }

    private Patient add(String name, int age) {
        Patient patient = new Patient(nextId.getAndIncrement(), name, age);
        patients.put(patient.id(), patient);
        return patient;
    }
}
