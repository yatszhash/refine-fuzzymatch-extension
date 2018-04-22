package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.ProjectManager;
import com.google.refine.expr.EvalError;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.expr.HasFieldsListImpl;
import com.google.refine.expr.WrappedRow;
import com.google.refine.grel.Function;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class FuzzyCross implements Function {


    public enum ALGORITHMS {
        SYMSPELL
        //BKTREE
        //
    }

    public FuzzyCross() {
    }

    //TODO performance test
    // TODO should support prefix search?
    public Object call(Properties bindings, Object[] args) {
        WrappedRow row;
        Project fromProject;
        List<String> fromKeyColumnNames;
        Project toProject;
        List<String> toKeyColumnNames;
        List<Integer> maxEditDistances;
        Integer returnMaxRowCount;
        Integer numKeys;

        //TODO remove try block for performance optimization
        try {
            if (args.length != 6) {
                throw new IllegalArgumentException("The number of Arguments should be 6");
            }

            //TODO support string list query
            row = parseRowArg(args[0]);
            fromProject = (Project) bindings.get("project");

            fromKeyColumnNames = parseColumnNames(
                    args[1], fromProject);
            toProject = parseProject(args[2]);
            toKeyColumnNames = parseColumnNames(args[3], toProject);
            maxEditDistances = parsePositiveIntegers(args[4], "distance");
            returnMaxRowCount = parsePositiveInt(args[5], "row count");

            numKeys = toKeyColumnNames.size();
            if (numKeys != fromKeyColumnNames.size() || numKeys != maxEditDistances.size()) {
                throw new IllegalArgumentException("the sizes of key columns and thresholds should be equal");
            }
        } catch (IllegalArgumentException e) {
            return new EvalError(e);
        }

        FuzzyIndicesModel model = (FuzzyIndicesModel) toProject.overlayModels.get("FuzzyIndicesModel");

        Set<Integer> candidateRowNums = null;
        for (int i = 0; i < numKeys; i++) {
            String columnName = toKeyColumnNames.get(i);
            Integer maxDistance = maxEditDistances.get(i);
            if (!model.hasIndices(toProject, columnName, maxDistance)) {
                model.createIndices(toProject, columnName, maxDistance);
            }

            //TODO allow OR
            //TODO blocking with previous result
            //TODO add buffer to returnMaxRowCount
            int fromColumnIndex = fromProject.columnModel.getColumnIndexByName(
                    fromKeyColumnNames.get(i));
            String value = (String) row.row.getCellValue(fromColumnIndex);

            if (candidateRowNums != null) {
                candidateRowNums.retainAll(model.columnIndicesMap.get(columnName)
                        .lookup(value, maxDistance, returnMaxRowCount)
                );
            } else {
                candidateRowNums = model.columnIndicesMap.get(columnName)
                        .lookup(value, maxDistance, returnMaxRowCount);
            }
        }

        //convert indices to rows;
        //TODO should return distances?
        HasFieldsListImpl resultRows = new HasFieldsListImpl();

        if (candidateRowNums == null) {
            return resultRows;
        }

        for (int rowIndex : candidateRowNums) {
            resultRows.add(new WrappedRow(toProject, rowIndex, toProject.rows.get(rowIndex)));
        }
        return resultRows;
    }

    protected void parseParams(Object[] args) {

    }

    protected WrappedRow parseRowArg(Object v) {
        if (v != null && !(v instanceof WrappedRow)) {
            throw new IllegalArgumentException("first argument for FuzzyCross should be row");
        }
        return (WrappedRow) v;
    }

    protected String parseColumnName(Object s, Project project) {
        if (!(s instanceof String)) {
            throw new IllegalArgumentException("column name should be string.");
        }
        String columnName = (String) s;
        Column column = project.columnModel.getColumnByName(columnName);

        if (column == null) {
            throw new IllegalArgumentException(String.format("Column <%s> doesn't exist in project <%d>",
                    columnName, project.id
            ));
        }
        return columnName;
    }

    protected List<String> parseColumnNames(Object l, Project project) {
        if (l == null || (!(l instanceof String) && !l.getClass().isArray() && !(l instanceof List<?>))) {
            throw new IllegalArgumentException("require array or list of string for columnNames");
        }

        List<String> columnNames;
        if (l instanceof String) {
            columnNames = new ArrayList<>();
            columnNames.add(parseColumnName(l, project));
            return columnNames;
        }

        if (l.getClass().isArray()) {
            Object[] lArray = (Object[]) l;
            int length = lArray.length;
            columnNames = new ArrayList<>(length);

            for (Object c : lArray) {
                columnNames.add(parseColumnName(c, project));
            }
            return columnNames;
        }

        List<Object> lList = ExpressionUtils.toObjectList(l);
        int length = lList.size();
        columnNames = new ArrayList<>(length);

        for (Object c : lList) {
            columnNames.add(parseColumnName(c, project));
        }

        return columnNames;
    }

    protected Project parseProject(Object v) {
        Project project;
        if (v instanceof Long) {
            project = ProjectManager.singleton.getProject((Long) v);
        } else if (v instanceof String) {
            Long projectId = ProjectManager.singleton.getProjectID((String) v);
            project = ProjectManager.singleton.getProject(projectId);
        } else {
            throw new IllegalArgumentException("project parameter should be String or Long");
        }

        if (project != null) {
            return project;
        }
        throw new IllegalArgumentException(String.format("project %s doesn't exist", v.toString()));
    }

    protected int parsePositiveInt(Object v, String parameterName) {
        if (v instanceof Integer && (int) v >= 0) {
            return (int) v;
        }
        throw new IllegalArgumentException(String.format("%s should be positive int or 0", parameterName));
    }

    protected List<Integer> parsePositiveIntegers(Object l, String parameterName) {
        if (l == null || (!(l instanceof Integer) && !l.getClass().isArray() && !(l instanceof List<?>))) {
            throw new IllegalArgumentException("require integer, array or list of integers for maxDistances");
        }

        List<Integer> integers;
        if (l instanceof Integer) {
            integers = new ArrayList<>();
            integers.add(parsePositiveInt(l, parameterName));
            return integers;
        }

        if (l.getClass().isArray()) {
            Object[] lArray = (Object[]) l;
            int length = lArray.length;
            integers = new ArrayList<>(length);

            for (Object c : lArray) {
                integers.add(parsePositiveInt(c, parameterName));
            }
            return integers;
        }

        List<Object> lList = ExpressionUtils.toObjectList(l);
        int length = lList.size();
        integers = new ArrayList<>(length);

        for (Object c : lList) {
            integers.add(parsePositiveInt(c, parameterName));
        }

        return integers;
    }

    public void write(JSONWriter writer, Properties options) throws JSONException {
        writer.object();
        writer.key("description");
        writer.value("join with another project by column");
        writer.key("params");
        writer.value("cell c or string value, string projectName, string columnName");
        writer.key("returns");
        writer.value("array");
        writer.endObject();
    }
}
