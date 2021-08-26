package edu.illinois.ncsa.incore.service.dfr3.daos;

import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRestorationDAO {
    void initialize();

    List<RestorationSet> getRestorations();

    String saveRestoration(RestorationSet restorationSet);

    Optional<RestorationSet> getRestorationSetById(String id);

    RestorationSet deleteRestorationSetById(String id);

    List<RestorationSet> searchRestorations(String text);

    List<RestorationSet> queryRestorations(String attributeType, String attributeValue);

    List<RestorationSet> queryRestorations(Map<String, String> typeValueMap);

    int getRestorationCountByCreator(String creator);
}
