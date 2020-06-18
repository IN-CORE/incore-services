/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.typeconverters;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import dev.morphia.morphia.converters.SimpleValueConverter;
import dev.morphia.morphia.converters.TypeConverter;
import dev.morphia.morphia.mapping.MappedField;
import dev.morphia.morphia.mapping.MappingException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigDecimalConverter extends TypeConverter implements SimpleValueConverter {

    public BigDecimalConverter() {
        super(BigDecimal.class);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }
        BigDecimal bigDecimalValue = (BigDecimal) value;

        if (bigDecimalValue.scale() > 18) {
            bigDecimalValue = bigDecimalValue.setScale(18, BigDecimal.ROUND_DOWN);
        }

        DBObject dbo = new BasicDBObject();

        dbo.put("unscaled", bigDecimalValue.unscaledValue().longValue());
        dbo.put("scale", bigDecimalValue.scale());

        return dbo;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object decode(Class targetClass, Object fromDBObject, MappedField field) throws MappingException {
        DBObject dbo = (DBObject) fromDBObject;
        if (dbo == null) {
            return null;
        }

        BigDecimal bigDecimal = null;

        Long unscaled = (Long) dbo.get("unscaled");
        Integer scale = (Integer) dbo.get("scale");

        if (unscaled != null && scale != null) {
            bigDecimal = new BigDecimal(new BigInteger(unscaled.toString()), scale);
        }

        return bigDecimal;
    }
}
