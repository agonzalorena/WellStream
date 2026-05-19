package com.agonzalorena.msvc.analyzer.cache;

import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.persistence.repository.WellAlertRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveAlertCacheManager {
    private final WellAlertRepository wellAlertRepository;
    //wellId-metric("pozo1-Temperature) --> LimitType(MAX o MIN)
    private final Map<String, WellAlert> activeAlertsCache = new ConcurrentHashMap<>();

    public ActiveAlertCacheManager(WellAlertRepository wellAlertRepository) {
        this.wellAlertRepository = wellAlertRepository;
        loadUnresolvedAlerts();
    }

    private void loadUnresolvedAlerts() {
        wellAlertRepository.findByResolvedFalse().forEach(this::save);

        System.out.println("Loaded unresolved alerts into cache: " + activeAlertsCache);
    }

    public void save(WellAlert alert){
        String key = alert.getWellId() + "-" + alert.getMetricType();
        activeAlertsCache.put(key, alert);
    }

    public WellAlert get(String wellId, String metricType){
        String key = wellId + "-" + metricType;
        return activeAlertsCache.get(key);
    }

    public void remove(String wellId, String metricType){
        String key = wellId + "-" + metricType;
        activeAlertsCache.remove(key);
    }
}
