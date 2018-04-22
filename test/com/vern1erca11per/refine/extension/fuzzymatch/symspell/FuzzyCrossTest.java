package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.expr.EvalError;
import com.google.refine.expr.HasFields;
import com.google.refine.expr.HasFieldsListImpl;
import com.google.refine.expr.WrappedRow;
import com.google.refine.grel.ControlFunctionRegistry;
import com.google.refine.grel.Function;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.tests.RefineTest;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * Test cases for fuzzy cross function.
 */
public class FuzzyCrossTest extends RefineTest {
    static Properties bindings;

    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    Project fromProject;
    Project toProject;

    @BeforeMethod
    public void SetUp() {
        bindings = new Properties();

        String projectName = "Typographical Address Book";
        String input = "id,name,address\n"
                + "1,jonn,120 Main St.\n"
                + "2,mary,50 Broadway Ave.\n"
                + "3,john,120 Main .\n"
                + "4,anne,17 Morning Crescent\n";
        toProject = createCSVProject(projectName, input);
        toProject.overlayModels.put("FuzzyIndicesModel", new FuzzyIndicesModel());

        projectName = "Typographical recipients";
        input = "gift,recipient,address\n"
                + "lamp,mary,120 Main St.\n"
                + "clock,john,120 Main St.\n"
                + "ring,yang,444 Central City";
        fromProject = createCSVProject(projectName, input);

        bindings.put("project", fromProject);
        // add a column address based on column recipient
        bindings.put("columnName", "recipient");
        ControlFunctionRegistry.registerFunction("fuzzyCross", new FuzzyCross());
    }

    @Test
    public void crossFunctionWithTwoKeys() throws Exception {
        List<String> fromKeyColumnNames = Arrays.asList("recipient", "address");
        List<String> toKeyColumnNames = Arrays.asList("name", "address");
        List<Long> maxEditDistances = Arrays.asList((long) 1, (long) 2);
        long returnMaxRowCount = 5;

        Row queryRow = fromProject.rows.get(1);
        HasFieldsListImpl rows = (HasFieldsListImpl) invoke("fuzzyCross",
                new WrappedRow(fromProject, 1, queryRow), fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances, returnMaxRowCount);
        Set<String> expected = new HashSet(Arrays.asList("1", "3"));

        Assert.assertEquals(expected.size(), rows.length());
        Set<String> actual = new HashSet<>();

        for (HasFields row : rows) {
            actual.add(((WrappedRow) row).row.getCellValue(0).toString());
        }
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void crossFunctionWithTwoKeysButOnlyMatchEither() throws Exception {
        List<String> fromKeyColumnNames = Arrays.asList("recipient", "address");
        List<String> toKeyColumnNames = Arrays.asList("name", "address");
        List<Long> maxEditDistances = Arrays.asList((long) 1, (long) 2);
        long returnMaxRowCount = 5;
        int rowIdx = 0;

        Row queryRow = fromProject.rows.get(rowIdx);
        HasFieldsListImpl rows = (HasFieldsListImpl) invoke("fuzzyCross",
                new WrappedRow(fromProject, rowIdx, queryRow), fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances, returnMaxRowCount);

        Assert.assertEquals(rows.length(), 0);
    }

    @Test
    public void crossFunctionWithNotNotRegisteredRow() throws Exception {
        List<String> fromKeyColumnNames = Arrays.asList("recipient", "address");
        List<String> toKeyColumnNames = Arrays.asList("name", "address");
        List<Long> maxEditDistances = Arrays.asList((long) 1, (long) 2);
        long returnMaxRowCount = 5;
        int rowIdx = 2;

        Row queryRow = fromProject.rows.get(rowIdx);
        HasFieldsListImpl rows = (HasFieldsListImpl) invoke("fuzzyCross",
                new WrappedRow(fromProject, rowIdx, queryRow), fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances, returnMaxRowCount);

        Assert.assertEquals(rows.length(), 0);
    }

    /**
     * rest of cells shows "Error: cross expects a string or cell, a project name to join with, and a column name in that project"
     */
    @Test
    public void crossFunctionNonLiteralValue() throws Exception {
        List<String> fromKeyColumnNames = Arrays.asList("recipient", "address");
        List<String> toKeyColumnNames = Arrays.asList("name", "address");
        List<Long> maxEditDistances = Arrays.asList((long) 1, (long) 2);
        long returnMaxRowCount = 5;
        int rowIdx = 2;

        Row queryRow = fromProject.rows.get(rowIdx);

        // only 5 argument
        Object rows = invoke("fuzzyCross",
                new WrappedRow(fromProject, rowIdx, queryRow), fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances);
        Assert.assertTrue(rows instanceof EvalError);

        // not row (string object)
        rows = invoke("fuzzyCross",
                "dummy", fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances, returnMaxRowCount);
        Assert.assertTrue(rows instanceof EvalError);

        // not exist column
        rows = invoke("fuzzyCross",
                queryRow, "NON-EXIST",
                toProject.getMetadata().getName(), toKeyColumnNames, maxEditDistances, returnMaxRowCount);
        Assert.assertTrue(rows instanceof EvalError);

        // not exist project
        rows = invoke("fuzzyCross",
                queryRow, fromKeyColumnNames,
                "NON-EXIST", toKeyColumnNames, maxEditDistances, returnMaxRowCount);
        Assert.assertTrue(rows instanceof EvalError);

        // not equal size of key
        List<String> lackedKeyColumnNames = new ArrayList<>(toKeyColumnNames);
        lackedKeyColumnNames.remove(1);
        rows = invoke("fuzzyCross",
                queryRow, fromKeyColumnNames,
                toProject.getMetadata().getName(), lackedKeyColumnNames, maxEditDistances, returnMaxRowCount);
        Assert.assertTrue(rows instanceof EvalError);

        // negative distance
        rows = invoke("fuzzyCross",
                queryRow, fromKeyColumnNames,
                toProject.getMetadata().getName(), toKeyColumnNames, -1, returnMaxRowCount);
        Assert.assertTrue(rows instanceof EvalError);
    }

    /**
     * Lookup a control function by name and invoke it with a variable number of args
     */
    private static Object invoke(String name, Object... args) {
        // registry uses static initializer, so no need to set it up
        Function function = ControlFunctionRegistry.getFunction(name);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function " + name);
        }
        if (args == null) {
            return function.call(bindings, new Object[0]);
        } else {
            return function.call(bindings, args);
        }
    }
}
