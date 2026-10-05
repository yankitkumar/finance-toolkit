package com.minihealth.appointment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Appointments kept in memory. Booking checks with patient-service that the patient exists. */
@RestController
public class AppointmentController {

    public record AppointmentRequest(Long patientId, String doctor, LocalDateTime time) {
    }

    public record Appointment(Long id, Long patientId, String patientName, String doctor, LocalDateTime time) {
    }

    private final List<Appointment> appointments = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final PatientClient patientClient;

    public AppointmentController(PatientClient patientClient) {
        this.patientClient = patientClient;
    }

    @GetMapping("/appointments")
    public List<Appointment> list() {
        return appointments;
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@RequestBody AppointmentRequest request) {
        if (request.patientId() == null || request.doctor() == null || request.doctor().isBlank()
                || request.time() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId, doctor and time are required");
        }
        PatientClient.Patient patient = patientClient.getPatient(request.patientId());
        Appointment appointment = new Appointment(
                nextId.getAndIncrement(), patient.id(), patient.name(), request.doctor(), request.time());
        appointments.add(appointment);
        return appointment;
    }
}
