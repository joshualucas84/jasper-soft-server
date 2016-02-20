/*
 * Copyright (C) 2005 - 2015 TIBCO Software Inc. All rights reserved.
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
 * @author: Dima Gorbenko
 * @version: $Id: RepositoryChooserDialogFactory.js 277 2015-10-13 13:54:16Z dgorbenk $
 */

define(function (require) {

	"use strict";

	var repositoryResourceTypes = require("bi/repo/enum/repositoryResourceTypes"),
		RepositoryItemChooserDialog = require("bi/repo/dialog/RepositoryChooserDialog/RepositoryItemChooserDialogView"),
		RepositoryFolderChooserDialog = require("bi/repo/dialog/RepositoryChooserDialog/RepositoryFolderChooserDialogView");

	var registeredDialogs = {
		"item": RepositoryItemChooserDialog,
		"folder": RepositoryFolderChooserDialog
	};

	return {

		getDialog: function(dialogType) {

			var constructor;

			// by default
			constructor = RepositoryItemChooserDialog;

			if (registeredDialogs[dialogType]) {
				constructor = registeredDialogs[dialogType];
			}

			return constructor;
		}
	}
});