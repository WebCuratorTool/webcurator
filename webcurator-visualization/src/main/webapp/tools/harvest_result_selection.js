var all_ti_hr_map={};
function func_fetch_ti_hr_data(){
    $("#id-ti-select").change(function() {
        var selected_ti_id=$(this).val();
        var ti=all_ti_hr_map[selected_ti_id];
        var hrList=ti.hrList;
        fun_fill_hr_list(ti);
    });

    var reqUrl="/vis/all_hr_results";
    fetchHttp(reqUrl, null, function(data){
        for(var i=0; i<data.length; i++){
            var ti=data[i];
            var tiId=ti.tiId;
            all_ti_hr_map[tiId]=ti;
        }
        func_fill_ti_select(data);
    });
}

function func_fill_ti_select(data){
    var h="";
    for(var i=0; i<data.length; i++){
        var ti=data[i];
        var tiId=ti.tiId;
        var option="<option>" + tiId + "</option>";
        h+=option;
    }
    $("#id-ti-select").html(h);

    if(data.length > 0){
        fun_fill_hr_list(data[0]);
    }
}

function fun_fill_hr_list(ti){
    console.log(hrList);
    var jobId=ti.tiId;
    var hrList=ti.hrList;
    var h="";
    for(var i=0; i<hrList.length; i++){
        var hr=hrList[i];
        var h_tr="<tr>";
        h_tr=h_tr+"<td><span class='badge bg-primary'>"+hr.hrNumber+"</span></td>";
        if(hr.indexed){
            h_tr=h_tr+"<td>Indexed</td>";
        }else{
            h_tr=h_tr+"<td>UnIndexed</td>";
        }
        h_tr=h_tr+"<td><a href='javascript:'>Review</a></td>";
        h_tr=h_tr+"</tr>";

        h+=h_tr;
    }

    $("#id-hr-select").html(h);
}
