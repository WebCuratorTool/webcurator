function browseUrl(data){
	if (data.contentType==='text/html') {
		var url='/curator/tools/browse/'+harvestResultId+'/?url='+data.url;
		window.open(url);
	}else if(data.contentType.startsWith('image/')
		|| data.contentType.startsWith('text/')
		|| data.contentType.startsWith('application/pdf')
		|| data.contentType.startsWith('application/xml')) {
		var url='/curator/tools/download/'+harvestResultId+'/?url='+btoa(data.url);
		window.open(url);
	}else{
		var url='/curator/tools/download/'+harvestResultId+'/?url='+btoa(data.url);

	    var a = document.createElement("a");
	    document.body.appendChild(a);
	    a.style = "display: none";
	    a.href = url;
	    a.download = data.url;
	    a.click();
	    // window.URL.revokeObjectURL(url);
	}
	
}