package com.seguros.polizas.application.service;

import com.seguros.polizas.application.command.AgregarRiesgoCommand;
import com.seguros.polizas.application.command.CancelarRiesgoCommand;
import com.seguros.polizas.application.port.in.RiesgoCommandHandler;
import com.seguros.polizas.domain.enums.EstadoPoliza;
import com.seguros.polizas.domain.enums.EstadoRiesgo;
import com.seguros.polizas.domain.enums.TipoPoliza;
import com.seguros.polizas.domain.event.EventPublisher;
import com.seguros.polizas.domain.event.RiesgoAgregado;
import com.seguros.polizas.domain.event.RiesgoCancelado;
import com.seguros.polizas.domain.exception.BusinessRuleException;
import com.seguros.polizas.domain.exception.PolizaNotFoundException;
import com.seguros.polizas.domain.model.Poliza;
import com.seguros.polizas.domain.model.Riesgo;
import com.seguros.polizas.domain.port.out.PolizaRepository;
import com.seguros.polizas.domain.port.out.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del puerto de entrada {@link RiesgoCommandHandler}.
 */
@Service
@Transactional
public class RiesgoCommandService implements RiesgoCommandHandler {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final EventPublisher eventPublisher;

    public RiesgoCommandService(PolizaRepository polizaRepository,
                                RiesgoRepository riesgoRepository,
                                EventPublisher eventPublisher) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Riesgo agregarRiesgo(AgregarRiesgoCommand command) {
        Poliza poliza = polizaRepository.findById(command.polizaId())
                .orElseThrow(() -> new PolizaNotFoundException("Poliza no encontrada con ID: " + command.polizaId()));

        if (poliza.getTipo() == TipoPoliza.INDIVIDUAL) {
            throw new BusinessRuleException(
                    "Operacion rechazada: Una poliza individual solo puede tener 1 riesgo. " +
                    "Para administrar multiples riesgos la poliza debe ser COLECTIVA."
            );
        }

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new BusinessRuleException(
                    "Operacion rechazada: No es posible agregar riesgos a una poliza en estado CANCELADA."
            );
        }

        Riesgo nuevoRiesgo = new Riesgo(
                poliza,
                EstadoRiesgo.ACTIVO,
                command.direccionInmueble(),
                command.ciudad(),
                command.nombreAsegurado(),
                command.valorAsegurado()
        );

        poliza.addRiesgo(nuevoRiesgo);
        Riesgo guardado = riesgoRepository.save(nuevoRiesgo);
        eventPublisher.publish(new RiesgoAgregado(command.polizaId(), guardado.getId()));
        return guardado;
    }

    @Override
    public Riesgo cancelarRiesgo(CancelarRiesgoCommand command) {
        Riesgo riesgo = riesgoRepository.findById(command.riesgoId())
                .orElseThrow(() -> new PolizaNotFoundException("Riesgo no encontrado con ID: " + command.riesgoId()));

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo guardado = riesgoRepository.save(riesgo);

        Long polizaId = riesgo.getPoliza() != null ? riesgo.getPoliza().getId() : null;
        eventPublisher.publish(new RiesgoCancelado(polizaId, guardado.getId()));
        return guardado;
    }
}
