
package edu.illinois.ncsa.incore.semantic.units.common;

import edu.illinois.ncsa.incore.semantic.units.io.serializer.ISerializer;

import java.io.Writer;

public interface ISerializable {
    String serialize();
    String serialize(ISerializer serializer);
    void serialize(ISerializer serialize, Writer writer);
}
