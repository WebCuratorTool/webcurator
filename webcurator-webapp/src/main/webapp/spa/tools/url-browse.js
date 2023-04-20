var _browse_archive='http://localhost:8090/wayback/*/';
var _browse_access_tool='http://localhost:8090/wayback/*/';

function browseUrl(data, _browse_type) {
	_browse_type=_browse_type.toUpperCase();
	var url;
	switch(_browse_type) {
	    case 'LOCAL':
		    url=webContextPath+'/curator/tools/browse/' + harvestResultId + '/?url='+btoa(data.url);
		    break;
		case 'LIVESITE':
		    url=data.url;
		    break;
		case 'ACCESSTOOL':
		    url=_browse_access_tool + data.url;
		    break;
		case 'ARCHIVE':
		    url=_browse_archive + data.url;
		    break;
	}
	window.open(url);
}

function downloadUrl(data){
	var url=webContextPath+'/curator/tools/download/'+harvestResultId+'/?url='+btoa(data.url);
	downloadRemote(url, data.url);
}

function downloadRemote(url, name){
	fetch(url).then((res) => {
		if (res.ok) {
			return res.blob();
		}else if(res.status === 404){
			popupMessage('Resource Not Exist(404): ' + data.url);
		}else{
			popupMessage(res.status + ': ' + res.statusText);
		}
		return null;
	}).then((blob) => {
		console.log(blob);
		if(blob){
			saveAs(blob, name);
		}
	});
}

function popupMessage(msg){
	toastr.error(msg, 'Error', {
		showDuration: 30,
		hideDuration: 100,
		extendedTimeOut: 700,
		timeOut: 5000,
	});
}