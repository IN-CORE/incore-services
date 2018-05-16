
package edu.illinois.ncsa.incore.semantic.units.io.serializer;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.io.Writer;

public interface ISerializer {
    String serialize(Unit unit);

    String serialize(Dimension dimension);

    String serialize(Prefix prefix);

    void serialize(Unit unit, Writer writer);

    void serialize(Dimension dimension, Writer writer);

    void serialize(Prefix prefix, Writer writer);
}
