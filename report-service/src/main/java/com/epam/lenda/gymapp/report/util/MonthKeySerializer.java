package com.epam.lenda.gymapp.report.util;

import java.time.Month;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class MonthKeySerializer extends StdSerializer<Month> {

    public MonthKeySerializer() {
        super(Month.class);
    }

    @Override
    public void serialize(Month value, JsonGenerator gen, SerializationContext ctx) throws JacksonException {
        final var nameUppercase = value.name();
        gen.writeName(nameUppercase.charAt(0) + nameUppercase.substring(1).toLowerCase());
    }
}
