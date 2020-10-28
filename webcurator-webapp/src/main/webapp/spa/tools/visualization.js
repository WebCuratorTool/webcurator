function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

var gUrl=null, gReq=null, gCallback=null;
function fetchHttp(url, req, callback){
  var ROOT_URL='/curator';
  var reqUrl=ROOT_URL + url;
  var reqBodyPayload="{}";
  if(req !== null){
    reqBodyPayload = JSON.stringify(req);
  }
  console.log('Fetch url:' + url);

  fetch(reqUrl, { 
    method: 'POST',
    redirect: 'follow',
    headers: {'Content-Type': 'application/json'},
    body: reqBodyPayload
  }).then((response) => {
    if (response.redirected) {
      console.log('Need authentication');
      gUrl=url;
      gReq=req;
      gCallback=callback;
      gParentHarvestResultViewTab.popupLoginWindow();
      return null;
    }

    if(response.headers && response.headers.get('Content-Type') && response.headers.get('Content-Type').startsWith('application/json')){
      console.log('Fetch success and callback');
      return response.json();
    }
  }).then((response) => {
    callback(response);
  });
}

function getEmbedFlag(){
  return true;
}

function authCallback(){
  console.log('Auth call back');
  fetchHttp(gUrl, gReq, gCallback);
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
  }else if(action==='recrawl'){
    target.recrawl(dataset);
  }else if(action==='prune'){
    target.pruneHarvest(dataset);
  }else if(action==='browse'){
    browseUrl(data, scope);
  }else if(action==='download'){
    downloadUrl(data);
  }else if(action==='undo'){
    target.undo(dataset, source);
  }else if(action==='clear'){
    source.clear(dataset);
  }else if(action==='exportInspect'){
    target.exportData(dataset);
  }else if(action==='exportToBeModified'){
    target.exportData(dataset);
  }else if(action==='edit'){
    target.editImport(data);
  }else if(action==='copy'){
    // navigator.clipboard.write(JSON.stringify(data));
  }
}


var itemsPruneHarvest={
    "prune-current": {"name": "Current"},
    "prune-selected": {"name": "Selected"}
};

var itemsRecrawlHarvest={
    "recrawl-current": {"name": "Current"},
    "recrawl-selected": {"name": "Selected"}
};

var itemsBrowse={ 
  "browse-local": {name: "WCT Browse", icon: "far fa-dot-circle"},
  "browse-livesite": {name: "Live Site Browse", icon: "far fa-dot-circle"},
  "browse-openwayback": {name: "OpenWayback Browse", icon: "far fa-dot-circle"},
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
  "sep1": "---------",
  "pruneHarvest": {name: "Prune", icon: "far fa-times-circle", items: itemsPruneHarvest},
  "recrawlHarvest": {name: "Recrawl", icon: "fas fa-redo", items: itemsRecrawlHarvest},
  "import-current": {name: "Import From File", icon: "fas fa-file-import"},
  "sep2": "---------",
  "browse": {name: "Browse", icon: "fab fa-internet-explorer text-primary", items: itemsBrowse},
  "download": {name: "Download", icon: "fas fa-download text-warning"},
  "sep3": "---------",
  "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: {
      "exportInspect-selected": {"name": "Selected"},
      "exportInspect-all": {"name": "All"}
  }},
};

var contextMenuItemsUrlGrid=JSON.parse(JSON.stringify(contextMenuItemsUrlBasic));
var contextMenuItemsUrlTree=JSON.parse(JSON.stringify(contextMenuItemsUrlBasic));

var contextMenuItemsFolderTree={
  "pruneFolder": {name: "Prune Folder", icon: "far fa-times-circle"},
  "recrawlFolder": {name: "Recrawl Folder", icon: "fas fa-redo"},
};

var contextMenuItemsToBeModified={
    "hoppath-current": {name: "HopPath", icon: "fas fa-link"},
    "sep1": "---------",
    "undo": {name: "Undo", icon: "fas fa-undo", items: itemsUndo},
    "sep2": "---------",
    "browse": {name: "Browse", icon: "fab fa-internet-explorer text-primary", items: itemsBrowse},
    "download": {name: "Download", icon: "fas fa-download text-warning"},
    "sep3": "---------",
    "exportLinks": {name: "Export Data", icon: "fas fa-file-export", items: {
        "exportToBeModified-selected": {"name": "Selected"},
        "exportToBeModified-all": {"name": "All"}
    }}
};


function formatModifyHavestGridRow(params){
  if(!params.data.flag){
    return 'grid-row-normal';
  }

  if (params.data.flag==='prune') {
    return 'grid-row-delete';
  }else if (params.data.flag==='recrawl') {
    return 'grid-row-recrawl';
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
      {headerName: "URL", field: "url", width: 1000, filter: true, cellRenderer:  (row) => {
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
      {headerName: "Type", field: "contentType", width: 200, filter: true},
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

function getGridOption(params){
  if (params.value === 'Prune') { //Prune
    return 'text-danger';
  }else if (params.value === 'Import') { //Import by File
    return 'text-primary';
  }else{ //Recrawl
    return 'text-warning';
  }
}

var gridOptionsToBeModified={
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
    {headerName: "Flag", field: "existingFlag", width:80, cellRenderer:  (row) => {
        if (!row.data.existingFlag) {
          return '<span class="right badge badge-danger">New</span>';
        }else{
          return '';
        }
    }},
    {headerName: "Option", field: "option", width:80},
    {headerName: "Target", field: "url", width: 400},
    {headerName: "Source", field: "name", width: 400},
    {headerName: "ModifyDate", field: "lastModified", width: 160, cellRenderer:  (row) => {
        if (row.data.respCode!=0 || !row.data.modifiedMode) {
          return '-';
        }
        
        var modifiedMode=row.data.modifiedMode.toUpperCase();
        if(modifiedMode==='TBC'){
          return 'TBC';
        }else if(row.data.lastModified > 0){
          var dt=moment(row.data.lastModified);
          return dt.format('YYYY-MM-DDTHH:mm');
        }else{
          return modifiedMode;
        }
    }},
  ],
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
    {headerName: "Option", field: "option", width:80},
    {headerName: "Target", field: "url", width: 600, cellRenderer:  (row) => {
        if (!row.data.existingFlag) {
          return '<span class="right badge badge-danger">New</span>&nbsp;'+row.data.url;
        }else{
          return row.data.url;
        }
    }},
    {headerName: "Validation Result", field: "respMsg", width: 300, cellRenderer:  (row) => {
        if (row.data.respCode===0) {
          return 'OK';
        }else{
          return row.data.respMsg;
        }
    }},
    {headerName: "File", field: "name", width: 200},
    {headerName: "ModifyDate", field: "lastModified", width: 160, cellRenderer:  (row) => {
        if (row.data.respCode!=0 || !row.data.modifiedMode) {
          return '-';
        }

        var modifiedMode=row.data.modifiedMode.toUpperCase();
        if(modifiedMode==='TBC'){
          return 'TBC';
        }else if(row.data.lastModified > 0){
          var dt=moment(row.data.lastModified);
          return dt.format('YYYY-MM-DDTHH:mm');
        }else{
          return 'N/A';
        }
    }},
    {headerName: "Action", field: "respCode", width: 120, pinned: "right", cellRenderer:  (row) => {
        if (row.data.respCode!=0) {
          return '<a href="javascript: deleteImportedRow('+row.rowIndex+')"><i class="fas fa-times-circle text-danger"></i><a/>&nbsp;' + row.data.respMsg;
        }
        if (row.data.option==='FILE' && !row.data.isValidFile) {
          return '<a href="javascript: uploadImportedFile('+row.rowIndex+')"><i class="fas fa-cloud-upload-alt text-info">Upload</i><a/>';
        }
        return '<i class="fas fa-check-circle text-success"></i>';
    }},

    
    
  ],
  // getRowClass: formatModifyHavestGridRow
  getRowClass: formatModifyHavestGridRow
};

var StateMap={
    0: 'Finished',
    1: 'Endorsed',
    2: 'Rejected',
    3: 'Indexing',
    4: 'Aborted',
    5: 'Crawling',
    6: 'Modifying'
};

var StatusMap={
    0: '',
    1: 'Scheduled',
    2: 'Running',
    3: 'Paused',
    4: 'Terminated',
    5: 'Finished'
};

function updateDerivedHarvestResults(derivedHarvestResult){
  var reqUrl='/target/derived-harvest-results?targetInstanceOid='+jobId+'&harvestResultId='+harvestResultId+'&harvestNumber='+harvestResultNumber;
  fetchHttp(reqUrl, null, function(hrList){
    console.log(hrList);
    if (derivedHarvestResult) {
      // $('#derived-hr-badge').html(hrList.length);
      $('#derived-hr-badge').html(1);
      $('#derived-hr-badge').show();
    }else{
      $('#derived-hr-badge').hide();
    }

    
    var content='<span class="dropdown-item dropdown-header">Derived Harvest Results</span>';    
    for(var i=0; i<hrList.length; i++){
      var hr=hrList[i];

      content+='<div class="dropdown-divider"></div>';
      content+='<a href="javascript: popupDerivedSummaryWindow('+ hr.oid + ', ' + hr.harvestNumber + ')" class="dropdown-item">';
      
      //Color the new added one
      if (derivedHarvestResult && hr.harvestNumber === derivedHarvestResult.harvestNumber) {
        content+='<i class="fas fa-egg sm-icon text-warning">&nbsp;</i>' + hr.harvestNumber;
      }else{
        content+='<i class="fas fa-egg sm-icon">&nbsp;</i>' + hr.harvestNumber;
      }

      content+='<span class="float-right text-muted text-sm">'+StateMap[hr.state]+' '+StatusMap[hr.status]+'</span>';
      content+='</a>';
    }
    $('#derived-hr-list').html(content);

  });
}

function popupDerivedSummaryWindow(derivedHarvestId, derivedHarvestNumber){
  var reqUrl='/spa/tools/patching-view-hr.html?targetInstanceOid='+jobId+'&harvestResultId='+harvestResultId+'&harvestNumber='+derivedHarvestNumber;
  $('#body-derived-summary').html('<iframe src="'+reqUrl+'" style="width: 100vw; height: 1000px;"></iframe>');
  $('#popup-window-derived-summary').show();
}


var overlayLoadingReferedNumber=0;
function g_TurnOnOverlayLoading(){
  console.log('Loading on: ' + overlayLoadingReferedNumber);
  if (overlayLoadingReferedNumber===0) {
      $('#main-tab-group .overlay').show();
  }
  overlayLoadingReferedNumber++;
}

function g_TurnOffOverlayLoading(){
  console.log('Loading off: ' + overlayLoadingReferedNumber);

  overlayLoadingReferedNumber--;
  if (overlayLoadingReferedNumber === 0) {
    $('#main-tab-group .overlay').hide();
  }
}