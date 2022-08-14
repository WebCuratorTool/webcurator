import React, { useCallback, useMemo } from 'react';
import moment from 'moment';

import Button from '../Button';
import Table from '../Table';

import { BsChevronDown, BsChevronUp } from 'react-icons/bs';

function TargetsTable(props) {
    const {
        onChangeSortBy,
        pageOffset,
        setPageOffset,
        sortOptions,
        targets,
    } = props

    const renderSortableHeader = useCallback((accessor, name) => {
        return (
            <div
                onClick={() => onChangeSortBy(accessor)}
                className='table-header-cell'
                data-testid={`clickable-table-cell-${accessor}`}
            >
                {name}
                {sortOptions.accessor === accessor && sortOptions.direction === 'desc' ? <BsChevronUp /> : <BsChevronDown />}
            </div>
        )
    }, [onChangeSortBy, sortOptions])

    const tableColumns = useMemo(() => [
        {
            Header: 'ID',
            accessor: 'targetId',
        },       
        {
            Header: renderSortableHeader('creationDate', 'Created'),
            accessor: 'creationDate',
            Cell: (props) => {
                return moment(props.value).local().format("DD/MM/YY")
            }
        },
        {
            Header: renderSortableHeader('name', 'Name'),
            accessor: 'name',
        },     
        {
            Header: 'Agency',
            accessor: 'agency',
        },
        {
            Header: 'Owner',
            accessor: 'owner',
        },
        {
            Header: 'Seed', 
            accessor: 'seeds[0].seed',
        },
        {
            Header: 'Status',
            accessor: 'state',
        }
    ], [renderSortableHeader])

    return (
        <>
            <Table
                columns={tableColumns}
                data={targets}
            />
            <div className='button-group'>
                {pageOffset > 9 && <Button name='Prev' onClick={() => setPageOffset(pageOffset - 10)} />}
                <Button name='Next' onClick={() => setPageOffset(pageOffset + 10)} />
            </div>    
        </>
    )
}

export default TargetsTable