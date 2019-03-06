package edu.illinois.ncsa.incore.service.data.models.spaces;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Metadata {
    @JsonProperty("name")
    private String name;

    public Metadata(){
        this.name = "";
    }

    public Metadata(String name){
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
