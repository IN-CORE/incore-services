package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.morphia.annotations.Embedded;
import org.json.JSONObject;

import java.util.List;

@Embedded
public class IncoreLabQuota {
    public List<Integer> cpu;
    public List<Integer> mem;
    public int disk;

    public IncoreLabQuota(){ }

    public JSONObject toJson() {
        JSONObject outJson = new JSONObject();

        outJson.put("cpu", this.cpu);
        outJson.put("mem", this.mem);
        outJson.put("disk", this.disk);

        return outJson;
    }

    public List<Integer> getCpu() {
        return this.cpu;
    }

    public void setCpu(List<Integer> cpu) {
        this.cpu = cpu;
    }

    public List<Integer> getMem(){
        return this.mem;
    }

    public void setMem(List<Integer> mem) {
        this.mem = mem;
    }

    public int getDisk() {
        return this.disk;
    }

    public void setDisk(int disk){
        this.disk = disk;
    }

}
