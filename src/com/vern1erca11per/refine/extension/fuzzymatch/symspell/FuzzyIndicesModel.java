package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FuzzyIndicesModel implements OverlayModel {
    //TODO encapsule
    protected Map<String, FuzzySearchIndices> columnIndicesMap;
    Project project;

    private FuzzyIndicesModel() {
    }

    public FuzzyIndicesModel(Project project) {
        this.project = project;
        columnIndicesMap = new HashMap<>();
    }

    public void createIndices(String columnName, int maxDistance) {
        //TODO should check equality of the column?
        if (hasIndices(columnName, maxDistance)) {
            return;
        }

        FuzzySearchIndices indices = new FuzzySearchIndices(project, columnName);
        indices.create(maxDistance);
        columnIndicesMap.put(columnName, indices);
    }

    public boolean hasIndices(String columnName, int maxDistance) {
        return columnIndicesMap.containsKey(columnName) && columnIndicesMap.get(columnName).maxEditDistance <= maxDistance;
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

    @Override
    public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
        //TODO implement
    }
}
