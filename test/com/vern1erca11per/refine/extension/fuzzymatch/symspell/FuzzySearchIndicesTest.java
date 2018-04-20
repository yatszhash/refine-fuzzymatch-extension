package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.model.ModelException;
import com.google.refine.model.Project;
import com.google.refine.tests.RefineTest;
import org.json.JSONException;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;


public class FuzzySearchIndicesTest extends RefineTest {
    Project project;

    @BeforeTest
    @Override
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @BeforeMethod
    public void setUp() throws JSONException, IOException, ModelException {
        project = createCSVProject("FuzzySearchIndices",
                "COLUMN1,COLUMN2\n"
                        + "0,\n"
                        + "a,ac\n"
                        + "b,ab\n"
                        + "c,ab\n"
                        + "d,acd\n"
                        + "e,adc\n");
    }

    @Test
    public void testCreate() {
        int maxDistance = 2;
        String columnName = "COLUMN2";


        List<FuzzySearchIndices.InverseIndices> expected = new ArrayList<>(maxDistance + 3);

        FuzzySearchIndices.InverseIndices distance0Indices = new FuzzySearchIndices.InverseIndices();
        distance0Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(1)));
        distance0Indices.indices.putIfAbsent("ab", new HashSet<>(Arrays.asList(2, 3)));
        distance0Indices.indices.putIfAbsent("acd", new HashSet<>(Arrays.asList(4)));
        distance0Indices.indices.putIfAbsent("adc", new HashSet<>(Arrays.asList(5)));
        expected.add(distance0Indices);

        FuzzySearchIndices.InverseIndices distance1Indices = new FuzzySearchIndices.InverseIndices();
        distance1Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(1, 2, 3)));
        distance1Indices.indices.putIfAbsent("b", new HashSet<>(Arrays.asList(2, 3)));
        distance1Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(1)));
        distance1Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("ad", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("cd", new HashSet<>(Arrays.asList(4)));
        distance1Indices.indices.putIfAbsent("dc", new HashSet<>(Arrays.asList(5)));
        expected.add(distance1Indices);

        FuzzySearchIndices.InverseIndices distance2Indices = new FuzzySearchIndices.InverseIndices();
        distance2Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("d", new HashSet<>(Arrays.asList(4, 5)));
        expected.add(distance2Indices);

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);

        sut.create(maxDistance);

        List<FuzzySearchIndices.InverseIndices> actual = sut.invertIndicesList;

        assertEquals(actual.size(), expected.size());

        for (int i = 0; i < actual.size(); i++) {
            FuzzySearchIndices.InverseIndices actualIndices = actual.get(i);
            FuzzySearchIndices.InverseIndices expectedIndices = expected.get(i);

            assertEquals(actualIndices.indices, expectedIndices.indices);

        }
    }

    public List<FuzzySearchIndices.InverseIndices> createTestIndices(int maxDistance) {

        List<FuzzySearchIndices.InverseIndices> preCreated = new ArrayList<>(maxDistance + 3);

        FuzzySearchIndices.InverseIndices distance0Indices = new FuzzySearchIndices.InverseIndices();
        distance0Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(1)));
        distance0Indices.indices.putIfAbsent("ab", new HashSet<>(Arrays.asList(2, 3)));
        distance0Indices.indices.putIfAbsent("acd", new HashSet<>(Arrays.asList(4)));
        distance0Indices.indices.putIfAbsent("adc", new HashSet<>(Arrays.asList(5)));
        preCreated.add(distance0Indices);

        FuzzySearchIndices.InverseIndices distance1Indices = new FuzzySearchIndices.InverseIndices();
        distance1Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(1, 2, 3)));
        distance1Indices.indices.putIfAbsent("b", new HashSet<>(Arrays.asList(2, 3)));
        distance1Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(1)));
        distance1Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("ad", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("cd", new HashSet<>(Arrays.asList(4)));
        distance1Indices.indices.putIfAbsent("dc", new HashSet<>(Arrays.asList(5)));
        preCreated.add(distance1Indices);

        FuzzySearchIndices.InverseIndices distance2Indices = new FuzzySearchIndices.InverseIndices();
        distance2Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("d", new HashSet<>(Arrays.asList(4, 5)));
        preCreated.add(distance2Indices);

        return preCreated;
    }

    @Test
    public void testLookupWithInVWord() {
        int maxDistance = 2;
        String columnName = "COLUMN2";

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);
        sut.invertIndicesList = createTestIndices(maxDistance);
        sut.maxEditDistance = 2;

        String key = "acb";
        Set<Integer> expected = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> actual = sut.lookup(key, 2, 10);
        assertEquals(actual, expected);
    }

    @Test
    public void testLookupWithInVWordAndRowNumConstraint() {
        int maxDistance = 2;
        String columnName = "COLUMN2";
        int maxRowNum = 3;

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);
        sut.invertIndicesList = createTestIndices(maxDistance);
        sut.maxEditDistance = 2;

        String key = "acc";
        Set<Integer> expected = new HashSet<>(Arrays.asList(1, 4, 5));
        Set<Integer> actual = sut.lookup(key, 2, maxRowNum);
        assertEquals(actual, expected);
    }

    @Test
    public void testLookupWithSmallerDistanceThanOneOnCreation() {
        int maxDistance = 2;
        String columnName = "COLUMN2";
        int maxRowNum = 10;

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);
        sut.invertIndicesList = createTestIndices(maxDistance);
        sut.maxEditDistance = maxDistance;

        String key = "acc";
        Set<Integer> expected = new HashSet<>(Arrays.asList(1, 4, 5));
        Set<Integer> actual = sut.lookup(key, 1, maxRowNum);
        assertEquals(actual, expected);
    }

    @Test
    public void testLookupWithOOVWord() {
        int maxDistance = 2;
        String columnName = "COLUMN";

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);
        sut.invertIndicesList = createTestIndices(maxDistance);
        sut.maxEditDistance = 2;

        String key = "xyz";
        Set<Integer> expected = new HashSet<>();
        Set<Integer> actual = sut.lookup(key, 2, 10);
        assertEquals(actual, expected);

    }

    @Test(expectedExceptions = {java.lang.IllegalArgumentException.class})
    public void testLookupWithLargerDistanceThanOneOnCreation() {
        int maxDistance = 2;
        String columnName = "COLUMN2";
        int maxRowNum = 10;

        FuzzySearchIndices sut = new FuzzySearchIndices(project, columnName);
        sut.invertIndicesList = createTestIndices(maxDistance);
        sut.maxEditDistance = maxDistance;

        String key = "acc";
        Set<Integer> actual = sut.lookup(key, 3, maxRowNum);
    }

}