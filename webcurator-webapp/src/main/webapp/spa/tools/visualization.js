function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

function saveData(data, fileName) {
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";

    var json = JSON.stringify(data),
        blob = new Blob([data], {type: "text/plain;charset=utf-8"}),
        url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
}

// var status='on';
function toggleNetworkMapGrid(status){
  if (status === 'on') {
    $('#network-map-canvas').width('calc(100vw - 30px)');
    $('#networkmap-side-container').hide();
    // status='off';
  }else{
    $('#network-map-canvas').width('75vw');
    $('#networkmap-side-container').show();
    // status='on';
  }
}

function formatStringArrayToJsonArray(listStr){
  var listObj=[];
  for(var i=0;i<listStr.length;i++){
    var elementStr=listStr[i];
    var elementObj=JSON.parse(elementStr);
    listObj.push(elementObj);
  }

  return listObj;
}

function sp(id){
  $(".main-nav-link").removeClass("active");
  $("#"+id).addClass("active");

  $(".subnav").hide();
  $("#navbar-nav-"+id).show();

  $(".content-page").hide();
  $("#page-"+id).show();
}

// function checkUrls(domainName, contentType, statusCode){
//   var aryDomainName=[];
//   if (domainName && domainName!==null && domainName!=="null") {
//     aryDomainName.push(domainName);
//   }

//   var aryContentType=[];
//   if(contentType && contentType!==null && contentType!=="null"){
//     aryContentType.push(contentType);
//   }

//   var aryStatusCode=[];
//   if(statusCode && statusCode > 0){
//     aryStatusCode.push(statusCode);
//   }

//   var searchCondition={
//     "domainNames": aryDomainName,
//     "contentTypes": aryContentType,
//     "statusCodes": aryStatusCode
//   }

//   var sourceUrl="/visualization/networkmap/search/urls?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber;
//   fetch(sourceUrl, {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//     },
//     body: JSON.stringify(searchCondition)
//   }).then((response) => {
//       return response.json();
//   }).then((rawData) => {
//       var data=formatStringArrayToJsonArray(rawData);
//       drawNetworkUrlGrid(data);

//       sp("urls");
//   });
// }

function formatDataForTreeGrid(listObj){
  for(var i=0;i<listObj.length;i++){
    var e=listObj[i];
    e.title=e.url;
    if (e.outlinks.length>0) {
      e.lazy=true;
    }else{
      e.lazy=false;
    }
    delete e["children"];
    delete e["outlinks"];
    //addTitleForTreeGrid(e.children);
  }
  return listObj;
}

var K=1024, M=K*1024, G=M*1024;
function formatContentLength(l){
  if(l>G){
    return Math.round(l/G)+'G';
  }else if(l>M){
    return Math.round(l/M)+'M';
  }else if(l>K){
    return Math.round(l/K)+'K';
  }else{
    return l;
  }
}

function renderImportOption(params){
  if(params.data.option && params.data.option==='doc'){
    return '<i class="far fa-file-alt text-danger">File</i>';
  }
  if(params.data.option && params.data.option==='url'){
    return '<i class="fas fa-link text-primary>URL</i>';
  }
}

function formatContentLengthAg(params){
    return formatContentLength(params.value);
}

function contextMenuCallback(key, data, source, target){
  var keyItems=key.split('-');
  var action=keyItems[0], scope=keyItems[1];
  var dataset;
  if(scope==='current'){
    dataset=[data];
  }else if(scope==='selected'){
    dataset=source.getSelectedNodes();
    // source.deselectAll();
  }else if(scope==='all'){
    dataset=source.getAllNodes();
  }

  if(action==='hoppath'){
    visHopPath.draw(data.id);
  }else if(action==='import'){
    target.showImport(data);
  }else if(action==='outlinks'){
    target.showOutlinks(dataset);
  }else if(action==='prune'){
    target.pruneHarvest(dataset);
  }else if(action==='browse'){
    // 
  }else if(action==='undo'){
    target.undo(dataset, source);
  }else if(action==='clear'){
    source.clear(dataset);
  }else if(action==='export'){
    target.exportData(dataset);
  }
}


var itemsPruneHarvest={
                  "prune-current": {"name": "Current"},
                  "prune-selected": {"name": "Selected"}
              };
var itemsHierarchyOutlink={
                  "outlinks-current": {"name": "Current"},
                  "outlinks-selected": {"name": "Selected"}
              };
var itemsClearHarvest={
                  "clear-current": {"name": "Current"},
                  "clear-selected": {"name": "Selected"},
                  "clear-all": {"name": "All"},
              };
var itemsBrowse={
                  "browse-Url": {name: "Review this URL", icon: "fas fa-dice-one"},
                  "browse-InAccessTool": {name: "Review in Access Tool", icon: "fas fa-dice-two"},
                  "browse-LiveSite": {name: "Live Site", icon: "fas fa-dice-three"},
                  "browse-ArchiveOne": {name: "Archive One", icon: "fas fa-dice-four"},
                  "browse-ArchiveTwo": {name: "Archive Two", icon: "fas fa-dice-five"},
                  "browse-WebArchive": {name: "Web Archive", icon: "fas fa-dice-six"}
                };
var itemsExportLinks={
                  "export-selected": {"name": "Selected"},
                  "export-all": {"name": "All"}
              };
var itemsUndo={
                  "undo-current": {name: "Current"},
                  "undo-selected": {name: "Selected"},
                  "undo-all": {name: "All"}
                };

var contextMenuItemsUrlBasic={
                  "hoppath-current": {name: "HopPath Current", icon: "fas fa-link"},
                  "import-current": {name: "Import Current", icon: "fas fa-file-import"},
                  "sep1": "---------",
                  "pruneHarvest": {name: "Prune", icon: "far fa-times-circle", items: itemsPruneHarvest},
                  "sep2": "---------",
                  // "clearHarvest": {name: "Clear", icon: "delete", items: itemsClearHarvest},
                  // "hierarchyOutlinks": {name: "Inspect Outlinks", icon: "fab fa-think-peaks", items: itemsHierarchyOutlink},
                  "hierarchyOutlinks": {name: "Inspect Outlinks", icon: "far fa-eye", items: itemsHierarchyOutlink},
                  "sep3": "---------",
                  "browseUrl": {name: "Browse Current", icon: "fab fa-chrome", items: itemsBrowse},
                  "sep4": "---------",
                  "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: itemsExportLinks}
                };
var contextMenuItemsUrlTree={
                  "hoppath-current": {name: "HopPath", icon: "fas fa-link"},
                  "import-current": {name: "Import", icon: "fas fa-file-import"},
                  "sep1": "---------",
                  "pruneHarvest": {name: "Prune", icon: "far fa-times-circle", items: itemsPruneHarvest},
                  "sep2": "---------",
                  "browseUrl": {name: "Browse", icon: "fab fa-chrome", items: itemsBrowse},
                  "sep3": "---------",
                  "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: itemsExportLinks}
                };
var contextMenuItemsPrune={
    "hoppath-current": {name: "HopPath", icon: "fas fa-link"},
    "sep1": "---------",
    "undo": {name: "Undo", icon: "fas fa-undo", items: itemsUndo},
    "sep2": "---------",
    "browseUrl": {name: "Browse", icon: "fab fa-chrome", items: itemsBrowse},
    "sep3": "---------",
    "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: itemsExportLinks}
};

var contextMenuItemsImport={
  "undo": {name: "Undo", icon: "fas fa-undo", items: itemsUndo},
  "sep1": "---------",
  "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: itemsExportLinks}
};

function formatModifyHavestGridRow(params){
  if(!params.data.flag){
    return 'grid-row-normal';
  }

  if (params.data.flag==='prune') {
    return 'grid-row-delete';
  }else if (params.data.flag==='import') {
    return 'grid-row-import';
  }else if (params.data.flag==='new') {
    return 'grid-row-new';
  }

  return 'grid-row-normal';
};

var gridRowClassRules={
  'grid-row-normal': function(params){return !params.data.flagDelete && !params.data.flagNew},
  'grid-row-delete': function(params){return params.data.flagDelete},
  'grid-row-new': function(params){return params.data.flagNew}
}

var gridOptionsCandidate={
  suppressRowClickSelection: true,
  rowSelection: 'multiple',
  defaultColDef: {
    resizable: true,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "", width:45, pinned: "left", headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, checkboxSelection: true},
    {headerName: "Normal", children:[
      {headerName: "URL", field: "url", width: 400, filter: true, cellRenderer:  (row) => {
          if(row.data.seedType===-1 || row.data.seedType===2){
            return row.data.url;
          }
          if(row.data.seedType===0){
            return '<span class="right badge badge-danger">P</span>&nbsp;' + row.data.url;
          }
          if(row.data.seedType===1){
            return '<span class="right badge badge-warning">S</span>&nbsp;' + row.data.url;
          }
      }},
      {headerName: "Type", field: "contentType", width: 120, filter: true},
      {headerName: "Status", field: "statusCode", width: 100, filter: 'agNumberColumnFilter'},
      {headerName: "Size", field: "contentLength", width: 100, filter: 'agNumberColumnFilter', valueFormatter: formatContentLengthAg},
    ]},
    {headerName: "Outlinks", children:[
        {headerName: "TotUrls", field: "totUrls", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "Failed", field: "totFailed", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "Success", field: "totSuccess", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "TotSize", field: "totSize", width: 100, filter: 'agNumberColumnFilter', valueFormatter: formatContentLengthAg},
    ]},
  ],
  // rowClassRules: gridRowClassRules,
  getRowClass: formatModifyHavestGridRow
};

var gridOptionsPrune={
  suppressRowClickSelection: true,
  rowSelection: 'multiple',
  defaultColDef: {
    resizable: true,
    filter: true,
    sortable: true
  },
  rowData: [],
  components: {
    renderImportOption: renderImportOption
  },
  columnDefs: [
    {headerName: "", width:45, pinned: "left", headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, checkboxSelection: true},
    {headerName: "Normal", children:[
      {headerName: "URL", field: "url", width: 400, filter: true},
      {headerName: "Type", field: "contentType", width: 120, filter: true},
      {headerName: "Status", field: "statusCode", width: 100, filter: 'agNumberColumnFilter'},
      {headerName: "Size", field: "contentLength", width: 100, filter: 'agNumberColumnFilter', valueFormatter: formatContentLengthAg},
    ]},
    {headerName: "Outlinks", children:[
        {headerName: "TotUrls", field: "totUrls", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "Failed", field: "totFailed", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "Success", field: "totSuccess", width: 100, filter: 'agNumberColumnFilter'},
        {headerName: "TotSize", field: "totSize", width: 100, filter: 'agNumberColumnFilter', valueFormatter: formatContentLengthAg},
     ]},
    // {headerName: "Cascade", field: "flagCascade", width: 40, filter: true, pinned: 'right', cellRenderer: 'renderImportOption', cellClass: 'grid-cell-centered'}
  ],
  // rowClassRules: gridRowClassRules
  getRowClass: formatModifyHavestGridRow
};

var gridOptionsImport={
  suppressRowClickSelection: true,
  rowSelection: 'multiple',
  defaultColDef: {
    resizable: true,
    filter: true,
    sortable: true
  },
  rowData: [],
  components: {
    renderImportOption: renderImportOption
  },
  columnDefs: [
    {headerName: "", width:45, pinned: "left", headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, checkboxSelection: true},
    {headerName: "Option", field: "option", width:80, cellClass: function(params) { return (params.value==='File'?'text-primary':'text-danger');}},
    {headerName: "Target", field: "url", width: 400},
    {headerName: "Source", field: "name", width: 400},
    {headerName: "ModifyDate", field: "lastModified", width: 160, cellRenderer:  (row) => {
       var dt=new Date(row.data.lastModified);
       return dt.toLocaleDateString() + " " + dt.toLocaleTimeString();
    }},
    // {headerName: "#", field: "uploadedFlag", width: 50, pinned: "right", cellClass: "import-result", cellRenderer:  (row) => {
    //       if(row.data.uploadedFlag && row.data.uploadedFlag===1){
    //         return '<i class="fas fa-check-circle text-success"></i>';
    //       }else if(row.data.uploadedFlag && row.data.uploadedFlag===-1){
    //         return '<i class="fas fa-times-circle text-danger"></i>';            
    //       }else{
    //         return '<i class="fas fa-cloud-upload-alt text-info"></i>'
    //       }
    //   }}
  ],
  // getRowClass: formatModifyHavestGridRow
  getRowClass: formatModifyHavestGridRow
};

var gridOptionsImportPrepare={
  suppressRowClickSelection: true,
  rowSelection: 'multiple',
  defaultColDef: {
    resizable: true,
    filter: true,
    sortable: true
  },
  rowData: [],
  components: {
    renderImportOption: renderImportOption
  },
  columnDefs: [
    // {headerName: "", width:45, pinned: "left", headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, checkboxSelection: true},
    // {headerName: "Msg", field: "respMsg", width: 400},
    {headerName: "Option", field: "option", width:80, cellClass: function(params) { return (params.value==='File'?'text-primary':'text-danger');}},
    {headerName: "Target", field: "url", width: 400},
    {headerName: "Source", field: "name", width: 400},
    {headerName: "ModifyDate", field: "lastModified", width: 160, cellRenderer:  (row) => {
       var dt=new Date(row.data.lastModified);
       return dt.toLocaleDateString() + " " + dt.toLocaleTimeString();
    }},
    {headerName: "Progress", field: "respCode", width: 200, pinned: "right", cellRenderer:  (row) => {
        var badge='';
        if(row.data.respCode && row.data.respCode===1){
          badge = '<i class="fas fa-check-circle text-success"></i>';
        }else if(row.data.respCode && row.data.respCode<0){
          badge = '<i class="fas fa-times-circle text-danger"></i>';            
        }else{
          badge = '<i class="fas fa-cloud-upload-alt text-info"></i>';
        }
        return badge+'&nbsp;'+row.data.respMsg;
    }},
    
  ],
  // getRowClass: formatModifyHavestGridRow
  getRowClass: formatModifyHavestGridRow
};