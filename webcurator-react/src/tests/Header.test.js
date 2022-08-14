import React from "react";
import { render, screen, fireEvent } from '@testing-library/react';
import Header from "../components/Header";

describe('Header - rendering', () => {
    it ('should render the correct title', () => {
      render (<Header title='test header '/>);
      const titleElement = screen.getByText('test header');
      expect(titleElement).toBeInTheDocument();
    })
})