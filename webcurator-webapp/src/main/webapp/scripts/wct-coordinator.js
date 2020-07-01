function toggleContentWindowSize(isToFullScreen){
    $('#iframe-networkmap-content').removeClass();
    if(isToFullScreen){
        $('#iframe-networkmap-content').addClass('content-window-full-screen');
        $('.sub-win-footer').hide();
        $('#iconColumn').hide();
    }else{
        $('#iframe-networkmap-content').addClass('content-window-normal-screen');
        $('.sub-win-footer').show();
        $('#iconColumn').show();
    }
}

function closeContentWindow(){
    $('#btn-link-done').trigger('click');
}

function applyContentWindow(){
    $('#iframe-networkmap-content')[0].contentWindow.gPopupModifyHarvest.apply();
}