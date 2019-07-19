package edu.illinois.ncsa.incore.service.fragility.models;

import org.mongodb.morphia.annotations.Entity;

@Entity("FragilityMappingSet")
public class RestorationMappingSet extends MappingSet {
    RestorationMappingSet() {
        super();
    }
}
