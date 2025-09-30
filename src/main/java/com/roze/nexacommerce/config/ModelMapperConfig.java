package com.roze.nexacommerce.config;

import com.roze.nexacommerce.common.BaseEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setDeepCopyEnabled(false)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        List<String> baseEntityFields = getBaseEntityFieldNames();
        modelMapper.getConfiguration().setPropertyCondition(context -> {
            String destinationFieldName = context.getMapping().getLastDestinationProperty().getName();
            boolean isDestinationBaseEntity = BaseEntity.class.isAssignableFrom(context.getDestinationType());
            if (isDestinationBaseEntity && baseEntityFields.contains(destinationFieldName)) {
                return false;
            }
            return true;
        });
        return modelMapper;
    }

    private List<String> getBaseEntityFieldNames() {
        return Arrays.stream(BaseEntity.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}