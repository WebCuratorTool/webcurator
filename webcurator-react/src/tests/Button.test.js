import React from "react";
import { render, screen, fireEvent } from '@testing-library/react';
import Button from "../components/Button";

const onClickSpy = jest.fn()

describe('Button tests', () => {
    it ('should render the correct name', () => {
      render (<Button name='test button' onClick={jest.fn} />);
      const buttonElement = screen.getByText('test button');
      expect(buttonElement).toBeInTheDocument();
    })

    it ('should fire onClick function when clicked', () => {
        render (<Button name='test button' onClick={onClickSpy} />);
        const buttonElement = screen.getByText('test button');
        fireEvent.click(buttonElement)
        expect(onClickSpy).toBeCalled();
    })
})