/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
 * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;

var logger = Packages.org.slf4j.LoggerFactory.getLogger("fuzzy-match-extension");

/*
 * Function invoked to initialize the extension.
 */
function init() {
  // Packages.java.lang.System.err.println("Initializing sample extension");
  // Packages.java.lang.System.err.println(module.getMountPoint());
    var libPath = new Packages.java.io.File(module.getPath(), "MOD-INF/lib/jython/").getCanonicalPath();

    logger.info("Registering Fuzzy Match Extension......");
    var RS = Packages.com.google.refine.RefineServlet;
    //register command
    RS.registerCommand(module, "create-fuzzy-search-indices",
        new Packages.com.vern1erca11per.refine.extension.fuzzymatch.symspell.CreateFuzzySearchIndicesCommand());

  //register change
  RS.registerClassMapping("UpdateFuzzySearchIndicesModelChange",
  "com.vern1erca11per.refine.extension.fuzzymatch.symspell.UpdateFuzzyIndicesModelChange");
  RS.cacheClass(Packages.com.vern1erca11per.refine.extension.fuzzymatch.symspell.UpdateFuzzyIndicesModelChange);

  //var S = Packages.java.lang.System;
  //var currentLibPath = S.getProperty("");

  //register overlay models
  var project = Packages.com.google.refine.model.Project;
  project.registerOverlayModel(
    "FuzzyIndicesModel",
    Packages.com.vern1erca11per.refine.extension.fuzzymatch.symspell.FuzzyIndicesModel
  );

  //register grel
  var grel = Packages.com.google.refine.grel;
  grel.ControlFunctionRegistry.registerFunction(
        "fuzzyCross",
        new Packages.com.vern1erca11per.refine.extension.fuzzymatch.symspell.FuzzyCross()
        );

  //add operations
  var operations = Packages.com.google.refine.operations;
  var OR = operations.OperationRegistry.registerOperation(
      module,
      "createFuzzySearchIndicesModelOperation",
      Packages.com.vern1erca11per.refine.extension.fuzzymatch.symspell.CreateFuzzySearchIndicesModelOperation
  );


    // Script files to inject into /project page
    ClientSideResourceManager.addPaths(
        "project/scripts",
        module,
        [
            "scripts/project-injection.js",
            //"scripts/index/jquery.contextMenu.min.js",
            //"scripts/index/jquery.ui.position.min.js"
        ]
    );

    // Style files to inject into /index page
    // ClientSideResourceManager.addPaths(
    //     "index/styles",
    //     module,
    //     [
    //         "styles/jquery.contextMenu.css",
    //         "styles/pure.css",
    //         "styles/bootstrap.css",
    //         "styles/database-import.less"
    //     ]
    // );

    // Script files to inject into /project page
    // ClientSideResourceManager.addPaths(
    //     "project/scripts",
    //     module,
    //     [
    //         "scripts/database-extension.js",
    //         "scripts/project/database-exporters.js"
    //     ]
    // );
    logger.info("Fuzzy Match Extension Registration done!!");
}

/*
 * Function invoked to handle each request in a custom way.
 */
//function process(path, request, response) {
//  // Analyze path and handle this request yourself.
//
//  if (path == "/" || path == "") {
//    var context = {};
//    // here's how to pass things into the .vt templates
//    context.someList = ["Superior","Michigan","Huron","Erie","Ontario"];
//    context.someString = "foo";
//    context.someInt = Packages.com.google.refine.sampleExtension.SampleUtil.stringArrayLength(context.someList);
//
//    send(request, response, "index.vt", context);
//  }
//}
//
//function send(request, response, template, context) {
//  butterfly.sendTextFromTemplate(request, response, context, template, encoding, html);
//}
