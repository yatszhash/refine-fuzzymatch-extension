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
    public static final String PROJECT_NOT_EXIST_MESSAGE = "project %s doesn't exist";


    public enum ALGORITHMS {
        SYMSPELL
        //BKTREE
        //
    }

    public FuzzyCross() {
    }

    //TODO performance test
    // TODO should support prefix search?

    /**
     * @param bindings
     * @param args     require 6 or 7 elements [
     *                 row (type = WrappedRow),
     *                 fromProjectKeyColumnNames (type = StringArry, List of String, String),
     *                 toProject (type = String(Name) or Long(Id) ),
     *                 toProjectKeyColumnNames (type = StringArry or List of String or String),
     *                 editDistanceThresholds (long or longArray or List <long>),
     *                 candidtatesCountThreshold to abort search (long),
     *                 prefix Length to create indices (long, optional)
     *                 ]
     * @return
     */
    public Object call(Properties bindings, Object[] args) {
        WrappedRow row;
        Project fromProject;
        List<String> fromKeyColumnNames;
        Project toProject;
        List<String> toKeyColumnNames;
        // very slow if maxEditDistance >= 5
        List<Long> maxEditDistances;
        Long returnMaxRowCount;
        Integer numKeys;
        Long prefixLength;

        //TODO remove try block for performance optimization
        try {
            if (args.length != 6 && args.length != 7) {
                throw new IllegalArgumentException("The number of Arguments should be 6 or 7");
            }

            //TODO support string list query
            row = parseRowArg(args[0]);
            fromProject = (Project) bindings.get("project");

            fromKeyColumnNames = parseColumnNames(
                    args[1], fromProject);
            toProject = parseProject(args[2]);
            toKeyColumnNames = parseColumnNames(args[3], toProject);
            maxEditDistances = parsePositiveNumbers(args[4], "distance");
            returnMaxRowCount = parsePositiveNumber(args[5], "row count");

            if (args.length == 7) {
                prefixLength = parsePositiveNumber(args[6], "prefix length");
            } else {
                prefixLength = (long) FuzzySearchIndices.DEFAULT_PREFIX_LENGTH;
            }

            numKeys = toKeyColumnNames.size();
            if (numKeys != fromKeyColumnNames.size() || numKeys != maxEditDistances.size()) {
                throw new IllegalArgumentException("the sizes of key columns and thresholds should be equal");
            }
        } catch (IllegalArgumentException e) {
            return new EvalError(e);
        }

        String modelName = "FuzzyIndicesModel";
        FuzzyIndicesModel model = (FuzzyIndicesModel) toProject.overlayModels.get(modelName);

        //FIXME is this a safe operation?
        if (model == null) {
            toProject.overlayModels.put(modelName, new FuzzyIndicesModel());
        }

        Set<Integer> candidateRowNums = null;
        for (int i = 0; i < numKeys; i++) {
            String columnName = toKeyColumnNames.get(i);
            Long maxDistance = maxEditDistances.get(i);
            if (!model.hasIndices(toProject, columnName, maxDistance, prefixLength)) {
                model.createIndices(toProject, columnName, maxDistance, prefixLength);
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
        throw new IllegalArgumentException(String.format(PROJECT_NOT_EXIST_MESSAGE,
                v.toString()));
    }

    protected long parsePositiveNumber(Object v, String parameterName) {
        if ((v instanceof Long) && (long) v >= 0) {
            return (long) v;
        }
        throw new IllegalArgumentException(String.format("%s should be positive int or 0", parameterName));
    }

    protected List<Long> parsePositiveNumbers(Object l, String parameterName) {
        if (l == null || (!(l instanceof Long) && !l.getClass().isArray()
                && !(l instanceof List<?>))) {
            throw new IllegalArgumentException("require long, array or list of longs for maxDistances");
        }

        List<Long> numbers;
        if (l instanceof Long) {
            numbers = new ArrayList<>();
            numbers.add(parsePositiveNumber(l, parameterName));
            return numbers;
        }

        if (l.getClass().isArray()) {
            Object[] lArray = (Object[]) l;
            int length = lArray.length;
            numbers = new ArrayList<>(length);

            for (Object c : lArray) {
                numbers.add(parsePositiveNumber(c, parameterName));
            }
            return numbers;
        }

        List<Object> lList = ExpressionUtils.toObjectList(l);
        int length = lList.size();
        numbers = new ArrayList<>(length);

        for (Object c : lList) {
            numbers.add(parsePositiveNumber(c, parameterName));
        }

        return numbers;
    }

    public void write(JSONWriter writer, Properties options) throws JSONException {
        writer.object();
        writer.key("description");
        writer.value("join with another project by columns of fuzzy matching");
        writer.key("params");
        writer.value("6 or 7 params \n" +
                "     *                 row (type = WrappedRow),\n" +
                "     *                 fromProjectKeyColumnNames (type = StringArry, List of String, String),\n" +
                "     *                 toProject (type = String(Name) or Long(Id) ),\n" +
                "     *                 toProjectKeyColumnNames (type = StringArry or List of String or String),\n" +
                "     *                 editDistanceThresholds (long or longArray or List <long>),\n" +
                "     *                 candidtatesCountThreshold to abort search (long)\n" +
                "     *                 prefix Length to create indices (long, optional)" +
                "");
        writer.key("returns");
        writer.value("array");
        writer.endObject();
    }
}
