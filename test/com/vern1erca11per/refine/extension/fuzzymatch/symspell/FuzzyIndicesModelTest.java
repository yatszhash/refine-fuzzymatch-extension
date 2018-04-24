package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;
import com.google.refine.tests.RefineTest;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FuzzyIndicesModelTest extends RefineTest {


    public FuzzySearchIndices createTestIndices(
            Project project, String columnName, int maxDistance) {
        FuzzySearchIndices preCreated = new FuzzySearchIndices(project, columnName);

        List<FuzzySearchIndices.InverseIndices> indicesList = new ArrayList<>(maxDistance + 3);
        preCreated.invertIndicesList = indicesList;
        preCreated.maxEditDistance = maxDistance;

        FuzzySearchIndices.InverseIndices distance0Indices = new FuzzySearchIndices.InverseIndices();
        distance0Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(1)));
        distance0Indices.indices.putIfAbsent("ab", new HashSet<>(Arrays.asList(2, 3)));
        distance0Indices.indices.putIfAbsent("acd", new HashSet<>(Arrays.asList(4)));
        distance0Indices.indices.putIfAbsent("adc", new HashSet<>(Arrays.asList(5)));
        indicesList.add(distance0Indices);

        FuzzySearchIndices.InverseIndices distance1Indices = new FuzzySearchIndices.InverseIndices();
        distance1Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(1, 2, 3)));
        distance1Indices.indices.putIfAbsent("b", new HashSet<>(Arrays.asList(2, 3)));
        distance1Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(1)));
        distance1Indices.indices.putIfAbsent("ac", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("ad", new HashSet<>(Arrays.asList(4, 5)));
        distance1Indices.indices.putIfAbsent("cd", new HashSet<>(Arrays.asList(4)));
        distance1Indices.indices.putIfAbsent("dc", new HashSet<>(Arrays.asList(5)));
        indicesList.add(distance1Indices);

        if (maxDistance < 2) {
            return preCreated;
        }
        FuzzySearchIndices.InverseIndices distance2Indices = new FuzzySearchIndices.InverseIndices();
        distance2Indices.indices.putIfAbsent("a", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("c", new HashSet<>(Arrays.asList(4, 5)));
        distance2Indices.indices.putIfAbsent("d", new HashSet<>(Arrays.asList(4, 5)));
        indicesList.add(distance2Indices);

        return preCreated;
    }

    @Test
    public void testWriteAndLoad() throws IOException {
        FuzzyIndicesModel sut = new FuzzyIndicesModel();
        sut.project = new Project();

        sut.columnIndicesMap.put("COLUMN1", createTestIndices(sut.project, "COLUMN1", 2));
        sut.columnIndicesMap.put("COLUMN2", createTestIndices(sut.project, "COLUMN2", 1));

        Path tempPath = Files.createTempFile("FuzzyIndicesModelTest", ".json");
        try {
            try (BufferedWriter bw = Files.newBufferedWriter(tempPath)) {
                JSONWriter writer = new JSONWriter(bw);
                sut.write(writer, new Properties());
            }
            assertTrue(Files.exists(tempPath));

            JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(tempPath)));
            OverlayModel loaded = FuzzyIndicesModel.load(sut.project, jsonObject);

            assertEquals(loaded, sut);
        } finally {
            if (Files.exists(tempPath)) {
                Files.delete(tempPath);
            }
        }
    }
}