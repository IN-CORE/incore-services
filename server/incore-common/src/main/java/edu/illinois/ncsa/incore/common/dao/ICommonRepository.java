package edu.illinois.ncsa.incore.common.dao;

import java.util.List;

public interface ICommonRepository {

    void initialize();

    List<Object> getAllDemandDefintions();

    Object getDemandDefinitionById(String id);
}
