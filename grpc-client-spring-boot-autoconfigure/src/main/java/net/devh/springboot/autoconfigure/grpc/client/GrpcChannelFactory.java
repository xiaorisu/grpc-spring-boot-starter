package net.devh.springboot.autoconfigure.grpc.client;

import java.util.List;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;

public interface GrpcChannelFactory {

    Channel createChannel(String name);

    Channel createChannel(String name, List<ClientInterceptor> interceptors);
}
