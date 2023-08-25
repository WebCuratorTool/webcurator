import './TargetsSearch.scss';
import { Form, Table, Button, Stack } from 'react-bootstrap';

import Select from 'react-select'
import WctFormControlGroup from "../../components/WctFormControlGroup"
import WctDropdownSelect from '../../components/WctDropdownSelect';

const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
]

function TargetsSearchView() {
  return (
    <Form>
        <Stack direction="horizontal" gap={3}>
            <WctFormControlGroup controlId="targetId" labelName="ID"  type="text" placeholder="" value="323"/>
            <WctFormControlGroup controlId="targetName" labelName="Name"  type="text" placeholder="" value="rnz"/>
            <WctFormControlGroup controlId="targetSeed" labelName="Seed"  type="text" placeholder="" value="https://"/>
            <WctDropdownSelect labelName="Agency" options={[]} />
            <Button variant="outline-primary">Archive</Button>
            <Button variant="primary">Search</Button>
        </Stack>
        <Table>
            <tbody>
                <tr>
                <td>
                    
                </td>                   
                <td style={{width:"10%", background:"red"}}></td>
                <td style={{width:"20%", background: "green"}}> </td>
                </tr>
            </tbody>
        </Table>
    </Form>
  );
}

export default TargetsSearchView;