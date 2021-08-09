package edu.illinois.ncsa.incore.service.space.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Members {
    @JsonProperty("members")
    private List<String> members;

    public Members() {
        this.members = new ArrayList<>();
    }

    public Members(List<String> members) {
        this.members = members;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String member) {
        this.members.add(member);
    }

    public boolean hasMember(String member) {
        if (this.members == null) return false;
        for (String datasetId : this.members) {
            if (datasetId.equals(member))
                return true;
        }
        return false;
    }

}
