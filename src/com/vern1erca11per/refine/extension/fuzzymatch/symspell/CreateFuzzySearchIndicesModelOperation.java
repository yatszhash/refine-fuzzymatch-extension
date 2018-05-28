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

//TODO fix write and reload history
public class CreateFuzzySearchIndicesModelOperation extends AbstractOperation {
    final protected Map<String, Integer> columnDistanceMap;
    final protected Project project;

    public CreateFuzzySearchIndicesModelOperation(
            Project project,
            Map<String, Integer> columnDistanceMap
    ) {
        this.project = project;
        this.columnDistanceMap = columnDistanceMap;
    }

    @Override
    public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
        jsonWriter.object();

        jsonWriter.key("op");
        jsonWriter.value(OperationRegistry.s_opClassToName.get(this.getClass()));

        jsonWriter.key("columnDistanceMap");
        jsonWriter.object();
        for (Map.Entry<String, Integer> entry : columnDistanceMap.entrySet()) {
            jsonWriter.key(entry.getKey());
            jsonWriter.value(entry.getValue());
        }
        jsonWriter.endObject();

        jsonWriter.endObject();
    }

    @Override
    protected HistoryEntry createHistoryEntry(Project project, long historyEntryID) throws Exception {

        Change change = new UpdateFuzzyIndicesModelChange(columnDistanceMap);

        return new HistoryEntry(
                historyEntryID, project, getBriefDescription(project), this, change
        );
    }

    @Override
    protected String getBriefDescription(Project project) {
        StringBuilder description = new StringBuilder("Create FuzzySearchIndicesModel which has indices for ");
        for (Map.Entry<String, Integer> entry : columnDistanceMap.entrySet()) {
            description.append(String.format("(column %s, distance %d), ",
                    entry.getKey(), entry.getValue()));
        }

        return description.toString();
    }

    static public AbstractOperation reconstruct(Project project, JSONObject jsonObject) throws Exception {
        Map<String, Integer> columnDistanceMap = new HashMap<>();

        JSONObject columnDistanceJsonObj = jsonObject.getJSONObject("columnDistanceMap");
        for (String key : columnDistanceJsonObj.keySet()) {
            columnDistanceMap.put(key, columnDistanceJsonObj.getInt(key));
        }
        return new CreateFuzzySearchIndicesModelOperation(project, columnDistanceMap);
    }

    //    @Override
//    protected RowVisitor createRowVisitor(Project project, List<CellChange> list, long l) throws Exception {
//        return null;
//    }
//
//    @Override
//    protected String createDescription(Column column, List<CellChange> list) {
//        return null;
//    }
}
