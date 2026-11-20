package com.mpaz.panol.equipos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpaz.panol.equipos.entities.Equipo;
import com.mpaz.panol.equipos.repository.EquipoRepository;

// Carga el inventario de prueba (datos ficticios, los mismos del catálogo de la app) solo si la tabla está vacía
@Component
public class DatosIniciales implements CommandLineRunner {

    private final EquipoRepository equipoRepo;

    public DatosIniciales(EquipoRepository equipoRepo) {
        this.equipoRepo = equipoRepo;
    }

    @Override
    public void run(String... args) {
        if (equipoRepo.count() > 0) {
            return;
        }
        equipoRepo.save(new Equipo("NOT-2024-08", "Notebook Lenovo ThinkPad E14", "Lenovo", "E14 Gen 4", "Notebook",
                "Intel Core i5, 16 GB RAM. Ideal para clases y presentaciones. Batería de 6 horas.",
                8, 8, "Disponible", "Pañol Central - Estante A1"));
        equipoRepo.save(new Equipo("PRO-2024-03", "Proyector Láser Epson PowerLite", "Epson", "PowerLite L210", "Proyector",
                "Full HD 1080p, 4000 lúmenes, entrada HDMI. Incluye control remoto.",
                3, 2, "Disponible", "Pañol Central - Estante B2"));
        equipoRepo.save(new Equipo("PAR-2024-01", "Parlante Portátil Bluetooth", "JBL", "PartyBox 110", "Parlante",
                "Potencia 40W con micrófono inalámbrico.",
                1, 0, "Prestado", "Pañol Central - Estante C1"));
        equipoRepo.save(new Equipo("ROB-2024-05", "Kit Robótica Educativa Arduino", "Arduino", "Starter Kit", "Robótica",
                "Set con placa Arduino UNO, sensores, servomotores y protoboard.",
                3, 1, "Reservado", "Pañol Central - Estante D1"));
        equipoRepo.save(new Equipo("ROB-2024-09", "Kit LEGO WeDo 2.0", "LEGO", "WeDo 2.0", "Robótica",
                "Kit de robótica para primer ciclo con motor, sensores y bloques.",
                6, 4, "Disponible", "Pañol Central - Estante D2"));
        equipoRepo.save(new Equipo("ROB-2024-12", "Placa de desarrollo ESP32", "Espressif", "ESP32 DevKit V1", "Robótica",
                "Microcontrolador con WiFi y Bluetooth para proyectos de tecnología.",
                15, 10, "Disponible", "Pañol Central - Cajón D3"));
        equipoRepo.save(new Equipo("COM-2024-02", "Carro de 30 Tablets Galaxy Tab", "Samsung", "Galaxy Tab A9", "Computador",
                "Estación móvil de carga con candado de seguridad.",
                1, 0, "En Mantenimiento", "Sala de Mantención TI"));
        equipoRepo.save(new Equipo("COM-2024-06", "Computador de escritorio HP", "HP", "ProDesk 400 G7", "Computador",
                "Intel Core i3, 8 GB RAM, monitor 22 pulgadas.",
                5, 3, "Disponible", "Pañol Central - Zona E"));
        equipoRepo.save(new Equipo("ALA-2024-04", "Alargador Robusto 10 Metros", "Genérico", "AL-10M", "Alargador",
                "Interruptor de encendido y protección contra sobrecarga.",
                12, 12, "Disponible", "Pañol Central - Estante F1"));
        equipoRepo.save(new Equipo("RED-2024-01", "Switch Cisco 24 puertos", "Cisco", "CBS250-24T", "Equipamiento de red",
                "Switch administrable de 24 puertos Gigabit.",
                1, 0, "Fuera de Servicio", "Rack Sala de Servidores (ubicación técnica ficticia)"));
        equipoRepo.save(new Equipo("RED-2024-02", "Router TP-Link Archer", "TP-Link", "Archer C6", "Equipamiento de red",
                "Router WiFi doble banda para actividades de laboratorio.",
                2, 2, "Disponible", "Pañol Central - Estante G1"));
    }
}
