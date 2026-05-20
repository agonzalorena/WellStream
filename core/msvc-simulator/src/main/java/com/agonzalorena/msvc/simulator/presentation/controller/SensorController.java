package com.agonzalorena.msvc.simulator.presentation.controller;

import com.agonzalorena.msvc.simulator.presentation.dto.response.SuccessResponse;
import com.agonzalorena.msvc.simulator.service.SensorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/multipliers")
    public ResponseEntity<SuccessResponse> getMultipliers() {
        return ResponseEntity.status(200).body(new SuccessResponse(200, sensorService.getMultipliers()));
    }

    @PutMapping("/multipliers/pressure")
    public ResponseEntity<SuccessResponse> setPressureMultiplier(@RequestParam double multiplier) {
        sensorService.setPressureMultiplier(multiplier);
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicador de presion establecido en "+ multiplier));
    }

    @PutMapping("/multipliers/temperature")
    public ResponseEntity<SuccessResponse> setTemperatureMultiplier(@RequestParam double multiplier) {
        sensorService.setTemperatureMultiplier(multiplier);
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicador de temperatura establecido en "+ multiplier));
    }

    @PutMapping("/multipliers/flow")
    public ResponseEntity<SuccessResponse> setFlowMultiplier(@RequestParam double multiplier) {
        sensorService.setFlowMultiplier(multiplier);
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicador de flujo establecido en "+ multiplier));
    }

    @PostMapping("/multipliers/reset")
    public ResponseEntity<SuccessResponse> resetMultipliers() {
        sensorService.resetMultipliers();
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicadores restablecidos a 1.0"));
    }
}

