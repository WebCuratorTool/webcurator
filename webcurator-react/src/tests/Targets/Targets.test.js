import React from "react";
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import TargetsContainer from "../../components/Targets/Targets.container";
import TargetsView from "../../components/Targets/Targets.view";
import TargetsTable from "../../components/Targets/TargetsTable";
import { act } from "react-dom/test-utils";

const MockTargetsContainer = () => {
  return (
    <BrowserRouter>
      <TargetsContainer />
    </BrowserRouter>
  )
}

const MockTargetsView = ({ loading = false }) => {
  const mockRenderTargetsTable = () => {
    return (
      <TargetsTable
          onChangeSortBy={jest.fn()}
          pageOffset={0}
          setPageOffset={jest.fn()}
          sortOptions={{}}
          targets={[]}
      />
    )
  }

  return (
    <BrowserRouter>
        <TargetsView
            loading={loading}
            clearSearchTerms={jest.fn()}
            onSearchTargets={jest.fn()}
            renderTargetsTable={mockRenderTargetsTable}
            searchTerms={{}}
            updateSearchTerms={jest.fn()}
        />
    </BrowserRouter>
  );
}

// Correct elements are rendered at the correct times
describe('Targets - rendering', () => {
  it ('should render a loading element while loading', () => {
    render (<MockTargetsView loading={true} />);
    const loadingElement = screen.queryByTestId('loading');
    expect(loadingElement).toBeInTheDocument();
  })

  it ('should not render a loading element when not loading', () => {
    render (<MockTargetsView loading={false} />);
    const loadingElement = screen.queryByTestId('loading');
    expect(loadingElement).not.toBeInTheDocument();
  })

  it ('should not render a table while loading', () => {
    render (<MockTargetsView loading={true} />);
    const table = screen.queryByRole('table');
    expect(table).not.toBeInTheDocument()
  })

  it ('should render a table when not loading', () => {
    render (<MockTargetsView loading={false} />);
    const table = screen.queryByRole('table');
    expect(table).toBeInTheDocument();
  })

  it ('should render a form', () => {
    render (<MockTargetsView />);
    const form = screen.queryByRole('form');
    expect(form).toBeInTheDocument();
  })
})
 

// Form inputs update correctly - this tests both the form and the function updating state
describe('Targets - update search inputs', () => {
  it ('should update id field', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    })
    const input = screen.getByTestId('targetId-input');
    fireEvent.change(input, { target: { value: 2 }});
    
    expect(input.value).toBe('2');
  })

  it ('should update name field', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    });
    const input = screen.getByTestId('name-input');
    fireEvent.change(input, { target: { value: 'test' }});
    
    expect(input.value).toBe('test');
  })

  it ('should update seed field', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    });
    const input = screen.getByTestId('seed-input');
    fireEvent.change(input, { target: { value: 'http://example.com' }});
    
    expect(input.value).toBe('http://example.com');
  })

  it ('should update description field', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    });
    const input = screen.getByTestId('description-input');
    fireEvent.change(input, { target: { value: 'a test target' }});
    
    expect(input.value).toBe('a test target');
  })
})

// The functions that interact with the api work correctly - mocked in ../__mocks__/axios.js
describe('Targets - api functions', () => {
  it('should fetch two targets and render them in table rows (3 rows including head)', async () => {
      render (
        <MockTargetsContainer />
      );
      const tableRows = await screen.findAllByRole('row');
      expect(tableRows.length).toBe(3);
  })

  it ('should fetch and render one target when id field is submitted with 1', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    });
    const input = screen.getByTestId('targetId-input');
    const button = screen.getByText('Search');
    fireEvent.change(input, { target: { value: 1 }});
    fireEvent.click(button);
    
    const tableRows = await screen.findAllByRole("row");
    expect(tableRows.length).toBe(2);
  })
})

describe('Targets - sort functions work', () => {
  it('should send the correct query when sorting', async () => {
    await act(async () => {
      render (<MockTargetsContainer />);
    });
    const nameHeader = screen.getByTestId("clickable-table-cell-name");
    fireEvent.click(nameHeader);
    const tableCell = await screen.findByTestId("table-row-0-cell-2");
    expect(tableCell.textContent).toBe("test3");
  })
  // it('should send the correct query when sorting', async () => {
  //   // await act(async () => {
  //   //   render (<MockTargetsContainer />)
  //   // })
  //   // const nameHeader = await screen.findByTestId("clickable-table-cell-name")
  //   // fireEvent.click(nameHeader)
  //   // const tableCell = await screen.findByTestId("table-row-0-cell-2");
  //   // expect(tableCell.textContent).toBe("test1")
  // })
})