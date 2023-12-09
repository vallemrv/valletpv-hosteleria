export default {
    getItemById: (state) => (tableName, id) => {
        return state[tableName] ? Object.values(state[tableName]).find(e => e.id === id) : null;
    },
    getFilters: (state) => (tableName, field, values) => {
        if (!state[tableName]) return [];
        return Object.values(state[tableName]).map(e => ({ [values]: e[field] }));
    },
    getListValues: (state) => (tableName, field) => {
        return state[tableName] ? Object.values(state[tableName]).map(e => e[field]) : [];
    },
    getItemsOrdered: (state) => (items, column) => {
        return items.sort((a, b) => ('' + a[column]).localeCompare(b[column]));
    },
    getItemsFiltered: (state) => (filter, tableName) => {
        if (!filter || !state[tableName]) return Object.values(state[tableName]);
        const { filters, include } = filter;
        if (!filters || filters.length === 0) return [];

        return Object.values(state[tableName]).filter(o => {
            return filters.some(q => {
                return Object.keys(q).every(k => {
                    return include ? o[k].toLowerCase().includes(q[k].toLowerCase()) : o[k] === q[k];
                });
            });
        });
    }
}
