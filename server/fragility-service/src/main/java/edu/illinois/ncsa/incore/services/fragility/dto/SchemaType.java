package edu.illinois.ncsa.incore.services.fragility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SchemaType {
    @JsonProperty("building")
    Building,
    @JsonProperty("bridge")
    Bridge,
    @JsonProperty("roadway")
    Roadway,
    @JsonProperty("railway")
    Railway
}
