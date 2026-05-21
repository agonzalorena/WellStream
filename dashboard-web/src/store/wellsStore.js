import { create } from "zustand";

export const useWellsStore = create((set, get) => ({
  // State
  searchTerm: "",
  viewMode: "cards", // 'cards' or 'table'
  allWells: new Map(),
  filteredWells: [],

  // Actions
  setSearchTerm: (term) => {
    set((state) => {
      const searchLower = term.toLowerCase();
      const filtered = searchLower
        ? Array.from(state.allWells.values()).filter((well) =>
            well.wellId.toLowerCase().includes(searchLower),
          )
        : Array.from(state.allWells.values());

      return {
        searchTerm: term,
        filteredWells: filtered,
      };
    });
  },

  setAllWells: (wells) => {
    set((state) => {
      const searchLower = state.searchTerm.toLowerCase();
      const filtered = searchLower
        ? Array.from(wells.values()).filter((well) =>
            well.wellId.toLowerCase().includes(searchLower),
          )
        : Array.from(wells.values());

      return {
        allWells: wells,
        filteredWells: filtered,
      };
    });
  },

  setViewMode: (mode) => set({ viewMode: mode }),

  clearSearch: () => {
    set((state) => ({
      searchTerm: "",
      filteredWells: Array.from(state.allWells.values()),
    }));
  },

  // Getters
  getFilteredWells: () => get().filteredWells,
  getSearchTerm: () => get().searchTerm,
  getViewMode: () => get().viewMode,
  getAllWells: () => get().allWells,
}));
