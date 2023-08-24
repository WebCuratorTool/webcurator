import './TargetsSearch.css';
import { Form, Row, Col, Button } from 'react-bootstrap';

import Select from 'react-select'

const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
]

function TargetsSearchView() {
  return (
    <Form>
        <Row>
            <Col>
                <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
                    <Form.Label>ID</Form.Label>
                    <Form.Control type="text" placeholder="" />
                </Form.Group>
            </Col>
            <Col>
                <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
                    <Form.Label>Name</Form.Label>
                    <Form.Control type="text" placeholder="name@example.com" />
                </Form.Group>
            </Col>
            <Col>
                <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
                    <Form.Label>Seed</Form.Label>
                    <Form.Control type="text" placeholder="name@example.com" />
                </Form.Group>
            </Col>
            <Col>
                <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
                    <Form.Label>Agency</Form.Label>
                    <Select options={options} />
                </Form.Group>
            </Col>
            <Col>
                <Button variant="primary">Primary</Button>
            </Col>
            <Col>
                <Button variant="primary">Primary</Button>
            </Col>
        </Row>
    </Form>
  );
}

export default TargetsSearchView;