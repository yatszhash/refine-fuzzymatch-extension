package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CreateFuzzySearchIndicesModelOperation extends AbstractOperation {
    final protected Map<String, IndexConfig> columnConfigMap;
    final protected Project project;

    public CreateFuzzySearchIndicesModelOperation(
            Project project,
            Map<String, IndexConfig> columnConfigMap
    ) {
        this.project = project;
        this.columnConfigMap = columnConfigMap;
    }

    @Override
    public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
        jsonWriter.object();

        jsonWriter.key("op");
        jsonWriter.value(OperationRegistry.s_opClassToName.get(this.getClass()));

        jsonWriter.key("columnConfigMap");
        jsonWriter.object();
        for (Map.Entry<String, IndexConfig> entry : columnConfigMap.entrySet()) {
            jsonWriter.key(entry.getKey());
            entry.getValue().write(jsonWriter, properties);
        }
        jsonWriter.endObject();

        jsonWriter.endObject();
    }

    @Override
    protected HistoryEntry createHistoryEntry(Project project, long historyEntryID) throws Exception {

        Change change = new UpdateFuzzyIndicesModelChange(columnConfigMap);

        return new HistoryEntry(
                historyEntryID, project, getBriefDescription(project), this, change
        );
    }

    @Override
    protected String getBriefDescription(Project project) {
        StringBuilder description = new StringBuilder("Create FuzzySearchIndicesModel which has indices for ");
        for (Map.Entry<String, IndexConfig> entry : columnConfigMap.entrySet()) {
            description.append(String.format("(column %s distance %d prefixLen %d), ",
                    entry.getKey(), entry.getValue().getMaxEditDistance(), entry.getValue().getPrefixLength()));
        }

        return description.toString();
    }

    static public AbstractOperation reconstruct(Project project, JSONObject jsonObject) throws Exception {
        Map<String, IndexConfig> columnDistanceMap = new HashMap<>();

        JSONObject columnDistanceJsonObj = jsonObject.getJSONObject("columnConfigMap");
        for (String key : columnDistanceJsonObj.keySet()) {
            columnDistanceMap.put(key, IndexConfig.load(columnDistanceJsonObj.getJSONObject(key)));
        }
        return new CreateFuzzySearchIndicesModelOperation(project, columnDistanceMap);
    }
}