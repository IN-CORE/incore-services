package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SpaceMetadata {
    @JsonProperty("name")
    private String name;

    public SpaceMetadata(){
        this.name = "";
    }

    public SpaceMetadata(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @JsonProperty("name")
    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return "{\"name\":" + this.name;
    }

}
