<template>
  <div>
    <v-menu v-model="menu" :close-on-content-click="false" offset-y>
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-menu-down</v-icon>
          <v-tooltip activator="parent" location="bottom">
            {{ companies.length > 1 ? 'Gestionar empresas' : 'Editar empresa' }}
          </v-tooltip>
        </v-btn>
      </template>

      <v-list>
        <v-list-item v-for="company in companies" :key="company.id">
          <v-list-item-title @click="companies.length > 1 ? setActiveCompany(company) : null" :style="{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            cursor: companies.length > 1 ? 'pointer' : 'default'
          }">
            <div style="display: flex; align-items: center;">
              <v-icon v-if="selectedCompany && selectedCompany.id === company.id && companies.length > 1">
                mdi-check
              </v-icon>
              <span :style="{ marginLeft: companies.length > 1 ? '8px' : '0px' }">{{ company.name }}</span>
            </div>
            <div style="display: flex; gap: 4px;">
              <v-btn icon flat small @click.stop="editCompany(company)">
                <v-icon>mdi-pencil</v-icon>
                <v-tooltip activator="parent" location="bottom">Editar empresa</v-tooltip>
              </v-btn>
              <v-btn v-if="companies.length > 1" icon flat small @click.stop="removeCompany(company.id)">
                <v-icon>mdi-delete</v-icon>
                <v-tooltip activator="parent" location="bottom">Eliminar empresa</v-tooltip>
              </v-btn>
            </div>
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
import { useCompanyStore } from '@/stores/companyStore';

export default {
  props: {
    companies: {
      type: Array,
      required: true,
    },
    selectedCompany: {
      type: Object,
      default: null,
    },
  },
  emits: ['edit-company'],
  data() {
    return {
      menu: false,
    };
  },
  methods: {
    setActiveCompany(company) {
      const companyStore = useCompanyStore();
      companyStore.setActiveCompany(company);
      this.menu = false;
    },
    removeCompany(companyId) {
      const companyStore = useCompanyStore();
      companyStore.removeCompany(companyId);
    },
    editCompany(company) {
      this.$emit('edit-company', company);
      this.menu = false;
    },
  },
};
</script>