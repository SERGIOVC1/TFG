package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.IpGeoLog;
import com.tfg.tfg.persistance.repository.IpGeoLogRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class IpGeoService {

    private final IpGeoLogRepository ipGeoLogRepository;

    public IpGeoService(IpGeoLogRepository ipGeoLogRepository) {
        this.ipGeoLogRepository = ipGeoLogRepository;
    }

    public void saveLog(IpGeoLog log) {
        try {
            // Obtener IP interna del servidor
            String internalIp = InetAddress.getLocalHost().getHostAddress();
            log.setInternalIpAddress(internalIp);

            ipGeoLogRepository.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
