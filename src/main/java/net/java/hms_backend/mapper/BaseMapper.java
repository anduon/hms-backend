package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.AuditDto;
import net.java.hms_backend.entity.base.Auditable;

public class BaseMapper {

    public static <T extends Auditable> void mapAuditFields(T source, AuditDto target) {
        target.setCreatedBy(source.getCreatedBy());
        target.setUpdatedBy(source.getUpdatedBy());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
