import React from 'react'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import { fireEvent, screen } from '@testing-library/react'
import { act } from "react-dom/test-utils";
// We're using our own custom render function and not RTL's render.
import { renderWithProviders } from '../utils'
import Targets from '../../components/Targets/Targets'
import { defaultData, dataBySeed } from './mockedData';

// We use msw to intercept the network request during the test,
// and return the response 'John Smith' after 150ms
// when receiving a get request to the `/api/user` endpoint
export const handlers = [
  // rest.get('/wct/api/v1/targets', (res, ctx) => {
  //   return res(ctx.json({ defaultData }), ctx.delay(150))
  // }),
  rest.get('/wct/api/v1/targets', (req, res, ctx) => {
    let data = defaultData;
    
    if (req.url.searchParams.get('seed') == 'http://test.govt.nz') {
      return res(ctx.json({ dataBySeed }), ctx.delay(150))
    }
    return res(ctx.json({ data }), ctx.delay(150))
  })
]

const server = setupServer(...handlers)

// Enable API mocking before tests.
beforeAll(() => server.listen())

// Reset any runtime request handlers we may add during the tests.
afterEach(() => server.resetHandlers())

// Disable API mocking after the tests are done.
afterAll(() => server.close())

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
  //   });

    
  //   const tableRows = await screen.findAllByRole("row");
  //   expect(tableRows.length).toBe(3);
  //   expect(screen.getAllByText('http://test.govt.nz').length).toBe(2);
  // })