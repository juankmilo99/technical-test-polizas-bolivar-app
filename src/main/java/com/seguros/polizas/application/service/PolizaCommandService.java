package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.CancelarPolizaCommand;
import com.seguros.polizas.application.command.CrearPolizaCommand;
import com.seguros.polizas.application.command.RenovarPolizaCommand;
import com.seguros.polizas.application.port.in.PolizaCommandHandler;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.event.PolizaCreada;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implementación del puerto de entrada {@link PolizaCommandHandler}.
 *
 * <p>Orquesta los casos de uso de escritura (CQRS command side): carga el
 * agregado, delega la mutación en el dominio y publica los eventos de dominio
 * generados a través del {@link EventPublisher}.</p>
 */
@Service
@Transactional
public class PolizaCommandService implements PolizaCommandHandler {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final EventPublisher eventPublisher;

    public PolizaCommandService(PolizaRepository polizaRepository,
                                RiesgoRepository riesgoRepository,
                                EventPublisher eventPublisher) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Poliza crear(CrearPolizaCommand command) {
        if (polizaRepository.existsByNumeroPoliza(command.numeroPoliza())) {
            throw new BusinessRuleException("Ya existe una poliza con el numero: " + command.numeroPoliza());
        }

        LocalDate fechaInicio = command.fechaInicio();
        LocalDate fechaFin = fechaInicio.plusMonths(command.mesesVigencia()).minusDays(1);

        Poliza poliza = new Poliza(
                command.numeroPoliza(),
                command.tipo(),
                EstadoPoliza.VIGENTE,
                fechaInicio,
                fechaFin,
                command.mesesVigencia(),
                command.canonMensual(),
                command.tomador(),
                command.asegurado(),
                command.beneficiario()
        );

        Poliza guardada = polizaRepository.save(poliza);
        publish(new PolizaCreada(guardada.getId(), guardada.getNumeroPoliza()));
        return guardada;
    }

    @Override
    public Poliza renovar(RenovarPolizaCommand command) {
        Poliza poliza = polizaRepository.findById(command.polizaId())
                .orElseThrow(() -> new PolizaNotFoundException("Poliza no encontrada con ID: " + command.polizaId()));

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new BusinessRuleException("No se puede renovar una poliza en estado CANCELADA.");
        }

        poliza.renovar(command.ipc());
        Poliza guardada = polizaRepository.save(poliza);
        publishDrained(poliza);
        return guardada;
    }

    @Override
    public Poliza cancelar(CancelarPolizaCommand command) {
        Poliza poliza = polizaRepository.findById(command.polizaId())
                .orElseThrow(() -> new PolizaNotFoundException("Poliza no encontrada con ID: " + command.polizaId()));

        poliza.cancelar();
        Poliza guardada = polizaRepository.save(poliza);
        publishDrained(poliza);
        return guardada;
    }

    /**
     * Publica los eventos de dominio generados internamente por el agregado.
     */
    private void publishDrained(Poliza poliza) {
        for (DomainEvent event : poliza.drainEvents()) {
            publish(event);
        }
    }

    private void publish(DomainEvent event) {
        eventPublisher.publish(event);
    }
}
