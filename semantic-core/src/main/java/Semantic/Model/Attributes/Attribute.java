package Semantic.Model.Attributes;

import Semantic.Model.Concepts.Common.Definition;

import java.util.ArrayList;
import java.util.List;

public class Attribute {
    public String name;
    public List<Definition> definitions = new ArrayList<>();
    public boolean isNullable = true;
    public boolean hasUnits = false;
    public boolean isRequired = false;
    public boolean isUnique = false;
}

