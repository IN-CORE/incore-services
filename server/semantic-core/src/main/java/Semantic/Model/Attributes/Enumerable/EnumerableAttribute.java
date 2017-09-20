package Semantic.Model.Attributes.Enumerable;

import Semantic.Model.Attributes.Attribute;
import Semantic.Model.Attributes.Enumerable.Enumeration;

import java.util.ArrayList;
import java.util.List;

public class EnumerableAttribute<T> extends Attribute {
    public List<Enumeration<T>> enumerations = new ArrayList<>();

}
