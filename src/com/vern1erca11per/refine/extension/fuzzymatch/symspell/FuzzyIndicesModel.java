package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.model.AbstractOperation;
import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import org.json.JSONException;
import org.json.JSONObject;
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

    public static OverlayModel load(Project project, JSONObject json) {
        return new FuzzyIndicesModel();
    }

    public static AbstractOperation recunstruct(Project project, JSONObject json) {
        return new AbstractOperation() {
            @Override
            public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
                //TODO implement
            }
        };
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
        //T writer.object();
        //        writer.key("description");
        //        writer.value("join with another project by column");
        //        writer.key("params");
        //        writer.value("cell c or string value, string projectName, string columnName");
        //        writer.key("returns");
        //        writer.value("array");
        //        writer.endObject();ODO implement

    }
}
