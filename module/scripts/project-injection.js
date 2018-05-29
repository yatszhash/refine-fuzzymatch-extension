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

// This file is added to the /project page

var FuzzyExtension = {};

var FuzzyIndicesCreationDialog = function (columnName, expression) {
    this.column = columnName;
    this.aboutExpression = expression;

    this.createDialog();
};

FuzzyIndicesCreationDialog.prototype.createDialog = function () {
    var self_ = this;

    var dialogElement = $(DOM.loadHTML("fuzzy-match-extension",
        "scripts/create-fuzzy-indices-dialog.html"
    ));

    var controls = DOM.bind(dialogElement);

    controls.dialogHeader.text("Create Fuzzy Index based on " + " " + this.column.name + " ");
    controls.views_configIndices.text("index config");
    controls.views_distance.text("threshold edit distance");
    controls.views_prefixLength.text("prefix length");
    controls.okButton.html($.i18n._('core-buttons')["ok"]);
    controls.cancelButton.text($.i18n._('core-buttons')["cancel"]);


    var dismiss = function () {
        DialogSystem.dismissUntil(level - 1);
    };

    var submit = function (distance, prefixLength) {
        var body = {};
        body.indicesConfig = JSON.stringify([
            {
                columnName: self_.column.name,
                distance: distance,
                prefixLength: prefixLength
            }]);

        Refine.postProcess(
            "fuzzy-match-extension",
            "create-fuzzy-search-indices",
            null,
            body,
            {modelChanged: true},
            {
                onDone: function (o) {
                    dismiss();
                }
            }
        )
    };

    controls.okButton.click(
        function () {
            var distance = parseInt($.trim(controls.distanceInput[0].value), 10);

            if (!distance || distance < 0) {
                alert("distance should be positive integer or 0");
                return;
            }

            var prefixLength = parseInt($.trim(controls.prefixLengthInput[0].value), 10);

            if (!prefixLength || prefixLength <= 0) {
                alert("prefix length should be positive integer or 0");
                return;
            }

            submit(distance, prefixLength);
        }
    );
    controls.cancelButton.click(dismiss);

    var level = DialogSystem.showDialog(dialogElement);
};


DataTableColumnHeaderUI.extendMenu(
    function (column, columnHeaderUI, menu) {
        MenuSystem.appendTo(menu, "", {
                id: "fuzzy-extension/create/indices",
            label: "create fuzzy index",
                click: function () {
                    new FuzzyIndicesCreationDialog(column, "");
                }
            }
        );
    });

