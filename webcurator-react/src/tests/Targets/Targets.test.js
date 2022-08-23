import React from 'react'
import { fireEvent, screen } from '@testing-library/react'
import { act } from "react-dom/test-utils";
// We're using our own custom render function and not RTL's render.
import { renderWithProviders } from '../utils'
import Targets from '../../components/Targets/Targets'

test('Targets: shows loading element while loading, renders table when api returns', async () => {
  renderWithProviders(<Targets />);

  // shows loading element initally
  expect(screen.queryByTestId('loading')).toBeInTheDocument();
  // when await resolves, shows table and no loading element
  const tableRows = await screen.findAllByRole('row');
  expect(tableRows.length).toBe(3);
  expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
})

// Form inputs update correctly - this tests both the form and the function updating state
describe('Targets - update search inputs', () => {
  it ('should update each form field', async () => {
    await act(async () => {
      renderWithProviders(<Targets />)
    });

    const idInput = screen.getByTestId('targetId-input');
    fireEvent.change(idInput, { target: { value: 2 }});  
    expect(idInput.value).toBe('2');

    const nameInput = screen.getByTestId('name-input');
    fireEvent.change(nameInput, { target: { value: 'test' }});
    expect(nameInput.value).toBe('test');

    const seedInput = screen.getByTestId('seed-input');
    fireEvent.change(seedInput, { target: { value: 'http://example.com' }});
    expect(seedInput.value).toBe('http://example.com');

    const descriptionInput = screen.getByTestId('description-input');
    fireEvent.change(descriptionInput, { target: { value: 'a test target' }});
    expect(descriptionInput.value).toBe('a test target');
  })
})

  // it ('should fetch and render new data on search', async () => {
  //   renderWithProviders(<Targets />);
    
  //   const input = screen.getByTestId('seed-input');
  //   const button = screen.getByDisplayValue('Search');
  //   await act(async () => {
  //     fireEvent.change(input, { target: { value: 'http://test.govt.nz' }});
  //     fireEvent.click(button);
  //     const tableRows = await screen.findAllByRole("row");
  //     expect(tableRows.length).toBe(2);
  //     expect(screen.getAllByText('http://test.govt.nz').length).toBe(1);
  //   });
  // })