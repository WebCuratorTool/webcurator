import './Targets.css';
import MainContentTitle from '../MainContentTitle'; 
import WctFormControlGroup from "../../components/WctFormControlGroup"
import WctDropdownSelect from '../../components/WctDropdownSelect';
import { useState,useEffect, useRef } from 'react';
import { BsX, BsXLg, BsFilter, BsSearch } from 'react-icons/bs'; 
// import { ReactTabulator } from "react-tabulator";
// import {TabulatorFull as Tabulator} from 'tabulator-tables/dist/js/tabulator.js';
// import {Tabulator} from 'react-tabulator/lib/ReactTabulator';
import { TabulatorFull as Tabulator } from 'react-tabulator/lib/types/TabulatorTypes';


const options = [
  { oid: 'chocolate', name: 'Chocolate' },
  { oid: 'strawberry', name: 'Strawberry' },
  { oid: 'vanilla', name: 'Vanilla' }
]

function TargetsSearchTile() {
  return (
    <div className="d-flex align-items-center mb-3" style={{width: "85%", height: "100%"}}>
        <WctFormControlGroup controlId="targetId" labelName="ID"  type="text" placeholder="" value="323"/>
        <WctFormControlGroup controlId="targetName" labelName="Name"  type="text" placeholder="" value="rnz"/>
        <WctFormControlGroup controlId="targetSeed" labelName="Seed"  type="text" placeholder="" value="https://"/>
        <WctDropdownSelect labelName="Agency" options={options} />
        <div className="p-1">
            <label className="form-label"> &nbsp; </label>
            <button className="form-control btn btn-outline-primary">Archive</button>
        </div>
        <div className="ms-auto">
            <label className="form-label"> &nbsp;  </label>
            <button className="form-control btn btn-primary">Search &ensp; <BsSearch /> </button>
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

function TargetsView() {
  let targetTable:Tabulator;
  // let targetTableElement=useRef();
  const columns={

  };

  useEffect(() => {
      t=new Tabulator('#tbl-targets',{height:props.height,data:[],layout:"fitColumns",columns:props.columns});
  }, []);


  return (
    <div className='main-content'>
        <MainContentTitle mainContentTitle='Targets' />

        <div className="queue-search">
            <div className="search-title">Query</div>
            <TargetsSearchTile />
            <TargetFilterTile/>
            <br/>
            <div id="tbl-targets"></div>
        </div>
    </div>
  );
}

export default TargetsView;