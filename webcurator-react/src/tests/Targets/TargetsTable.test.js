import { BrowserRouter } from "react-router-dom"
import { render, screen, fireEvent } from '@testing-library/react'
import TargetsTable from "../../components/Targets/TargetsTable"

const onChangeSortBySpy = jest.fn();
const setPageOffsetSpy = jest.fn();

const MockTargetsTable = ({ pageOffset = 0 }) => {
    return (
      <BrowserRouter>
          <TargetsTable
            onChangeSortBy={onChangeSortBySpy}
            setPageOffset={setPageOffsetSpy}
            pageOffset={pageOffset}
            sortOptions={{}}
            targets={[]}
          />
      </BrowserRouter>
    )
}

describe('TargetsTable - rendering', () => {
    it ('should render a table', () => {
        render (<MockTargetsTable />);
        const table = screen.queryByRole('table');
        expect(table).toBeInTheDocument();
    })
    it('should render a next button', () => {
        render (<MockTargetsTable />);
        const nextButton = screen.getByText('Next');
        expect(nextButton).toBeInTheDocument();
    })
    it('should not render a prev button when on first page', () => {
        render (<MockTargetsTable />);
        const prevButton = screen.queryByText('Prev');
        expect(prevButton).not.toBeInTheDocument();
    })
    it('should render a prev button when not on first page', () => {
        render (<MockTargetsTable pageOffset={10} />);
        const prevButton = screen.queryByText('Prev');
        expect(prevButton).toBeInTheDocument();
    })
})

describe('TargetsTable - sort function', () => {
    it('should call sort function when Name header clicked', () => {
        render (<MockTargetsTable />);
        const nameHeader = screen.getByText('Name');
        fireEvent.click(nameHeader);
        expect(onChangeSortBySpy).toHaveBeenCalled();
    })
    it('should call sort function when Created header clicked', () => {
        render (<MockTargetsTable />);
        const createdHeader = screen.getByText('Created');
        fireEvent.click(createdHeader);
        expect(onChangeSortBySpy).toHaveBeenCalled();
    })
})

describe('Targets table - pagination', () => {
    it('should call setOffset function when next button clicked', () => {
        render (<MockTargetsTable />);
        const nextButton = screen.getByText('Next');
        fireEvent.click(nextButton);
        expect(setPageOffsetSpy).toHaveBeenCalled();
    })
    it('should call setOffset function when prev button clicked', () => {
        render (<MockTargetsTable pageOffset={10} />);
        const prevButton = screen.getByText('Prev');
        fireEvent.click(prevButton);
        expect(setPageOffsetSpy).toHaveBeenCalled();
    })
})