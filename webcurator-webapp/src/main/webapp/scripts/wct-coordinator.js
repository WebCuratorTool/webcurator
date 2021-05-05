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

function getEmbedFlag(){
  return true;
}

function popupLoginWindow(){
    console.log('Popup login window');
    $('#popup-window-login').html('<iframe src="/logon.jsp" title="Login" class="content-window-full-screen" id="popup-window-login-iframe"></iframe>');
    $('#popup-window-login').show();
}

function authCallback(){
  console.log('Auth call back');
  $('#popup-window-login').hide();
  $('#popup-window-login').html('');
  document.getElementById("iframe-networkmap-content").contentWindow.authCallback();
}