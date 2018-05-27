package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import com.google.refine.commands.EngineDependentCommand;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class CreateFuzzySearchIndicesCommand extends EngineDependentCommand {

    @Override
    protected AbstractOperation createOperation(Project project, HttpServletRequest httpServletRequest, JSONObject jsonObject) throws Exception {
        String jsonString = httpServletRequest.getParameter("indicesConfig");

        if (jsonString == null) {
            throw new ServletException("require indices config in parameter");
        }
        Map<String, Integer> indicesConfig = new HashMap<>();

        for (Object eachConfig : new JSONArray(jsonString)) {
            if (eachConfig instanceof JSONObject) {
                indicesConfig.put(((JSONObject) eachConfig).getString("columnName"),
                        ((JSONObject) eachConfig).getInt("distance"));
            } else {
                throw new SecurityException("invalid form of parameter");
            }
        }

        return new CreateFuzzySearchIndicesModelOperation(project, indicesConfig);
    }
}
