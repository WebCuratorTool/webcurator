import React from 'react';
import { useTable } from 'react-table';

import '../styles/table.css'

function Table(props) {
    const { columns, data } = props;

    const {
        getTableProps,
        getTableBodyProps,
        prepareRow,
        headerGroups,
        rows,
      } = useTable({
        columns,
        data,
      })
    
    return (
        <table {...getTableProps()}>
            <thead>
            {headerGroups.map(headerGroup => (
                <tr {...headerGroup.getHeaderGroupProps()}>
                {headerGroup.headers.map((column, i) => (
                    <th data-testid={`table-header-${i}`} {...column.getHeaderProps()}>{column.render('Header')}</th>
                ))}
                </tr>
            ))}
            </thead>
            <tbody {...getTableBodyProps()}>
            {rows.map((row, i) => {
                prepareRow(row)
                return (
                <tr {...row.getRowProps()}>
                    {row.cells.map((cell, j) => {
                    return <td data-testid={`table-row-${i}-cell-${j}`} {...cell.getCellProps()}>{cell.render('Cell')}</td>
                    })}
                </tr>
                )
            })}
            </tbody>
        </table>
    );
}

export default Table;
