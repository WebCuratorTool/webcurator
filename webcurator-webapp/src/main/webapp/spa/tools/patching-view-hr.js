function rendLogViewColumn(row){
   if (row.data.flagShowMsg) {return '';}
   return '<a href="'+webContextPath+'/curator/target/patch-log-viewer.html?targetInstanceOid='+jobId+'&harvestResultNumber='+harvestResultNumber+'&logFileName='+row.data.name+'&prefix='+row.data.prefix+'" target="_blank">View</a>';
}

function rendLogDownloadColumn(row){
   if (row.data.flagShowMsg) {return '';}
   return '<a href="'+webContextPath+'/curator/target/patch-log-retriever.html?targetInstanceOid='+jobId+'&harvestResultNumber='+harvestResultNumber+'&logFileName='+row.data.name+'&prefix='+row.data.prefix+'" target="_blank">Download</a>';
}

var gridOptionsLogsCrawling= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "Patch Crawling Logs: File Name", field: "name", width:380},
    {headerName: "View", field: "name", width: 80, pinned: "right", cellRenderer: rendLogViewColumn},
    {headerName: "Download", field: "name", width: 110, pinned: "right", cellRenderer: rendLogDownloadColumn},
    {headerName: "Size", field: "lengthString", width: 90, pinned: "right", cellRenderer:  (row) => {
        if (row.data.flagShowMsg) {return '';}
        return row.data.lengthString;
    }},
  ]
};

var gridOptionsLogsModifying= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "Modifying Logs: File Name", field: "name", width:380},
    {headerName: "View", field: "name", width: 80, pinned: "right", cellRenderer: rendLogViewColumn},
    {headerName: "Download", field: "name", width: 110, pinned: "right", cellRenderer: rendLogDownloadColumn},
    {headerName: "Size", field: "lengthString", width: 90, pinned: "right", cellRenderer:  (row) => {
        if (row.data.flagShowMsg) {return '';}
        return row.data.lengthString;
    }},
  ]
};

var gridOptionsLogsIndexing= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "Indexing Logs: File Name", field: "name", width:380},
    {headerName: "View", field: "name", width: 80, pinned: "right", cellRenderer: rendLogViewColumn},
    {headerName: "Download", field: "name", width: 110, pinned: "right", cellRenderer: rendLogDownloadColumn},
    {headerName: "Size", field: "lengthString", width: 90, pinned: "right", cellRenderer:  (row) => {
        if (row.data.flagShowMsg) {return '';}
        return row.data.lengthString;
    }},
  ]
};


function rendTargetUrlResultColumn(row){
  if (row.data.flagShowMsg) {return '';}
  var cellContent='<div style="text-align: center;">';
  if (row.data.respCode===0) {
    cellContent+='<i class="fas fa-check text-success"></i>';
  }else{
    cellContent+='<i class="fas fa-times text-failed"></i>';
  }
  cellContent+='</div>';
  return cellContent;
}

var gridOptionsCommandPrune= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "To Be Pruned URL: Target URL", field: "url", width:800},
    {headerName: "Result", field: "respCode", width: 80, pinned: "right", cellRenderer: rendTargetUrlResultColumn},
  ]
};

var gridOptionsCommandImportByUrl= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "To Be Imported By URL: Target URL", field: "url", width:800},
    {headerName: "Result", field: "respCode", width: 80, pinned: "right", cellRenderer: rendTargetUrlResultColumn},
  ]
};

var gridOptionsCommandImportByFile= {
  suppressHorizontalScroll: true,
	rowSelection: "single",
	defaultColDef: {
    resizable: false,
    filter: true,
    sortable: true
  },
  rowData: [],
  columnDefs: [
    {headerName: "To Be Imported By File: Target URL", field: "url", width:800},
    {headerName: "File Name", field: "name", width:200, cellRenderer:  (row) => {
        if (row.data.flagShowMsg) {return '';}
        return row.data.name;
    }},
    {headerName: "Modified Date", field: "lastModifiedPresentationString", width:120, cellRenderer:  (row) => {
        if (row.data.flagShowMsg) {return '';}
        return row.data.lastModifiedPresentationString;
    }},
    {headerName: "Result", field: "respCode", width: 80, pinned: "right", cellRenderer: rendTargetUrlResultColumn},
  ]
};
