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
 * @version: $Id: RepositoryFolderChooserDialogView.js 295 2015-10-29 17:04:08Z tbidyuk $
 */

define(function (require) {

	"use strict";

	var _ = require('underscore'),
		i18n = require('bundle!all'),
		jrsConfigs = require('jrs.configs'),
		Dialog = require("common/component/dialog/Dialog"),

		treeFactory = require("bi/repo/dialog/RepositoryChooserDialog/RepositoryFolderTreeFactory"),

		content = require('text!./template/repositoryFolderChooserDialogTemplate.htm'),
		tooltipTemplate = require("text!./template/repositoryFolderChooserDialogTooltipTemplate.htm");

	return Dialog.extend({
		
		constructor: function(options) {
			options || (options = {});

			var additionalCssClasses = "selectFolder newResizeableDialog";
			if (options.additionalCssClasses) {
				additionalCssClasses += " " + options.additionalCssClasses;
			}

			Dialog.prototype.constructor.call(this, {
				modal: true,
				resizable: true,
				minWidth: 400,
				minHeight: 400,
				additionalCssClasses: additionalCssClasses,
				title: i18n["repository.content"],
				content: _.template(content)({ i18n: i18n }),
				buttons: [
					{ label: i18n["button.select"], action: "select", primary: true },
					{ label: i18n["button.cancel"], action: "cancel", primary: false }
				]
			});

			this.on('button:select', _.bind(this._onSelectDialogButtonClick, this));
			this.on('button:cancel', _.bind(this._onCancelDialogButtonClick, this));
		},

		initialize: function(options) {
			Dialog.prototype.initialize.apply(this, arguments);

			this.foldersTree = treeFactory({
				contextPath: jrsConfigs.contextPath,
				tooltipContentTemplate: tooltipTemplate
			});

			this.selectedResource = {
				resourceUri: ""
			};

			this.listenTo(this.foldersTree, "selection:change", function(selection){
				var parentFolderUri;

				if (selection && _.isArray(selection) && selection[0] && selection[0].uri) {
					parentFolderUri = selection[0].uri;
				}

				this.selectedResource.resourceUri = parentFolderUri;

				this.enableButton("select");
			});

			var $body = this.$contentContainer.find(".control.groupBox .body");
			$body.append(this.foldersTree.render().el);

			this.disableButton("select");
		},

		open: function(){
			Dialog.prototype.open.apply(this, arguments);
		},

		close: function() {

			Dialog.prototype.close.apply(this, arguments);
		},

		remove: function() {
			this.foldersTree.remove();

			Dialog.prototype.remove.apply(this, arguments);
		},

		_onSelectDialogButtonClick: function(){
			this.close();
		},

		_onCancelDialogButtonClick: function(){
			this.selectedResource.resourceUri = "";
			this.close();
		}
	});

});