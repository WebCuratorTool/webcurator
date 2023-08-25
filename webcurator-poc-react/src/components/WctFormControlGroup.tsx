import { Form } from 'react-bootstrap';

function WctFormControlGroup(props:any) {
    return (
        <Form.Group className="mb-3" controlId={props.controlId}>
            <Form.Label>{props.labelName}</Form.Label>
            <Form.Control type={props.type} placeholder={props.placeholder} />
        </Form.Group>
    );
}

export default WctFormControlGroup;