package net.devh.springboot.autoconfigure.grpc.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import io.grpc.Server;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.services.HealthStatusManager;
import lombok.extern.slf4j.Slf4j;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class NettyGrpcServerFactory implements GrpcServerFactory {
	private static final Logger log = LoggerFactory.getLogger(NettyGrpcServerFactory.class);
    private final GrpcServerProperties properties;
    private final List<GrpcServiceDefinition> services = Lists.newLinkedList();

    @Autowired
    private HealthStatusManager healthStatusManager;

    public NettyGrpcServerFactory(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(getAddress()), getPort()));

        // support health check
        builder.addService(healthStatusManager.getHealthService());

        for (GrpcServiceDefinition service : this.services) {
            String serviceName = service.getDefinition().getServiceDescriptor().getName();
            log.info("Registered gRPC service: " + serviceName + ", bean: " + service.getBeanName() + ", class: " + service.getBeanClazz().getName());
            builder.addService(service.getDefinition());
            healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
        }

        if (this.properties.getSecurity().getEnabled()) {
            File certificateChain = new File(this.properties.getSecurity().getCertificateChainPath());
            File certificate = new File(this.properties.getSecurity().getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        if(properties.getMaxMessageSize() > 0) {
        	builder.maxInboundMessageSize(properties.getMaxMessageSize());
        }

        return builder.build();
    }

    @Override
    public String getAddress() {
        return this.properties.getAddress();
    }

    @Override
    public int getPort() {
        return this.properties.getPort();
    }

    @Override
    public void addService(GrpcServiceDefinition service) {
        this.services.add(service);
    }

    @Override
    public void destroy() {
        for (GrpcServiceDefinition grpcServiceDefinition : services) {
            String serviceName = grpcServiceDefinition.getDefinition().getServiceDescriptor().getName();
            healthStatusManager.clearStatus(serviceName);
        }
    }
}
