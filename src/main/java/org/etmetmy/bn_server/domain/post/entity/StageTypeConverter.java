package org.etmetmy.bn_server.domain.post.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StageTypeConverter implements AttributeConverter<StageType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(StageType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public StageType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        for (StageType stageType : StageType.values()) {
            if (stageType.getCode() == dbData) {
                return stageType;
            }
        }

        throw new IllegalArgumentException("Unknown StageType code: " + dbData);
    }
}