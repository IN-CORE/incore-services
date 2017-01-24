package SemanticModels.Dataset;

import SemanticModels.Attributes.Attribute;
import Metadata.Metadata;

import java.util.List;

public class Dataset {
    // TODO category enum?
    public Category category;
    public List<Attribute> attributes;
    public List<Metadata> metadata;
}
