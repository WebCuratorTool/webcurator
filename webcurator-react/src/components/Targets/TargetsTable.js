import React, { useCallback, useMemo } from 'react';
import { useSelector, useDispatch } from 'react-redux'
import moment from 'moment';

import Button from '../Button';
import Table from '../Table';

import { BsChevronDown, BsChevronUp } from 'react-icons/bs';

import {
    onChangeSortBy,
    setPageOffset,
} from '../../redux/targets';

function TargetsTable() {
    const dispatch = useDispatch();
    const {
        targets,
        pageOffset,
        sortOptions,
    } = useSelector((state) => state.targets);

    const renderSortableHeader = useCallback((accessor, name) => {
        return (
            <div
                onClick={() => dispatch(onChangeSortBy(accessor))}
                className='table-header-cell'
                data-testid={`clickable-table-cell-${accessor}`}
            >
                {name}
                {sortOptions.accessor === accessor && sortOptions.direction === 'desc' ? <BsChevronUp /> : <BsChevronDown />}
            </div>
        )
    },[dispatch, sortOptions])

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
                {pageOffset > 9 && <Button name='Prev' onClick={() => dispatch(setPageOffset(pageOffset - 10))} />}
                <Button name='Next' onClick={() => dispatch(setPageOffset(pageOffset + 10))} />
            </div>    
        </>
    )
}

export default TargetsTable