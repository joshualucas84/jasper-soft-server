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
 * @version: $Id: outputTabView.js 9604 2015-10-29 17:03:48Z tbidyuk $
 */

/* global outputRepository */

define(function (require) {

	"use strict";

	var $ = require('jquery'),
		_ = require('underscore'),
		i18n = require('bundle!all'),
		config = require('jrs.configs'),
		Backbone = require('backbone'),
		picker = require('components.pickers'),
		resource = require('resource.base'),
		outputTabTemplate = require("text!scheduler/template/editor/outputTabTemplate.htm"),
		RepositoryChooserDialogFactory = require("bi/repo/dialog/RepositoryChooserDialog/RepositoryChooserDialogFactory");

	return Backbone.View.extend({

		events: {
			"click .ftp-test": "testFTPConnection",
			"click [name=outputRepositoryButton]": "outputRepositoryButtonClick"
		},

		// initialize view
		initialize: function () {

			// save link to context
			var self = this;

			this.model.on('change:repositoryDestination', function (model, value) {
				var rp = model.get('repositoryDestination');
				self.$el.find("[name=ftpPort]").val(rp.outputFTPInfo.port);
				self.$el.find("[name=useFTPS]").prop("checked", rp.outputFTPInfo.type === "TYPE_FTPS");
			});
		},

		getFolderChooserDialog: function() {
			if (this.folderChooserDialog) {
				return this.folderChooserDialog;
			}

			var self = this;
			var Dialog = RepositoryChooserDialogFactory.getDialog("folder");
			this.folderChooserDialog = new Dialog();

			this.listenTo(this.folderChooserDialog, "close", function() {
				var resourceUri;

				if (!this.folderChooserDialog.selectedResource) {
					return;
				}
				if (!this.folderChooserDialog.selectedResource.resourceUri) {
					return;
				}

				resourceUri = this.folderChooserDialog.selectedResource.resourceUri;

				self.$el.find('[name=outputRepository]').val(resourceUri).trigger('change');
			});

			return this.folderChooserDialog;
		},

		outputRepositoryButtonClick: function() {
			var dfd = new $.Deferred(),
				path = this.model.get("repositoryDestination").folderURI,
				splittedPath = path.split("/"),
				dialog = this.getFolderChooserDialog(),
				foldersTree = dialog.foldersTree;


			// if path looks like this -> /path/Samples/Reports, we need to cut Reports part to open
			// only /path/Samples
			var pathToOpen = splittedPath.splice(0, splittedPath.length - 1).join("/");

			dialog.open();

			foldersTree.rootLevel.on("ready", function() {
				dfd.done(function() {

					foldersTree.select(path); // select /path/Samples/Reports

					// scroll to item
					var $tree = foldersTree.$el,
						$scrollContainer = $tree.parent(),
						selectedItem = foldersTree.getLevel(path).$el,
						scrollTo = (selectedItem.offset().top - $tree.offset().top) - $scrollContainer.height() / 2;

					$scrollContainer.scrollTop(scrollTo);
				});

				open.call(foldersTree, pathToOpen, dfd);
			});
		},

		render: function() {

			var templateData = _.extend({}, {
				_: _,
				i18n: i18n,
				timeZones: config.timeZones
			}, this.model.attributes);

			this.setElement($(_.template(outputTabTemplate, templateData)));

			var self = this;

			// adjust this checkbox into proper state depending on the server-side variable
			this.$el.find("[name=outputToHostFileSystem]").attr("disabled", (config.enableSaveToHostFS === "true" || config.enableSaveToHostFS === true) ? false : "disabled");

			this.map = [
				{attr: 'baseOutputFilename', control: 'baseOutputFilename'},
				{attr: 'repositoryDestination/outputDescription', control: 'outputDescription'},
				{attr: 'outputTimeZone', control: 'timeZone'},
				{attr: 'outputLocale', control: 'outputLocale'},
				{attr: 'outputFormats/outputFormat', control: 'outputFormats'},
				{attr: 'repositoryDestination/overwriteFiles', control: 'overwriteFiles'},
				{attr: 'repositoryDestination/sequentialFilenames', control: 'sequentialFilenames'},
				{attr: 'repositoryDestination/timestampPattern', control: 'timestampPattern'},
				{
					attr: 'repositoryDestination', control: 'timestampPattern',
					depends: 'repositoryDestination/sequentialFilenames'
				},
				{attr: 'repositoryDestination/saveToRepository', control: 'outputToRepository'},
				{
					attr: 'repositoryDestination', control: 'outputRepository, outputRepositoryButton',
					depends: 'repositoryDestination/saveToRepository'
				},

				{attr: 'repositoryDestination/folderURI', control: 'outputRepository'},
				{
					attr: 'repositoryDestination', control: 'outputHostFileSystem',
					depends: 'repositoryDestination/outputLocalFolder', disabled: !(config.enableSaveToHostFS === "true" || config.enableSaveToHostFS === true)
				},
				{
					attr: 'repositoryDestination/outputLocalFolder', control: 'outputToHostFileSystem',
					getter: function (value) {
						if (false === value) return null;
						return '';
					},
					setter: function (value) {
						return value === '' || !!value;
					}
				},
				{attr: 'repositoryDestination/outputLocalFolder', control: 'outputHostFileSystem'},
				{
					attr: 'repositoryDestination/outputFTPInfo/enabled', control: 'outputToFTPServer',
					getter: function (value) {
						if (value === false) {
							// clear the errors in this block in case when this block has some errors and user decided to disable it
							self.$el.find("#ftpServerOutput").find('.error').removeClass('error');
						}
						return value;
					}
				},
				{
					attr: 'repositoryDestination/outputFTPInfo',
					control: 'ftpAddress, ftpDirectory, ftpUsername, ftpPassword, ftpTestButton, ftpPort, useFTPS',
					depends: 'repositoryDestination/outputFTPInfo/enabled'
				},
				{attr: 'repositoryDestination/outputFTPInfo/serverName', control: 'ftpAddress'},
				{attr: 'repositoryDestination/outputFTPInfo/folderPath', control: 'ftpDirectory'},
				{
					attr: 'repositoryDestination/outputFTPInfo/userName', control: 'ftpUsername',
					setter: function (value) {
						if (value == "anonymous")
							return null;
						return value;
					}
				},
				{attr: 'repositoryDestination/outputFTPInfo/password', control: 'ftpPassword'},
				{attr: 'repositoryDestination/outputFTPInfo/port', control: 'ftpPort'}
			];

			_.each(this.map, function (data) {
				// get control by name
				data.control = data.control.split(',');
				data.control = data.control.map(function (a) {
					return '[name=' + a + ']'
				});
				data.control = self.$el.find(data.control.join(','));

				if (data.attr) data.attr = data.attr.split('/');

				if (data.depends) {
					if (data.depends[0] === '!') {
						data.invert = true;
						data.depends = data.depends.substr(1);
					}
				}

				if (data.depends && !data.attr) {
					data.depends = self.$el.find('[name=' + data.depends + ']');

					var change = function () {
						var val = data.setter
							? data.setter(data.depends)
							: data.depends.val();

						data.control.attr('disabled', data.disabled ? "disabled" : (data.invert ? !!val : !val));
					};

					data.depends.on('change', change);

					return change();
				}

				// handle model change and update element
				self.model.on('change:' + data.attr[0], function (model, value) {
					value = model.value(data.attr.join('/'));

					if (data.setter)
						value = data.setter(value);

					if (data.depends) {
						var val = model.value(data.depends);

						if (data.setter)
							val = data.setter(val);
						else if (val === '') val = true;

						return data.control.attr('disabled', data.disabled ? "disabled" : (data.invert ? !!val : !val));
					}

					if (data.control.is('[type=checkbox]') && data.control.length === 1)
						data.control.prop('checked', value);
					else if (data.control.is('[type=radio]'))
						data.control.filter('[value=' + value + ']').prop('checked', true);
					else
						data.control.val(value);
				});

				// handle element change and update model
				if (data.control.length && !data.depends) {
					data.control.on('change', function () {
						var key, value, update, target;

						value = data.control.is('[type=checkbox]')
							? data.control.filter(':checked').map(function () {
							return $(this).val()
						}).get()
							: data.control.val();

						if (data.control.is('[type=checkbox]') && data.control.length === 1)
							value = !!value[0];

						if (data.control.is('[type=radio]'))
							value = data.control.filter(':checked').val();

						if (data.getter)
							value = data.getter(value, _.clone(self.model.value(data.attr.join('/'))));

						if (data.attr.length > 1) {
							target = update = _.clone(self.model.get(data.attr[0]));

							for (var i = 1, l = data.attr.length - 1; i < l; i++) {
								key = data.attr[i];
								target[key] = target[key]
									? _.clone(target[key])
									: {};
								target = target[key];
							}

							target[data.attr[data.attr.length - 1]] = value;
						}

						self.model.update(data.attr[0], update || value);
					});
				}
			});

			this.$el.find("[name=useFTPS]").on("click", function () {
				var type = "TYPE_FTP", port = "21";
				if ($(this).is(":checked")) {
					type = "TYPE_FTPS";
					port = "990";
				}

				var m = $.extend(true, {}, self.model.get("repositoryDestination"));
				m = m || {};
				m.outputFTPInfo = m.outputFTPInfo || {};
				m.outputFTPInfo.type = type;
				m.outputFTPInfo.port = port;

				self.model.update('repositoryDestination', m);
			});
		},

		testFTPConnection: function () {
			$("#ftpTestButton").addClass('disabled');
			this.model.testFTPConnection(function () {
				$("#ftpTestButton").removeClass('disabled');
			});
		}
	});

	function open(path, dfd, index) {
		var self = this,
			splittedPath = path.split("/"),
			level;

		index = index || 0;

		var pathFragment = splittedPath[index]
			? path.match(new RegExp(".*" + "\\/" + splittedPath[index]))[0]
			: "/";

		level = this.getLevel(pathFragment);

		// get out if it's the end of the path
		if (index === splittedPath.length) {
			return dfd.resolve(splittedPath);
		}

		if (level) { // if level exists open it and go ahead
			if (level.collapsed) { // open if collapsed
				level.once("ready", function() {
					open.call(self, path, dfd, index + 1); // open next level
				});

				level.open();
			} else {
				open.call(this, path, dfd, index + 1);
			}
		}
	}

});