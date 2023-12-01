package edu.illinois.ncsa.incore.service.semantics.daos;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.semantics.model.Type;
import jakarta.ws.rs.core.Response;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;


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
            matchedTypeList = this.typeDataStore.getDatabase()
                .getCollection("Type")
                .find(Type.class)
                .filter(eq("dc:title", name))
                .into(new ArrayList<Type>());
        } else {
            matchedTypeList = this.typeDataStore.getDatabase()
                .getCollection("Type")
                .find(Type.class)
                .filter(and(eq("dc:title", name),
                    eq("openvocab:versionnumber", version)))
                .into(new ArrayList<Type>());
        }

        if (matchedTypeList.isEmpty()) return null;

        return matchedTypeList;
    }

    @Override
    public List<Type> searchType(String typeName) {
        List<Type> typeList = this.typeDataStore.getDatabase().getCollection("Type")
            .find(Type.class).into(new ArrayList<Type>());
        List<Type> matchTypeList = new ArrayList<Type>();
        for (Type datsetType : typeList) {
            String title = datsetType.getTitle();
            if (title.toLowerCase().contains(typeName.toLowerCase())) {
                matchTypeList.add(datsetType);
            }
        }

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
            this.typeDataStore.getDatabase().getCollection("Type").insertOne(newType);
            return newType;
        } else {
            throw new IllegalArgumentException();
        }
    }


    @Override
    public Type deleteType(String name) {
        Type type = this.typeDataStore.getDatabase()
            .getCollection("Type")
            .find(Type.class)
            .filter(eq("dc:title", name))
            .first();
        this.typeDataStore.getDatabase()
            .getCollection("Type").deleteOne(eq("dc:title", name));
        return type;
    }

    @Override
    public Boolean hasType(String name) {
        Type type = this.typeDataStore.getDatabase()
            .getCollection("Type")
            .find(Type.class)
            .filter(eq("dc:title", name))
            .first();
        return type != null;
    }
}
