import React from "react";
import { render, screen, fireEvent } from '@testing-library/react';
import { renderWithProviders } from './utils'

import Header from "../components/Header";

describe('Header - rendering', () => {
    it ('should render the correct title', () => {
      renderWithProviders(<Header title='test header '/>);
      const titleElement = screen.getByText('test header');
      expect(titleElement).toBeInTheDocument();
    })
})