/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under MIT License by camunda Services GmbH
 *
 * For the full copyright and license information, please read the
 * Camunda-License.txt file that was distributed with this source code.
 */
var saveDiagramFunctionCall;

(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c='function'==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code='MODULE_NOT_FOUND',a;}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r);},p,p.exports,r,e,n,t);}return n[i].exports;}for(var u='function'==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o;}return r;}()({1:[function(require,module,exports){module.exports = {
        name: 'Kitodo Template',
        prefix: 'template',
        uri: 'http://www.kitodo.org/template',
        xml: {tagAlias: 'lowerCase'},
        associations: [],
        types: [{
            name: 'TemplateTaskEvent',
            'extends': ['bpmn:Task'],
            properties: [{name: 'priority',
                isAttr: !0,
                type: 'Integer'
            }, {name: 'editType',
                isAttr: !0,
                type: 'Integer'
            }, {name: 'processingStatus',
                isAttr: !0,
                type: 'Integer'
            }, {name: 'permittedUserRole',
                isAttr: !0,
                type: 'Integer'
            }, {name: 'typeMetadata',
                isAttr: !0,
                type: 'Boolean'
            }, {name: 'typeAutomatic',
                isAttr: !0,
                type: 'Boolean'
            }, {name: 'typeImagesRead',
                isAttr: !0,
                type: 'Boolean'
            }, {name: 'typeImagesWrite',
                isAttr: !0,
                type: 'Boolean'
            }, {name: 'typeExportDMS', isAttr: !0, type: 'Boolean'}, {
                name: 'typeAcceptClose',
                isAttr: !0,
                type: 'Boolean'
            }, {name: 'typeCloseVerify', isAttr: !0, type: 'Boolean'}, {
                name: 'batchStep',
                isAttr: !0,
                type: 'Boolean'
            }]
        }, {
            name: 'TemplateScriptTaskEvent',
            'extends': ['bpmn:ScriptTask'],
            properties: [{name: 'scriptName', isAttr: !0, type: 'String'}, {
                name: 'scriptPath',
                isAttr: !0,
                type: 'String'
            }]
        }]
    };},{}],2:[function(require,module,exports){'use strict';function _interopRequireDefault(obj){return obj&&obj.__esModule?obj:{default:obj};}function setDiagramXML(){placeholderForDiagram=document.getElementById('editForm:workflowTabView:xmlDiagram'),placeholderForDiagram?(diagramXML=placeholderForDiagram.value,openDiagram(diagramXML)):alert('diagram is empty!');}function createNewDiagram(){openDiagram(_newDiagram2.default);}function openDiagram(xml){var xmlParam;var svgParam;bpmnModeler.importXML(xml,function(err){err?(container.removeClass('with-diagram').addClass('with-error'),container.find('.error pre').text(err.message),console.error(err)):container.removeClass('with-error').addClass('with-diagram');})};	saveDiagramFunctionCall = function saveDiagramAction() {var xmlParam = "";var svgParam = "";bpmnModeler.saveXML({format: !0}, function(err, xml) {err ? alert('diagram xml save failed', err) : xmlParam = xml;});bpmnModeler.saveSVG({format: !0}, function(err, svg) {err ? alert('diagram svg save failed', err) : svgParam = svg;});document.getElementById('editForm:workflowTabView:xmlDiagram').value = xmlParam + "kitodo-diagram-separator" + svgParam;};function registerFileDrop(container,callback){function handleFileSelect(e){e.stopPropagation(),e.preventDefault();var files=e.dataTransfer.files;var file=files[0];var reader=new FileReader();reader.onload=function(e){var xml=e.target.result;callback(xml);},reader.readAsText(file);}function handleDragOver(e){e.stopPropagation(),e.preventDefault(),e.dataTransfer.dropEffect='copy';}container.get(0).addEventListener('dragover',handleDragOver,!1),container.get(0).addEventListener('drop',handleFileSelect,!1);}var _jquery=require('jquery');var _jquery2=_interopRequireDefault(_jquery);var _Modeler=require('bpmn-js/lib/Modeler');var _Modeler2=_interopRequireDefault(_Modeler);var _bpmnJsPropertiesPanel=require('bpmn-js-properties-panel');var _bpmnJsPropertiesPanel2=_interopRequireDefault(_bpmnJsPropertiesPanel);var _template=require('./provider/template');var _template2=_interopRequireDefault(_template);var _template3=require('./descriptors/template');var _template4=_interopRequireDefault(_template3);var _minDash=require('min-dash');var _newDiagram=require('../resources/newDiagram.bpmn');var _newDiagram2=_interopRequireDefault(_newDiagram);var container=(0,_jquery2.default)('#js-drop-zone');var placeholderForDiagram=document.getElementById('editForm:workflowTabView:xmlDiagram');var observer=new MutationObserver(function(mutations){mutations.forEach(function(mutation){console.log(mutation),setDiagramXML();});});var config={characterData:!0,subtree:!0};observer.observe(placeholderForDiagram,config);var diagramXML;var bpmnModeler=new _Modeler2.default({container:'#js-canvas',propertiesPanel:{parent:'#js-properties-panel'},additionalModules:[_bpmnJsPropertiesPanel2.default,_template2.default],moddleExtensions:{template:_template4.default}});window.FileList&&window.FileReader?registerFileDrop(container,openDiagram):window.alert('Looks like you use an older browser that does not support drag and drop. '+'Try using Chrome, Firefox or the Internet Explorer > 10.'),(0,_jquery2.default)(function(){(0,_jquery2.default)('#editForm\\:workflowTabView\\:js-create-diagram').click(function(e){e.stopPropagation(),e.preventDefault(),createNewDiagram();}),(0,_jquery2.default)('#editForm\\:workflowTabView\\:btnReadXmlDiagram').click(function(e){e.stopPropagation(),e.preventDefault(),setDiagramXML();});});},{'../resources/newDiagram.bpmn':469,'./descriptors/template':1,'./provider/template':4,'bpmn-js-properties-panel':7,'bpmn-js/lib/Modeler':59,jquery:351,'min-dash':438}],3:[function(require,module,exports){'use strict';function _interopRequireDefault(obj){return obj&&obj.__esModule?obj:{default:obj};}function createGeneralTabGroups(element,bpmnFactory,elementRegistry,translate){var generalGroup={id:'general',label:'General Properties',entries:[]};(0,_IdProps2.default)(generalGroup,element,translate),(0,_NameProps2.default)(generalGroup,element,translate),(0,_ProcessProps2.default)(generalGroup,element,translate);var detailsGroup={id:'details',label:'Details',entries:[]};(0,_LinkProps2.default)(detailsGroup,element,translate),(0,_EventProps2.default)(detailsGroup,element,bpmnFactory,elementRegistry,translate),(0,_SequenceFlowProps2.default)(detailsGroup,element,bpmnFactory,translate);var taskGroup={id:'task',label:'Task Properties',entries:[]};(0,_TaskProperties2.default)(taskGroup,element);var scriptTaskGroup={id:'scriptTask',label:'Script Properties',entries:[]};return(0,_ScriptTaskProperties2.default)(scriptTaskGroup,element),[generalGroup,detailsGroup,taskGroup,scriptTaskGroup];}function TemplatePropertiesProvider(eventBus,bpmnFactory,elementRegistry,translate){_PropertiesActivator2.default.call(this,eventBus),this.getTabs=function(element){var generalTab={id:'general',label:'Properties',groups:createGeneralTabGroups(element,bpmnFactory,elementRegistry,translate)};return[generalTab];};}Object.defineProperty(exports,'__esModule',{value:!0}),exports.default=TemplatePropertiesProvider;var _inherits=require('inherits');var _inherits2=_interopRequireDefault(_inherits);var _PropertiesActivator=require('bpmn-js-properties-panel/lib/PropertiesActivator');var _PropertiesActivator2=_interopRequireDefault(_PropertiesActivator);var _ProcessProps=require('bpmn-js-properties-panel/lib/provider/bpmn/parts/ProcessProps');var _ProcessProps2=_interopRequireDefault(_ProcessProps);var _EventProps=require('bpmn-js-properties-panel/lib/provider/bpmn/parts/EventProps');var _EventProps2=_interopRequireDefault(_EventProps);var _LinkProps=require('bpmn-js-properties-panel/lib/provider/bpmn/parts/LinkProps');var _LinkProps2=_interopRequireDefault(_LinkProps);var _IdProps=require('bpmn-js-properties-panel/lib/provider/bpmn/parts/IdProps');var _IdProps2=_interopRequireDefault(_IdProps);var _NameProps=require('bpmn-js-properties-panel/lib/provider/bpmn/parts/NameProps');var _NameProps2=_interopRequireDefault(_NameProps);var _SequenceFlowProps=require('bpmn-js-properties-panel/lib/provider/camunda/parts/SequenceFlowProps');var _SequenceFlowProps2=_interopRequireDefault(_SequenceFlowProps);var _ScriptTaskProperties=require('./parts/ScriptTaskProperties');var _ScriptTaskProperties2=_interopRequireDefault(_ScriptTaskProperties);var _TaskProperties=require('./parts/TaskProperties');var _TaskProperties2=_interopRequireDefault(_TaskProperties);(0,_inherits2.default)(TemplatePropertiesProvider,_PropertiesActivator2.default);},{'./parts/ScriptTaskProperties':5,'./parts/TaskProperties':6,'bpmn-js-properties-panel/lib/PropertiesActivator':8,'bpmn-js-properties-panel/lib/provider/bpmn/parts/EventProps':33,'bpmn-js-properties-panel/lib/provider/bpmn/parts/IdProps':34,'bpmn-js-properties-panel/lib/provider/bpmn/parts/LinkProps':35,'bpmn-js-properties-panel/lib/provider/bpmn/parts/NameProps':36,'bpmn-js-properties-panel/lib/provider/bpmn/parts/ProcessProps':37,'bpmn-js-properties-panel/lib/provider/camunda/parts/SequenceFlowProps':48,inherits:350}],4:[function(require,module,exports){'use strict';function _interopRequireDefault(obj){return obj&&obj.__esModule?obj:{default:obj};}Object.defineProperty(exports,'__esModule',{value:!0});var _TemplatePropertiesProvider=require('./TemplatePropertiesProvider');var _TemplatePropertiesProvider2=_interopRequireDefault(_TemplatePropertiesProvider);exports.default={__init__:['propertiesProvider'],propertiesProvider:['type',_TemplatePropertiesProvider2.default]};},{'./TemplatePropertiesProvider':3}],5:[function(require,module,exports){'use strict';function _interopRequireDefault(obj){return obj&&obj.__esModule?obj:{default:obj};}Object.defineProperty(exports,'__esModule',{value:!0}),exports.default=function(group,element){(0,_ModelUtil.is)(element,'bpmn:ScriptTask')&&(group.entries.push(_EntryFactory2.default.textField({id:'scriptName',description:'Insert script name',label:'Script name',modelProperty:'scriptName'})),group.entries.push(_EntryFactory2.default.textField({id:'scriptPath',description:'Insert script path',label:'Script path',modelProperty:'scriptPath'})));};var _EntryFactory=require('bpmn-js-properties-panel/lib/factory/EntryFactory');var _EntryFactory2=_interopRequireDefault(_EntryFactory);var _ModelUtil=require('bpmn-js/lib/util/ModelUtil');},{'bpmn-js-properties-panel/lib/factory/EntryFactory':19,'bpmn-js/lib/util/ModelUtil':159}],6:[function(require,module,exports){'use strict';function _interopRequireDefault(obj){return obj&&obj.__esModule?obj:{default:obj};}Object.defineProperty(exports, '__esModule', {value: !0}), exports.default = function (group, element) {
        (0, _ModelUtil.is)(element, 'bpmn:Task') && (group.entries.push(_EntryFactory2.default.textField({
            id: 'priority',
            description: '',
            label: 'Priority',
            modelProperty: 'priority'
        })), group.entries.push(_EntryFactory2.default.selectBox({
            id: 'processingStatus',
            description: '',
            label: 'Processing status',
            selectOptions : [
                {name: 'Locked', value: 0},
                {name: 'Open', value: 1},
                {name: 'In processing', value: 2},
                {name: 'Closed', value: 3}],
            modelProperty: 'processingStatus'
        })), group.entries.push(_EntryFactory2.default.selectBox({
            id: 'permittedUserRole',
            description: '',
            label: 'Permitted User Role',
            selectOptions : availableUserRoles,
            modelProperty: 'permittedUserRole'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeMetadata',
            description: '',
            label: 'Metadata',
            modelProperty: 'typeMetadata'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeImagesRead',
            description: '',
            label: 'Read images',
            modelProperty: 'typeImagesRead'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeImagesWrite',
            description: '',
            label: 'Write images',
            modelProperty: 'typeImagesWrite'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeExportDMS',
            description: '',
            label: 'Export DMS',
            modelProperty: 'typeExportDMS'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeCloseVerify',
            description: '',
            label: 'Validate on exit',
            modelProperty: 'typeCloseVerify'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeAcceptClose',
            description: '',
            label: 'Finalise task after accepting',
            modelProperty: 'typeAcceptClose'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'typeAutomatic',
            description: '',
            label: 'Automatic task',
            modelProperty: 'typeAutomatic'
        })), group.entries.push(_EntryFactory2.default.checkbox({
            id: 'batchStep',
            description: '',
            label: 'Batch task',
            modelProperty: 'batchStep'
        })));