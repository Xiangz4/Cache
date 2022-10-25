package com.xyf;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CaffeineRegister implements ImportBeanDefinitionRegistrar {
    /*
        扫描自定义注解
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                // 是独立的类并且不是注解
                if (annotationMetadata.isIndependent()) {
                    if (!annotationMetadata.isAnnotation()){
                        // 满足要求的类
                        return true;
                    }
                }
                // 默认不满足要求
                return false;
            }
        };
        //定义需要扫描的注解名称
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(CaffeineAnnotation.class);
        scanner.addIncludeFilter(annotationTypeFilter);
        //获取base包的名称
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableCaffeine.class.getName());
        String[] basePackages = (String[])((String[])annotationAttributes.get("basePackages"));
        //根据base，和注解名称进行接口的扫描
        Set<BeanDefinition> allCandidateComponents = new HashSet<>();
        if (basePackages.length > 0){
            for (String basePackage : basePackages) {
                allCandidateComponents.addAll(scanner.findCandidateComponents(basePackage));
            }
        }

        //对扫描到的注解生成代理对象、
        if (!CollectionUtils.isEmpty(allCandidateComponents)){
            for (BeanDefinition candidateComponents : allCandidateComponents) {
                // 先判断扫描到的接口是不是接口，有可能注解写在类上面了，也可以被扫描到
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) candidateComponents;
                AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                Assert.isTrue(annotationMetadata.isConcrete(), "@CustomizeAnnotation注解只能用于具体上");

                // 创建我们的自定义工厂实例
                CaffeineFactory customizeFactoryBean = new CaffeineFactory();
                // 设置类型
                String className = annotationMetadata.getClassName();
                Class clazz = ClassUtils.resolveClassName(className, null);
                customizeFactoryBean.setType(clazz);

                // 通过bean定义构造器来bean对象
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                        // 这里调用了我们自定义的bean工厂来创建bean对象
                        .genericBeanDefinition(clazz, () -> {
                            try {
                                return customizeFactoryBean.getObject();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                // 设置自动注入模式，为按类型自动注入
                beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                // 设置是否懒加载
                beanDefinitionBuilder.setLazyInit(true);
                AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

                // bean的别名
                String beanName = className.substring(className.lastIndexOf(".") + 1) + "Caffeine";
                String[] beanNames = new String[]{beanName};

                // 注册到Spring容器
                BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                        beanNames);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
            }
        }
    }

}
