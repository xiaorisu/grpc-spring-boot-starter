package net.devh.springboot.autoconfigure.grpc.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 5/17/16
 */
public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware, GrpcServiceDiscoverer {
	private static final Logger log = LoggerFactory.getLogger(AnnotationGrpcServiceDiscoverer.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Collection<String> findGrpcServiceBeanNames() {
        String[] beanNames = this.applicationContext.getBeanNamesForAnnotation(GrpcService.class);
        return Arrays.asList(beanNames);
    }

    @Override
    public Collection<GrpcServiceDefinition> findGrpcServices() {
        Collection<String> beanNames = findGrpcServiceBeanNames();
        List<GrpcServiceDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        GlobalServerInterceptorRegistry globalServerInterceptorRegistry = applicationContext.getBean(GlobalServerInterceptorRegistry.class);
        List<ServerInterceptor> globalInterceptorList = globalServerInterceptorRegistry.getServerInterceptors();
        for (String beanName : beanNames) {
            BindableService bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GrpcService grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            serviceDefinition = bindInterceptors(serviceDefinition, grpcServiceAnnotation, globalInterceptorList);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
            log.debug("Found gRPC service: " + serviceDefinition.getServiceDescriptor().getName() + ", bean: " + beanName + ", class: " + bindableService.getClass().getName());
        }
        return definitions;
    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GrpcService grpcServiceAnnotation, List<ServerInterceptor> globalInterceptorList) {
        Set<ServerInterceptor> interceptorSet = Sets.newHashSet();
        interceptorSet.addAll(globalInterceptorList);
        for (Class<? extends ServerInterceptor> serverInterceptorClass : grpcServiceAnnotation.interceptors()) {
            ServerInterceptor serverInterceptor;
            if (applicationContext.getBeanNamesForType(serverInterceptorClass).length > 0) {
                serverInterceptor = applicationContext.getBean(serverInterceptorClass);
            } else {
                try {
                    serverInterceptor = serverInterceptorClass.newInstance();
                } catch (Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptorSet.add(serverInterceptor);
        }
        return ServerInterceptors.intercept(serviceDefinition, Lists.newArrayList(interceptorSet));
    }
}
