import './Targets.css';
import MainContentTitle from '../MainContentTitle'; 
import WctFormControlGroup from "../../components/WctFormControlGroup"
import WctDropdownSelect from '../../components/WctDropdownSelect';
import { useState,useEffect, useRef } from 'react';
import { BsX, BsXLg, BsFilter, BsSearch } from 'react-icons/bs'; 
import {TabulatorFull as Tabulator} from "tabulator-tables"; //import Tabulator library
import 'react-tabulator/css/bootstrap/tabulator_bootstrap.css';


async function auth(){
  var url="/wct/auth/v1/token?username=lql&password=1qaz@WSX";
  const response = await fetch(url, {
      method: 'POST',
      redirect: 'follow',
      headers: {
        'Content-Type': 'application/json',
      }
  });
  return response.text();
}



var gUrl=null, gReq=null, gCallback=null;
function fetchHttp(url, token, req, callback){
  // var ROOT_URL=webContextPath+'/curator';
  // var reqUrl=ROOT_URL + url;
  var reqUrl=url;
  var reqBodyPayload="{}";
  if(req !== null){
    reqBodyPayload = JSON.stringify(req);
  }
  console.log('Fetch url:' + url);

  fetch(reqUrl, { 
    method: 'POST',
    redirect: 'follow',
    // mode: "no-cors",
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token,
    },
    body: reqBodyPayload
  }).then((response) => {
    if (response.redirected) {
      console.log('Need authentication');
      gUrl=url;
      gReq=req;
      gCallback=callback;
      // gParentHarvestResultViewTab.popupLoginWindow();
      return null;
    }

    if(response.headers && response.headers.get('Content-Type') && response.headers.get('Content-Type').startsWith('application/json')){
      console.log('Fetch success and callback: ' + url);
      return response.json();
    }
  }).then((response) => {   
    callback(response);
  });
}


const options = [
  { oid: '', name: '' },
  { oid: 'bootstrap', name: 'bootstrap' },
  { oid: 'foo', name: 'foo' },
]

function TargetsSearchTile(props) {
  const [targetId,setTargetId]=useState("");
  const [targetName,setTargetName]=useState("");
  const [targetSeed,setTargetSeed]=useState("");
  const [targetAgency,setTargetAgency]=useState("");
  const search=e=>{
    console.log('agency=' + targetAgency);
    props.callbackSearch(targetId,targetName,targetSeed,targetAgency);
  };

  return (
    <div className="d-flex align-items-center mb-3" style={{width: "85%", height: "100%"}}>
        <WctFormControlGroup controlId="targetId" labelName="ID"  type="text" placeholder="" value="323" onTextChange={setTargetId} />
        <WctFormControlGroup controlId="targetName" labelName="Name"  type="text" placeholder="" value="rnz" onTextChange={setTargetName} />
        <WctFormControlGroup controlId="targetSeed" labelName="Seed"  type="text" placeholder="" value="https://" onTextChange={setTargetSeed} />
        <WctDropdownSelect labelName="Agency" options={options} onTextChange={setTargetAgency}/>
        <div className="p-1">
            <label className="form-label"> &nbsp; </label>
            <button className="form-control btn btn-outline-primary">Archive</button>
        </div>
        <div className="ms-auto">
            <label className="form-label"> &nbsp;  </label>
            <button className="form-control btn btn-primary" onClick={search}>Search &ensp; <BsSearch /> </button>
        </div>
    </div>
  );
}

function TargetFilterTile(){
  const [isFilterBtnNextTwoMonths, setIsFilterBtnNextTwoMonths]=useState(true);
  const [isFilterBtnAllActiveFlag, setIsFilterBtnAllActive]=useState(true);
  const [isFilterBtnOwnedByMeFlag, setIsFilterBtnOwnedByMe]=useState(true);
  const resetFilter=()=>{
    setIsFilterBtnNextTwoMonths(true);
    setIsFilterBtnAllActive(true);
    setIsFilterBtnOwnedByMe(true);
  };
  const applyFilter=()=>{
    console.log(isFilterBtnNextTwoMonths);
  };

  return(
    <div className="row queue-filter">
        <div className="col-12">
            <span className="queue-filter-btn-group">
                {isFilterBtnNextTwoMonths? <span v-show="filterBtnNextTwoMonths"> In the next two months &ensp;<a href="#" onClick={()=>setIsFilterBtnNextTwoMonths(false)}><BsX /></a> </span> : <></>}                
                {isFilterBtnAllActiveFlag? <span v-show="filterBtnAllActive"> All Actions &ensp;<a href="#" onClick={()=>setIsFilterBtnAllActive(false)}><BsX /></a> </span> : <></>}
                {isFilterBtnOwnedByMeFlag? <span v-show="filterBtnOwnedByMe"> Owned by me &ensp;<a href="#" onClick={()=>setIsFilterBtnOwnedByMe(false)}><BsX /></a> </span> : <></>}
            </span>

            <span className="queue-filter-operation">
                <a href="#" onClick={resetFilter}><BsXLg/>&nbsp; Reset filter</a>
                <a href="#" onClick={applyFilter}><BsFilter/>&nbsp;Filter</a>
            </span>
        </div>
    </div>
  );
}

function formatSeed(cell){
  var data=cell.getData();
  var seeds=data["seeds"];
  for(var i=0;i<seeds.length;i++){
    var s=seeds[i];
    if(s.primary){
      return s.seed;
    }
  }
  return "";
}


function TargetsView() {
  var tblTargets;
  // let targetTableElement=useRef();
  const columnsTblTargets=[
    {title:"ID", field:"id"},
    {title:"Name", field:"name"},
    {title:"Owner", field:"owner"},
    {title:"Agency", field:"agency"},
    {title:"State", field:"state"},
    {title:"Primary Seed", field:"seeds", formatter: formatSeed},    
  ];

  useEffect(() => {
    tblTargets=new Tabulator('#tbl-targets',{height:500,data:[],layout:"fitColumns",columns:columnsTblTargets});
  }, []);

  const search=(targetId,targetName,targetSeed,targetAgency)=>{
    // console.log("targetId=" + targetId);
    var filter={
      "targetId":targetId,
      "name":targetName,
      "seed":targetSeed,
      "agency": targetAgency,
    };
    var searchParams={
      "filter": filter,
      "offset":0,
      "limit":50,
      "sortBy":"name,asc",
    };

    auth().then(token=>{
      console.log(token);
      fetchHttp("/wct/api/v1/targets", token, searchParams, (rsp)=>{
        var targets=rsp["targets"];
        console.log(targets);
        tblTargets.clearData();
        tblTargets.addData(targets);
      });
    });
  };

  return (
    <div className='main-content'>
        <MainContentTitle mainContentTitle='Targets' />

        <div className="queue-search">
            <div className="search-title">Query</div>
            <TargetsSearchTile callbackSearch={search} />
            <TargetFilterTile/>
            <br/>
            <div id="tbl-targets"></div>
        </div>
    </div>
  );
}

export default TargetsView;