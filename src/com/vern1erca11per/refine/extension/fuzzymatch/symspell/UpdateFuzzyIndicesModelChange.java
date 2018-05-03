package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.ProjectManager;
import com.google.refine.history.Change;
import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import com.google.refine.util.Pool;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

class UpdateFuzzyIndicesModelChange implements Change {
    OverlayModel oldModel;
    Map<String, Integer> columnDistanceMap;
    FuzzyIndicesModel newModel;

    public UpdateFuzzyIndicesModelChange(Map<String, Integer> columnDistanceMap) {
        this.columnDistanceMap = columnDistanceMap;
    }

    @Override
    public void apply(final Project project) {
        synchronized (project) {
            oldModel = project.overlayModels.getOrDefault(
                    FuzzyIndicesModel.class.getSimpleName(), new FuzzyIndicesModel());

            newModel = new FuzzyIndicesModel();

            //Copy
            for (Map.Entry<String, FuzzySearchIndices> entry : ((FuzzyIndicesModel) oldModel).columnIndicesMap.entrySet()) {
                newModel.columnIndicesMap.put(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Integer> entry : columnDistanceMap.entrySet()) {
                newModel.createIndices(project, entry.getKey(), entry.getValue());
            }

            project.overlayModels.put(FuzzyIndicesModel.class.getSimpleName(), newModel);
        }
    }

    @Override
    public void revert(final Project project) {
        synchronized (project) {
            project.overlayModels.put(FuzzyIndicesModel.class.getSimpleName(), oldModel);
        }
    }

    @Override
    public void save(final Writer writer, final Properties properties) throws IOException {
        //TODO more efficient way
        JSONWriter jsonWriter = new JSONWriter(writer);
        jsonWriter.object();

        jsonWriter.key("projectID");
        jsonWriter.value(newModel.project.id);

        jsonWriter.key("updatedIndices");
        jsonWriter.object();
        for (Map.Entry<String, Integer> entry : columnDistanceMap.entrySet()) {
            jsonWriter.key(entry.getKey());
            jsonWriter.value(entry.getValue());
        }
        jsonWriter.endObject();

        jsonWriter.key("oldIndices");
        oldModel.write(jsonWriter, properties);
        jsonWriter.key("newIndices");
        newModel.write(jsonWriter, properties);

        jsonWriter.endObject();
    }

    public static Change load(LineNumberReader reader, Pool pool) throws Exception {
        final JSONTokener tokener = new JSONTokener(reader.readLine());

        final JSONObject jsonObject = (JSONObject) tokener.nextValue();

        Map<String, Integer> columnDistanceMap =
                jsonObject.getJSONObject("updatedIndices")
                        .toMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                v -> Integer.parseInt(v.getValue().toString())
                        ));


        UpdateFuzzyIndicesModelChange loadedChange = new UpdateFuzzyIndicesModelChange(columnDistanceMap);

        Project project = ProjectManager.singleton.getProject(jsonObject.getLong("projectID"));

        loadedChange.oldModel = FuzzyIndicesModel.load(project, jsonObject.getJSONObject("oldIndices"));
        loadedChange.newModel = (FuzzyIndicesModel) FuzzyIndicesModel.load(project, jsonObject.getJSONObject("newIndices"));

        return loadedChange;
    }
}
