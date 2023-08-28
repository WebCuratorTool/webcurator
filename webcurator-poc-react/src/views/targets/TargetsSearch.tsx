import './TargetsSearch.scss';
import { Form, Table, Button, Stack } from 'react-bootstrap';

import Select from 'react-select'
import WctFormControlGroup from "../../components/WctFormControlGroup"
import WctDropdownSelect from '../../components/WctDropdownSelect';

const options = [
  { oid: 'chocolate', name: 'Chocolate' },
  { oid: 'strawberry', name: 'Strawberry' },
  { oid: 'vanilla', name: 'Vanilla' }
]

function TargetsSearchView() {
  return (
    <Form>
        <div className="d-flex align-items-center mb-3" style={{width: "85%"}}>
            <WctFormControlGroup controlId="targetId" labelName="ID"  type="text" placeholder="" value="323"/>
            <WctFormControlGroup controlId="targetName" labelName="Name"  type="text" placeholder="" value="rnz"/>
            <WctFormControlGroup controlId="targetSeed" labelName="Seed"  type="text" placeholder="" value="https://"/>
            <WctDropdownSelect labelName="Agency" options={options} />
            <div className="mb-3 p-2">
                <label className="form-label"> &nbsp; </label>
                <Button className="form-control" variant="outline-primary">Archive</Button>
            </div>
            <div className="ms-auto mb-3">
                <label className="form-label"> &nbsp;  </label>
                <Button className="form-control" variant="primary">Search</Button>
            </div>
        </div>
    </Form>
  );
}

export default TargetsSearchView;