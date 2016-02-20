/*
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

define(function (require) {
    "use strict";

    var BaseDialog = require("dataSource/view/dialog/BaseDialog"),
        _ = require("underscore"),
        $ = require("jquery"),
        AjaxFormSubmitter = require("common/transport/AjaxFormSubmitter"),
        dialogs = require("components.dialogs"),
        request = require("request"),
        i18n = require("bundle!jasperserver_messages"),
        uploadJdbcDriverDialogTemplate = require("text!dataSource/template/dialog/uploadJdbcDriverDialogTemplate.htm"),
        fileUploadTemplate = require("text!dataSource/template/dialog/fileUploadTemplate.htm");

    require("utils.common");

    return BaseDialog.extend({
        TITLE: i18n["resource.dataSource.jdbc.selectDriverTitle"],
        PRIMARY_BUTTON_LABEL: i18n["button.upload"],
        SECONDARY_BUTTON_LABEL: i18n["button.cancel"],

        events: function () {
            return _.extend({}, BaseDialog.prototype.events, {
                "change input[type='file']": "onFileChange"
            });
        },

        initialize: function (options) {
            this.driverClass = options.driverClass;
            this.driverAvailable = options.driverAvailable;
            this.fileIndex = 0;
            BaseDialog.prototype.initialize.apply(this, arguments);
        },

        onFileChange: function (e) {
            var $targetEl = $(e.target), $validationEl = $targetEl.next(".message.warning");

            $validationEl.parent().removeClass("error");

            if (!$targetEl.val().match(/.jar$/)) {
                $validationEl.text(i18n["resource.dataSource.jdbc.upload.wrongExtension"]).parent().addClass("error");
            } else if ($targetEl.is(this.$("input[type='file']:last-of-type"))) {

                // determine if we need to add one more input to select more files
                var selectedFiles = 0, inputs = this.$("input[type='file']");
                _.each(inputs, function (input, index) {
                    selectedFiles += index+1;
                });

                if (selectedFiles >= inputs.length) {
                    this.addFileInput();
                }
            }
        },

        render: function () {
            var self = this;

            this.$(".body").html(_.template(uploadJdbcDriverDialogTemplate, {className: this.driverClass, i18n: i18n}));

            _.defer(function () {
                self.addFileInput();

                if (self.driverAvailable) {
                    self.$(".warningMessageContainer").removeClass("hidden").find(".message").text(i18n["resource.dataSource.jdbc.upload.overwriteWarning"]);
                } else {
                    self.$(".warningMessageContainer").addClass("hidden").find(".message").text("");
                }
            });

            return this;
        },

        onSuccessCallback: function(response){
            this.trigger("driverUpload", response);
            this.hide();
            dialogs.systemConfirm.show(i18n["resource.dataSource.jdbc.upload.driverUploadSuccess"]);
            _.defer(_.bind(this.remove, this));
        },

        onErrorCallback: function(response){
            var errorMessage;
            var response = response.responseJSON ? response.responseJSON : response;

            if ("illegal.parameter.value.error" === response.errorCode && response.parameters
                && response.parameters.length && "className" === response.parameters[0]) {
                errorMessage = i18n["resource.dataSource.jdbc.classNotFound"].replace("{0}", this.driverClass);
            } else {
                errorMessage = response.message;
            }

            this.$(".errorMessageContainer").addClass("error").find(".message").text(errorMessage);
        },

        addFileInput: function () {
            this.$("ul").append(_.template(fileUploadTemplate, {fileIndex: this.fileIndex}));
            this.fileIndex++;
        },

        primaryButtonOnClick: function () {
            this.$(".errorMessageContainer").removeClass("error").find(".message").text("");
            var form = this.$("form"), self = this;

            new AjaxFormSubmitter(form[0]).submit().done(function (response) {
                if (response.errorCode) {
                    self.onErrorCallback(response);
                } else {
                    self.onSuccessCallback(response);
                }
            }).fail(function (response) {
                self.onErrorCallback(response);
            });
        },

        secondaryButtonOnClick: function () {
            this.hide();
            _.defer(_.bind(this.remove, this));
        }
    });
});