import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from 'axios';

const initialState = {
    targets: [],
    loading: true,
    pageOffset: 0,
    searchTerms: {
        targetId: '',
        name: '',
        seed: '',
        description: ''
    },
    shouldSort: false,
    sortOptions: {
        accessor: '',
        direction: 'asc',
    }
}

export const fetchTargets = createAsyncThunk('targets/fetchTargets', async (query) => {
    let url = '/wct/api/v1/targets';
    if (query.length > 0) {
        url += `?${query}`;
    }
    const response = await axios.get(url);
    return response.data.targets;
})

export const targetsSlice = createSlice({
    name: 'targets',
    initialState,
    extraReducers: builder => {
        builder.addCase(fetchTargets.pending, state => {
            state.loading = true
          })
          builder.addCase(fetchTargets.fulfilled, (state, action) => {
            state.loading = false
            state.targets = action.payload
            state.error = ''
          })
          builder.addCase(fetchTargets.rejected, (state, action) => {
            state.loading = false
            state.targets = []
            state.error = action.error.message
          })
    },
    reducers: {
        resetSearchTerms: (state) => {
            state.searchTerms = initialState.searchTerms;
            state.shouldSort = false;
        },
        onChangeSortBy: (state, action) => {
            state.shouldSort = true;
            state.sortOptions.accessor = action.payload;
            state.sortOptions.direction = state.sortOptions.direction === 'asc' ? 'desc' : 'asc'
        },
        setPageOffset: (state, action) => {
            state.pageOffset = action.payload;
        },
        setSearchTerms: (state, action) => {
            state.searchTerms[action.payload.key] = action.payload.value;
        },
        setSholdSort: (state, action) => {
            state.shouldSort = action.payload;
        }
    }
});

export const { onChangeSortBy, resetSearchTerms, setPageOffset, setSearchTerms, setSholdSort } = targetsSlice.actions;
export default targetsSlice.reducer;