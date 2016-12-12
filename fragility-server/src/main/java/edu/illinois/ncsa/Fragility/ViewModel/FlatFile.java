package edu.illinois.ncsa.Fragility.ViewModel;

import edu.illinois.ncsa.Fragility.JaxPModel.FragilityDataset;

import java.util.ArrayList;
import java.util.List;

public class FlatFile {
    String filename;

    String hazardType = "";
    String InventoryType = "";
    String code = "";
    String Author = "";
    String description = "";
    String structureType = "";
    String id = "";

    public FlatFile(String filename, FragilityDataset.FragilityDatasetSets.FragilitySet dataset) {
        FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetProperties properties = dataset.getFragilitySetProperties();
        this.filename = filename;

        this.code = properties.getCode();
        this.Author = properties.getAuthor();
        this.description = properties.getDescription();
        this.structureType = properties.getStructureType();
        this.id = properties.getID();
    }

    public static List<FlatFile> getFiles(String filename, FragilityDataset dataset) {
        List<FlatFile> files = new ArrayList<>();

        for (FragilityDataset.FragilityDatasetSets.FragilitySet fragilitySet : dataset.getFragilityDatasetSets().getFragilitySet()) {
            files.add(new FlatFile(filename, fragilitySet));
        }

        return files;
    }

    @Override
    public String toString() {
        return filename + "," + hazardType + "," + InventoryType + ",\"" + code + "\",\"" + Author + "\",\"" + structureType + "\",\"" + description + "\",\"" + id + "\"";
    }

    public static List<String> getOutput(List<FlatFile> files) {
        List<String> output = new ArrayList<>();

        for (FlatFile file : files) {
            output.add(file.toString());
        }

        return output;
    }
}
