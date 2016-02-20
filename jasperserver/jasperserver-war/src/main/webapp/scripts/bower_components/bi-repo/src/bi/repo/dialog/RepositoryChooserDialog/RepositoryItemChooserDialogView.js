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
 * @version: $Id: RepositoryItemChooserDialogView.js 293 2015-10-27 19:17:37Z yplakosh $
 */

define(function (require) {

	"use strict";

	var _ = require('underscore'),
		$ = require("jquery"),
		json3 = require("json3"),

		Dialog = require("common/component/dialog/Dialog"),
		AlertDialog = require("common/component/dialog/AlertDialog"),
		Tree = require('common/component/tree/Tree'),
		TreeDataLayer = require('common/component/tree/TreeDataLayer'),

		tabbedPanelTrait = require('common/component/panel/trait/tabbedPanelTrait'),

		SearchTreePlugin = require('common/component/tree/plugin/SearchPlugin'),
		InfiniteScrollPlugin = require('common/component/tree/plugin/InfiniteScrollPlugin'),
		NoSearchResultsMessagePlugin = require('common/component/tree/plugin/NoSearchResultsMessagePlugin'),

		contextPath = require("jrs.configs").contextPath,
		repositoryResourceTypes = require("bi/repo/enum/repositoryResourceTypes"),
		i18n = require('bundle!adhoc_messages'),
		browserDetection = require("common/util/browserDetection"),

		resourceDialogTemplate = require("text!./template/resourceDialogTemplate.htm"),
		sidebarTreeLeafTemplate = require("text!./template/sidebarTreeLeafTemplate.htm"),
		tabPanelButtonTemplate = require("text!./template/tabPanelButtonTemplate.htm")


	var LIST_ITEM_HEIGHT = 22;
	var LIST_TAB_NAME = "list";
	var TREE_BUFFER_SIZE = 5000;

	var BASE_URI = contextPath + "/rest_v2/api/resources?{{= id != '@fakeRoot' ? 'folderUri=' + id : ''}}";
	var URI_SUFFIX = "&offset={{= offset }}&limit={{= limit }}&forceTotalCount=true&forceFullPage=true";

	var dataUriTemplateForResourceTreeView = BASE_URI + "&recursive=false&containerType=" + repositoryResourceTypes.FOLDER + URI_SUFFIX;
	var dataUriTemplateForResourceListView = BASE_URI + "&recursive=true" + URI_SUFFIX;
	var dataUriTemplate_hackForRootFolderInTree = contextPath + "/flow.html?_flowId=searchFlow&method=getNode&provider=repositoryExplorerTreeFoldersProvider&uri=/&depth=1";

	//////////////////////////////////////////////////////////////

	return Dialog.extend({

		constructor: function (options) {
			options || (options = {});
			this.options = options;

			this._dfdRenderSerachFormTo = $.Deferred();

			var ResourcesTree = Tree
				.use(InfiniteScrollPlugin)
				.create();

			var ResourcesTreeWithSearch = Tree
				.use(NoSearchResultsMessagePlugin)
				.use(SearchTreePlugin, {
					dfdRenderTo: this._dfdRenderSerachFormTo
				})
				.create();

			var processors = {
				treeNodeProcessor: {
					processItem: function (item) {
						item._node = _.contains([repositoryResourceTypes.FOLDER], item.value.resourceType);
						return item;
					}
				},
				filterPublicFolderProcessor: {
					processItem: function (item) {
						if (item.value.uri !== '/public') {
							return item;
						}
					}
				},
				filterTempFolderProcessor: {
					processItem: function (item) {
						if (item.value.uri.indexOf('/temp') === -1) {
							return item;
						}
					}
				},
				filterEmptyFoldersProcessor: {
					processItem: function (item) {
						if (!(item.value.resourceType === 'folder' && item.value._links.content === '')) {
							return item;
						}
					}
				},
				cssClassItemProcessor: {
					processItem: cssClassItemProcessor
				}
			};

			this.prepareSpecificChooserDialog();

			this.resourcesTreeView = ResourcesTree.instance({
				itemsTemplate: sidebarTreeLeafTemplate,
				listItemHeight: LIST_ITEM_HEIGHT,
				selection: { allowed: true, multiple: false },
				rootless: true,
				collapsed: true,
				lazyLoad: true,
				bufferSize: TREE_BUFFER_SIZE,
				additionalCssClasses: "folders",
				dataUriTemplate: dataUriTemplateForResourceTreeView,
				levelDataId: "uri",

				customDataLayers: {
					//workaround for correct viewing of '/public' folder label
					"/": _.extend(new TreeDataLayer({
						dataUriTemplate: dataUriTemplate_hackForRootFolderInTree,
						processors: _.chain(processors).omit("filterPublicFolderProcessor").values().value(),
						getDataArray: function (data) {
							data = json3.parse($(data).text());
							var publicFolderLabel = _.find(data.children, function(item) {
								return item.uri === '/public';
							}).label;

							return [
								{ id: "@fakeRoot", label: data.label, uri: "/", resourceType: 'folder', _links: {content: "@fakeContentLink"} },
								{ id: "/public", label: publicFolderLabel, uri: "/public", resourceType: 'folder', _links: {content: "@fakeContentLink"} }
							];
						}
					}), {
						accept: 'text/html',
						dataType: 'text'
					})
				},
				
				processors: _.values(processors),
				
				getDataArray: function (data, status, xhr) {
					return data && data[repositoryResourceTypes.RESOURCE_LOOKUP] ? data[repositoryResourceTypes.RESOURCE_LOOKUP] : [];
				},

				getDataSize: function (data, status, xhr) {
					return +xhr.getResponseHeader("Total-Count");
				}
			});

			this.resourcesListView = ResourcesTreeWithSearch.instance({
				rootLevelHeight: _.bind(this.getContentHeight, this),
				itemsTemplate: sidebarTreeLeafTemplate,
				listItemHeight: LIST_ITEM_HEIGHT,
				selection: {allowed: true, multiple: false },
				rootless: true,
				collapsed: true,
				lazyLoad: true,
				dataUriTemplate: dataUriTemplateForResourceListView,
				levelDataId: "uri",
				cache: {
					searchKey: "searchString",
					pageSize: 100
				},

				processors: [
					processors.cssClassItemProcessor,
					processors.filterTempFolderProcessor
				],

				getDataArray: function (data, status, xhr) {
					return data && data[repositoryResourceTypes.RESOURCE_LOOKUP] ? data[repositoryResourceTypes.RESOURCE_LOOKUP] : [];
				},

				getDataSize: function (data, status, xhr) {
					return +xhr.getResponseHeader("Total-Count");
				}
			});

			Dialog.prototype.constructor.call(this, {
				template: resourceDialogTemplate,
				modal: true,
				resizable: true,
				additionalCssClasses: "sourceDialogNew",
				title: i18n["ADH_108_DATA_CHOOSER_DIALOG_TITLE"],
				traits: [ tabbedPanelTrait ],
				tabHeaderContainerSelector: ".tabHeaderContainer",
				tabContainerClass: "tabContainer control groupBox treeBox",
				optionTemplate: tabPanelButtonTemplate,
				toggleClass: "down",
				tabs: [
					{
						label: i18n["ADH_108_DATA_CHOOSER_FOLDERS_TAB"],
						action: "tree",
						content: this.resourcesTreeView,
						exposeAction: true,
						additionalCssClasses: "action square small",
						i18n: i18n
					},
					{
						label: i18n["ADH_108_DATA_CHOOSER_LIST_TAB"],
						action: "list",
						content: this.resourcesListView,
						exposeAction: true,
						additionalCssClasses: "action square small",
						i18n: i18n
					}
				],
				buttons: [
					{ label: i18n["ADH_016_BUTTON_OK"], action: "apply", primary: true },
					{ label: i18n["ADH_010_BUTTON_CANCEL"], action: "cancel", primary: false }
				]
			});

			this.disableButton("apply");

			this.on('button:apply', _.bind(this._onSelectDataDialogOkButtonClick, this));
			this.on('button:cancel', _.bind(this._onSelectDataDialogCancelButtonClick, this));
		},

		prepareSpecificChooserDialog: function() {

			if (!this.options.resourcesTypeToSelect || !this.options.resourcesTypeToSelect.length) {
				this.options.resourcesTypeToSelect = [repositoryResourceTypes.REPORT_UNIT];
			}

			var types = [];
			for (var i = 0; i < this.options.resourcesTypeToSelect.length; i++) {
				types.push("type=" + this.options.resourcesTypeToSelect[i]);
			}

			types = types.join("&");

			dataUriTemplateForResourceTreeView = dataUriTemplateForResourceTreeView + "&" + types;
			dataUriTemplateForResourceListView = dataUriTemplateForResourceListView + "&" + types;
		},

		initialize: function (options) {

			this.listenTo(this.resourcesTreeView, "selection:change", this.selectionListener);
			this.listenTo(this.resourcesTreeView, "levelRenderError", _.bind(this._onLevelRenderError, this));
			this.listenTo(this.resourcesTreeView, "item:dblclick", this._onSelectDataDialogOkButtonClick);

			this.listenTo(this.resourcesListView, "selection:change", this.selectionListener);
			this.listenTo(this.resourcesListView, "levelRenderError", _.bind(this._onLevelRenderError, this));
			this.listenTo(this.resourcesListView, "item:dblclick", this._onSelectDataDialogOkButtonClick);
			this.listenTo(this.resourcesListView.searchForm, "search", this._onSearch);
			this.listenTo(this.resourcesListView.searchForm, "clear", this._resetItemSelection);

			this.on("tab:tree tab:list", this._resetItemSelection, this);

			Dialog.prototype.initialize.apply(this, arguments);
		},

		getErrorDialog: function() {
			return this.errorDialog ? this.errorDialog : (this.errorDialog = new AlertDialog());
		},

		events: {
			"resize": "_onResizeHeight"
		},

		getContentHeight: function() {
			return this.$el.height() - this._resizableContainerShiftHeight - 6;
		},

		render: function () {
			Dialog.prototype.render.apply(this, arguments);

			// connect search form to dialog header
			this._dfdRenderSerachFormTo.resolve(this.$tabHeaderContainer);

			this.openTab(LIST_TAB_NAME);

			return this;
		},

		open: function () {
			var self = this;
			this._resizableContainerShiftHeight = 6;

			Dialog.prototype.open.apply(this, arguments);

			var setTabContainerStyles = _.bind(function () {
				this.$contentContainer.find(".tabContainer").css({
					"height": "inherit",
					"overflow-y": "auto"
				});
			}, this);

			/*
			 IE8, IE9 compatibility workaround
			 */
			if (browserDetection.isIE8() || browserDetection.isIE9()) {
				setTimeout(setTabContainerStyles, 1);
			} else {
				setTabContainerStyles();
			}

			this.$el
				.children()
				.not(".subcontainer")
				.not(".ui-resizable-e")
				.not(".ui-resizable-se")
				.each(function() {
					self._resizableContainerShiftHeight += self.$(this).outerHeight(true);
				});
			this.$contentContainer.height(this.$el.height() - this._resizableContainerShiftHeight);
		},

		close: function () {
			if (this.resourcesTreeView) {
				this.resourcesTreeView.collapse("@fakeRoot", {silent: true});
				this.resourcesTreeView.collapse("/public", {silent: true});
				this.resourcesTreeView.resetSelection();
			}

			Dialog.prototype.close.apply(this, arguments);
		},

		selectionListener: function (selection) {

			var okButton = _.find(this.buttons.options, function(option) {
				return option.model.attributes.action === "apply";
			}).$el;

			var itemSelected = selection && _.isArray(selection) && selection[0];
			var resourceUri = itemSelected ? selection[0].uri : undefined;
			var resourceType = itemSelected ? selection[0].resourceType : undefined;

			if (!(itemSelected || resourceUri || resourceType)) {
				this.disableButton("apply");
				this.$('.itemDescription').empty();
				return;
			}

			resourceType === repositoryResourceTypes.FOLDER
				? this.$('.itemDescription').empty()
				: this.$(".itemDescription").text(itemSelected.description || "");

			// set the label of the button even if it already contains the same label
			okButton.find(".wrap").text(i18n["ADH_016_BUTTON_OK"]);

			// do not enable OK button if user has clicked on folders which he might want to open
			if (_.contains(_.union([repositoryResourceTypes.FOLDER]), resourceType)) {
				
				// drop information about selecting which probably was made already
				this.selectedResource = false;
				this.disableButton("apply");
				return;
			}

			// save selected resource information.
			// this could be used by anyone to get information about what has been selected
			this.selectedResource = {
				resourceUri: resourceUri,
				event: this._getAdHocFlowEvent(resourceType)
			};

			this.enableButton("apply");
		},

		_onResizeHeight: function() {
			this.$contentContainer.height(this.$el.height() - this._resizableContainerShiftHeight);

			this.resourcesListView.rootLevel.list.$el.height(this.$el.height() - this._resizableContainerShiftHeight - 6);
			this.resourcesTreeView.rootLevel.list.$el.css("height", "100%");
		},

		_onSearch: function() {
			this.openTab(LIST_TAB_NAME);
			this.disableButton("apply");
		},
		_resetItemSelection: function() {
			this.resourcesTreeView.resetSelection();
			this.resourcesListView.resetSelection();
			this.$('.itemDescription').empty();
		},

		_getAdHocFlowEvent: function (resourceType) {
			var adHocFlowEvent = "";
			if (resourceType == repositoryResourceTypes.REPORT_UNIT || resourceType == repositoryResourceTypes.DOMAIN_TOPIC) {
				adHocFlowEvent = "startAdHocWithTopic";
			} else if (resourceType == repositoryResourceTypes.SEMANTIC_LAYER_DATA_SOURCE) {
				adHocFlowEvent = "startQueryBuilder";
			}

			return adHocFlowEvent;
		},

		_onSelectDataDialogOkButtonClick: function () {
			if (this.selectedResource) {
				this.close();
			}
		},

		_onLevelRenderError: function(responseStatus, error, level){
			error = json3.parse(error);
			var errorDialog = this.getErrorDialog();
			errorDialog.setMessage(error.parameters[0].substring(error.parameters[0].indexOf(": ") + 2, error.parameters[0].indexOf("\n")));
			errorDialog.open();
			level.$el.removeClass(level.loadingClass).addClass(level.openClass);
		},

		_onSelectDataDialogCancelButtonClick: function () {
			// removing the selection if some was made
			this.selectedResource = false;
			this.close();
		},

		_getParameterByName: function (name) {
			var urlParamPattern = new RegExp("[\\?&]" + name + "=([^&#]*)"),
				paramValues = urlParamPattern.exec(location.search);
			return paramValues == null ? "" : decodeURIComponent(paramValues[1].replace(/\+/g, " "));
		}
	});

	function cssClassItemProcessor(item) {

		switch (item.value.resourceType) {

			case repositoryResourceTypes.REPORT_UNIT:
				item.cssClass = "topic";
				break;

			case repositoryResourceTypes.ADHOC_DATA_VIEW:
				item.cssClass = "adhocDataView";
				break;

			default:
				break;
		}

		return item;
	}

});