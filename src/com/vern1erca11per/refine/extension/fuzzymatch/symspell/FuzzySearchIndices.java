package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.expr.ExpressionUtils;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO support serialize
public class FuzzySearchIndices {
    //TODO replace with more efficient data structure
    //TODO add store count for correct answer
    List<InverseIndices> invertIndicesList;
    String srcColumnDigest;
    String columnName;
    Project project;
    int maxEditDistance = 0;

    public enum DISTANCE_TYPE {
        //LEVENSHTEIN,
        OPTIMIZED_DAMEREOU
    }

    public static final class InverseIndices {
        Map<String, Set<Integer>> indices;

        public InverseIndices() {
            indices = new HashMap<>();
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
        //TODO can it be paralleled?
        //TODO support prefix search
        if (maxEditDistance < 0) {
            throw new IllegalArgumentException("maxEditDistance should be positive or 0");
        }
        this.maxEditDistance = maxEditDistance;

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
                addIndices(i, value.toString(), value.toString(), 0);
            }
        }
    }

    public void creste(long maxEditDistance) {
        create(Integer.parseInt(Long.toString(maxEditDistance)));
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
                newQueries.add(value);
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

}
