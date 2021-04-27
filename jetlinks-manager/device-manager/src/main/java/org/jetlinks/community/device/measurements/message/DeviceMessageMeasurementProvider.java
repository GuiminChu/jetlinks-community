package org.jetlinks.community.device.measurements.message;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.community.dashboard.supports.StaticMeasurementProvider;
import org.jetlinks.community.device.measurements.DeviceDashboardDefinition;
import org.jetlinks.community.device.measurements.DeviceObjectDefinition;
import org.jetlinks.community.device.timeseries.DeviceTimeSeriesMetric;
import org.jetlinks.community.gateway.annotation.Subscribe;
import org.jetlinks.community.micrometer.MeterRegistryManager;
import org.jetlinks.community.timeseries.TimeSeriesManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeviceMessageMeasurementProvider extends StaticMeasurementProvider {

    MeterRegistry registry;

    public DeviceMessageMeasurementProvider(EventBus eventBus,
                                            MeterRegistryManager registryManager,
                                            DeviceRegistry deviceRegistry,
                                            TimeSeriesManager timeSeriesManager) {
        super(DeviceDashboardDefinition.instance, DeviceObjectDefinition.message);

        registry = registryManager.getMeterRegister(DeviceTimeSeriesMetric.deviceMetrics().getId(),
            "target", "msgType", "productId");

        addMeasurement(new DeviceMessageMeasurement(eventBus, deviceRegistry, timeSeriesManager));

    }

    @Subscribe("/device/*/*/message/**")
    public Mono<Void> incrementMessage(DeviceMessage message) {
        log.info("======> DeviceMessageMeasurementProvider");
        return Mono.fromRunnable(() -> {
            registry
                .counter("message-count", convertTags(message))
                .increment();
        });
    }

    static final String[] empty = new String[0];

    private String[] convertTags(DeviceMessage message) {
        if (message == null) {
            return empty;
        }
        return new String[]{
            "productId", message.getHeader("productId").map(String::valueOf).orElse("unknown")
        };
    }
}
