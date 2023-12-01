package edu.illinois.ncsa.incore.service.semantics.daos;

import com.mongodb.MongoClientURI;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.semantics.model.Type;
import jakarta.ws.rs.core.Response;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class MongoDBTypeDAO extends MongoDAO implements ITypeDAO {

    private List<Type> typeList;

    public MongoDBTypeDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore();
        this.loadTypes();
    }

    @Override
    public List<Type> getTypes() {
        this.loadTypes();
        return this.typeList;
    }

    private void loadTypes() {
        this.typeList = this.typeDataStore.getDatabase().getCollection("Type")
            .find(Type.class).into(new ArrayList<Type>());
    }

    @Override
    public List<Type> getTypeByName(String name, String version) {
        List<Type> matchedTypeList;

        // version can be all, latest or specific version
        if (version.equals("all") || version.equals("latest")) {
            // due to latest and all need to be restricted by space
            // check latest later
            matchedTypeList = this.typeDataStore.find(Type.class)
                .filter(Filters.eq("dc:title", name)).iterator().toList();
        } else {
            matchedTypeList = this.typeDataStore.find(Type.class)
                .filter(Filters.and(Filters.eq("dc:title", name),
                    Filters.eq("version", version))).iterator().toList();
        }

        if (matchedTypeList.isEmpty()) return null;

        return matchedTypeList;
    }

    @Override
    public List<Type> searchType(String typeName) {
        Query<Type> query = this.typeDataStore.find(Type.class).filter(
            Filters.or(
                Filters.regex("dc:title").pattern(typeName).caseInsensitive()
            ));
        List<Type> typeList = query.iterator().toList();

        List<Type> matchTypeList = new ArrayList<>();
        matchTypeList.addAll(typeList);

        return matchTypeList;
    }

    private Boolean checkNewType(Document newType) {

        return newType.get("@context") != null
            && newType.get("dc:license") != null
            && newType.get("dc:title") != null
            && newType.get("dc:description") != null
            && newType.get("url") != null
            && newType.get("openvocab:versionnumber") != null
            && newType.get("tableSchema") != null;
    }

    @Override
    public Document postType(Document newType) {
        if (newType != null && checkNewType(newType)) {
            String name = newType.get("dc:title").toString();
            if (this.hasType(name))
                throw new IncoreHTTPException(Response.Status.UNAUTHORIZED, name + " already exists.");
            // insert new type
            this.typeDataStore.insert(newType);
            return newType;
        } else {
            throw new IllegalArgumentException();
        }
    }


    @Override
    public Type deleteType(String name) {
        Type type = this.typeDataStore.find(Type.class)
            .filter(Filters.eq("dc:title", name)).first();
        if (type != null) {
            Query<Type> query = this.typeDataStore.find(Type.class);
            query.filter(Filters.eq("dc:title", name)).findAndDelete();
            return type;
        }
        return null;
    }

    @Override
    public Boolean hasType(String name) {
        Type type = this.typeDataStore.find(Type.class)
            .filter(Filters.eq("dc:title", name)).first();
        return type != null;
    }
}
