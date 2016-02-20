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

/**
 * @author: Olesya Bobruyko
 * @version: $Id: designerPermissionTrait.js 8900 2015-05-06 20:57:14Z yplakosh $
 */

define(function(require) {

    var i18n = require("bundle!AttributesBundle"),
        confirmDialogTypesEnum = require("serverSettingsCommon/enum/confirmDialogTypesEnum");

    return {

        _initPermissionConfirmEvents: function() {
            this.listenTo(this.confirmationDialogs[confirmDialogTypesEnum.PERMISSION_CONFIRM], "button:yes", this._onPermissionConfirm);
            this.listenTo(this.confirmationDialogs[confirmDialogTypesEnum.PERMISSION_CONFIRM], "button:no", this._onPermissionCancel);
        },

        _onPermissionConfirm: function() {
            this.currentChildView
                ? this.currentChildView.triggerModelValidation({dfd: this.validateDfD})
                : this.model.get("changedChildView").model.setState("confirmedState");
        },

        _onPermissionCancel: function() {
            this._revertChangedModelProperty("_embedded");
        },

        _getPermissionConfirmContent: function(editMode) {
            var mode = editMode ? "edit" : "view",
                textPropertyName = "attributes.confirm.permission.dialog." + mode + ".mode.text";

            return i18n[textPropertyName];
        },

        _showPermissionConfirm: function(childView) {
            childView._showPermissionConfirm(false);
        }

    };

});