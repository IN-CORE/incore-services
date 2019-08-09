package edu.illinois.ncsa.incore.service.dfr3.daos;

import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRepairDAO {
    void initialize();
    List<RepairSet> getRepairs();
    String saveRepair(RepairSet repairSet);
    Optional<RepairSet> getRepairSetById(String id);
    List<RepairSet> searchRepairs(String text);
    List<RepairSet> queryRepairs(String attributeType, String attributeValue);
    List<RepairSet> queryRepairs(Map<String, String> typeValueMap);
}
