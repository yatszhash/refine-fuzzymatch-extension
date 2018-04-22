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


    //TODO remove project
    public FuzzyIndicesModel() {
        columnIndicesMap = new HashMap<>();
    }

    //TODO add project indices
    public void createIndices(Project project, String columnName, int maxDistance) {
        //TODO should check equality of the column?
        if (this.project == null) {
            this.project = project;
        } else {
            if (this.project != project) {
                throw new IllegalArgumentException("all the projects in an indices model should be same.");
            }
        }
        if (hasIndices(project, columnName, maxDistance)) {
            return;
        }

        FuzzySearchIndices indices = new FuzzySearchIndices(project, columnName);
        indices.create(maxDistance);
        columnIndicesMap.put(columnName, indices);
    }

    public void createIndices(Project project, String columnName, long maxDistance) {
        //FIXME it's not safe, but grel only support long value, but I want to avoid larger
        // memory consuming by retaining long indices
        createIndices(project, columnName, Integer.parseInt(Long.toString(maxDistance)));
    }

    public boolean hasIndices(Project project, String columnName, int maxDistance) {
        return columnIndicesMap.containsKey(columnName) && columnIndicesMap.get(columnName).maxEditDistance <= maxDistance;
    }

    public boolean hasIndices(Project project, String columnName, long maxDistance) {
        return hasIndices(project, columnName, Integer.parseInt(Long.toString(maxDistance)));
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
