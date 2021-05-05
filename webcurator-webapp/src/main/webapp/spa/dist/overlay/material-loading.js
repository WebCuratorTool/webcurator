ml_state = false;
ml_ready = false;
function materialLoading(state){
  if(state && ml_ready) document.getElementById('materialLoading').className = 'show';
  else if(ml_ready) document.getElementById('materialLoading').className = 'hide';
  ml_state = state;
}
window.addEventListener('load', function(){
  console.log('material-loading.js v1.2')
  var ml = document.createElement('div');
    ml.id = 'materialLoading';
    ml.className = 'hide';
  var mlCentered = document.createElement('div');
    mlCentered.id = 'materialLoadingCentered'
  var mlContent = document.createElement('div');
    mlContent.id = 'materialLoadingContent';
    mlContent.innerHTML = '<div class="mdl-spinner mdl-spinner--single-color mdl-js-spinner is-active"></div>';
  mlCentered.appendChild(mlContent);
  ml.appendChild(mlCentered);
  document.body.appendChild(ml);
  ml_ready = true;
  materialLoading(ml_state);
})
