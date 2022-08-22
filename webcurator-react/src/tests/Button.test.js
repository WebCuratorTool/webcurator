import React from "react";
import { screen, fireEvent } from '@testing-library/react';
import { renderWithProviders } from './utils'

import Button from "../components/Button";

const onClickSpy = jest.fn()

describe('Button tests', () => {
    it ('should render the correct name', () => {
      renderWithProviders(<Button name='test button' onClick={jest.fn} />);
      const buttonElement = screen.getByText('test button');
      expect(buttonElement).toBeInTheDocument();
    })

    it ('should fire onClick function when clicked', () => {
        renderWithProviders(<Button name='test button' onClick={onClickSpy} />);
        const buttonElement = screen.getByText('test button');
        fireEvent.click(buttonElement)
        expect(onClickSpy).toBeCalled();
    })
})