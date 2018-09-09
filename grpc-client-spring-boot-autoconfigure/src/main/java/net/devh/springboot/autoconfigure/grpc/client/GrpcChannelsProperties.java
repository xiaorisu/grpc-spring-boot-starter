package net.devh.springboot.autoconfigure.grpc.client;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.google.common.collect.Maps;

import lombok.Data;

@Data
@ConfigurationProperties("grpc")
public class GrpcChannelsProperties {

    @NestedConfigurationProperty
    private Map<String, GrpcChannelProperties> client = Maps.newHashMap();

    public GrpcChannelProperties getChannel(String name) {
        GrpcChannelProperties grpcChannelProperties = client.get(name);
        if (grpcChannelProperties == null) {
            grpcChannelProperties = GrpcChannelProperties.DEFAULT;
        }
        return grpcChannelProperties;
    }
}
