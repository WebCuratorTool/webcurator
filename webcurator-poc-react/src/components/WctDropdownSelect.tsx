import { Form } from 'react-bootstrap';

function WctDropdownSelect(props:any) {
    let options='';
    for(var i=0; i<props.options.length; i++){
        var option=props.options[i];
        options+='<option value="' + option.oid + '">' + option.name + '</option>'; 
    }
    return (
        <Form.Group className="mb-3" controlId={props.controlId}>
            <Form.Label>{props.labelName}</Form.Label>
            <Form.Select>  {options} </Form.Select>
        </Form.Group>
    );
}

export default WctDropdownSelect;