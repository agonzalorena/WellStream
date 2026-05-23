package com.agonzalorena.msvc.simulator.presentation.controller;

import com.agonzalorena.msvc.simulator.common.enums.MultiplierType;
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

    @GetMapping("")
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.status(200).body(new SuccessResponse(200, sensorService.getWells()));
    }

    @PutMapping("/multipliers/{type}/{wellId}")
    public ResponseEntity<SuccessResponse> setMultiplier(@PathVariable String wellId, @PathVariable MultiplierType type, @RequestParam double multiplier) {
        sensorService.setMultiplier(wellId, type, multiplier);
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicador de "+ type.name() + " establecido en "+ multiplier));
    }

    @PostMapping("/multipliers/reset")
    public ResponseEntity<SuccessResponse> resetMultipliers() {
        sensorService.resetMultipliers();
        return ResponseEntity.ok()
                .body(new SuccessResponse(200, "Multiplicadores restablecidos a 1.0"));
    }
}

