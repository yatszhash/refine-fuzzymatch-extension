package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.Jsonizable;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

//TODO support serialize
public class FuzzySearchIndices implements Jsonizable {
    //TODO replace with more efficient data structure
    //TODO add store count for correct answer
    //TODO support custom identifier column
    int DEFAULT_PREFIX_LENGTH = 15;

    List<InverseIndices> invertIndicesList;
    //String srcColumnDigest;
    String columnName;
    Project project;
    int maxEditDistance = 0;
    int prefixLength = 15;

    public enum DISTANCE_TYPE {
        //LEVENSHTEIN,
        OPTIMIZED_DAMEREOU
    }

    public static final class InverseIndices implements Jsonizable {
        Map<String, Set<Integer>> indices;

        public InverseIndices() {
            indices = new HashMap<>();
        }

        @Override
        public void write(JSONWriter writer, Properties options) {
            writer.object();
            for (Map.Entry<String, Set<Integer>> index : indices.entrySet()) {

                writer.key(index.getKey());
                writer.array();
                for (int rowNumber : index.getValue()) {
                    writer.value(rowNumber);
                }
                writer.endArray();

            }
            writer.endObject();
        }

        public static InverseIndices load(JSONObject jsonObject){
            InverseIndices inverseIndices = new InverseIndices();
            Set<String> keys = jsonObject.keySet();

            for (String key: keys){
                JSONArray rowNumbersJsonArray = jsonObject.getJSONArray(key);
                Set<Integer> rowNumbers = new HashSet<>(rowNumbersJsonArray.length());

                for (Object rowNumber: rowNumbersJsonArray){
                    rowNumbers.add((int) rowNumber);
                }

                inverseIndices.indices.put(key, rowNumbers);
            }
            return inverseIndices;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InverseIndices indices1 = (InverseIndices) o;
            return Objects.equals(indices, indices1.indices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(indices);
        }
    }

    public FuzzySearchIndices(Project project, String columnName) {
        this.project = project;
        this.columnName = columnName;
    }

    public boolean requireUpdate(Long projectId) {

        //TODO digest for column

        return false;
    }

    public void create(int maxEditDistance) {
        create(maxEditDistance, DEFAULT_PREFIX_LENGTH);
    }

    public void create(int maxEditDistance, int prefixLength) {
        //TODO can it be paralleled?
        //TODO support prefix search
        if (maxEditDistance < 0) {
            throw new IllegalArgumentException("maxEditDistance should be positive or 0");
        }
        this.maxEditDistance = maxEditDistance;

        if (prefixLength <= 0) {
            throw new IllegalArgumentException("prefix length should be positive");
        }
        this.prefixLength = prefixLength;

        invertIndicesList = new ArrayList<>(this.maxEditDistance + 1);

        for (int i = 0; i < this.maxEditDistance + 1; i++) {
            invertIndicesList.add(new InverseIndices());
        }

        int rowNums = project.rows.size();
        Column column = project.columnModel.getColumnByName(columnName);

        //recursive method
        for (int i = 0; i < rowNums; ++i) {
            Row row = project.rows.get(i);
            Object value = row.getCellValue(column.getCellIndex());
            if (ExpressionUtils.isNonBlankData(value)) {
                String prefix = extractPrefix(value.toString());
                //TODO normalize
                addIndices(i, prefix, prefix, 0);
            }
        }
    }

    public void creste(long maxEditDistance) {
        create(Integer.parseInt(Long.toString(maxEditDistance)));
    }

    protected String extractPrefix(String rawString) {
        if (prefixLength > rawString.length()) {
            return rawString;
        }

        return rawString.substring(0, prefixLength);
    }

    protected void addIndices(int rowIndex, String rawWord, String editedWord, int currentDeletionCount) {
        if (rawWord.length() <= 1 || editedWord.length() <= 1
                || currentDeletionCount > maxEditDistance) {
            return;
        }

        int editedWordLength = editedWord.length();
        List<String> deletions = new ArrayList<>(editedWordLength);

        if (currentDeletionCount <= 0) {
            deletions.add(editedWord);
            invertIndicesList.get(currentDeletionCount).indices.putIfAbsent(editedWord, new HashSet<>());
            invertIndicesList.get(currentDeletionCount).indices.get(editedWord).add(rowIndex);
        } else {
            for (int i = 0; i < editedWordLength; i++) {
                String deletion = editedWord.substring(0, i) + editedWord.substring(i + 1, editedWordLength);
                invertIndicesList.get(currentDeletionCount).indices.putIfAbsent(deletion, new HashSet<>());
                invertIndicesList.get(currentDeletionCount).indices.get(deletion).add(rowIndex);
                deletions.add(deletion);
            }
        }

        for (String deletion : deletions) {
            addIndices(rowIndex, rawWord, deletion, currentDeletionCount + 1);
        }
    }

    /**
     * @param value
     * @param maxSearchEditDistance
     * @param maxNumRows            if return current edit distance
     * @return
     */
    public Set<Integer> lookup(String value, int maxSearchEditDistance,
                               int maxNumRows) {
        if (maxSearchEditDistance > maxEditDistance) {
            throw new IllegalArgumentException("search edit distance " +
                    "should be smaller than the one on creation of the indices");
        }

        Set<Integer> candidates = new HashSet<>();
        List<Set<String>> queriesList = new ArrayList<>();

        for (int i = 0; i <= maxSearchEditDistance; i++) {
            if (candidates.size() >= maxNumRows) {
                break;
            }

            //create query
            Set<String> newQueries;
            if (i == 0) {
                newQueries = new HashSet<>();
                newQueries.add(extractPrefix(value));
            } else {
                Set<String> previousQueries = queriesList.get(i - 1);
                newQueries = createQueries(previousQueries);
            }

            //search all for new queries
            addCandidates(newQueries, candidates, i, 0);

            //search only for current max edit for old queries
            for (int j = 0; j < queriesList.size(); j++) {
                addCandidates(queriesList.get(j), candidates, i, i);
            }

            queriesList.add(newQueries);
        }

        return candidates;
    }

    /**
     * @param value
     * @param maxSearchEditDistance
     * @return
     */
    public Set<Integer> lookup(String value, long maxSearchEditDistance, long maxNumRows) {
        return lookup(value, Integer.parseInt(Long.toString(maxSearchEditDistance)),
                Integer.parseInt(Long.toString(maxNumRows)));
    }

    protected Set<String> createQueries(Set<String> previousQueries) {
        Set<String> deletions = new HashSet<>();
        for (String edited : previousQueries) {
            int editedLength = edited.length();
            if (editedLength <= 1) {
                break;
            }

            for (int j = 0; j < editedLength; j++) {
                String deletion = edited.substring(0, j)
                        + edited.substring(j + 1, editedLength);
                deletions.add(deletion);
            }
        }
        return deletions;
    }

    protected void addCandidates(Set<String> queries, Set<Integer> candidates, int currentDistance,
                                 int searchedStartDistance) {
        for (int i = searchedStartDistance; i <= currentDistance; i++) {
            for (String query : queries) {
                Set<Integer> newCandidates = invertIndicesList.get(i).indices.get(query);

                if (newCandidates != null) {
                    candidates.addAll(newCandidates);
                }
            }
        }
    }

    public static FuzzySearchIndices load(Project project, JSONObject jsonObject){
        FuzzySearchIndices searchIndices = new FuzzySearchIndices(project, jsonObject.getString("columnName"));

        searchIndices.maxEditDistance = jsonObject.getInt("maxEditDistance");
        searchIndices.prefixLength = jsonObject.getInt("prefixLength");

        JSONArray indicesArray = jsonObject.getJSONArray("indices");
        searchIndices.invertIndicesList = new ArrayList<>(indicesArray.length());

        for (int i=0; i < indicesArray.length(); i++){
            searchIndices.invertIndicesList.add(InverseIndices.load(indicesArray.getJSONObject(i)));
        }
        return searchIndices;
    }

    @Override
    public void write(JSONWriter jsonWriter, Properties properties) throws JSONException {
        jsonWriter.object();

        jsonWriter.key("columnName");
        jsonWriter.value(columnName);

        jsonWriter.key("maxEditDistance");
        jsonWriter.value(maxEditDistance);

        jsonWriter.key("prefixLength");
        jsonWriter.value(maxEditDistance);

        jsonWriter.key("indices");

        jsonWriter.array();
        for (InverseIndices indices : invertIndicesList) {
            indices.write(jsonWriter, properties);
        }
        jsonWriter.endArray();

        jsonWriter.endObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuzzySearchIndices indices = (FuzzySearchIndices) o;
        return maxEditDistance == indices.maxEditDistance &&
                prefixLength == indices.prefixLength &&
                Objects.equals(invertIndicesList, indices.invertIndicesList) &&
                Objects.equals(columnName, indices.columnName) &&
                Objects.equals(project, indices.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invertIndicesList, columnName, project.id, maxEditDistance,
                prefixLength);
    }
}
