package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.model.AbstractOperation;
import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class FuzzyIndicesModel implements OverlayModel {
    //TODO encapsule
    protected Map<String, FuzzySearchIndices> columnIndicesMap;
    protected Project project;


    //TODO remove project
    public FuzzyIndicesModel() {
        columnIndicesMap = new HashMap<>();
    }

    public static AbstractOperation reconstruct(Project project, JSONObject json) throws Exception {
        return CreateFuzzySearchIndicesModelOperation.reconstruct(project, json);
    }


    public void createIndices(Project project, String columnName, int maxDistance, int prefixLength) {
        //TODO should check equality of the column?
        if (this.project == null) {
            this.project = project;
        } else {
            if (this.project != project) {
                throw new IllegalArgumentException("all the projects in an indices model should be same.");
            }
        }
        if (hasIndices(project, columnName, maxDistance, prefixLength)) {
            return;
        }

        FuzzySearchIndices indices = new FuzzySearchIndices(project, columnName);
        indices.create(maxDistance, prefixLength);
        columnIndicesMap.put(columnName, indices);
    }

    public void createIndices(Project project, String columnName, long maxDistance, long prefixLength) {
        //FIXME it's not safe, but grel only support long value and I want to avoid larger
        // memory consuming by retaining long indices
        createIndices(project, columnName, Integer.parseInt(Long.toString(maxDistance)),
                Integer.parseInt(Long.toString(prefixLength)));
    }

    public boolean hasIndices(Project project, String columnName, int maxDistance, int prefixLength) {
        return columnIndicesMap.containsKey(columnName)
                && columnIndicesMap.get(columnName).maxEditDistance >= maxDistance
                && columnIndicesMap.get(columnName).prefixLength == prefixLength;
    }

    public boolean hasIndices(Project project, String columnName, long maxDistance, long prefixLength) {
        return hasIndices(project, columnName,
                Integer.parseInt(Long.toString(maxDistance)),
                Integer.parseInt(Long.toString(prefixLength)));
    }

    public void removeIndices(String columnName) {
        columnIndicesMap.remove(columnName);
    }

    @Override
    public void onBeforeSave(Project project) {
        // check project toProject's key columns doesn't change after creating indices with the digest
        // if changed, remove the indices for the column

    }

    @Override
    public void onAfterSave(Project project) {
        //empty
    }

    @Override
    public void dispose(Project project) {
        //empty
    }

    public static OverlayModel load(Project project, JSONObject jsonObj) {
        FuzzyIndicesModel model = new FuzzyIndicesModel();

        model.project = project;
        JSONObject indicesMapJson = jsonObj.getJSONObject("indicesMap");
        Iterator<String> columnNameIterator = indicesMapJson.keys();

        while (columnNameIterator.hasNext()) {
            String columnName = columnNameIterator.next();
            model.columnIndicesMap.put(columnName, FuzzySearchIndices.load(project,
                    indicesMapJson.getJSONObject(columnName)));
        }

        return model;
    }

    @Override
    public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
        jsonWriter.object();
        jsonWriter.key("description");
        jsonWriter.value("inverted indices for fuzzy match");

        jsonWriter.key("indicesMap");
        jsonWriter.object();
        for (Map.Entry<String, FuzzySearchIndices> entry : columnIndicesMap.entrySet()) {
            jsonWriter.key(entry.getKey());
            entry.getValue().write(jsonWriter, properties);
        }
        jsonWriter.endObject();

        jsonWriter.endObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuzzyIndicesModel that = (FuzzyIndicesModel) o;
        return Objects.equals(columnIndicesMap, that.columnIndicesMap) &&
                Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnIndicesMap, project.id);
    }
}
